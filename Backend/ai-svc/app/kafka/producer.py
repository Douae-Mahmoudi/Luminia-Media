from aiokafka import AIOKafkaProducer
from app.core.config import settings
import json

async def publish_recommendation_done(user_id: str, media_ids: list):
    producer = AIOKafkaProducer(bootstrap_servers=settings.KAFKA_BROKER)
    await producer.start()
    try:
        message = json.dumps({"userId": user_id, "mediaIds": media_ids}).encode("utf-8")
        await producer.send_and_wait("recommendation.done", message)
    finally:
        await producer.stop()