from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from enum import Enum


class ImageType(str, Enum):
    PRODUCT = "product"    # 상품 이미지 (흰 배경, 스튜디오)
    PROMO = "promo"        # 홍보 이미지 (마케팅 스타일)
    PREMIUM = "premium"    # 프리미엄 이미지 (최고 품질)


class ProviderType(str, Enum):
    STABLE_DIFFUSION = "stable_diffusion"
    DALLE = "dalle"
    MIDJOURNEY = "midjourney"


@dataclass
class GenerationRequest:
    product_id: int
    title: str
    description: str
    category: str
    price: int
    image_type: ImageType
    # 이미지 크기 (기본값: 타입에 따라 자동 설정)
    width: int = 0
    height: int = 0


@dataclass
class GenerationResult:
    image_data: bytes
    provider: ProviderType
    prompt_used: str
    generation_time_ms: int
    # 비용 (USD)
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
        """이미지 1장당 예상 비용 (USD)"""
        pass

    @abstractmethod
    async def generate(self, request: GenerationRequest) -> GenerationResult:
        """이미지 생성 메인 메서드"""
        pass

    @abstractmethod
    async def is_available(self) -> bool:
        """프로바이더 사용 가능 여부 (헬스체크)"""
        pass

    def build_english_prompt(self, request: GenerationRequest) -> str:
        """공통 영어 프롬프트 생성 (한국어 제목 대응)"""
        base = f"{request.title}, {request.description}, {request.category} product"
        if request.image_type == ImageType.PRODUCT:
            return (
                f"professional product photography of {base}, "
                f"clean white background, studio lighting, sharp focus, "
                f"high resolution, commercial photography"
            )
        elif request.image_type == ImageType.PROMO:
            return (
                f"promotional advertisement for {base}, "
                f"dynamic composition, vibrant colors, luxury showcase, "
                f"dramatic lighting, marketing material, cinematic"
            )
        else:  # PREMIUM
            return (
                f"ultra high quality image of {base}, "
                f"award winning photography, masterpiece, "
                f"perfect composition, stunning visual"
            )
