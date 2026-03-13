import asyncio
import logging
from contextlib import asynccontextmanager
from fastapi import FastAPI
from consumer import start_consumer

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(message)s",
)


logger = logging.getLogger(__name__)


async def _run_consumer():
    try:
        await start_consumer()
    except Exception as e:
        logger.error(f"RabbitMQ 연결 실패: {e}", exc_info=True)


@asynccontextmanager
async def lifespan(app: FastAPI):
    task = asyncio.create_task(_run_consumer())
    yield
    task.cancel()


app = FastAPI(lifespan=lifespan)


@app.get("/health")
def health():
    return {"status": "ok"}
