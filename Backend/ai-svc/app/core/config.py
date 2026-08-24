from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    KAFKA_BROKER: str = "kafka:9092"
    MONGO_URL: str = "mongodb://ai-svc-mongo:27017"
    MONGO_DB: str = "ai_db"
    JWT_SECRET: str = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
    COLLECTION_SVC_URL: str = "http://collection-service-app:8083"
    MEDIA_SVC_URL: str = "http://media-service-app:8082"
    COLLECTION_DB_URL: str = "postgresql://postgres:ppp@collection-db:5432/collection_db"
settings = Settings()