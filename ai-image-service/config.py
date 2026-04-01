from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # RabbitMQ
    rabbitmq_host: str = "localhost"
    rabbitmq_port: int = 5672
    rabbitmq_user: str = "admin"
    rabbitmq_password: str = "admin"

    # ComfyUI (RunPod URL)
    comfyui_url: str = "http://localhost:8188"
    comfyui_timeout: int = 300

    # OpenAI DALL·E (ComfyUI 폴백용)
    openai_api_key: str = ""

    # GoAPI Midjourney (최후 폴백용)
    goapi_key: str = ""

    # 로컬 저장 (S3 없이 테스트)
    use_local_storage: bool = True
    local_storage_path: str = "./generated_images"

    # AWS S3 (실 서비스용)
    aws_access_key_id: str = ""
    aws_secret_access_key: str = ""
    aws_region: str = "ap-northeast-2"
    s3_bucket_name: str = "store-product-images"
    s3_base_url: str = ""

    class Config:
        env_file = ".env"


settings = Settings()
