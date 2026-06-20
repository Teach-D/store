FROM python:3.11-slim
WORKDIR /app

# sentence-transformers 빌드 의존성
RUN apt-get update && apt-get install -y gcc g++ && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# ChromaDB 영구 저장 디렉토리
VOLUME ["/app/chroma_db"]

EXPOSE 8000
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
