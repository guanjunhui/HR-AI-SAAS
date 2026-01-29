#!/bin/bash

# HR AI SaaS - 停止开发环境脚本

set -e

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
echo "=========================================="
