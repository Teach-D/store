import logging

import chromadb
from sentence_transformers import SentenceTransformer

from config import settings

logger = logging.getLogger(__name__)


class ProductIndexer:
    """
    상품 정보와 리뷰를 ChromaDB에 벡터로 저장하는 인덱서.

    컬렉션 구조:
      - name: "products"
      - metadata 필드: product_id (str), source_type ("product_info" | "review")
      - 유사도: cosine
    """

    def __init__(self):
        self.client = chromadb.PersistentClient(path=settings.chroma_persist_dir)
        self.collection = self.client.get_or_create_collection(
            name="products",
            metadata={"hnsw:space": "cosine"},
        )
        # 최초 실행 시 모델 다운로드 (~420MB)
        self.model = SentenceTransformer(settings.embedding_model)
        logger.info("ProductIndexer 초기화 완료 — 임베딩 모델: %s", settings.embedding_model)

    def index_product(
        self,
        product_id: int,
        title: str,
        description: str,
        category: str,
        price: int,
    ):
        """상품 기본 정보 인덱싱 (재인덱싱 시 기존 데이터 교체)"""
        self._delete_by_source(product_id, "product_info")

        doc = f"상품명: {title}\n카테고리: {category}\n가격: {price:,}원\n설명: {description}"
        embedding = self.model.encode([doc]).tolist()

        self.collection.add(
            documents=[doc],
            embeddings=embedding,
            metadatas=[{"product_id": str(product_id), "source_type": "product_info"}],
            ids=[f"product_{product_id}_info"],
        )
        logger.info("상품 정보 인덱싱 완료: productId=%s", product_id)

    def index_reviews(self, product_id: int, reviews: list[dict]):
        """리뷰 목록 인덱싱 (기존 리뷰 전체 교체)"""
        self._delete_by_source(product_id, "review")

        if not reviews:
            return

        docs, metadatas, ids = [], [], []
        for review in reviews:
            docs.append(f"고객 리뷰 (별점 {review['rating']}점): {review['content']}")
            metadatas.append({"product_id": str(product_id), "source_type": "review"})
            ids.append(f"product_{product_id}_review_{review['id']}")

        embeddings = self.model.encode(docs).tolist()
        self.collection.add(
            documents=docs,
            embeddings=embeddings,
            metadatas=metadatas,
            ids=ids,
        )
        logger.info("리뷰 인덱싱 완료: productId=%s, %d건", product_id, len(reviews))

    def _delete_by_source(self, product_id: int, source_type: str):
        """특정 상품의 source_type 문서 삭제"""
        self.collection.delete(
            where={
                "$and": [
                    {"product_id": {"$eq": str(product_id)}},
                    {"source_type": {"$eq": source_type}},
                ]
            }
        )
