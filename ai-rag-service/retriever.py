import logging
from typing import List

from indexer import ProductIndexer
from models import Source

logger = logging.getLogger(__name__)


class ProductRetriever:
    """
    ChromaDB에서 질문과 유사한 상품 문서를 검색하는 리트리버.
    상품 정보(product_info)를 우선 포함하고, 리뷰(review)를 보완으로 추가.
    """

    def __init__(self, indexer: ProductIndexer):
        self.indexer = indexer

    def retrieve(self, product_id: int, question: str, top_k: int = 4) -> List[Source]:
        question_embedding = self.indexer.model.encode([question]).tolist()

        results = self.indexer.collection.query(
            query_embeddings=question_embedding,
            n_results=top_k,
            where={"product_id": {"$eq": str(product_id)}},
            include=["documents", "metadatas", "distances"],
        )

        sources = []
        if results["documents"] and results["documents"][0]:
            for doc, meta, dist in zip(
                results["documents"][0],
                results["metadatas"][0],
                results["distances"][0],
            ):
                # 유사도가 너무 낮은 문서 제외 (cosine distance > 0.7)
                if dist > 0.7:
                    continue
                sources.append(Source(content=doc, source_type=meta["source_type"]))

        logger.debug("검색 결과: productId=%s, question='%s', %d건", product_id, question, len(sources))
        return sources
