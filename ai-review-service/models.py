from pydantic import BaseModel
from typing import List


class ReviewCreatedEvent(BaseModel):
    product_id: int
    review_id: int


class ReviewItem(BaseModel):
    id: int
    content: str
    rating: int  # 1~5


class SentimentResult(BaseModel):
    label: str                      # POSITIVE | NEGATIVE | MIXED
    positive_keywords: List[str]
    negative_keywords: List[str]


class ReviewSummaryResult(BaseModel):
    product_id: int
    summary: str
    sentiment: SentimentResult
    review_count: int
    avg_rating: float
