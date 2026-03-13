import json
import asyncio
import logging
import time

import aio_pika
from deep_translator import GoogleTranslator

from config import settings
from comfyui_client import ComfyUIClient
from s3_uploader import S3Uploader

logger = logging.getLogger(__name__)

PRODUCT_EXCHANGE = "product.exchange"
PRODUCT_CREATED_QUEUE = "product.created"

# 카테고리별 프롬프트 스타일 템플릿
CATEGORY_STYLES = {
    "전자기기": "on a clean tech-styled surface, minimalist background, blue accent lighting",
    "스마트폰": "on a sleek reflective surface, tech background, modern minimalist",
    "노트북":   "on a desk setup, professional workspace background",
    "의류":     "on a mannequin or flat lay, fashion editorial style, neutral background",
    "식품":     "on a wooden table, natural lighting, fresh ingredients around",
    "화장품":   "on a marble surface, soft pastel background, beauty editorial style",
    "가구":     "in a modern interior setting, lifestyle photography",
    "스포츠":   "on a dynamic background, action-oriented composition, energetic lighting",
}
DEFAULT_STYLE = "clean white background, studio lighting"


def translate(text: str) -> str:
    try:
        return GoogleTranslator(source="auto", target="en").translate(text)
    except Exception:
        return text


def get_category_style(category: str) -> str:
    for key, style in CATEGORY_STYLES.items():
        if key in category:
            return style
    return DEFAULT_STYLE


def build_product_prompt(title: str, description: str, category: str) -> str:
    t = translate(f"{title} {description}")
    style = get_category_style(category)
    return (
        f"professional product photography of {t}, "
        f"{style}, sharp focus, high resolution, commercial photography"
    )


def build_promo_prompt(title: str, description: str, category: str) -> str:
    t = translate(f"{title} {description}")
    style = get_category_style(category)
    return (
        f"promotional advertisement for {t}, "
        f"{style}, vibrant colors, dynamic composition, "
        f"marketing material, dramatic lighting, high quality"
    )


async def handle(message: aio_pika.IncomingMessage, exchange: aio_pika.Exchange):
    async with message.process(requeue=False):
        data = json.loads(message.body)
        product_id = data["productId"]
        title      = data["title"]
        description= data["description"]
        category   = data.get("categoryName", "")

        logger.info(f"이미지 생성 시작 productId={product_id} category={category}")
        total_start = time.time()

        comfyui = ComfyUIClient(settings.comfyui_url, settings.comfyui_timeout)
        s3      = S3Uploader()

        # 1. 상품 이미지 (SDXL 기본 워크플로우)
        t1 = time.time()
        product_image = await comfyui.generate_product_image(
            build_product_prompt(title, description, category)
        )
        product_url = await s3.upload(product_image, f"products/{product_id}/product_image.png")
        logger.info(f"상품 이미지 완료: {time.time() - t1:.1f}초 | {product_url}")

        # 2. 홍보 이미지 (SDXL + LCM-LoRA 워크플로우)
        t2 = time.time()
        promo_image = await comfyui.generate_promo_image(
            build_promo_prompt(title, description, category)
        )
        promo_url = await s3.upload(promo_image, f"products/{product_id}/promo_image.png")
        logger.info(f"홍보 이미지 완료: {time.time() - t2:.1f}초 (LCM-LoRA) | {promo_url}")

        # 3. product-service로 결과 전송
        await exchange.publish(
            aio_pika.Message(
                body=json.dumps({
                    "productId":    product_id,
                    "imageUrl":     product_url,
                    "promoImageUrl": promo_url,
                }).encode(),
                content_type="application/json",
            ),
            routing_key="product.image.ready",
        )
        logger.info(f"이미지 생성 완료 productId={product_id} 총 소요시간={time.time() - total_start:.1f}초")


async def start_consumer():
    connection = await aio_pika.connect_robust(
        host=settings.rabbitmq_host,
        port=settings.rabbitmq_port,
        login=settings.rabbitmq_user,
        password=settings.rabbitmq_password,
    )
    async with connection:
        channel  = await connection.channel()
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
                await handle(message, exchange)
