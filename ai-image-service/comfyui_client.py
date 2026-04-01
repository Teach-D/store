import asyncio
import json
import random
import logging
from pathlib import Path
from typing import Optional

import aiohttp

logger = logging.getLogger(__name__)

WORKFLOWS_DIR = Path(__file__).parent / "workflows"


NGROK_HEADERS = {"ngrok-skip-browser-warning": "true"}


class ComfyUIClient:
    def __init__(self, base_url: str, timeout: int = 300):
        self.base_url = base_url.rstrip("/")
        self.timeout = timeout

    async def generate_product_image(self, prompt: str) -> bytes:
        """상품 이미지 — SDXL 기본 (흰 배경, 스튜디오 조명)"""
        workflow = self._load("product_image.json")
        workflow["6"]["inputs"]["text"] = prompt
        workflow["3"]["inputs"]["seed"] = random.randint(0, 2 ** 32 - 1)
        return await self._run(workflow)

    async def generate_promo_image(self, prompt: str) -> bytes:
        """홍보 이미지 — SDXL + LCM-LoRA (8스텝, 빠른 생성)"""
        workflow = self._load("promo_image.json")
        workflow["6"]["inputs"]["text"] = prompt
        workflow["3"]["inputs"]["seed"] = random.randint(0, 2 ** 32 - 1)
        return await self._run(workflow)

    async def upload_image(self, filename: str, image_data: bytes) -> str:
        """ComfyUI에 이미지 업로드 후 파일명 반환 (ControlNet 참고 이미지용)"""
        form = aiohttp.FormData()
        form.add_field("image", image_data, filename=filename, content_type="image/png")
        form.add_field("type", "input")
        form.add_field("overwrite", "true")
        async with aiohttp.ClientSession(headers=NGROK_HEADERS) as s:
            async with s.post(
                f"{self.base_url}/upload/image",
                data=form,
                timeout=aiohttp.ClientTimeout(total=30),
            ) as r:
                r.raise_for_status()
                return (await r.json())["name"]

    async def generate_controlnet_image(
        self, prompt: str, negative: str, reference_image: bytes
    ) -> bytes:
        """ControlNet 워크플로우로 참고 이미지 구도를 따라 생성"""
        uploaded_name = await self.upload_image("control_reference.png", reference_image)
        workflow = self._load("controlnet_image.json")
        workflow["6"]["inputs"]["text"] = prompt
        workflow["7"]["inputs"]["text"] = negative
        workflow["13"]["inputs"]["image"] = uploaded_name
        workflow["3"]["inputs"]["seed"] = random.randint(0, 2 ** 32 - 1)
        return await self._run(workflow)

    async def _run(self, workflow: dict) -> bytes:
        prompt_id = await self._queue(workflow)
        logger.info(f"ComfyUI 작업 등록 prompt_id={prompt_id}")
        await self._wait(prompt_id)
        return await self._download(prompt_id)

    async def _queue(self, workflow: dict) -> str:
        async with aiohttp.ClientSession(headers=NGROK_HEADERS) as s:
            async with s.post(
                f"{self.base_url}/prompt",
                json={"prompt": workflow},
                timeout=aiohttp.ClientTimeout(total=30),
            ) as r:
                r.raise_for_status()
                return (await r.json())["prompt_id"]

    async def _wait(self, prompt_id: str):
        """폴링으로 완료 감지 (ngrok WebSocket 미지원 대응)"""
        await self._poll(prompt_id)

    async def _poll(self, prompt_id: str):
        """WebSocket 실패 시 폴링으로 대체"""
        deadline = asyncio.get_event_loop().time() + self.timeout
        async with aiohttp.ClientSession(headers=NGROK_HEADERS) as s:
            while True:
                if asyncio.get_event_loop().time() > deadline:
                    raise TimeoutError(f"ComfyUI 타임아웃 prompt_id={prompt_id}")
                async with s.get(f"{self.base_url}/history/{prompt_id}") as r:
                    history = await r.json()
                    if prompt_id in history:
                        outputs = history[prompt_id].get("outputs", {})
                        status = history[prompt_id].get("status", {})
                        if outputs:
                            return
                        if status.get("status_str") in ("error", "failed"):
                            raise RuntimeError(f"ComfyUI 생성 오류 prompt_id={prompt_id}")
                await asyncio.sleep(2)

    async def _download(self, prompt_id: str) -> bytes:
        async with aiohttp.ClientSession(headers=NGROK_HEADERS) as s:
            async with s.get(f"{self.base_url}/history/{prompt_id}") as r:
                history = await r.json()
                for output in history[prompt_id]["outputs"].values():
                    if "images" in output:
                        img = output["images"][0]
                        url = f"{self.base_url}/view?filename={img['filename']}&subfolder={img.get('subfolder','')}&type=output"
                        async with s.get(url) as img_r:
                            img_r.raise_for_status()
                            return await img_r.read()
        raise RuntimeError(f"이미지 없음 prompt_id={prompt_id}")

    def _load(self, filename: str) -> dict:
        with open(WORKFLOWS_DIR / filename, encoding="utf-8") as f:
            return json.load(f)
