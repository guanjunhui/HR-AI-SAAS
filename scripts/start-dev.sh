#!/bin/bash

# HR AI SaaS - 开发环境启动脚本
# 技术栈: MySQL + Redis + Nacos 3.x + Kafka + Milvus

set -e

echo "=========================================="
echo "HR AI SaaS - 开发环境启动"
echo "技术栈迁移版本 (Nacos 3.x + Kafka + Milvus)"
echo "=========================================="

# 检查 Docker 是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker 未运行,请先启动 Docker"
    exit 1
fi

# 切换到项目根目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

echo ""
echo "📦 第1步: 启动基础服务 (MySQL, Redis)..."
docker-compose up -d mysql redis

echo ""
echo "⏳ 等待 MySQL 就绪..."
until docker exec hr-ai-mysql mysqladmin ping -h localhost --silent 2>/dev/null; do
    echo "  等待 MySQL..."
    sleep 2
done
echo "✅ MySQL 已就绪"

echo ""
echo "📦 第2步: 启动 Nacos 配置中心..."
docker-compose up -d nacos

echo ""
echo "⏳ 等待 Nacos 就绪..."
NACOS_RETRY=0
until curl -sf http://localhost:8848/nacos/v1/console/health/readiness > /dev/null 2>&1 || [ $NACOS_RETRY -ge 30 ]; do
    echo "  等待 Nacos... ($NACOS_RETRY/30)"
    sleep 3
    NACOS_RETRY=$((NACOS_RETRY + 1))
done
if [ $NACOS_RETRY -ge 30 ]; then
    echo "⚠️ Nacos 启动超时，请检查日志: docker logs hr-ai-nacos"
else
    echo "✅ Nacos 已就绪"
fi

echo ""
echo "📦 第3步: 启动 Kafka 消息队列..."
docker-compose up -d kafka

echo ""
echo "⏳ 等待 Kafka 就绪..."
KAFKA_RETRY=0
until docker exec hr-ai-kafka /opt/kafka/bin/kafka-broker-api-versions.sh --bootstrap-server localhost:9092 > /dev/null 2>&1 || [ $KAFKA_RETRY -ge 20 ]; do
    echo "  等待 Kafka... ($KAFKA_RETRY/20)"
    sleep 3
    KAFKA_RETRY=$((KAFKA_RETRY + 1))
done
if [ $KAFKA_RETRY -ge 20 ]; then
    echo "⚠️ Kafka 启动超时，请检查日志: docker logs hr-ai-kafka"
else
    echo "✅ Kafka 已就绪"
fi

# 启动 Kafka UI
docker-compose up -d kafka-ui

echo ""
echo "📦 第4步: 启动 Milvus 向量数据库..."
docker-compose up -d etcd minio

echo ""
echo "⏳ 等待 Etcd 和 MinIO 就绪..."
sleep 5

docker-compose up -d milvus

echo ""
echo "⏳ 等待 Milvus 就绪..."
MILVUS_RETRY=0
until curl -sf http://localhost:9091/healthz > /dev/null 2>&1 || [ $MILVUS_RETRY -ge 30 ]; do
    echo "  等待 Milvus... ($MILVUS_RETRY/30)"
    sleep 3
    MILVUS_RETRY=$((MILVUS_RETRY + 1))
done
if [ $MILVUS_RETRY -ge 30 ]; then
    echo "⚠️ Milvus 启动超时，请检查日志: docker logs hr-ai-milvus"
else
    echo "✅ Milvus 已就绪"
fi

# 启动 Attu (Milvus UI)
docker-compose up -d attu

echo ""
echo "🔍 检查所有服务状态..."
docker-compose ps

echo ""
echo "=========================================="
echo "✅ 开发环境启动完成!"
echo "=========================================="
echo ""
echo "📊 服务访问地址:"
echo ""
echo "  基础服务:"
echo "  ├─ MySQL:       localhost:3306 (root/hr_ai_2025)"
echo "  └─ Redis:       localhost:6379"
echo ""
echo "  配置中心:"
echo "  └─ Nacos:       http://localhost:8848/nacos (nacos/nacos)"
echo ""
echo "  消息队列:"
echo "  ├─ Kafka:       localhost:9092"
echo "  └─ Kafka UI:    http://localhost:9080"
echo ""
echo "  向量数据库:"
echo "  ├─ Milvus:      localhost:19530"
echo "  ├─ MinIO:       http://localhost:9001 (minioadmin/minioadmin)"
echo "  └─ Attu UI:     http://localhost:8000"
echo ""
echo "=========================================="
echo "启动 Spring Boot 应用:"
echo "  cd hr-ai-agent-core && mvn spring-boot:run"
echo "=========================================="
