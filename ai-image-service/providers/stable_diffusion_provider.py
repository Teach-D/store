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
NGROK_HEADERS = {"ngrok-skip-browser-warning": "true"}

# 카테고리별 스타일 전략
CATEGORY_STYLES = {
    "전자제품": {
        "style": "sleek modern tech product, dark gradient background, blue accent rim lighting",
        "negative": "organic, nature, food, clothing, dirty",
        "lora": "tech_product_xl.safetensors",
    },
    "스마트폰": {
        "style": "smartphone on sleek reflective surface, minimalist dark background, tech photography",
        "negative": "cracked screen, dirty, case damage",
        "lora": "tech_product_xl.safetensors",
    },
    "노트북": {
        "style": "laptop on clean desk, professional workspace, soft ambient lighting",
        "negative": "dust, scratches, messy background",
        "lora": None,
    },
    "의류": {
        "style": "fashion photography, soft diffused studio lighting, neutral background, garment focus",
        "negative": "mannequin face, plastic dummy, wrinkles, stains",
        "lora": "fashion_xl.safetensors",
    },
    "신발": {
        "style": "sneaker product photography, 3/4 angle, clean gradient background, sharp detail",
        "negative": "dirty sole, scuff marks, worn",
        "lora": None,
    },
    "식품": {
        "style": "food photography, appetizing presentation, natural warm lighting, wooden surface, shallow depth of field",
        "negative": "plastic packaging, unappetizing, mold, expired",
        "lora": "food_photography_xl.safetensors",
    },
    "화장품": {
        "style": "beauty product photography, marble surface, soft pastel background, luxury editorial",
        "negative": "used product, smudges, dirty",
        "lora": None,
    },
    "가구": {
        "style": "interior lifestyle photography, warm ambient lighting, modern Scandinavian home setting",
        "negative": "outdoor, damage, scratches, messy",
        "lora": None,
    },
    "스포츠": {
        "style": "dynamic sports product photography, energetic composition, gradient background",
        "negative": "worn out, damage, dirty",
        "lora": None,
    },
    "도서": {
        "style": "book flat lay, clean white background, soft shadows, editorial style",
        "negative": "damaged pages, coffee stains, torn",
        "lora": None,
    },
    "default": {
        "style": "clean white background, professional studio lighting, sharp focus",
        "negative": "blurry, low quality, deformed",
        "lora": None,
    },
}


class StableDiffusionProvider(ImageGenerationProvider):
    """
    ComfyUI를 통한 Stable Diffusion XL 이미지 생성
    - 상품 이미지: SDXL Base (깨끗한 배경, 스튜디오 조명)
    - 홍보 이미지: SDXL + LCM-LoRA (마케팅 스타일)
    - ControlNet: referenceImage 제공 시 구도 유지 생성
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

        strategy = self._get_strategy(request.category)
        prompt = self._build_sd_prompt(request, strategy)
        negative = self._build_negative(request, strategy)

        if request.reference_image:
            # ControlNet: 참고 이미지 구도를 따라 생성
            uploaded_name = await self._upload_reference_image(request.reference_image)
            workflow = self._build_controlnet_workflow(prompt, negative, request, uploaded_name)
            logger.info(f"[SD] ControlNet 모드 productId={request.product_id}")
        elif request.image_type == ImageType.PROMO and strategy["lora"]:
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
            metadata={
                "category_strategy": request.category,
                "controlnet": request.reference_image is not None,
            },
        )

    async def is_available(self) -> bool:
        try:
            async with aiohttp.ClientSession(headers=NGROK_HEADERS) as session:
                async with session.get(
                    f"{self.base_url}/system_stats",
                    timeout=aiohttp.ClientTimeout(total=5),
                ) as resp:
                    return resp.status == 200
        except Exception:
            return False

    # ── 프롬프트 빌더 ──────────────────────────────────────────────────────────

    def _get_strategy(self, category: str) -> dict:
        for key, strategy in CATEGORY_STYLES.items():
            if key != "default" and key in category:
                return strategy
        return CATEGORY_STYLES["default"]

    def _build_sd_prompt(self, request: GenerationRequest, strategy: dict) -> str:
        base = self.build_english_prompt(request)
        return f"{base}, {strategy['style']}"

    def _build_negative(self, request: GenerationRequest, strategy: dict) -> str:
        base_negative = self.get_negative_prompt(request.image_type)
        category_negative = strategy.get("negative", "")
        if category_negative:
            return f"{base_negative}, {category_negative}"
        return base_negative

    # ── 워크플로우 빌더 ────────────────────────────────────────────────────────

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

    def _build_controlnet_workflow(
        self, prompt: str, negative: str, request: GenerationRequest, reference_filename: str
    ) -> dict:
        w, h = self._resolve_size(request)
        with open(WORKFLOWS_DIR / "controlnet_image.json", encoding="utf-8") as f:
            workflow = json.load(f)
        workflow["6"]["inputs"]["text"] = prompt
        workflow["7"]["inputs"]["text"] = negative
        workflow["13"]["inputs"]["image"] = reference_filename
        workflow["5"]["inputs"]["width"] = w
        workflow["5"]["inputs"]["height"] = h
        workflow["3"]["inputs"]["seed"] = random.randint(0, 2 ** 32 - 1)
        return workflow

    async def _upload_reference_image(self, image_data: bytes) -> str:
        """ComfyUI에 참고 이미지 업로드 후 파일명 반환"""
        form = aiohttp.FormData()
        form.add_field(
            "image",
            image_data,
            filename="control_reference.png",
            content_type="image/png",
        )
        form.add_field("type", "input")
        form.add_field("overwrite", "true")

        async with aiohttp.ClientSession(headers=NGROK_HEADERS) as session:
            async with session.post(
                f"{self.base_url}/upload/image",
                data=form,
                timeout=aiohttp.ClientTimeout(total=30),
            ) as resp:
                resp.raise_for_status()
                result = await resp.json()
                logger.info(f"[SD] 참고 이미지 업로드 완료: {result['name']}")
                return result["name"]

    def _resolve_size(self, request: GenerationRequest) -> tuple[int, int]:
        if request.width and request.height:
            return request.width, request.height
        if request.image_type == ImageType.PROMO:
            return 1280, 720
        return 1024, 1024

    # ── ComfyUI API ────────────────────────────────────────────────────────────

    async def _run_workflow(self, workflow: dict) -> bytes:
        prompt_id = await self._queue_prompt(workflow)
        logger.info(f"[SD] 작업 큐 등록 prompt_id={prompt_id}")
        await self._wait_for_completion(prompt_id)
        return await self._download_result_image(prompt_id)

    async def _queue_prompt(self, workflow: dict) -> str:
        async with aiohttp.ClientSession(headers=NGROK_HEADERS) as session:
            async with session.post(
                f"{self.base_url}/prompt",
                json={"prompt": workflow},
                timeout=aiohttp.ClientTimeout(total=30),
            ) as resp:
                resp.raise_for_status()
                return (await resp.json())["prompt_id"]

    async def _wait_for_completion(self, prompt_id: str):
        """WebSocket 우선, 미지원 시 폴링으로 fallback"""
        try:
            async with aiohttp.ClientSession(headers=NGROK_HEADERS) as session:
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
                                return
                        elif msg.type == aiohttp.WSMsgType.ERROR:
                            raise RuntimeError(f"[SD] WebSocket 오류 prompt_id={prompt_id}")
        except Exception:
            await self._wait_by_polling(prompt_id)

    async def _wait_by_polling(self, prompt_id: str):
        deadline = asyncio.get_event_loop().time() + self.timeout
        async with aiohttp.ClientSession(headers=NGROK_HEADERS) as session:
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
        async with aiohttp.ClientSession(headers=NGROK_HEADERS) as session:
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
