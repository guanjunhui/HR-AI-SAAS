#!/bin/bash

# HR AI SaaS - 停止开发环境脚本
# 技术栈: MySQL + Redis + Nacos 3.x + Kafka + Milvus

set -e

# 切换到项目根目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.."

echo "=========================================="
echo "HR AI SaaS - 停止开发环境"
echo "=========================================="

echo ""
echo "🛑 停止所有服务..."
docker-compose down

echo ""
echo "✅ 所有服务已停止"
echo ""
echo "💡 提示:"
echo "  - 如需清理数据,执行: docker-compose down -v"
echo "  - 如需重启服务,执行: ./scripts/start-dev.sh"
echo ""
echo "  数据卷列表 (docker-compose down -v 会删除):"
echo "  ├─ mysql_data     - MySQL 数据"
echo "  ├─ redis_data     - Redis 数据"
echo "  ├─ nacos_data     - Nacos 配置数据"
echo "  ├─ kafka_data     - Kafka 消息数据"
echo "  ├─ etcd_data      - Etcd 元数据"
echo "  ├─ minio_data     - MinIO 对象存储"
echo "  └─ milvus_data    - Milvus 向量数据"
echo "=========================================="
