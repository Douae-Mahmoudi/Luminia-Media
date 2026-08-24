import httpx
import asyncpg
import time
import re
from app.core.config import settings
from app.services.knn_service import compute_recommendations
from app.db.recommendation_repo import save_recommendation, get_recommendation
from app.db.mongo import db as mongo_db




def _extract_username(user_id: str) -> str:
    if '@' in user_id:
        return user_id.split('@')[0]
    return user_id


def _is_external(media_id: str) -> bool:
    return str(media_id).startswith("EXT_")


def _parse_external_type(media_id: str) -> str | None:
    parts = str(media_id).split("_", 2)
    if len(parts) >= 2:
        return parts[1]
    return None



_topics_cache = {}
_CACHE_TTL = 0


def _get_cached_topics(user_id: str) -> dict | None:
    if _CACHE_TTL <= 0:
        return None
    if user_id in _topics_cache:
        entry = _topics_cache[user_id]
        if time.time() - entry["timestamp"] < _CACHE_TTL:
            print(f"[topics] depuis cache mémoire pour user={user_id}")
            return entry["topics"]
        else:
            del _topics_cache[user_id]
    return None


def _set_cached_topics(user_id: str, topics: dict) -> None:
    if _CACHE_TTL <= 0:
        return
    _topics_cache[user_id] = {
        "topics": topics,
        "timestamp": time.time()
    }
    print(f"[topics] sauvegardé dans cache mémoire pour user={user_id}")


def _invalidate_topics_cache(user_id: str) -> None:
    if user_id in _topics_cache:
        del _topics_cache[user_id]
        print(f"[topics]  cache mémoire invalidé pour user={user_id}")
    else:
        print(f"[topics]  invalidation - user={user_id} pas dans cache")



async def save_liked_title(user_id: str, external_id: str, title: str, media_type: str) -> None:
    await mongo_db.liked_titles.update_one(
        {"user_id": user_id, "external_id": external_id},
        {"$set": {
            "user_id": user_id,
            "external_id": external_id,
            "title": title.lower().strip(),
            "media_type": media_type.upper(),
        }},
        upsert=True,
    )
    print(f"[mongo] titre sauvegardé: '{title}' [{external_id}] type='{media_type}' user='{user_id}'")
    _invalidate_topics_cache(user_id)


async def delete_liked_title(user_id: str, external_id: str) -> None:
    await mongo_db.liked_titles.delete_one(
        {"user_id": user_id, "external_id": external_id}
    )
    print(f"[mongo] titre supprimé: [{external_id}] user='{user_id}'")
    _invalidate_topics_cache(user_id)


async def fetch_liked_titles(user_id: str) -> list[dict]:
    cursor = mongo_db.liked_titles.find({"user_id": user_id})
    docs = await cursor.to_list(length=500)
    print(f"[mongo] {len(docs)} titre(s) liké(s) pour user='{user_id}'")
    return docs




def extract_keywords_with_weight(titles: list[str]) -> dict[str, int]:
    keywords = {}
    for title in titles:
        if not title:
            continue
        clean_title = title.lower().strip()
        words = re.findall(r'\b[a-zA-Z]{4,}\b', clean_title)
        for word in words:
            keywords[word] = keywords.get(word, 0) + 1
        compounds = re.findall(r'\b[a-zA-Z]+[- ][a-zA-Z]+\b', clean_title)
        for compound in compounds:
            compound_key = compound.replace(' ', '-')
            keywords[compound_key] = keywords.get(compound_key, 0) + 1
    print(f"[keywords] pondérés: {keywords}")
    return keywords




async def search_external_by_keyword(keyword: str, media_type: str) -> list[dict]:
    results = []

    try:
        async with httpx.AsyncClient(timeout=10.0) as client:

            if media_type == "FILM":
                resp = await client.get(f"https://api.tvmaze.com/search/shows?q={keyword}")
                if resp.status_code == 200:
                    data = resp.json()
                    if isinstance(data, list):
                        for item in data[:5]:
                            show = item.get("show", {}) if isinstance(item, dict) else {}
                            if isinstance(show, dict):
                                title = show.get("name", "")
                                if keyword.lower() in title.lower():
                                    results.append({
                                        "id": f"EXT_FILM_{show.get('id')}",
                                        "title": title,
                                        "type": "FILM",
                                        "source": "TVMaze",
                                        "imageUrl": show.get("image", {}).get("medium", "") if isinstance(show.get("image"), dict) else "",
                                        "author": show.get("network", {}).get("name", "TV Series") if isinstance(show.get("network"), dict) else "TV Series",
                                    })

            elif media_type == "PODCAST":
                resp = await client.get(
                    f"https://itunes.apple.com/search",
                    params={"term": keyword, "media": "podcast", "limit": 5}
                )
                if resp.status_code == 200:
                    data = resp.json()
                    for item in data.get("results", []):
                        if isinstance(item, dict):
                            title = item.get("collectionName", "")
                            if keyword.lower() in title.lower():
                                results.append({
                                    "id": f"EXT_PODCAST_{item.get('collectionId')}",
                                    "title": title,
                                    "type": "PODCAST",
                                    "source": "iTunes",
                                    "imageUrl": item.get("artworkUrl100", ""),
                                    "author": item.get("artistName", "Unknown"),
                                })

            elif media_type == "BOOK":
                resp = await client.get(
                    f"https://www.googleapis.com/books/v1/volumes",
                    params={"q": keyword, "maxResults": 5}
                )
                if resp.status_code == 200:
                    data = resp.json()
                    for item in data.get("items", []):
                        if isinstance(item, dict):
                            volume = item.get("volumeInfo", {})
                            if isinstance(volume, dict):
                                title = volume.get("title", "")
                                if keyword.lower() in title.lower():
                                    authors = volume.get("authors", ["Unknown"])
                                    results.append({
                                        "id": f"EXT_BOOK_{item.get('id')}",
                                        "title": title,
                                        "type": "BOOK",
                                        "source": "GoogleBooks",
                                        "imageUrl": volume.get("imageLinks", {}).get("thumbnail", ""),
                                        "author": authors[0] if authors else "Unknown",
                                    })

            elif media_type == "GAME":
                resp = await client.get(
                    f"https://api.rawg.io/api/games",
                    params={"search": keyword, "page_size": 5, "key": "c8159cd8a83b4c59a11fdc4e06447408"}
                )
                if resp.status_code == 200:
                    data = resp.json()
                    for item in data.get("results", []):
                        if isinstance(item, dict):
                            title = item.get("name", "")
                            if keyword.lower() in title.lower():
                                results.append({
                                    "id": f"EXT_GAME_{item.get('id')}",
                                    "title": title,
                                    "type": "GAME",
                                    "source": "RAWG",
                                    "imageUrl": item.get("background_image", ""),
                                    "author": item.get("developers", [{}])[0].get("name", "Unknown") if item.get("developers") else "Unknown",
                                })
    except Exception as e:
        print(f"[API] Erreur recherche par mot-clé {keyword}: {e}")

    return results


async def get_keyword_based_recommendations(user_id: str) -> list[dict]:
    liked_docs = await fetch_liked_titles(user_id)
    titles = [doc.get("title", "") for doc in liked_docs]

    keywords_weight = extract_keywords_with_weight(titles)

    sorted_keywords = sorted(keywords_weight.items(), key=lambda x: x[1], reverse=True)
    print(f"[keywords] triés par poids: {sorted_keywords[:10]}")

    recommendations = []
    seen_titles = set()

    for keyword, weight in sorted_keywords[:10]:
        for media_type in ["PODCAST", "FILM", "BOOK", "GAME"]:
            results = await search_external_by_keyword(keyword, media_type)
            for media in results[:3]:
                title = media.get("title", "")
                if title and title not in seen_titles:
                    seen_titles.add(title)
                    media["_keyword_score"] = weight
                    recommendations.append(media)
                if len(recommendations) >= 20:
                    break
        if len(recommendations) >= 20:
            break

    recommendations.sort(key=lambda x: x.get("_keyword_score", 0), reverse=True)

    print(f"[keyword-reco] {len(recommendations)} recommandations basées sur mots-clés pondérés")
    return recommendations[:15]




TOPIC_RULES: list[tuple[list[str], dict[str, list[str]]]] = [
    (["technology", "tech", "coding", "programming", "software", "java", "python", "api", "code"],
     {"podcast": ["technology", "software", "coding", "programming"],
      "book": ["technology", "computer science", "coding", "programming"],
      "film": ["technology", "documentary", "coding"],
      "game": ["strategy", "simulation", "coding"]}),

    (["health", "wellness", "medical", "fitness", "nutrition", "diet", "skin care"],
     {"podcast": ["health", "wellness", "medical advice"],
      "book": ["health", "nutrition", "wellness"],
      "film": ["documentary", "health"],
      "game": ["simulation", "fitness"]}),
    (["cooking", "food", "recipe", "chef", "restaurant", "cuisine",
      "baking", "pastry", "meal", "kitchen", "homemade"],
     {"podcast": ["cooking", "food", "culinary"],
      "book": ["cooking", "food", "recipes"],
      "film": ["cooking", "documentary", "food"],
      "game": ["simulation", "cooking"]}),
    (["yoga", "meditation", "mindfulness", "nidra"],
     {"podcast": ["yoga", "meditation", "mindfulness"],
      "book": ["yoga", "meditation", "wellness"],
      "film": ["documentary", "health"],
      "game": ["simulation", "fitness"]}),

    (["war", "military", "battle", "combat", "army", "soldier"],
     {"podcast": ["history", "military"],
      "book": ["history", "military"],
      "film": ["action", "documentary", "history"],
      "game": ["strategy", "action"]}),

    (["science", "physics", "biology", "chemistry", "space", "astronomy", "quantum"],
     {"podcast": ["science", "research"],
      "book": ["science", "popular science"],
      "film": ["documentary", "science"],
      "game": ["puzzle", "simulation"]}),

    (["adventure", "explore", "travel", "journey"],
     {"podcast": ["travel", "adventure"],
      "book": ["adventure", "travel"],
      "film": ["adventure", "documentary"],
      "game": ["adventure", "rpg"]}),
]


def infer_topics_from_titles(liked_docs: list[dict]) -> dict[str, list[str]]:
    by_type: dict[str, list[str]] = {"FILM": [], "BOOK": [], "GAME": [], "PODCAST": []}
    all_titles: list[str] = []

    for doc in liked_docs:
        title = doc.get("title", "").lower().strip()
        media_type = doc.get("media_type", "").upper()
        all_titles.append(title)
        if media_type in by_type:
            by_type[media_type].append(title)

    full_text = " ".join(all_titles).strip()
    print(f"[topics] texte global: '{full_text[:200]}'")

    topics: dict[str, set[str]] = {
        "PODCAST": set(), "BOOK": set(), "FILM": set(), "GAME": set(),
    }

    for keywords, mapping in TOPIC_RULES:
        if any(kw in full_text for kw in keywords):
            for media_type, topic_list in mapping.items():
                topics[media_type.upper()].update(topic_list)

    for liked_type, titles in by_type.items():
        if not titles:
            continue
        type_text = " ".join(titles)
        for keywords, mapping in TOPIC_RULES:
            if any(kw in type_text for kw in keywords):
                same_type_key = liked_type.lower()
                same_type_topics = mapping.get(same_type_key, [])
                if same_type_topics:
                    topics[liked_type].update(same_type_topics)
                    print(f"[topics]  boost {liked_type} → {same_type_topics}")

    fallbacks = {
        "PODCAST": ["technology", "science", "culture"],
        "BOOK": ["popular science", "fiction", "self-help"],
        "FILM": ["popular", "documentary"],
        "GAME": ["action", "adventure", "strategy"],
    }
    for t, fb in fallbacks.items():
        if not topics[t]:
            topics[t].update(fb)

    result = {t: list(v)[:15] for t, v in topics.items()}
    print(f"[topics] inférés: {result}")
    return result




async def fetch_user_likes(user_id: str) -> list:
    conn = await asyncpg.connect(settings.COLLECTION_DB_URL)
    try:
        username = _extract_username(user_id)
        rows = await conn.fetch(
            """
            SELECT media_id FROM likes
            WHERE (user_id = $1 OR user_id = $2)
              AND type = 'LIKE'
            """,
            user_id, username,
        )
        result = []
        for row in rows:
            mid = row["media_id"]
            if _is_external(mid):
                continue
            try:
                result.append({"id": int(mid)})
            except (ValueError, TypeError):
                print(f"[likes] media_id ignoré: '{mid}'")
        print(f"[likes] user='{user_id}' → {len(result)} like(s) interne(s)")
        return result
    finally:
        await conn.close()


async def get_liked_internal_ids(user_id: str, token: str) -> list[int]:
    liked = await fetch_user_likes(user_id)
    ids = [m["id"] for m in liked]
    print(f"[liked-internal] user='{user_id}' → ids: {ids}")
    return ids



async def fetch_liked_external_types(user_id: str) -> set[str]:
    conn = await asyncpg.connect(settings.COLLECTION_DB_URL)
    try:
        username = _extract_username(user_id)
        rows = await conn.fetch(
            """
            SELECT media_id FROM likes
            WHERE (user_id = $1 OR user_id = $2)
              AND type IN ('LIKE', 'FAVORITE')
              AND media_id LIKE 'EXT\_%' ESCAPE '\\'
            """,
            user_id, username,
        )
        types = set()
        for row in rows:
            t = _parse_external_type(row["media_id"])
            if t:
                types.add(t)
        return types
    finally:
        await conn.close()


async def fetch_liked_external_ids(user_id: str) -> set[str]:
    conn = await asyncpg.connect(settings.COLLECTION_DB_URL)
    try:
        username = _extract_username(user_id)
        rows = await conn.fetch(
            """
            SELECT media_id FROM likes
            WHERE (user_id = $1 OR user_id = $2)
              AND type IN ('LIKE', 'FAVORITE')
              AND media_id LIKE 'EXT\_%' ESCAPE '\\'
            """,
            user_id, username,
        )
        return {row["media_id"] for row in rows}
    finally:
        await conn.close()


async def save_external_like(
        user_id: str,
        external_media_id: str,
        like_type: str = "LIKE",
        title: str = "",
        media_type: str = "",
) -> None:
    import uuid

    if like_type not in ("LIKE", "FAVORITE"):
        raise ValueError(f"like_type invalide: '{like_type}'")

    if not _is_external(external_media_id):
        if title and media_type:
            await save_liked_title(user_id, external_media_id, title, media_type)
        return

    conn = await asyncpg.connect(settings.COLLECTION_DB_URL)
    try:
        await conn.execute(
            """
            INSERT INTO likes (id, media_id, type, user_id, created_at)
            VALUES ($1, $2, $3, $4, NOW())
            ON CONFLICT (user_id, media_id, type) DO NOTHING
            """,
            str(uuid.uuid4()), external_media_id, like_type, user_id,
        )
        print(f"[ext-like] saved: user='{user_id}' media='{external_media_id}'")
    finally:
        await conn.close()

    inferred_type = media_type or _parse_external_type(external_media_id) or ""
    saved_title = title if title else external_media_id.lower()
    await save_liked_title(user_id, external_media_id, saved_title, inferred_type)


async def delete_external_like(
        user_id: str,
        external_media_id: str,
        like_type: str = "LIKE",
) -> None:
    conn = await asyncpg.connect(settings.COLLECTION_DB_URL)
    try:
        username = _extract_username(user_id)
        await conn.execute(
            """
            DELETE FROM likes
            WHERE (user_id = $1 OR user_id = $2)
              AND media_id = $3
              AND type = $4
            """,
            user_id, username, external_media_id, like_type,
        )
        print(f"[ext-like] deleted: user='{user_id}' media='{external_media_id}'")
    finally:
        await conn.close()
    await delete_liked_title(user_id, external_media_id)


async def fetch_all_media(token: str) -> list:
    async with httpx.AsyncClient() as client:
        response = await client.get(
            f"{settings.MEDIA_SVC_URL}/api/media",
            headers={"Authorization": f"Bearer {token}"},
            timeout=10.0,
        )
        if response.status_code == 200:
            return response.json()
        return []




async def get_recommendations_for_user(user_id: str, token: str) -> list:
    print(f"[reco] computing for: {user_id}")

    liked = await fetch_user_likes(user_id)
    liked_ids = [m["id"] for m in liked]

    ext_info = await get_external_liked_info(user_id)
    liked_external_types = ext_info.get("likedTypes", [])

    keyword_recommendations = await get_keyword_based_recommendations(user_id)

    liked_docs = await fetch_liked_titles(user_id)
    topics_by_type = infer_topics_from_titles(liked_docs) if liked_docs else {}

    all_media = await fetch_all_media(token)

    if all_media:
        internal_recommendations = compute_recommendations(
            liked_ids,
            all_media,
            liked_external_types,
            topics_by_type,
        )
        internal_media = [m for m in all_media if m["id"] in internal_recommendations]
    else:
        internal_media = []

    final_recommendations = []
    seen_ids = set()

    for rec in keyword_recommendations:
        rec_id = rec.get("id")
        if rec_id and rec_id not in seen_ids:
            seen_ids.add(rec_id)
            final_recommendations.append(rec)
            print(f"[reco]  PRIORITÉ mots-clés: {rec.get('title')} (poids={rec.get('_keyword_score', 0)})")

    for rec in internal_media[:20]:
        rec_id = f"INT_{rec.get('id')}"
        if rec_id not in seen_ids and len(final_recommendations) < 20:
            seen_ids.add(rec_id)
            final_recommendations.append({
                "id": rec.get("id"),
                "title": rec.get("title", ""),
                "author": rec.get("author", ""),
                "type": rec.get("type"),
                "imageUrl": rec.get("imageUrl", ""),
                "source": "internal",
            })

    print(f"[reco] final: {len(final_recommendations)} recommandations (dont {len(keyword_recommendations)} par mots-clés)")
    return final_recommendations[:20]



ALL_MEDIA_TYPES = ["FILM", "BOOK", "GAME", "PODCAST"]


async def get_external_liked_info(user_id: str) -> dict:
    conn = await asyncpg.connect(settings.COLLECTION_DB_URL)
    try:
        username = _extract_username(user_id)

        ext_rows = await conn.fetch(
            """
            SELECT media_id FROM likes
            WHERE (user_id = $1 OR user_id = $2)
              AND type IN ('LIKE', 'FAVORITE')
              AND media_id LIKE 'EXT\_%' ESCAPE '\\'
            """,
            user_id, username,
        )

        int_rows = await conn.fetch(
            """
            SELECT media_id FROM likes
            WHERE (user_id = $1 OR user_id = $2)
              AND type = 'LIKE'
              AND media_id NOT LIKE 'EXT\_%' ESCAPE '\\'
            """,
            user_id, username,
        )

        liked_types = set()
        liked_ids = set()

        for row in ext_rows:
            mid = row["media_id"]
            liked_ids.add(mid)
            t = _parse_external_type(mid)
            if t:
                liked_types.add(t)

        if not liked_types:
            liked_types = set(ALL_MEDIA_TYPES)

        liked_internal_ids = []
        for row in int_rows:
            try:
                liked_internal_ids.append(int(row["media_id"]))
            except (ValueError, TypeError):
                pass

        return {
            "likedTypes": list(liked_types),
            "likedExternalIds": list(liked_ids),
            "likedInternalIds": liked_internal_ids,
        }
    finally:
        await conn.close()


async def get_topics_for_user(user_id: str) -> dict:
    print(f"[topics]  recalcul forcé pour user={user_id}")
    liked_docs = await fetch_liked_titles(user_id)
    topics = infer_topics_from_titles(liked_docs)
    return topics