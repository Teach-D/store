import logging
from typing import Optional

from .base import (
    ImageGenerationProvider,
    GenerationRequest,
    GenerationResult,
    ImageType,
    ProviderType,
)
from .stable_diffusion_provider import StableDiffusionProvider
from .dalle_provider import DallEProvider
from .midjourney_provider import MidjourneyProvider

logger = logging.getLogger(__name__)

# 이미지 타입별 프로바이더 우선순위
# (primary, fallback1, fallback2)
PROVIDER_PRIORITY: dict[ImageType, list[ProviderType]] = {
    ImageType.PRODUCT: [
        ProviderType.STABLE_DIFFUSION,  # 가장 저렴, 흰 배경 정확
        ProviderType.DALLE,             # SD 불가 시 DALL·E
        ProviderType.MIDJOURNEY,        # 최후 수단
    ],
    ImageType.PROMO: [
        ProviderType.DALLE,             # 자연어 프롬프트 이해도 최고
        ProviderType.STABLE_DIFFUSION,  # DALL·E 불가 시 SD
        ProviderType.MIDJOURNEY,
    ],
    ImageType.PREMIUM: [
        ProviderType.MIDJOURNEY,        # 예술적 품질 최고
        ProviderType.DALLE,
        ProviderType.STABLE_DIFFUSION,
    ],
}

# 최대 비용 제한 (USD) - 이 이상이면 다음 프로바이더로
MAX_COST_PER_IMAGE = {
    ImageType.PRODUCT: 0.05,
    ImageType.PROMO: 0.10,
    ImageType.PREMIUM: 0.20,
}


class ProviderSelector:
    """
    이미지 타입, 가용성, 비용에 따라 최적의 프로바이더를 선택.
    선택된 프로바이더 실패 시 자동으로 다음 프로바이더로 fallback.
    """

    def __init__(
        self,
        sd_provider: Optional[StableDiffusionProvider] = None,
        dalle_provider: Optional[DallEProvider] = None,
        mj_provider: Optional[MidjourneyProvider] = None,
    ):
        self._providers: dict[ProviderType, ImageGenerationProvider] = {}
        if sd_provider:
            self._providers[ProviderType.STABLE_DIFFUSION] = sd_provider
        if dalle_provider:
            self._providers[ProviderType.DALLE] = dalle_provider
        if mj_provider:
            self._providers[ProviderType.MIDJOURNEY] = mj_provider

    async def generate(self, request: GenerationRequest) -> GenerationResult:
        """우선순위에 따라 프로바이더 시도, 실패 시 자동 fallback"""
        priority = PROVIDER_PRIORITY.get(request.image_type, [ProviderType.STABLE_DIFFUSION])
        max_cost = MAX_COST_PER_IMAGE.get(request.image_type, 0.10)

        last_error: Optional[Exception] = None

        for provider_type in priority:
            provider = self._providers.get(provider_type)
            if provider is None:
                logger.debug(f"[Selector] {provider_type} 미설정, 건너뜀")
                continue

            if provider.cost_per_image_usd > max_cost:
                logger.debug(
                    f"[Selector] {provider_type} 비용 초과 "
                    f"(${provider.cost_per_image_usd:.3f} > ${max_cost:.3f}), 건너뜀"
                )
                continue

            if not await provider.is_available():
                logger.warning(f"[Selector] {provider_type} 응답 없음, 다음 프로바이더로")
                continue

            try:
                logger.info(
                    f"[Selector] {provider_type} 선택 "
                    f"(type={request.image_type}, productId={request.product_id})"
                )
                result = await provider.generate(request)
                logger.info(
                    f"[Selector] 생성 완료 provider={provider_type}, "
                    f"time={result.generation_time_ms}ms, cost=${result.cost_usd:.4f}"
                )
                return result

            except Exception as e:
                logger.warning(f"[Selector] {provider_type} 실패: {e}, 다음 프로바이더로 fallback")
                last_error = e

        raise RuntimeError(
            f"모든 프로바이더 실패 productId={request.product_id}: {last_error}"
        )

    def available_providers(self) -> list[ProviderType]:
        return list(self._providers.keys())
