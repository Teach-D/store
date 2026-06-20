from pydantic import BaseModel
from typing import List


class ProductCreatedEvent(BaseModel):
    productId: int
    title: str
    description: str
    categoryName: str
    price: int
    referenceImageUrl: str | None = None


class ChatRequest(BaseModel):
    question: str


class Source(BaseModel):
    content: str
    source_type: str   # "product_info" | "review"


class ChatResponse(BaseModel):
    product_id: int
    answer: str
    sources: List[Source]
