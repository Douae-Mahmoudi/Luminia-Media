from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel
from typing import Optional, List, Dict, Any
from app.core.security import get_current_user
from app.services.recommendation_service import (
    get_recommendations_for_user,
    get_external_liked_info,
    get_topics_for_user,
    save_external_like,
    delete_external_like,
    get_liked_internal_ids,
)
import httpx
import hashlib
import time

router = APIRouter(tags=["Recommendations"])



class ExternalLikeRequest(BaseModel):
    externalMediaId: str
    likeType: str
    title: Optional[str] = ""
    mediaType: Optional[str] = ""



YOUTUBE_API_KEY       = "AIzaSyB_f_UvBd4cxWDLJLqHWV_bY9Owx9ELJTk"
PODCASTINDEX_API_KEY  = "WDMGGBCVV5SANPEPUC6W"
PODCASTINDEX_API_SECRET = "DXceGN24vNf4f7CErXzP9s7rKsXHvsKF44kDLpe3"
RAWG_API_KEY          = "c8159cd8a83b4c59a11fdc4e06447408"


def get_podcastindex_headers():
    api_header_time  = int(time.time())
    api_header_token = hashlib.sha1(
        f"{PODCASTINDEX_API_KEY}{PODCASTINDEX_API_SECRET}{api_header_time}".encode()
    ).hexdigest()
    return {
        "X-Auth-Date":  str(api_header_time),
        "X-Auth-Key":   PODCASTINDEX_API_KEY,
        "Authorization": api_header_token,
        "User-Agent":   "LuminaMedia/1.0",
    }



def _topics_flat(topics: Dict[str, List[str]]) -> set:
    words = set()
    for topic_list in topics.values():
        for t in topic_list:
            for word in t.lower().split():
                if len(word) > 3:
                    words.add(word)
    return words


def _internal_matches_topics(media: dict, topic_words: set, topics: Dict[str, List[str]]) -> bool:

    media_type = (media.get("type") or "").upper()
    media_title = (media.get("title") or "").lower()
    media_genre = (media.get("genre") or "").lower()
    media_desc  = (media.get("description") or "").lower()

    type_topics = topics.get(media_type, [])

    if not type_topics:
        return True

    combined = f"{media_title} {media_genre} {media_desc}"
    for word in topic_words:
        if word in combined:
            return True

    for topic in type_topics:
        topic_lower = topic.lower()
        if topic_lower in combined:
            return True

    return False


def _score_internal(media: dict, topics: Dict[str, List[str]]) -> int:
    score = 0
    media_type  = (media.get("type") or "").upper()
    media_title = (media.get("title") or "").lower()
    media_genre = (media.get("genre") or "").lower()
    media_desc  = (media.get("description") or "").lower()
    combined    = f"{media_title} {media_genre} {media_desc}"

    type_topics = topics.get(media_type, [])
    for topic in type_topics:
        if topic.lower() in combined:
            score += 2

    for topic_list in topics.values():
        for topic in topic_list:
            if topic.lower() in combined:
                score += 1

    return score




async def search_external_media_by_topic(topic: str, media_type: str) -> List[Dict[str, Any]]:
    results = []

    try:
        async with httpx.AsyncClient(timeout=15.0) as client:

            if media_type == "FILM":
                try:
                    resp = await client.get(f"https://api.tvmaze.com/search/shows?q={topic}")
                    if resp.status_code == 200:
                        data = resp.json()
                        if isinstance(data, list):
                            for item in data[:5]:
                                if isinstance(item, dict):
                                    show = item.get("show", {})
                                    if isinstance(show, dict):
                                        results.append({
                                            "id":          f"EXT_FILM_{show.get('id', topic)}",
                                            "title":       show.get("name", topic),
                                            "type":        "FILM",
                                            "source":      "TVMaze",
                                            "imageUrl":    show.get("image", {}).get("medium", "") if isinstance(show.get("image"), dict) else "",
                                            "author":      show.get("network", {}).get("name", "TV Series") if isinstance(show.get("network"), dict) else "TV Series",
                                            "description": (show.get("summary") or "")[:200].replace("<p>", "").replace("</p>", ""),
                                            "genre":       ", ".join([g.get("name") for g in show.get("genres", [])]) if isinstance(show.get("genres"), list) else "",
                                        })
                except Exception as e:
                    print(f"[API] TVMaze error: {e}")

                try:
                    resp = await client.get(
                        "https://www.googleapis.com/youtube/v3/search",
                        params={"part": "snippet", "q": f"{topic} film", "maxResults": 5,
                                "key": YOUTUBE_API_KEY, "type": "video"},
                    )
                    if resp.status_code == 200:
                        data = resp.json()
                        for item in data.get("items", []):
                            if isinstance(item, dict):
                                video_id = item.get("id", {}).get("videoId", "")
                                if video_id:
                                    results.append({
                                        "id":         f"EXT_FILM_youtube-{video_id}",
                                        "title":      item.get("snippet", {}).get("title", topic),
                                        "type":       "FILM",
                                        "source":     "YouTube",
                                        "imageUrl":   item.get("snippet", {}).get("thumbnails", {}).get("medium", {}).get("url", ""),
                                        "author":     item.get("snippet", {}).get("channelTitle", "YouTube"),
                                        "contentUrl": f"https://www.youtube.com/watch?v={video_id}",
                                    })
                except Exception as e:
                    print(f"[API] YouTube error: {e}")

            elif media_type == "BOOK":
                try:
                    resp = await client.get(
                        "https://www.googleapis.com/books/v1/volumes",
                        params={"q": topic, "maxResults": 10},
                    )
                    if resp.status_code == 200:
                        data = resp.json()
                        for item in data.get("items", [])[:5]:
                            if isinstance(item, dict):
                                volume  = item.get("volumeInfo", {})
                                authors = volume.get("authors", ["Unknown"])
                                results.append({
                                    "id":          f"EXT_BOOK_{item.get('id', topic)}",
                                    "title":       volume.get("title", topic),
                                    "type":        "BOOK",
                                    "source":      "GoogleBooks",
                                    "imageUrl":    volume.get("imageLinks", {}).get("thumbnail", ""),
                                    "author":      authors[0] if authors else "Unknown",
                                    "description": (volume.get("description") or "")[:200],
                                    "genre":       ", ".join(volume.get("categories", [])),
                                    "releaseYear": volume.get("publishedDate", "")[:4],
                                })
                except Exception as e:
                    print(f"[API] GoogleBooks error: {e}")

            elif media_type == "PODCAST":
                try:
                    resp = await client.get(
                        "https://api.podcastindex.org/api/1.0/search/byterm",
                        params={"q": topic, "max": 10},
                        headers=get_podcastindex_headers(),
                    )
                    if resp.status_code == 200:
                        data = resp.json()
                        for item in data.get("feeds", [])[:5]:
                            if isinstance(item, dict):
                                results.append({
                                    "id":          f"EXT_PODCAST_{item.get('id', topic)}",
                                    "title":       item.get("title", topic),
                                    "type":        "PODCAST",
                                    "source":      "PodcastIndex",
                                    "imageUrl":    item.get("image", ""),
                                    "author":      item.get("author", "Unknown"),
                                    "description": (item.get("description") or "")[:200],
                                    "contentUrl":  item.get("url", ""),
                                })
                except Exception as e:
                    print(f"[API] PodcastIndex error: {e}")

                try:
                    resp = await client.get(
                        "https://itunes.apple.com/search",
                        params={"term": topic, "media": "podcast", "limit": 5},
                    )
                    if resp.status_code == 200:
                        data = resp.json()
                        for item in data.get("results", []):
                            if isinstance(item, dict):
                                results.append({
                                    "id":         f"EXT_PODCAST_itunes-{item.get('collectionId', topic)}",
                                    "title":      item.get("collectionName", topic),
                                    "type":       "PODCAST",
                                    "source":     "iTunes",
                                    "imageUrl":   item.get("artworkUrl100", ""),
                                    "author":     item.get("artistName", "Unknown"),
                                    "contentUrl": item.get("feedUrl", ""),
                                })
                except Exception as e:
                    print(f"[API] iTunes error: {e}")

            elif media_type == "GAME":
                try:
                    resp = await client.get(
                        "https://api.rawg.io/api/games",
                        params={"search": topic, "page_size": 10, "key": RAWG_API_KEY},
                    )
                    if resp.status_code == 200:
                        data = resp.json()
                        for item in data.get("results", [])[:5]:
                            if isinstance(item, dict):
                                results.append({
                                    "id":          f"EXT_GAME_{item.get('id', topic)}",
                                    "title":       item.get("name", topic),
                                    "type":        "GAME",
                                    "source":      "RAWG",
                                    "imageUrl":    item.get("background_image", ""),
                                    "author":      item.get("developers", [{}])[0].get("name", "Unknown") if item.get("developers") else "Unknown",
                                    "genre":       ", ".join([g.get("name") for g in item.get("genres", [])]),
                                    "releaseYear": item.get("released", "")[:4],
                                })
                except Exception as e:
                    print(f"[API] RAWG error: {e}")

    except Exception as e:
        print(f"[API] Erreur recherche {media_type}: {e}")

    return results


async def get_external_recommendations(topics: Dict[str, List[str]]) -> List[Dict[str, Any]]:
    recommendations = []
    seen_titles     = set()

    for media_type, topic_list in topics.items():
        if not topic_list:
            continue

        for topic in topic_list[:3]:
            if not topic or len(topic) < 3 or topic in ["popular", "best", "technology", "all"]:
                continue

            results = await search_external_media_by_topic(topic, media_type)

            for media in results:
                title = media.get("title", "")
                if title and title not in seen_titles:
                    seen_titles.add(title)
                    recommendations.append(media)

            if len([r for r in recommendations if r.get("type") == media_type]) >= 6:
                break

    return recommendations



@router.get("/me")
async def get_my_recommendations(current_user: dict = Depends(get_current_user)):
    user_id = current_user["sub"]
    token   = current_user.get("token", "")

    topics = await get_topics_for_user(user_id)
    topic_words = _topics_flat(topics)

    external_recommendations = await get_external_recommendations(topics)

    internal_recommendations = await get_recommendations_for_user(user_id, token)


    scored_internals = []
    for media in internal_recommendations:
        if not isinstance(media, dict):
            continue
        if _internal_matches_topics(media, topic_words, topics):
            score = _score_internal(media, topics)
            scored_internals.append((score, media))

    scored_internals.sort(key=lambda x: x[0], reverse=True)
    top_internals = [m for _, m in scored_internals[:4]]

    print(f"[RECO] internes pertinents: {len(top_internals)}/{len(internal_recommendations)} "
          f"(scores: {[s for s, _ in scored_internals[:4]]})")


    all_recommendations = []
    seen_ids            = set()

    for media in top_internals:
        media_id = media.get("id")
        if media_id is None:
            continue
        key = f"INT_{media_id}"
        if key not in seen_ids:
            seen_ids.add(key)
            all_recommendations.append({
                "id":          media_id,
                "externalId":  None,
                "title":       media.get("title", "Sans titre"),
                "author":      media.get("author", "Inconnu"),
                "type":        media.get("type"),
                "imageUrl":    media.get("imageUrl", ""),
                "source":      "internal",
                "description": media.get("description", ""),
                "genre":       media.get("genre", ""),
                "releaseYear": media.get("releaseYear"),
                "contentUrl":  media.get("contentUrl", ""),
            })

    for rec in external_recommendations:
        if len(all_recommendations) >= 20:
            break
        if not isinstance(rec, dict):
            continue
        rec_id = rec.get("id")
        if not rec_id or rec_id in seen_ids:
            continue
        seen_ids.add(rec_id)
        all_recommendations.append({
            "id":          rec_id,
            "externalId":  rec_id,
            "title":       rec.get("title", "Sans titre"),
            "author":      rec.get("author", "Inconnu"),
            "type":        rec.get("type"),
            "imageUrl":    rec.get("imageUrl", ""),
            "source":      rec.get("source", "external"),
            "description": rec.get("description", ""),
            "genre":       rec.get("genre", ""),
            "releaseYear": rec.get("releaseYear"),
            "contentUrl":  rec.get("contentUrl", ""),
        })

    print(f"[RECO] final → {len(top_internals)} interne(s) + "
          f"{len(all_recommendations) - len(top_internals)} externe(s) = "
          f"{len(all_recommendations)} total")

    return {
        "userId":          user_id,
        "recommendations": all_recommendations,
        "sources": {
            "external": len(external_recommendations),
            "internal": len(top_internals),
            "total":    len(all_recommendations),
        },
    }



@router.get("/liked-internal-ids")
async def get_my_liked_internal_ids(current_user: dict = Depends(get_current_user)):
    user_id = current_user["sub"]
    token   = current_user.get("token", "")
    ids     = await get_liked_internal_ids(user_id, token)
    return {"likedIds": ids}



@router.get("/external/liked-info")
async def get_liked_external_info(current_user: dict = Depends(get_current_user)):
    user_id = current_user["sub"]
    return await get_external_liked_info(user_id)




@router.get("/external/topics")
async def get_external_topics(current_user: dict = Depends(get_current_user)):
    user_id = current_user["sub"]
    return await get_topics_for_user(user_id)




@router.post("/external/like", status_code=status.HTTP_201_CREATED)
async def like_external_media(
        body: ExternalLikeRequest,
        current_user: dict = Depends(get_current_user),
):
    user_id = current_user["sub"]

    if not body.externalMediaId.startswith("EXT_"):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="externalMediaId doit commencer par 'EXT_'",
        )
    if body.likeType not in ("LIKE", "FAVORITE"):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="likeType doit être 'LIKE' ou 'FAVORITE'",
        )

    await save_external_like(
        user_id           = user_id,
        external_media_id = body.externalMediaId,
        like_type         = body.likeType,
        title             = body.title or "",
        media_type        = body.mediaType or "",
    )
    return {"message": "like sauvegardé", "mediaId": body.externalMediaId}



@router.delete("/external/like", status_code=status.HTTP_200_OK)
async def unlike_external_media(
        body: ExternalLikeRequest,
        current_user: dict = Depends(get_current_user),
):
    user_id = current_user["sub"]
    await delete_external_like(user_id, body.externalMediaId, body.likeType)
    return {"message": "like supprimé", "mediaId": body.externalMediaId}

