import logging
from typing import List

import httpx

from models import ChatResponse, Source

logger = logging.getLogger(__name__)

# 상품 정보 범위 내에서만 답변하도록 명확히 제한
PROMPT = """당신은 쇼핑몰 상품 전문 상담사입니다.
반드시 아래 [상품 정보]만을 근거로 답변하세요.
[상품 정보]에 없는 내용은 "해당 정보를 확인할 수 없습니다. 판매자에게 문의해주세요."라고 답하세요.
추측하거나 일반 지식으로 답변하지 마세요.

[상품 정보]
{context}

[고객 질문]
{question}

[답변]"""


async def answer(
    product_id: int,
    question: str,
    sources: List[Source],
    ollama_url: str,
    model_name: str,
) -> ChatResponse:
    context = "\n\n".join(s.content for s in sources)
    prompt = PROMPT.format(context=context, question=question)

    async with httpx.AsyncClient(timeout=60.0) as client:
        resp = await client.post(
            f"{ollama_url}/api/generate",
            json={
                "model": model_name,
                "prompt": prompt,
                "stream": False,
                "options": {
                    "temperature": 0.3,   # 낮게 유지 → 상품 정보 벗어난 창의적 답변 방지
                    "num_predict": 512,
                },
            },
        )
        resp.raise_for_status()
        reply = resp.json()["response"].strip()

    return ChatResponse(product_id=product_id, answer=reply, sources=sources)
