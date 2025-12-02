#!/bin/bash

# EKS 배포 삭제 스크립트
# 사용법: ./undeploy.sh

set -e

echo "🗑️  EKS 리소스 삭제 시작..."

# 1. Ingress 삭제
echo "Ingress 삭제 중..."
kubectl delete -f ingress/ || true

# 2. Services 삭제
echo "서비스 삭제 중..."
kubectl delete -f services/gateway/ || true
kubectl delete -f services/order/ || true
kubectl delete -f services/payment/ || true
kubectl delete -f services/member/ || true
kubectl delete -f services/product/ || true
kubectl delete -f services/discovery/ || true

# 3. 인프라 삭제
echo "인프라 삭제 중..."
kubectl delete -f infrastructure/zipkin/ || true
kubectl delete -f infrastructure/rabbitmq/ || true
kubectl delete -f infrastructure/mysql/ || true

# 4. ConfigMaps & Secrets 삭제
echo "ConfigMaps & Secrets 삭제 중..."
kubectl delete -f configmaps/ || true
kubectl delete -f secrets/ || true

# 5. Namespace 삭제 (선택사항)
read -p "Namespace도 삭제하시겠습니까? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]
then
    kubectl delete -f base/namespace.yaml
fi

echo "✅ EKS 리소스 삭제 완료!"