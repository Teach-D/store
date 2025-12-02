# 🚀 EKS 배포 가이드

E-commerce MSA 프로젝트를 AWS EKS에 배포하는 완전한 가이드입니다.

---

## 📋 목차

1. [사전 준비](#사전-준비)
2. [EKS 클러스터 생성](#eks-클러스터-생성)
3. [Docker 이미지 빌드 & Docker Hub 푸시](#docker-이미지-빌드--docker-hub-푸시)
4. [Kubernetes 리소스 배포](#kubernetes-리소스-배포)
5. [배포 확인](#배포-확인)
6. [트러블슈팅](#트러블슈팅)

---

## 🛠️ 사전 준비

### 필수 도구 설치

```bash
# AWS CLI 설치 확인
aws --version

# kubectl 설치 확인
kubectl version --client

# eksctl 설치 확인
eksctl version

# Docker 설치 확인
docker --version
```

### AWS 자격 증명 설정

```bash
aws configure
```

- **AWS Access Key ID**: `your-access-key`
- **AWS Secret Access Key**: `your-secret-key`
- **Default region**: `ap-northeast-2` (서울)
- **Default output format**: `json`

---

## 🌐 EKS 클러스터 생성

### 방법 1: eksctl 사용 (권장)

```bash
# EKS 클러스터 생성
eksctl create cluster \
  --name ecommerce-cluster \
  --region ap-northeast-2 \
  --nodegroup-name ecommerce-nodes \
  --node-type t3.medium \
  --nodes 3 \
  --nodes-min 2 \
  --nodes-max 5 \
  --managed
```

**예상 시간**: 15-20분

### 방법 2: AWS 콘솔 사용

1. AWS Console → EKS → Create cluster
2. 클러스터 이름: `ecommerce-cluster`
3. Kubernetes 버전: `1.28` (최신 stable)
4. Node Group 생성:
   - 이름: `ecommerce-nodes`
   - 인스턴스 타입: `t3.medium`
   - 노드 수: 3

### kubeconfig 설정

```bash
aws eks update-kubeconfig --region ap-northeast-2 --name ecommerce-cluster
```

### 확인

```bash
kubectl get nodes
```

---

## 🐳 Docker 이미지 빌드 & Docker Hub 푸시

### 1. Docker Hub 계정 준비

Docker Hub 계정이 없다면 https://hub.docker.com 에서 가입하세요.

### 2. 이미지 빌드 & 푸시

```bash
cd k8s

# 스크립트 실행 권한 부여
chmod +x build-push.sh

# 빌드 & 푸시 실행
./build-push.sh <DOCKERHUB_USERNAME>

# 예시
./build-push.sh myusername
```

스크립트 실행 시 Docker Hub 로그인이 필요합니다.

### 3. Deployment YAML 수정

각 서비스의 `deployment.yaml`에서 이미지 경로 수정:

```yaml
# 예시: k8s/services/order/deployment.yaml
spec:
  containers:
  - name: order-service
    image: myusername/order-service:latest
```

**수정 대상 파일**:
- `k8s/services/order/deployment.yaml`
- `k8s/services/payment/deployment.yaml`
- `k8s/services/member/deployment.yaml`
- `k8s/services/product/deployment.yaml`
- `k8s/services/discovery/deployment.yaml`
- `k8s/services/gateway/deployment.yaml`

**주의**: 현재 파일들은 `<YOUR_DOCKERHUB_USERNAME>`으로 설정되어 있습니다. 실제 사용자명으로 변경하세요.

---

## ⚙️ Kubernetes 리소스 배포

### 방법 1: 스크립트 사용 (권장)

```bash
cd k8s

# 실행 권한 부여
chmod +x deploy.sh

# 배포 실행
./deploy.sh
```

### 방법 2: 수동 배포

```bash
cd k8s

# 1. Namespace 생성
kubectl apply -f base/namespace.yaml

# 2. Secrets & ConfigMaps
kubectl apply -f secrets/
kubectl apply -f configmaps/

# 3. 인프라 (MySQL, RabbitMQ, Zipkin)
kubectl apply -f infrastructure/mysql/
kubectl apply -f infrastructure/rabbitmq/
kubectl apply -f infrastructure/zipkin/

# MySQL, RabbitMQ, Zipkin 준비 대기
kubectl wait --for=condition=ready pod -l app=mysql -n ecommerce --timeout=300s
kubectl wait --for=condition=ready pod -l app=rabbitmq -n ecommerce --timeout=300s
kubectl wait --for=condition=ready pod -l app=zipkin -n ecommerce --timeout=180s

# 4. Discovery Service
kubectl apply -f services/discovery/

# Discovery Service 준비 대기
kubectl wait --for=condition=ready pod -l app=discovery-service -n ecommerce --timeout=180s

# 5. 비즈니스 서비스
kubectl apply -f services/order/
kubectl apply -f services/payment/
kubectl apply -f services/member/
kubectl apply -f services/product/

# 6. Gateway
kubectl apply -f services/gateway/

# 7. Ingress (선택사항)
kubectl apply -f ingress/
```

---

## ✅ 배포 확인

### 전체 리소스 확인

```bash
kubectl get all -n ecommerce
```

### Pod 상태 확인

```bash
kubectl get pods -n ecommerce
```

**정상 상태**: 모든 Pod가 `Running` 상태

### Service 확인

```bash
kubectl get svc -n ecommerce
```

### Gateway 외부 URL 확인

```bash
# LoadBalancer URL 확인
kubectl get svc gateway-service -n ecommerce

# 또는
kubectl get svc gateway-service -n ecommerce -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'
```

### 애플리케이션 접속

```bash
# Gateway URL
http://<GATEWAY-LOADBALANCER-URL>

# Eureka Dashboard
http://<GATEWAY-LOADBALANCER-URL>/eureka

# Zipkin UI (Port Forward)
kubectl port-forward svc/zipkin 9411:9411 -n ecommerce
# http://localhost:9411
```

### 로그 확인

```bash
# Order Service 로그
kubectl logs -f deployment/order-service -n ecommerce

# Payment Service 로그
kubectl logs -f deployment/payment-service -n ecommerce

# 모든 로그 스트리밍
kubectl logs -f -l app=order-service -n ecommerce
```

---

## 🔧 트러블슈팅

### 1. Pod가 Pending 상태

```bash
kubectl describe pod <POD_NAME> -n ecommerce
```

**원인**:
- 노드 리소스 부족
- PVC가 Bound 안 됨

**해결**:
```bash
# 노드 확인
kubectl get nodes

# PVC 확인
kubectl get pvc -n ecommerce

# 노드 리소스 확인
kubectl top nodes
```

### 2. Pod가 CrashLoopBackOff

```bash
kubectl logs <POD_NAME> -n ecommerce --previous
```

**원인**:
- 환경 변수 설정 오류
- DB 연결 실패
- 메모리 부족

**해결**:
```bash
# ConfigMap 확인
kubectl get configmap application-config -n ecommerce -o yaml

# Secret 확인
kubectl get secret mysql-secret -n ecommerce -o yaml

# 리소스 제한 확인
kubectl describe pod <POD_NAME> -n ecommerce
```

### 3. MySQL 연결 실패

```bash
# MySQL Pod 확인
kubectl get pod -l app=mysql -n ecommerce

# MySQL 로그 확인
kubectl logs -f statefulset/mysql -n ecommerce

# MySQL 접속 테스트
kubectl exec -it mysql-0 -n ecommerce -- mysql -u root -p
```

### 4. RabbitMQ 연결 실패

```bash
# RabbitMQ Pod 확인
kubectl get pod -l app=rabbitmq -n ecommerce

# RabbitMQ 로그 확인
kubectl logs -f statefulset/rabbitmq -n ecommerce

# RabbitMQ Management UI 접속 (Port Forward)
kubectl port-forward svc/rabbitmq 15672:15672 -n ecommerce
# http://localhost:15672 (guest/guest)
```

### 5. Eureka 등록 실패

```bash
# Discovery Service 확인
kubectl logs -f deployment/discovery-service -n ecommerce

# 각 서비스의 Eureka 연결 로그 확인
kubectl logs deployment/order-service -n ecommerce | grep -i eureka
```

### 6. Zipkin 연결 실패

```bash
# Zipkin Pod 확인
kubectl get pod -l app=zipkin -n ecommerce

# Zipkin 로그 확인
kubectl logs -f deployment/zipkin -n ecommerce

# Zipkin UI 접속 (Port Forward)
kubectl port-forward svc/zipkin 9411:9411 -n ecommerce
# http://localhost:9411
```

---

## 🗑️ 배포 삭제

### 스크립트 사용

```bash
cd k8s
chmod +x undeploy.sh
./undeploy.sh
```

### 수동 삭제

```bash
# 모든 리소스 삭제
kubectl delete namespace ecommerce

# EKS 클러스터 삭제
eksctl delete cluster --name ecommerce-cluster --region ap-northeast-2
```

---

## 📊 리소스 사용량

| 서비스 | CPU Request | Memory Request | Replicas |
|--------|-------------|----------------|----------|
| Order | 250m | 512Mi | 2 |
| Payment | 250m | 512Mi | 2 |
| Member | 250m | 512Mi | 2 |
| Product | 250m | 512Mi | 2 |
| Discovery | 100m | 256Mi | 1 |
| Gateway | 250m | 512Mi | 2 |
| MySQL | 250m | 512Mi | 1 |
| RabbitMQ | 100m | 256Mi | 1 |
| Zipkin | 100m | 256Mi | 1 |
| **합계** | **1.75 CPU** | **4.5 GiB** | **14 Pods** |

**권장 노드 타입**: `t3.medium` (2 vCPU, 4 GiB) × 3대

---

## 🔐 보안 고려사항

### 1. Secrets 관리

**현재 방식**: Kubernetes Secrets (Base64 인코딩)

**프로덕션 권장**: AWS Secrets Manager 연동

```yaml
# ExternalSecrets 사용 예시
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: mysql-secret
spec:
  secretStoreRef:
    name: aws-secrets-manager
  target:
    name: mysql-secret
  data:
  - secretKey: password
    remoteRef:
      key: /ecommerce/mysql/password
```

### 2. Network Policy

```yaml
# 예시: Order Service는 MySQL과 RabbitMQ만 접근 가능
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: order-service-network-policy
spec:
  podSelector:
    matchLabels:
      app: order-service
  policyTypes:
  - Egress
  egress:
  - to:
    - podSelector:
        matchLabels:
          app: mysql
  - to:
    - podSelector:
        matchLabels:
          app: rabbitmq
```

### 3. RBAC

```bash
# Service Account 생성
kubectl create serviceaccount ecommerce-sa -n ecommerce

# Role Binding
kubectl create rolebinding ecommerce-rb \
  --clusterrole=view \
  --serviceaccount=ecommerce:ecommerce-sa \
  -n ecommerce
```

---

## 📈 모니터링 & 로깅

### Prometheus & Grafana 설치 (선택사항)

```bash
# Helm 설치
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Prometheus 설치
helm install prometheus prometheus-community/kube-prometheus-stack -n monitoring --create-namespace
```

### CloudWatch Container Insights

```bash
# CloudWatch Agent 설치
kubectl apply -f https://raw.githubusercontent.com/aws-samples/amazon-cloudwatch-container-insights/latest/k8s-deployment-manifest-templates/deployment-mode/daemonset/container-insights-monitoring/quickstart/cwagent-fluentd-quickstart.yaml
```

---

## 🚀 CI/CD 파이프라인 (향후 추가)

### GitHub Actions 예시

```yaml
name: Deploy to EKS

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2

    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v1
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: ap-northeast-2

    - name: Login to Docker Hub
      uses: docker/login-action@v2
      with:
        username: ${{ secrets.DOCKERHUB_USERNAME }}
        password: ${{ secrets.DOCKERHUB_TOKEN }}

    - name: Build and Push to Docker Hub
      run: |
        ./k8s/build-push.sh ${{ secrets.DOCKERHUB_USERNAME }}

    - name: Deploy to EKS
      run: |
        aws eks update-kubeconfig --name ecommerce-cluster --region ap-northeast-2
        ./k8s/deploy.sh
```

---

## 📞 문의

배포 관련 문의사항은 GitHub Issues에 남겨주세요.