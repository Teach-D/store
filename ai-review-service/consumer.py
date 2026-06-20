import asyncio
import json
import logging

import aio_pika
import httpx

from analyzer import analyze_reviews
from config import settings
from models import ReviewCreatedEvent, ReviewItem

logger = logging.getLogger(__name__)

REVIEW_EXCHANGE        = "review.exchange"
REVIEW_CREATED_QUEUE   = "review.created"
REVIEW_SUMMARY_QUEUE   = "review.summary.ready"
REVIEW_SUMMARY_ROUTING = "review.summary.ready"


async def _fetch_reviews(product_id: int) -> list[ReviewItem]:
    """Product Service에서 리뷰 목록 조회"""
    async with httpx.AsyncClient(timeout=10.0) as client:
        resp = await client.get(
            f"{settings.product_service_url}/reviews",
            params={"productId": product_id, "page": 0},
        )
        resp.raise_for_status()
        data = resp.json()
        items = data.get("content", data) if isinstance(data, dict) else data
        return [
            ReviewItem(id=r["id"], content=r["content"], rating=r["rating"])
            for r in items
        ]


async def _handle(message: aio_pika.IncomingMessage, channel: aio_pika.abc.AbstractChannel):
    async with message.process(requeue=True):
        event = ReviewCreatedEvent(**json.loads(message.body))
        logger.info("분석 시작: productId=%s", event.product_id)

        reviews = await _fetch_reviews(event.product_id)
        result = await analyze_reviews(
            event.product_id, reviews, settings.ollama_url, settings.model_name
        )

        exchange = await channel.get_exchange(REVIEW_EXCHANGE)
        await exchange.publish(
            aio_pika.Message(
                body=json.dumps(result.model_dump()).encode(),
                content_type="application/json",
            ),
            routing_key=REVIEW_SUMMARY_ROUTING,
        )
        logger.info(
            "분석 완료: productId=%s, sentiment=%s, avgRating=%.1f",
            event.product_id, result.sentiment.label, result.avg_rating,
        )


async def start_consumer():
    connection = await aio_pika.connect_robust(
        host=settings.rabbitmq_host,
        port=settings.rabbitmq_port,
        login=settings.rabbitmq_user,
        password=settings.rabbitmq_password,
    )
    async with connection:
        channel = await connection.channel()
        await channel.set_qos(prefetch_count=1)  # T4에서 추론은 한 번에 1건

        exchange = await channel.declare_exchange(
            REVIEW_EXCHANGE, aio_pika.ExchangeType.DIRECT, durable=True
        )

        # review.created: product service가 리뷰 저장 후 발행
        created_q = await channel.declare_queue(REVIEW_CREATED_QUEUE, durable=True)
        await created_q.bind(exchange, routing_key=REVIEW_CREATED_QUEUE)

        # review.summary.ready: product service가 수신해 Redis에 캐시
        await channel.declare_queue(REVIEW_SUMMARY_QUEUE, durable=True)

        await created_q.consume(lambda msg: _handle(msg, channel))
        logger.info("리뷰 분석 컨슈머 시작 — 모델: %s", settings.model_name)
        await asyncio.Future()  # 무한 대기
