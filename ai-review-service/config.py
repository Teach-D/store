from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    rabbitmq_host: str = "store-rabbitmq"
    rabbitmq_port: int = 5672
    rabbitmq_user: str = "admin"
    rabbitmq_password: str = "admin"

    # Colab T4에서 발급받은 ngrok URL로 교체
    ollama_url: str = "http://localhost:11434"
    model_name: str = "qwen2.5:7b-instruct"

    product_service_url: str = "http://product-service:8080"

    class Config:
        env_file = ".env"


settings = Settings()
