import pandas as pd
from sklearn.neighbors import NearestNeighbors
from typing import List, Dict
import re
from collections import Counter


def compute_recommendations(
        liked_ids: List[int],
        all_media: List[dict],
        liked_external_types: List[str] = [],
        topics_by_type: Dict[str, List[str]] = {},
        recent_titles: List[str] = [],
        all_liked_titles: List[str] = [],
        n: int = 5,
) -> List[int]:
    if not all_media:
        return []

    df = pd.DataFrame(all_media)

    if "genre" not in df.columns:
        return []

    all_keywords = Counter()
    for title in all_liked_titles:
        if title:
            words = re.findall(r'\b[a-zA-Z]{4,}\b', title.lower())
            all_keywords.update(words)
            compounds = re.findall(r'\b[a-zA-Z]+[- ][a-zA-Z]+\b', title.lower())
            all_keywords.update([c.replace(' ', '-') for c in compounds])

    recent_keywords = set()
    for title in recent_titles:
        if title:
            words = re.findall(r'\b[a-zA-Z]{4,}\b', title.lower())
            recent_keywords.update(words)

    print(f"[knn] mots-clés cumulés: {dict(all_keywords.most_common(10))}")
    print(f"[knn] mots-clés récents: {recent_keywords}")

    def topic_score(row: pd.Series) -> int:
        score = 0

        media_type = str(row.get("type", "")).upper()
        relevant_topics = topics_by_type.get(media_type, [])
        if not relevant_topics:
            relevant_topics = [t for tlist in topics_by_type.values() for t in tlist]

        text = " ".join([
            str(row.get("title", "") or ""),
            str(row.get("genre", "") or ""),
            str(row.get("description", "") or ""),
        ]).lower()

        # Match avec les topics
        score += sum(2 for t in relevant_topics if t.lower() in text)

        for keyword, count in all_keywords.items():
            if keyword in text:
                score += count

        for keyword in recent_keywords:
            if keyword in text:
                score += 5

        return score

    # Calcul des scores
    df["_topic_score"] = df.apply(topic_score, axis=1)
    print(f"[knn] topic scores: { df[['id','title','_topic_score']].sort_values('_topic_score', ascending=False).head(5).to_dict('records') }")

    genre_dummies = pd.get_dummies(df["genre"])
    type_dummies = pd.get_dummies(df.get("type", pd.Series(dtype=str)))
    df_encoded = pd.concat([df[["id"]], genre_dummies, type_dummies], axis=1)

    liked_indices = df_encoded[df_encoded["id"].isin(liked_ids)].index.tolist()

    boosted_ids: set[int] = set()
    if liked_external_types:
        boosted = df[df["type"].isin(liked_external_types)]["id"].tolist()
        boosted_ids = set(int(x) for x in boosted) - set(liked_ids)
        print(f"[knn] boost externe → types={liked_external_types}, {len(boosted_ids)} médias boostés")

    if not liked_indices:
        if boosted_ids:
            boosted_list = sorted(
                [m for m in all_media if m["id"] in boosted_ids],
                key=lambda m: df.loc[df["id"] == m["id"], "_topic_score"].values[0] if not df.loc[df["id"] == m["id"], "_topic_score"].empty else 0,
                reverse=True,
            )
            print(f"[knn] aucun like interne → retour des {len(boosted_ids)} médias boostés (triés par topics)")
            return [int(m["id"]) for m in boosted_list[:n]]

        if all_keywords:
            keyword_matches = []
            for _, row in df.iterrows():
                text = " ".join([
                    str(row.get("title", "") or ""),
                    str(row.get("genre", "") or ""),
                ]).lower()
                match_score = sum(count for kw, count in all_keywords.items() if kw in text)
                if match_score > 0 and row["id"] not in liked_ids:
                    keyword_matches.append((row["id"], match_score))

            keyword_matches.sort(key=lambda x: x[1], reverse=True)
            if keyword_matches:
                print(f"[knn]  recommandations par mots-clés cumulés: {keyword_matches[:n]}")
                return [int(mid) for mid, _ in keyword_matches[:n]]

        not_liked = [m["id"] for m in all_media if m["id"] not in liked_ids]
        return [int(x) for x in not_liked[:n]]

    features = df_encoded.drop(columns=["id"]).values

    k = min(n + len(liked_ids), len(all_media))
    knn = NearestNeighbors(n_neighbors=k, metric="cosine")
    knn.fit(features)

    liked_vectors = features[liked_indices]
    distances, indices = knn.kneighbors(liked_vectors)

    boosted_reco: List[int] = []
    regular_reco: List[int] = []

    for idx_list in indices:
        for idx in idx_list:
            media_id = int(df_encoded.iloc[idx]["id"])
            if media_id in liked_ids:
                continue
            if media_id in boosted_ids and media_id not in boosted_reco:
                boosted_reco.append(media_id)
            elif media_id not in boosted_ids and media_id not in regular_reco:
                regular_reco.append(media_id)

    if len(boosted_reco) + len(regular_reco) < n and all_keywords:
        for _, row in df.iterrows():
            if row["id"] in liked_ids or row["id"] in boosted_reco or row["id"] in regular_reco:
                continue
            text = " ".join([
                str(row.get("title", "") or ""),
                str(row.get("genre", "") or ""),
            ]).lower()
            match_score = sum(count for kw, count in all_keywords.items() if kw in text)
            if match_score > 0:
                regular_reco.append(row["id"])
            if len(boosted_reco) + len(regular_reco) >= n + 5:
                break

    def get_score(mid: int) -> int:
        row = df.loc[df["id"] == mid, "_topic_score"]
        return int(row.values[0]) if not row.empty else 0

    boosted_reco.sort(key=get_score, reverse=True)
    regular_reco.sort(key=get_score, reverse=True)

    recommended = (boosted_reco + regular_reco)[:n]
    print(f"[knn] résultat → {len(boosted_reco)} boosté(s) + {len(regular_reco)} normal/aux → {len(recommended)} retourné(s)")
    return recommended