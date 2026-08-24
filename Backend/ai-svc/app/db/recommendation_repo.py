from app.db.mongo import db
from datetime import datetime

async def save_recommendation(user_id: str, media_ids: list):
    await db.recommendations.update_one(
        {"userId": user_id},
        {"$set": {
            "userId": user_id,
            "mediaIds": media_ids,
            "updatedAt": datetime.utcnow()
        }},
        upsert=True
    )

async def get_recommendation(user_id: str):
    return await db.recommendations.find_one({"userId": user_id})