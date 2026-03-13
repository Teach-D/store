import asyncio
import time
import logging

import aiohttp

from .base import ImageGenerationProvider, GenerationRequest, GenerationResult, ProviderType, ImageType

logger = logging.getLogger(__name__)

# Midjourney는 공식 API 미제공 → GoAPI.ai 타사 래퍼 사용
# https://www.goapi.ai/midjourney-api
GOAPI_BASE_URL = "https://api.goapi.ai/mj/v2"

# Midjourney v6 파라미터
MJ_VERSION = "6.1"
MJ_QUALITY = "2"       # --q 2 (최고 품질)
MJ_STYLIZE = "750"     # --s 750 (스타일화 강도)


class MidjourneyProvider(ImageGenerationProvider):
    """
    Midjourney v6 이미지 생성 (GoAPI.ai 래퍼 사용)
    - 예술적 품질이 가장 뛰어남 (프리미엄 이미지)
    - 공식 API 없음 → 타사 래퍼 필요 (GoAPI, ImagineAPI 등)
    - 비용: GoAPI $0.03~0.08/장, Midjourney 구독 $10~30/월
    """

    def __init__(self, api_key: str):
        self.api_key = api_key
        self.headers = {
            "X-API-Key": api_key,
            "Content-Type": "application/json",
        }

    @property
    def provider_type(self) -> ProviderType:
        return ProviderType.MIDJOURNEY

    @property
    def cost_per_image_usd(self) -> float:
        return 0.05  # GoAPI 기준 약 $0.03~0.08

    async def generate(self, request: GenerationRequest) -> GenerationResult:
        start_ms = int(time.time() * 1000)

        prompt = self._build_mj_prompt(request)
        logger.info(f"[Midjourney] 이미지 생성 시작 productId={request.product_id}")

        task_id = await self._submit_imagine(prompt)
        image_url = await self._wait_for_result(task_id)

        # U1 업스케일 (최고 해상도)
        upscaled_url = await self._upscale(task_id, index=1)
        image_data = await self._download_image(upscaled_url)

        return GenerationResult(
            image_data=image_data,
            provider=self.provider_type,
            prompt_used=prompt,
            generation_time_ms=int(time.time() * 1000) - start_ms,
            cost_usd=self.cost_per_image_usd,
            metadata={"mj_version": MJ_VERSION, "task_id": task_id},
        )

    async def is_available(self) -> bool:
        if not self.api_key:
            return False
        try:
            async with aiohttp.ClientSession() as session:
                async with session.get(
                    f"{GOAPI_BASE_URL}/account",
                    headers=self.headers,
                    timeout=aiohttp.ClientTimeout(total=5),
                ) as resp:
                    return resp.status == 200
        except Exception:
            return False

    def _build_mj_prompt(self, request: GenerationRequest) -> str:
        """
        Midjourney 프롬프트 특성:
        - 짧고 강렬한 키워드 나열이 효과적
        - 파라미터(--v, --q, --s)로 품질 제어
        - --no 파라미터로 제외 요소 지정
        """
        base = self.build_english_prompt(request)

        if request.image_type in (ImageType.PROMO, ImageType.PREMIUM):
            style = (
                "cinematic lighting, ultra-detailed, photorealistic, "
                "8k resolution, award-winning commercial photography"
            )
            no_params = "--no text, watermark, logo, blurry"
        else:
            style = (
                "product photography, pure white background, "
                "studio lighting, sharp focus, ultra-realistic"
            )
            no_params = "--no background clutter, shadows, text"

        return (
            f"{base}, {style} "
            f"--v {MJ_VERSION} --q {MJ_QUALITY} --s {MJ_STYLIZE} "
            f"--ar {'16:9' if request.image_type == ImageType.PROMO else '1:1'} "
            f"{no_params}"
        )

    async def _submit_imagine(self, prompt: str) -> str:
        payload = {
            "prompt": prompt,
            "process_mode": "fast",
            "webhook_endpoint": "",
            "webhook_secret": "",
        }
        async with aiohttp.ClientSession() as session:
            async with session.post(
                f"{GOAPI_BASE_URL}/imagine",
                headers=self.headers,
                json=payload,
                timeout=aiohttp.ClientTimeout(total=30),
            ) as resp:
                resp.raise_for_status()
                data = await resp.json()
                task_id = data["data"]["task_id"]
                logger.info(f"[Midjourney] 작업 제출 task_id={task_id}")
                return task_id

    async def _wait_for_result(self, task_id: str, timeout: int = 300) -> str:
        """Midjourney 작업 완료까지 폴링 (평균 30~60초)"""
        deadline = asyncio.get_event_loop().time() + timeout
        async with aiohttp.ClientSession() as session:
            while True:
                if asyncio.get_event_loop().time() > deadline:
                    raise TimeoutError(f"[Midjourney] 타임아웃 task_id={task_id}")

                async with session.get(
                    f"{GOAPI_BASE_URL}/task/{task_id}/fetch",
                    headers=self.headers,
                    timeout=aiohttp.ClientTimeout(total=10),
                ) as resp:
                    resp.raise_for_status()
                    data = await resp.json()
                    status = data["data"]["status"]

                    if status == "finished":
                        url = data["data"]["task_result"]["image_url"]
                        logger.info(f"[Midjourney] 생성 완료 task_id={task_id}")
                        return url
                    elif status == "failed":
                        raise RuntimeError(f"[Midjourney] 생성 실패 task_id={task_id}")

                    logger.debug(f"[Midjourney] 진행중 status={status}")
                await asyncio.sleep(5)

    async def _upscale(self, task_id: str, index: int = 1) -> str:
        """U1~U4 업스케일로 단일 고해상도 이미지 추출"""
        payload = {
            "origin_task_id": task_id,
            "index": str(index),  # "1"~"4"
        }
        async with aiohttp.ClientSession() as session:
            async with session.post(
                f"{GOAPI_BASE_URL}/upscale",
                headers=self.headers,
                json=payload,
                timeout=aiohttp.ClientTimeout(total=30),
            ) as resp:
                resp.raise_for_status()
                data = await resp.json()
                upscale_task_id = data["data"]["task_id"]

        return await self._wait_for_result(upscale_task_id, timeout=120)

    async def _download_image(self, url: str) -> bytes:
        async with aiohttp.ClientSession() as session:
            async with session.get(url, timeout=aiohttp.ClientTimeout(total=60)) as resp:
                resp.raise_for_status()
                return await resp.read()
