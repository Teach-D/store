import asyncio
import logging
from contextlib import asynccontextmanager

import httpx
from fastapi import FastAPI, HTTPException

from chatbot import answer
from config import settings
from consumer import start_consumer
from indexer import ProductIndexer
from models import ChatRequest, ChatResponse
from retriever import ProductRetriever

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
logger = logging.getLogger(__name__)

indexer = ProductIndexer()
retriever = ProductRetriever(indexer)


@asynccontextmanager
async def lifespan(app: FastAPI):
    task = asyncio.create_task(start_consumer(indexer))
    yield
    task.cancel()


app = FastAPI(title="AI RAG Chatbot Service", version="1.0.0", lifespan=lifespan)


@app.get("/health")
async def health():
    count = indexer.collection.count()
    return {"status": "ok", "model": settings.model_name, "indexed_documents": count}


@app.post("/chat/{product_id}", response_model=ChatResponse)
async def chat(product_id: int, request: ChatRequest):
    """상품 기반 Q&A — 인덱싱된 상품 정보 + 리뷰를 검색해 답변 생성"""
    sources = retriever.retrieve(product_id, request.question)
    if not sources:
        raise HTTPException(
            status_code=404,
            detail="해당 상품의 인덱싱 데이터가 없습니다. /index/bulk 로 먼저 인덱싱하세요.",
        )
    return await answer(product_id, request.question, sources, settings.ollama_url, settings.model_name)


@app.post("/index/bulk")
async def bulk_index():
    """
    Product Service의 전체 상품을 ChromaDB에 인덱싱.
    서버 최초 실행 시 또는 상품 데이터 변경 시 호출.
    """
    async with httpx.AsyncClient(timeout=30.0) as client:
        resp = await client.get(f"{settings.product_service_url}/products/for-indexing")
        resp.raise_for_status()
        products = resp.json()

    if not products:
        return {"message": "인덱싱할 상품이 없습니다.", "count": 0}

    success, failed = 0, 0
    for p in products:
        try:
            indexer.index_product(
                product_id=p["id"],
                title=p["title"],
                description=p.get("description", ""),
                category=p.get("categoryName", ""),
                price=p.get("price", 0),
            )

            # 리뷰도 함께 인덱싱
            review_resp = await client.get(
                f"{settings.product_service_url}/reviews",
                params={"productId": p["id"], "page": 0},
            )
            if review_resp.status_code == 200:
                data = review_resp.json()
                items = data.get("content", data) if isinstance(data, dict) else data
                reviews = [{"id": r["id"], "content": r["content"], "rating": r["rating"]} for r in items]
                indexer.index_reviews(p["id"], reviews)

            success += 1
        except Exception as e:
            logger.error("인덱싱 실패: productId=%s, %s", p.get("id"), e)
            failed += 1

    logger.info("벌크 인덱싱 완료: 성공 %d건, 실패 %d건", success, failed)
    return {"message": "벌크 인덱싱 완료", "success": success, "failed": failed}


@app.post("/index/{product_id}")
async def manual_index(product_id: int, title: str, description: str, category: str = "", price: int = 0):
    """수동 인덱싱 — 테스트용"""
    indexer.index_product(product_id, title, description, category, price)
    return {"message": f"productId={product_id} 인덱싱 완료"}
