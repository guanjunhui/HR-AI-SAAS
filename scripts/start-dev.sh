#!/bin/bash

# HR AI SaaS - 开发环境启动脚本

set -e

echo "=========================================="
echo "HR AI SaaS - 开发环境启动"
echo "=========================================="

# 检查 Docker 是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker 未运行,请先启动 Docker"
    exit 1
fi

echo ""
echo "📦 第1步: 启动依赖服务 (MySQL, Redis, Qdrant, RabbitMQ)..."
docker-compose up -d

echo ""
echo "⏳ 等待服务就绪..."
sleep 10

# 检查服务状态
echo ""
echo "🔍 检查服务状态..."
docker-compose ps

echo ""
echo "✅ 依赖服务启动完成!"
echo ""
echo "📊 服务访问地址:"
echo "  - MySQL:       localhost:3306 (用户: root, 密码: hr_ai_2025)"
echo "  - Redis:       localhost:6379"
echo "  - Qdrant UI:   http://localhost:6333/dashboard"
echo "  - RabbitMQ UI: http://localhost:15672 (用户: guest, 密码: guest)"
echo ""
echo "=========================================="
echo "现在可以启动 Spring Boot 应用了!"
echo "执行: cd hr-ai-agent-core && mvn spring-boot:run"
echo "=========================================="
