import asyncio
import json
import logging

import aio_pika
import httpx

from config import settings
from indexer import ProductIndexer
from models import ProductCreatedEvent

logger = logging.getLogger(__name__)

PRODUCT_EXCHANGE = "product.exchange"
# ai-image-service와 동일한 이벤트를 별도 큐로 수신 (독립적 소비)
PRODUCT_CREATED_RAG_QUEUE = "product.created.rag"


async def _fetch_reviews(product_id: int) -> list[dict]:
    """Product Service에서 리뷰 목록 조회 후 인덱싱에 사용"""
    try:
        async with httpx.AsyncClient(timeout=10.0) as client:
            resp = await client.get(
                f"{settings.product_service_url}/reviews",
                params={"productId": product_id, "page": 0},
            )
            resp.raise_for_status()
            data = resp.json()
            items = data.get("content", data) if isinstance(data, dict) else data
            return [{"id": r["id"], "content": r["content"], "rating": r["rating"]} for r in items]
    except Exception as e:
        logger.warning("리뷰 조회 실패 (상품 정보만 인덱싱): productId=%s, %s", product_id, e)
        return []


async def _handle(message: aio_pika.IncomingMessage, indexer: ProductIndexer):
    async with message.process(requeue=True):
        event = ProductCreatedEvent(**json.loads(message.body))
        logger.info("인덱싱 시작: productId=%s, title=%s", event.productId, event.title)

        # 1. 상품 기본 정보 인덱싱
        indexer.index_product(
            product_id=event.productId,
            title=event.title,
            description=event.description,
            category=event.categoryName,
            price=event.price,
        )

        # 2. 기존 리뷰 인덱싱 (상품 등록 시점엔 없을 수 있음)
        reviews = await _fetch_reviews(event.productId)
        indexer.index_reviews(event.productId, reviews)

        logger.info(
            "인덱싱 완료: productId=%s, 리뷰 %d건", event.productId, len(reviews)
        )


async def start_consumer(indexer: ProductIndexer):
    connection = await aio_pika.connect_robust(
        host=settings.rabbitmq_host,
        port=settings.rabbitmq_port,
        login=settings.rabbitmq_user,
        password=settings.rabbitmq_password,
    )
    async with connection:
        channel = await connection.channel()
        await channel.set_qos(prefetch_count=1)

        # product.exchange는 이미 product service가 선언 — get_exchange로 참조
        exchange = await channel.declare_exchange(
            PRODUCT_EXCHANGE, aio_pika.ExchangeType.DIRECT, durable=True
        )

        # 별도 큐로 바인딩 → ai-image-service와 독립적으로 동일 이벤트 수신
        queue = await channel.declare_queue(PRODUCT_CREATED_RAG_QUEUE, durable=True)
        await queue.bind(exchange, routing_key="product.created")

        await queue.consume(lambda msg: _handle(msg, indexer))
        logger.info("RAG 인덱싱 컨슈머 시작")
        await asyncio.Future()
