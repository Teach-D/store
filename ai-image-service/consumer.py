import json
import asyncio
import logging
import time

import aio_pika
import aiohttp
from deep_translator import GoogleTranslator

from config import settings
from s3_uploader import S3Uploader
from providers.base import GenerationRequest, ImageType
from providers.selector import ProviderSelector
from providers.stable_diffusion_provider import StableDiffusionProvider
from providers.dalle_provider import DallEProvider

logger = logging.getLogger(__name__)

PRODUCT_EXCHANGE = "product.exchange"
PRODUCT_CREATED_QUEUE = "product.created"


def _translate(text: str) -> str:
    """한국어 → 영어 번역 (실패 시 원문 반환)"""
    try:
        return GoogleTranslator(source="auto", target="en").translate(text)
    except Exception:
        return text


def _build_selector() -> ProviderSelector:
    """
    ComfyUI(SD)를 기본으로 초기화.
    OPENAI_API_KEY 설정 시 DALL·E가 폴백으로 추가됨.
    """
    sd = StableDiffusionProvider(settings.comfyui_url, settings.comfyui_timeout)
    dalle = DallEProvider(settings.openai_api_key) if settings.openai_api_key else None
    selector = ProviderSelector(sd_provider=sd, dalle_provider=dalle)
    logger.info(f"프로바이더 초기화: {selector.available_providers()}")
    return selector


async def _download_bytes(url: str) -> bytes:
    async with aiohttp.ClientSession() as session:
        async with session.get(url, timeout=aiohttp.ClientTimeout(total=30)) as resp:
            resp.raise_for_status()
            return await resp.read()


async def handle(
    message: aio_pika.IncomingMessage,
    exchange: aio_pika.Exchange,
    selector: ProviderSelector,
):
    async with message.process(requeue=False):
        data        = json.loads(message.body)
        product_id  = data["productId"]
        title       = data["title"]
        description = data["description"]
        category    = data.get("categoryName", "")
        price       = data.get("price", 0)
        ref_url     = data.get("referenceImageUrl")

        logger.info(f"이미지 생성 시작 productId={product_id} category={category} price={price}")
        total_start = time.time()

        # 한국어 제목·설명 개별 번역 (함께 번역 시 문맥 손실 방지)
        title_en       = _translate(title)
        description_en = _translate(description)

        # ControlNet 참고 이미지 다운로드 (판매자가 URL 제공 시)
        reference_image = None
        if ref_url:
            try:
                reference_image = await _download_bytes(ref_url)
                logger.info(f"참고 이미지 다운로드 완료 productId={product_id}")
            except Exception as e:
                logger.warning(f"참고 이미지 다운로드 실패, ControlNet 없이 진행: {e}")

        product_req = GenerationRequest(
            product_id=product_id,
            title=title_en,
            description=description_en,
            category=category,
            price=price,
            image_type=ImageType.PRODUCT,
            reference_image=reference_image,  # 있으면 ControlNet 자동 사용
        )
        promo_req = GenerationRequest(
            product_id=product_id,
            title=title_en,
            description=description_en,
            category=category,
            price=price,
            image_type=ImageType.PROMO,
        )

        # 상품 이미지 + 홍보 이미지 병렬 생성 (프로바이더 폴백 포함)
        product_result, promo_result = await asyncio.gather(
            selector.generate(product_req),
            selector.generate(promo_req),
        )
        logger.info(
            f"생성 완료 productId={product_id} "
            f"product={product_result.provider}({product_result.generation_time_ms}ms) "
            f"promo={promo_result.provider}({promo_result.generation_time_ms}ms)"
        )

        # S3 업로드 병렬 처리
        s3 = S3Uploader()
        product_url, promo_url = await asyncio.gather(
            s3.upload(product_result.image_data, f"products/{product_id}/product_image.png"),
            s3.upload(promo_result.image_data, f"products/{product_id}/promo_image.png"),
        )

        # product-service로 완료 이벤트 전송
        await exchange.publish(
            aio_pika.Message(
                body=json.dumps({
                    "productId":     product_id,
                    "imageUrl":      product_url,
                    "promoImageUrl": promo_url,
                }).encode(),
                content_type="application/json",
            ),
            routing_key="product.image.ready",
        )
        logger.info(
            f"완료 productId={product_id} 총={time.time() - total_start:.1f}초 "
            f"controlnet={reference_image is not None}"
        )


async def start_consumer():
    selector = _build_selector()

    connection = await aio_pika.connect_robust(
        host=settings.rabbitmq_host,
        port=settings.rabbitmq_port,
        login=settings.rabbitmq_user,
        password=settings.rabbitmq_password,
    )
    async with connection:
        channel = await connection.channel()
        await channel.set_qos(prefetch_count=1)

        exchange = await channel.declare_exchange(
            PRODUCT_EXCHANGE, aio_pika.ExchangeType.DIRECT, durable=True
        )
        queue = await channel.declare_queue(
            PRODUCT_CREATED_QUEUE,
            durable=True,
            arguments={
                "x-dead-letter-exchange": "dlx.exchange",
                "x-dead-letter-routing-key": "product.created",
            },
        )
        await queue.bind(exchange, routing_key="product.created")

        logger.info("RabbitMQ 연결 완료 — product.created 큐 구독 시작")
        async with queue.iterator() as q:
            async for message in q:
                await handle(message, exchange, selector)
