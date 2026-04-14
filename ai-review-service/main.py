import asyncio
import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException

from analyzer import analyze_reviews
from config import settings
from consumer import start_consumer
from models import ReviewItem, ReviewSummaryResult

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")


@asynccontextmanager
async def lifespan(app: FastAPI):
    task = asyncio.create_task(start_consumer())
    yield
    task.cancel()


app = FastAPI(title="AI Review Service", version="1.0.0", lifespan=lifespan)


@app.get("/health")
async def health():
    return {"status": "ok", "model": settings.model_name, "ollama": settings.ollama_url}


@app.post("/analyze/{product_id}", response_model=ReviewSummaryResult)
async def analyze(product_id: int, reviews: list[ReviewItem]):
    """수동 분석 트리거 — 테스트 및 재분석용"""
    try:
        return await analyze_reviews(
            product_id, reviews, settings.ollama_url, settings.model_name
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
