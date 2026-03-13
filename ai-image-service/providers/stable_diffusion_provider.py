import asyncio
import json
import random
import time
import logging
from pathlib import Path

import aiohttp

from .base import ImageGenerationProvider, GenerationRequest, GenerationResult, ProviderType, ImageType

logger = logging.getLogger(__name__)

WORKFLOWS_DIR = Path(__file__).parent.parent / "workflows"

# 카테고리별 스타일 전략
CATEGORY_STYLES = {
    "전자제품": {
        "style": "sleek modern tech product, dark gradient background, blue accent rim lighting",
        "negative": "organic, nature, food, clothing",
        "lora": "tech_product_xl.safetensors",
    },
    "의류": {
        "style": "fashion photography, soft studio lighting, minimal background",
        "negative": "mannequin, plastic dummy, electronics",
        "lora": "fashion_xl.safetensors",
    },
    "식품": {
        "style": "food photography, appetizing presentation, natural warm lighting, wooden surface",
        "negative": "plastic, unappetizing, electronics",
        "lora": "food_photography_xl.safetensors",
    },
    "가구": {
        "style": "interior lifestyle photography, warm ambient lighting, modern home setting",
        "negative": "outdoor, electronics",
        "lora": None,
    },
    "default": {
        "style": "clean white background, studio lighting",
        "negative": "blurry, low quality, deformed",
        "lora": None,
    },
}


class StableDiffusionProvider(ImageGenerationProvider):
    """
    ComfyUI를 통한 Stable Diffusion XL 이미지 생성
    - 상품 이미지: SDXL Base (깨끗한 배경, 스튜디오 조명)
    - 홍보 이미지: SDXL + LoRA (마케팅 스타일)
    - 비용: ~$0.001/장 (RTX 3090 Spot 기준)
    """

    def __init__(self, base_url: str, timeout: int = 300):
        self.base_url = base_url.rstrip("/")
        self.timeout = timeout

    @property
    def provider_type(self) -> ProviderType:
        return ProviderType.STABLE_DIFFUSION

    @property
    def cost_per_image_usd(self) -> float:
        return 0.001  # RTX 3090 Spot ~$0.20/hr, 12초/장

    async def generate(self, request: GenerationRequest) -> GenerationResult:
        start_ms = int(time.time() * 1000)

        strategy = CATEGORY_STYLES.get(request.category, CATEGORY_STYLES["default"])
        prompt = self._build_sd_prompt(request, strategy)
        negative = strategy["negative"]

        if request.image_type == ImageType.PROMO and strategy["lora"]:
            workflow = self._build_lora_workflow(prompt, negative, strategy["lora"], request)
        else:
            workflow = self._build_base_workflow(prompt, negative, request)

        image_data = await self._run_workflow(workflow)

        return GenerationResult(
            image_data=image_data,
            provider=self.provider_type,
            prompt_used=prompt,
            generation_time_ms=int(time.time() * 1000) - start_ms,
            cost_usd=self.cost_per_image_usd,
            metadata={"category_strategy": request.category, "lora": strategy["lora"]},
        )

    async def is_available(self) -> bool:
        try:
            async with aiohttp.ClientSession() as session:
                async with session.get(
                    f"{self.base_url}/system_stats",
                    timeout=aiohttp.ClientTimeout(total=5),
                ) as resp:
                    return resp.status == 200
        except Exception:
            return False

    def _build_sd_prompt(self, request: GenerationRequest, strategy: dict) -> str:
        base = self.build_english_prompt(request)
        return f"{base}, {strategy['style']}"

    def _build_base_workflow(self, prompt: str, negative: str, request: GenerationRequest) -> dict:
        w, h = self._resolve_size(request)
        with open(WORKFLOWS_DIR / "product_image.json", encoding="utf-8") as f:
            workflow = json.load(f)
        workflow["6"]["inputs"]["text"] = prompt
        workflow["7"]["inputs"]["text"] = negative
        workflow["5"]["inputs"]["width"] = w
        workflow["5"]["inputs"]["height"] = h
        workflow["3"]["inputs"]["seed"] = random.randint(0, 2 ** 32 - 1)
        return workflow

    def _build_lora_workflow(
        self, prompt: str, negative: str, lora_name: str, request: GenerationRequest
    ) -> dict:
        w, h = self._resolve_size(request)
        with open(WORKFLOWS_DIR / "promo_image.json", encoding="utf-8") as f:
            workflow = json.load(f)
        workflow["6"]["inputs"]["text"] = prompt
        workflow["7"]["inputs"]["text"] = negative
        workflow["10"]["inputs"]["lora_name"] = lora_name
        workflow["5"]["inputs"]["width"] = w
        workflow["5"]["inputs"]["height"] = h
        workflow["3"]["inputs"]["seed"] = random.randint(0, 2 ** 32 - 1)
        return workflow

    def _resolve_size(self, request: GenerationRequest) -> tuple[int, int]:
        if request.width and request.height:
            return request.width, request.height
        if request.image_type == ImageType.PROMO:
            return 1280, 720
        return 1024, 1024

    async def _run_workflow(self, workflow: dict) -> bytes:
        prompt_id = await self._queue_prompt(workflow)
        logger.info(f"[SD] 작업 큐 등록 prompt_id={prompt_id}")
        await self._wait_for_completion(prompt_id)
        return await self._download_result_image(prompt_id)

    async def _queue_prompt(self, workflow: dict) -> str:
        async with aiohttp.ClientSession() as session:
            async with session.post(
                f"{self.base_url}/prompt",
                json={"prompt": workflow},
                timeout=aiohttp.ClientTimeout(total=30),
            ) as resp:
                resp.raise_for_status()
                return (await resp.json())["prompt_id"]

    async def _wait_for_completion(self, prompt_id: str):
        """WebSocket으로 완료 이벤트 수신 (폴링 대비 효율적)"""
        try:
            async with aiohttp.ClientSession() as session:
                async with session.ws_connect(
                    f"{self.base_url}/ws?clientId={prompt_id}",
                    timeout=aiohttp.ClientTimeout(total=self.timeout),
                ) as ws:
                    async for msg in ws:
                        if msg.type == aiohttp.WSMsgType.TEXT:
                            data = json.loads(msg.data)
                            if (
                                data.get("type") == "executing"
                                and data.get("data", {}).get("node") is None
                            ):
                                return  # 완료
                        elif msg.type == aiohttp.WSMsgType.ERROR:
                            raise RuntimeError(f"[SD] WebSocket 오류 prompt_id={prompt_id}")
        except Exception:
            # WebSocket 미지원 시 폴링으로 fallback
            await self._wait_by_polling(prompt_id)

    async def _wait_by_polling(self, prompt_id: str):
        deadline = asyncio.get_event_loop().time() + self.timeout
        async with aiohttp.ClientSession() as session:
            while True:
                if asyncio.get_event_loop().time() > deadline:
                    raise TimeoutError(f"[SD] 타임아웃 prompt_id={prompt_id}")
                async with session.get(f"{self.base_url}/history/{prompt_id}") as resp:
                    history = await resp.json()
                    if prompt_id in history:
                        status = history[prompt_id].get("status", {})
                        if status.get("completed"):
                            return
                        if status.get("status_str") == "error":
                            raise RuntimeError(f"[SD] 생성 오류 prompt_id={prompt_id}")
                await asyncio.sleep(2)

    async def _download_result_image(self, prompt_id: str) -> bytes:
        async with aiohttp.ClientSession() as session:
            async with session.get(f"{self.base_url}/history/{prompt_id}") as resp:
                history = await resp.json()
                for output in history[prompt_id]["outputs"].values():
                    if "images" in output:
                        img = output["images"][0]
                        params = f"filename={img['filename']}&subfolder={img.get('subfolder', '')}&type=output"
                        async with session.get(f"{self.base_url}/view?{params}") as img_resp:
                            img_resp.raise_for_status()
                            return await img_resp.read()
        raise RuntimeError(f"[SD] 이미지 결과 없음 prompt_id={prompt_id}")
