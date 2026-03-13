import logging
import os
from pathlib import Path

import aioboto3

from config import settings

logger = logging.getLogger(__name__)


class S3Uploader:
    def __init__(self):
        self.bucket = settings.s3_bucket_name
        self.base_url = (
            settings.s3_base_url
            or f"https://{self.bucket}.s3.{settings.aws_region}.amazonaws.com"
        )

    async def upload(self, image_data: bytes, key: str) -> str:
        if settings.use_local_storage:
            return await self._save_local(image_data, key)
        return await self._upload_s3(image_data, key)

    async def _save_local(self, image_data: bytes, key: str) -> str:
        """S3 없이 로컬 파일시스템에 저장 (테스트용)"""
        save_path = Path(settings.local_storage_path) / key
        save_path.parent.mkdir(parents=True, exist_ok=True)
        save_path.write_bytes(image_data)
        abs_path = save_path.resolve()
        logger.info(f"[LocalStorage] 저장 완료: {abs_path}")
        return f"file://{abs_path}"

    async def _upload_s3(self, image_data: bytes, key: str) -> str:
        session = aioboto3.Session(
            aws_access_key_id=settings.aws_access_key_id,
            aws_secret_access_key=settings.aws_secret_access_key,
            region_name=settings.aws_region,
        )
        async with session.client("s3") as s3:
            await s3.put_object(
                Bucket=self.bucket,
                Key=key,
                Body=image_data,
                ContentType="image/png",
                ACL="public-read",
            )
        url = f"{self.base_url}/{key}"
        logger.info(f"[S3] 업로드 완료: {url}")
        return url
