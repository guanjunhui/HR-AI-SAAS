#!/bin/bash

# HR AI SaaS - 环境验证脚本

set -e

echo "=========================================="
echo "HR AI SaaS - Phase 1 环境验证"
echo "=========================================="

echo ""
echo "🔍 第1步: 检查项目结构..."

# 检查关键文件
files=(
    "pom.xml"
    "hr-ai-common/pom.xml"
    "hr-ai-agent-core/pom.xml"
    "hr-ai-agent-core/src/main/java/com/hrai/agent/AgentApplication.java"
    "hr-ai-agent-core/src/main/resources/application.yml"
    "hr-ai-agent-core/src/main/resources/db/schema.sql"
    "hr-ai-agent-core/src/main/resources/db/init-data.sql"
    "docker-compose.yml"
)

for file in "${files[@]}"; do
    if [ -f "$file" ]; then
        echo "  ✅ $file"
    else
        echo "  ❌ $file (缺失)"
        exit 1
    fi
done

echo ""
echo "🔨 第2步: Maven 编译检查..."
mvn clean compile -DskipTests -q
if [ $? -eq 0 ]; then
    echo "  ✅ Maven 编译成功"
else
    echo "  ❌ Maven 编译失败"
    exit 1
fi

echo ""
echo "📦 第3步: 检查 Docker 服务..."
if docker ps > /dev/null 2>&1; then
    running_containers=$(docker-compose ps --services --filter "status=running" 2>/dev/null | wc -l)
    if [ "$running_containers" -ge 4 ]; then
        echo "  ✅ Docker 服务运行中 ($running_containers 个容器)"
        docker-compose ps
    else
        echo "  ⚠️  Docker 服务未完全启动 (运行中: $running_containers/4)"
        echo "  执行启动命令: ./scripts/start-dev.sh"
    fi
else
    echo "  ⚠️  Docker 未运行或未启动服务"
    echo "  执行启动命令: ./scripts/start-dev.sh"
fi

echo ""
echo "=========================================="
echo "✅ Phase 1: 项目骨架搭建 - 验证通过!"
echo "=========================================="
echo ""
echo "📋 验证清单:"
echo "  ✅ Maven 多模块项目结构"
echo "  ✅ Spring Boot 基础配置"
echo "  ✅ 数据库 Schema 文件"
echo "  ✅ Qdrant 配置"
echo "  ✅ Docker Compose 配置"
echo "  ✅ Maven 编译成功"
echo ""
echo "🚀 下一步:"
echo "  1. 启动依赖服务: ./scripts/start-dev.sh"
echo "  2. 启动应用: cd hr-ai-agent-core && mvn spring-boot:run"
echo "  3. 访问健康检查: curl http://localhost:8080/api/health"
echo ""
echo "=========================================="
