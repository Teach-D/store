#!/bin/bash
# ================================================================
# RunPod - ComfyUI 최초 설치 스크립트 (처음 한 번만 실행)
# RunPod Pod 터미널에서: bash /workspace/runpod_setup.sh
# ================================================================
set -e

COMFYUI_DIR="/workspace/ComfyUI"
LOG_FILE="/workspace/setup.log"

log() { echo "[$(date '+%H:%M:%S')] $1" | tee -a "$LOG_FILE"; }
hr()  { echo "================================================================" | tee -a "$LOG_FILE"; }

hr
log "ComfyUI 설치 시작"
hr

# ── 1. ComfyUI 설치 ────────────────────────────────────────────
if [ -d "$COMFYUI_DIR" ]; then
    log "ComfyUI 이미 설치됨 - 업데이트만 진행"
    cd "$COMFYUI_DIR" && git pull
else
    log "ComfyUI 클론 중..."
    cd /workspace
    git clone https://github.com/comfyanonymous/ComfyUI.git
fi

cd "$COMFYUI_DIR"
log "Python 패키지 설치 중..."
pip install -r requirements.txt -q

# ── 2. ComfyUI Manager 설치 (모델 브라우저 UI) ──────────────────
MANAGER_DIR="$COMFYUI_DIR/custom_nodes/ComfyUI-Manager"
if [ ! -d "$MANAGER_DIR" ]; then
    log "ComfyUI Manager 설치 중..."
    git clone https://github.com/ltdrdata/ComfyUI-Manager.git "$MANAGER_DIR"
else
    log "ComfyUI Manager 이미 설치됨"
fi

# ── 3. 디렉토리 구성 ────────────────────────────────────────────
mkdir -p "$COMFYUI_DIR/models/checkpoints"
mkdir -p "$COMFYUI_DIR/models/vae"
mkdir -p "$COMFYUI_DIR/models/loras"
mkdir -p "$COMFYUI_DIR/models/controlnet"

# ── 4. SDXL Base 모델 (~6.5GB) ──────────────────────────────────
SDXL_PATH="$COMFYUI_DIR/models/checkpoints/sd_xl_base_1.0.safetensors"
if [ ! -f "$SDXL_PATH" ]; then
    log "SDXL Base 모델 다운로드 중 (~6.5GB, 수 분 소요)..."
    wget -q --show-progress -c \
        "https://huggingface.co/stabilityai/stable-diffusion-xl-base-1.0/resolve/main/sd_xl_base_1.0.safetensors" \
        -O "$SDXL_PATH"
    log "SDXL Base 다운로드 완료"
else
    log "SDXL Base 모델 이미 존재"
fi

# ── 5. SDXL VAE (~335MB) ────────────────────────────────────────
VAE_PATH="$COMFYUI_DIR/models/vae/sdxl_vae.safetensors"
if [ ! -f "$VAE_PATH" ]; then
    log "SDXL VAE 다운로드 중..."
    wget -q --show-progress -c \
        "https://huggingface.co/stabilityai/sdxl-vae/resolve/main/sdxl_vae.safetensors" \
        -O "$VAE_PATH"
    log "SDXL VAE 다운로드 완료"
else
    log "SDXL VAE 이미 존재"
fi

# ── 6. LoRA - LCM-LoRA (~200MB) ─────────────────────────────────
# LCM-LoRA: 4~8스텝으로 고품질 이미지 생성 (기본 25스텝 대비 3~5배 빠름)
LORA_PATH="$COMFYUI_DIR/models/loras/lcm-lora-sdxl.safetensors"
if [ ! -f "$LORA_PATH" ]; then
    log "LoRA (LCM-LoRA-SDXL) 다운로드 중..."
    wget -q --show-progress -c \
        "https://huggingface.co/latent-consistency/lcm-lora-sdxl/resolve/main/pytorch_lora_weights.safetensors" \
        -O "$LORA_PATH"
    log "LoRA 다운로드 완료"
else
    log "LoRA 이미 존재"
fi

# ── 7. ControlNet - Canny (~1.4GB) ──────────────────────────────
# SDXL ControlNet: 윤곽선(Canny)으로 이미지 구도 제어
CONTROLNET_PATH="$COMFYUI_DIR/models/controlnet/control-lora-canny-rank256.safetensors"
if [ ! -f "$CONTROLNET_PATH" ]; then
    log "ControlNet (Canny) 다운로드 중 (~1.4GB)..."
    wget -q --show-progress -c \
        "https://huggingface.co/stabilityai/control-lora/resolve/main/control-LoRAs-rank256/control-lora-canny-rank256.safetensors" \
        -O "$CONTROLNET_PATH"
    log "ControlNet 다운로드 완료"
else
    log "ControlNet 이미 존재"
fi

# ── 8. 시작 스크립트 생성 ────────────────────────────────────────
cat > /workspace/start_comfyui.sh << 'EOF'
#!/bin/bash
cd /workspace/ComfyUI
nohup python main.py \
    --listen 0.0.0.0 \
    --port 8188 \
    --enable-cors-header \
    > /workspace/comfyui.log 2>&1 &
echo $! > /workspace/comfyui.pid
echo "ComfyUI 시작됨 (PID: $(cat /workspace/comfyui.pid))"
echo "로그 확인: tail -f /workspace/comfyui.log"
EOF
chmod +x /workspace/start_comfyui.sh

# ── 9. ComfyUI 실행 ─────────────────────────────────────────────
log "ComfyUI 시작 중..."
bash /workspace/start_comfyui.sh

sleep 5

# ── 10. 헬스체크 ────────────────────────────────────────────────
log "ComfyUI 헬스체크 중..."
for i in $(seq 1 12); do
    if curl -s http://localhost:8188/system_stats > /dev/null 2>&1; then
        log "ComfyUI 정상 실행 확인"
        break
    fi
    if [ $i -eq 12 ]; then
        log "경고: ComfyUI 응답 없음. 로그 확인: tail -f /workspace/comfyui.log"
    fi
    sleep 5
done

hr
log "설치 완료!"
hr
echo ""
echo "  설치된 모델:"
echo "  ✓ SDXL Base 1.0     (models/checkpoints/)"
echo "  ✓ SDXL VAE          (models/vae/)"
echo "  ✓ LCM-LoRA-SDXL     (models/loras/)       ← LoRA"
echo "  ✓ ControlNet Canny  (models/controlnet/)  ← ControlNet"
echo ""
echo "  다음 단계:"
echo "  RunPod 대시보드 → 해당 Pod → Connect 버튼"
echo "  → 'HTTP Service' 탭에서 포트 8188 URL 복사"
echo "  → ai-image-service/.env 의 COMFYUI_URL 에 붙여넣기"
echo ""
echo "  ComfyUI 재시작 필요 시: bash /workspace/start_comfyui.sh"
echo "  로그 확인: tail -f /workspace/comfyui.log"
