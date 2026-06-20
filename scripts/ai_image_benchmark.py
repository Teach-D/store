"""
AI 이미지 생성 성능 벤치마크

측정 항목:
  - 상품 이미지 (SDXL 25스텝)
  - 홍보 이미지 (SDXL + LCM-LoRA 8스텝)
  - ControlNet 이미지 (Canny 30스텝)
  - 순차 생성 vs 병렬 생성 비교

실행 방법:
  cd ai-image-service
  python ../scripts/ai_image_benchmark.py --url http://localhost:8188 --runs 3
  python ../scripts/ai_image_benchmark.py --url https://your-ngrok-url --runs 5
"""

import asyncio
import argparse
import statistics
import sys
import time
from pathlib import Path

# ai-image-service 경로를 모듈 검색 경로에 추가
sys.path.insert(0, str(Path(__file__).parent.parent / "ai-image-service"))

from providers.base import GenerationRequest, ImageType
from providers.stable_diffusion_provider import StableDiffusionProvider

# ── 벤치마크 샘플 데이터 ──────────────────────────────────────────────────────
SAMPLE = GenerationRequest(
    product_id=0,
    title="Samsung Galaxy S24 Ultra",
    description="latest flagship smartphone with AI camera",
    category="스마트폰",
    price=1_500_000,
    image_type=ImageType.PRODUCT,
)


def fmt(ms_list: list[float]) -> str:
    if not ms_list:
        return "데이터 없음"
    avg = statistics.mean(ms_list)
    mn  = min(ms_list)
    mx  = max(ms_list)
    p95 = sorted(ms_list)[int(len(ms_list) * 0.95)] if len(ms_list) >= 2 else ms_list[-1]
    return f"avg={avg/1000:.1f}s  min={mn/1000:.1f}s  max={mx/1000:.1f}s  p95={p95/1000:.1f}s"


async def run_once(sd: StableDiffusionProvider, req: GenerationRequest) -> float:
    """단일 생성 실행 후 소요시간(ms) 반환"""
    start = time.time()
    try:
        result = await sd.generate(req)
        elapsed = (time.time() - start) * 1000
        return elapsed
    except Exception as e:
        elapsed = (time.time() - start) * 1000
        print(f"    ✗ 실패 ({elapsed/1000:.1f}s): {e}")
        return -1


async def bench_sequential(sd: StableDiffusionProvider, runs: int) -> tuple[list, list]:
    """상품 + 홍보 이미지를 순차적으로 생성"""
    product_times, promo_times = [], []
    for i in range(runs):
        print(f"  [{i+1}/{runs}] 순차 생성 중...")

        req_product = GenerationRequest(**{**SAMPLE.__dict__, "image_type": ImageType.PRODUCT})
        t = await run_once(sd, req_product)
        if t > 0:
            product_times.append(t)
            print(f"    상품 이미지: {t/1000:.1f}s")

        req_promo = GenerationRequest(**{**SAMPLE.__dict__, "image_type": ImageType.PROMO})
        t = await run_once(sd, req_promo)
        if t > 0:
            promo_times.append(t)
            print(f"    홍보 이미지: {t/1000:.1f}s")

    return product_times, promo_times


async def bench_parallel(sd: StableDiffusionProvider, runs: int) -> list[float]:
    """상품 + 홍보 이미지를 병렬로 생성 — 전체 소요시간 측정"""
    total_times = []
    for i in range(runs):
        print(f"  [{i+1}/{runs}] 병렬 생성 중...")
        start = time.time()

        req_product = GenerationRequest(**{**SAMPLE.__dict__, "image_type": ImageType.PRODUCT})
        req_promo   = GenerationRequest(**{**SAMPLE.__dict__, "image_type": ImageType.PROMO})

        results = await asyncio.gather(
            sd.generate(req_product),
            sd.generate(req_promo),
            return_exceptions=True,
        )
        elapsed = (time.time() - start) * 1000
        errors = [r for r in results if isinstance(r, Exception)]
        if errors:
            print(f"    ✗ 일부 실패: {errors}")
        else:
            total_times.append(elapsed)
            print(f"    병렬 완료: {elapsed/1000:.1f}s")

    return total_times


async def bench_controlnet(sd: StableDiffusionProvider, runs: int) -> list[float]:
    """ControlNet 이미지 생성 시간 측정 (1x1 더미 PNG 사용)"""
    # 최소한의 유효한 1×1 흰색 PNG
    dummy_png = (
        b"\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01"
        b"\x00\x00\x00\x01\x08\x02\x00\x00\x00\x90wS\xde\x00\x00"
        b"\x00\x0cIDATx\x9cc\xf8\x0f\x00\x00\x01\x01\x00\x05\x18"
        b"\xd8N\x00\x00\x00\x00IEND\xaeB`\x82"
    )
    times = []
    for i in range(runs):
        print(f"  [{i+1}/{runs}] ControlNet 생성 중...")
        req = GenerationRequest(
            **{**SAMPLE.__dict__,
               "image_type": ImageType.PRODUCT,
               "reference_image": dummy_png}
        )
        t = await run_once(sd, req)
        if t > 0:
            times.append(t)
            print(f"    ControlNet: {t/1000:.1f}s")

    return times


async def main(comfyui_url: str, runs: int, skip_controlnet: bool):
    sd = StableDiffusionProvider(comfyui_url, timeout=300)

    # ComfyUI 연결 확인
    print(f"\n[연결 확인] {comfyui_url}")
    if not await sd.is_available():
        print("✗ ComfyUI에 접속할 수 없습니다. URL을 확인하세요.")
        return
    print("✓ ComfyUI 연결 성공\n")

    # ── 1. 순차 생성 ──────────────────────────────────────────────────────────
    print("=" * 60)
    print(f"[1] 순차 생성 ({runs}회)")
    print("=" * 60)
    product_times, promo_times = await bench_sequential(sd, runs)

    # ── 2. 병렬 생성 ──────────────────────────────────────────────────────────
    print("\n" + "=" * 60)
    print(f"[2] 병렬 생성 (asyncio.gather, {runs}회)")
    print("=" * 60)
    parallel_times = await bench_parallel(sd, runs)

    # ── 3. ControlNet ─────────────────────────────────────────────────────────
    controlnet_times = []
    if not skip_controlnet:
        print("\n" + "=" * 60)
        print(f"[3] ControlNet 생성 ({runs}회)")
        print("=" * 60)
        controlnet_times = await bench_controlnet(sd, runs)

    # ── 결과 출력 ─────────────────────────────────────────────────────────────
    print("\n" + "=" * 60)
    print("결과 요약")
    print("=" * 60)
    print(f"상품 이미지  (SDXL 25스텝, 1024×1024) : {fmt(product_times)}")
    print(f"홍보 이미지  (LCM-LoRA 8스텝, 1280×720): {fmt(promo_times)}")

    if product_times and promo_times:
        seq_avg  = statistics.mean(product_times) + statistics.mean(promo_times)
        par_avg  = statistics.mean(parallel_times) if parallel_times else 0
        print(f"\n순차 합산 평균  : {seq_avg/1000:.1f}s")
        print(f"병렬 생성 평균  : {par_avg/1000:.1f}s")
        if par_avg > 0:
            saved = (seq_avg - par_avg) / seq_avg * 100
            print(f"병렬화 절감     : {saved:.1f}%")

    if controlnet_times:
        print(f"\nControlNet       (Canny 30스텝, 1024×1024): {fmt(controlnet_times)}")

    print("=" * 60)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="AI 이미지 생성 벤치마크")
    parser.add_argument("--url",  default="http://localhost:8188", help="ComfyUI URL")
    parser.add_argument("--runs", type=int, default=3,             help="반복 횟수 (기본 3)")
    parser.add_argument("--skip-controlnet", action="store_true",  help="ControlNet 테스트 건너뜀")
    args = parser.parse_args()

    asyncio.run(main(args.url, args.runs, args.skip_controlnet))
