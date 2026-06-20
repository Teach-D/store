from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    rabbitmq_host: str = "store-rabbitmq"
    rabbitmq_port: int = 5672
    rabbitmq_user: str = "admin"
    rabbitmq_password: str = "admin"

    ollama_url: str = "http://localhost:11434"
    model_name: str = "qwen2.5:7b-instruct"

    # 다국어 지원 경량 임베딩 모델 (~420MB, CPU로도 충분히 빠름)
    embedding_model: str = "paraphrase-multilingual-MiniLM-L12-v2"

    # ChromaDB 영구 저장 경로 (Docker 볼륨 마운트)
    chroma_persist_dir: str = "./chroma_db"

    product_service_url: str = "http://product-service:8080"

    class Config:
        env_file = ".env"


settings = Settings()
