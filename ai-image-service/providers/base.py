from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from enum import Enum
from typing import Optional


class ImageType(str, Enum):
    PRODUCT = "product"    # 상품 이미지 (흰 배경, 스튜디오)
    PROMO = "promo"        # 홍보 이미지 (마케팅 스타일)
    PREMIUM = "premium"    # 프리미엄 이미지 (최고 품질)


class ProviderType(str, Enum):
    STABLE_DIFFUSION = "stable_diffusion"
    DALLE = "dalle"
    MIDJOURNEY = "midjourney"


# 가격대별 스타일 키워드 (내림차순 — 첫 번째 매칭 사용)
PRICE_TIERS = [
    (200_000, "luxury, premium quality, high-end craftsmanship"),
    (50_000,  "mid-range, quality craftsmanship, refined"),
    (10_000,  "everyday, practical, clean presentation"),
    (0,       "affordable, simple, clear"),
]

# 이미지 타입별 공통 네거티브 프롬프트
NEGATIVE_PROMPTS = {
    ImageType.PRODUCT: (
        "blurry, low quality, deformed, ugly, bad anatomy, "
        "watermark, text, logo, person, human, hand, finger, "
        "extra objects, background clutter, harsh shadow, "
        "cropped, out of frame, oversaturated, noise"
    ),
    ImageType.PROMO: (
        "blurry, low quality, deformed, watermark, "
        "text overlay, logo, ugly composition, "
        "amateur, dull, flat lighting, noise"
    ),
    ImageType.PREMIUM: (
        "blurry, low quality, watermark, text, logo, "
        "deformed, amateur, flat lighting, noise"
    ),
}


@dataclass
class GenerationRequest:
    product_id: int
    title: str
    description: str
    category: str
    price: int
    image_type: ImageType
    width: int = 0
    height: int = 0
    reference_image: Optional[bytes] = None   # ControlNet 참고 이미지 (선택)


@dataclass
class GenerationResult:
    image_data: bytes
    provider: ProviderType
    prompt_used: str
    generation_time_ms: int
    cost_usd: float = 0.0
    metadata: dict = field(default_factory=dict)


class ImageGenerationProvider(ABC):
    """이미지 생성 프로바이더 추상 기반 클래스"""

    @property
    @abstractmethod
    def provider_type(self) -> ProviderType:
        pass

    @property
    @abstractmethod
    def cost_per_image_usd(self) -> float:
        pass

    @abstractmethod
    async def generate(self, request: GenerationRequest) -> GenerationResult:
        pass

    @abstractmethod
    async def is_available(self) -> bool:
        pass

    def get_price_tier(self, price: int) -> str:
        for threshold, label in PRICE_TIERS:
            if price >= threshold:
                return label
        return PRICE_TIERS[-1][1]

    def get_negative_prompt(self, image_type: ImageType) -> str:
        return NEGATIVE_PROMPTS.get(image_type, NEGATIVE_PROMPTS[ImageType.PRODUCT])

    def build_english_prompt(self, request: GenerationRequest) -> str:
        """공통 영어 프롬프트 생성 (가격대 반영)"""
        price_style = self.get_price_tier(request.price)
        base = f"{request.title}, {request.description}, {request.category} product"

        if request.image_type == ImageType.PRODUCT:
            return (
                f"professional product photography of {base}, "
                f"{price_style}, "
                f"clean white background, studio lighting, sharp focus, "
                f"high resolution, commercial photography"
            )
        elif request.image_type == ImageType.PROMO:
            return (
                f"promotional advertisement for {base}, "
                f"{price_style}, "
                f"dynamic composition, vibrant colors, "
                f"dramatic lighting, marketing material, cinematic"
            )
        else:  # PREMIUM
            return (
                f"ultra high quality image of {base}, "
                f"{price_style}, "
                f"award winning photography, masterpiece, "
                f"perfect composition, stunning visual"
            )
