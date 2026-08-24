from aiokafka import AIOKafkaConsumer
from app.core.config import settings
import json

async def start_consumer():
    consumer = AIOKafkaConsumer(
        "user.liked",
        "user.favorited",
        "media.created",
        bootstrap_servers=settings.KAFKA_BROKER,
        group_id="ai-svc-group"
    )
    await consumer.start()
    try:
        async for msg in consumer:
            data = json.loads(msg.value.decode("utf-8"))
            topic = msg.topic

            if topic in ["user.liked", "user.favorited"]:
                user_id = data.get("userId")
                media_id = data.get("mediaId")
                print(f"Event reçu: {topic} - user={user_id} media={media_id}")

            elif topic == "media.created":
                print(f"Nouveau média ajouté: {data.get('id')}")

    finally:
        await consumer.stop()