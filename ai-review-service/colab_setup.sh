#!/bin/bash
# =========================================================
# Google Colab T4 GPU — Ollama + ngrok 셋업 스크립트
# Colab 셀에서 순서대로 실행하세요
# =========================================================

# [셀 1] Ollama 설치 및 실행
curl -fsSL https://ollama.com/install.sh | sh
ollama serve &
sleep 5

# [셀 2] 모델 다운로드 (qwen2.5 7B 4-bit, ~4.7GB, T4 16GB VRAM에서 안정 동작)
ollama pull qwen2.5:7b-instruct

# [셀 3] ngrok 설치 (무료 계정: https://dashboard.ngrok.com 에서 토큰 발급)
pip install pyngrok -q

# [셀 4] 아래 Python 코드를 Colab 셀에 붙여넣기
: '
from pyngrok import ngrok

ngrok.set_auth_token("YOUR_NGROK_TOKEN")  # 토큰 교체
tunnel = ngrok.connect(11434)
print(f"OLLAMA_URL={tunnel.public_url}")
# 출력된 URL을 .env의 OLLAMA_URL 값으로 설정
'

# [참고] 모델 변경 원할 경우
# ollama pull llama3.1:8b-instruct-q4_K_M   # 한국어 성능 비슷, 약간 더 빠름
# ollama pull gemma2:9b-instruct-q4_K_M     # 구글 모델
