import json
import logging
from typing import List

import httpx

from models import ReviewItem, ReviewSummaryResult, SentimentResult

logger = logging.getLogger(__name__)

# temperature=0.1로 JSON 구조가 일관되게 출력되도록 유도
PROMPT = """당신은 쇼핑몰 리뷰 분석 전문가입니다.
아래 고객 리뷰들을 분석하여 반드시 JSON 형식으로만 응답하세요. 다른 텍스트는 절대 출력하지 마세요.

[리뷰]
{reviews}

[JSON 응답 형식]
{{
  "summary": "핵심 장단점을 포함한 2~3문장 요약",
  "sentiment": "POSITIVE 또는 NEGATIVE 또는 MIXED 중 하나",
  "positive_keywords": ["키워드1", "키워드2", "키워드3"],
  "negative_keywords": ["키워드1", "키워드2"]
}}"""


async def analyze_reviews(
    product_id: int,
    reviews: List[ReviewItem],
    ollama_url: str,
    model_name: str,
) -> ReviewSummaryResult:
    # 내용이 있는 리뷰만, 최대 20개
    valid = [r for r in reviews if r.content and len(r.content.strip()) > 5][:20]

    if not valid:
        return _empty_result(product_id, reviews)

    reviews_text = "\n".join(f"- [별점 {r.rating}점] {r.content}" for r in valid)
    prompt = PROMPT.format(reviews=reviews_text)

    async with httpx.AsyncClient(timeout=120.0) as client:
        resp = await client.post(
            f"{ollama_url}/api/generate",
            json={
                "model": model_name,
                "prompt": prompt,
                "stream": False,
                "format": "json",
                "options": {
                    "temperature": 0.1,
                    "num_predict": 512,
                },
            },
        )
        resp.raise_for_status()
        parsed = json.loads(resp.json()["response"])

    avg_rating = round(sum(r.rating for r in reviews) / len(reviews), 2) if reviews else 0.0

    return ReviewSummaryResult(
        product_id=product_id,
        summary=parsed.get("summary", ""),
        sentiment=SentimentResult(
            label=parsed.get("sentiment", "MIXED"),
            positive_keywords=parsed.get("positive_keywords", []),
            negative_keywords=parsed.get("negative_keywords", []),
        ),
        review_count=len(reviews),
        avg_rating=avg_rating,
    )


def _empty_result(product_id: int, reviews: list) -> ReviewSummaryResult:
    return ReviewSummaryResult(
        product_id=product_id,
        summary="아직 충분한 리뷰가 없습니다.",
        sentiment=SentimentResult(label="MIXED", positive_keywords=[], negative_keywords=[]),
        review_count=len(reviews),
        avg_rating=0.0,
    )
