import time
import logging

import aiohttp

from .base import ImageGenerationProvider, GenerationRequest, GenerationResult, ProviderType, ImageType

logger = logging.getLogger(__name__)

DALLE_API_URL = "https://api.openai.com/v1/images/generations"

# DALL·E 3 공식 가격 (2025년 기준)
DALLE_PRICING = {
    "1024x1024": 0.040,   # Standard
    "1024x1792": 0.080,   # HD portrait
    "1792x1024": 0.080,   # HD landscape
}


class DallEProvider(ImageGenerationProvider):
    """
    OpenAI DALL·E 3 이미지 생성
    - 프롬프트 이해력이 뛰어나 홍보/마케팅 이미지에 최적
    - 비용: $0.04~0.08/장
    - 공식 OpenAI API 사용 (신뢰성 높음)
    """

    def __init__(self, api_key: str):
        self.api_key = api_key
        self.headers = {
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json",
        }

    @property
    def provider_type(self) -> ProviderType:
        return ProviderType.DALLE

    @property
    def cost_per_image_usd(self) -> float:
        return 0.040  # Standard 1024x1024

    async def generate(self, request: GenerationRequest) -> GenerationResult:
        start_ms = int(time.time() * 1000)

        prompt = self._build_dalle_prompt(request)
        size, quality = self._resolve_params(request)

        logger.info(f"[DALL·E] 이미지 생성 시작 productId={request.product_id}, size={size}")

        image_url = await self._call_api(prompt, size, quality)
        image_data = await self._download_image(image_url)

        cost = DALLE_PRICING.get(size, 0.040)
        if quality == "hd":
            cost *= 2

        return GenerationResult(
            image_data=image_data,
            provider=self.provider_type,
            prompt_used=prompt,
            generation_time_ms=int(time.time() * 1000) - start_ms,
            cost_usd=cost,
            metadata={"size": size, "quality": quality, "model": "dall-e-3"},
        )

    async def is_available(self) -> bool:
        if not self.api_key:
            return False
        try:
            async with aiohttp.ClientSession() as session:
                async with session.get(
                    "https://api.openai.com/v1/models",
                    headers=self.headers,
                    timeout=aiohttp.ClientTimeout(total=5),
                ) as resp:
                    return resp.status == 200
        except Exception:
            return False

    def _build_dalle_prompt(self, request: GenerationRequest) -> str:
        """
        DALL·E 3은 상세한 자연어 프롬프트에 강함.
        DALL·E 3 가이드라인: 저작권 캐릭터, 실존 인물 포함 금지.
        """
        base = self.build_english_prompt(request)

        if request.image_type == ImageType.PROMO:
            return (
                f"{base}. "
                f"Create a professional marketing advertisement image. "
                f"The image should be visually striking with dynamic composition, "
                f"suitable for e-commerce platform promotion. "
                f"No text or watermarks in the image."
            )
        else:
            return (
                f"{base}. "
                f"The product should be the sole focus with a clean, "
                f"pure white background. Professional e-commerce product photography. "
                f"No text, logos, or watermarks."
            )

    def _resolve_params(self, request: GenerationRequest) -> tuple[str, str]:
        """이미지 타입에 따라 크기와 품질 결정"""
        if request.image_type == ImageType.PREMIUM:
            return "1792x1024", "hd"
        elif request.image_type == ImageType.PROMO:
            return "1792x1024", "standard"
        else:
            return "1024x1024", "standard"

    async def _call_api(self, prompt: str, size: str, quality: str) -> str:
        payload = {
            "model": "dall-e-3",
            "prompt": prompt,
            "n": 1,
            "size": size,
            "quality": quality,
            "response_format": "url",
        }
        async with aiohttp.ClientSession() as session:
            async with session.post(
                DALLE_API_URL,
                headers=self.headers,
                json=payload,
                timeout=aiohttp.ClientTimeout(total=120),
            ) as resp:
                if resp.status == 429:
                    raise RuntimeError("[DALL·E] Rate limit 초과")
                if resp.status == 400:
                    body = await resp.json()
                    raise ValueError(f"[DALL·E] 프롬프트 오류: {body.get('error', {}).get('message')}")
                resp.raise_for_status()
                data = await resp.json()
                return data["data"][0]["url"]

    async def _download_image(self, url: str) -> bytes:
        async with aiohttp.ClientSession() as session:
            async with session.get(url, timeout=aiohttp.ClientTimeout(total=60)) as resp:
                resp.raise_for_status()
                return await resp.read()
