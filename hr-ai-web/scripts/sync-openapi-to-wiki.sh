#!/usr/bin/env bash
set -euo pipefail

OPENAPI_FILE="contracts/hr-business-service.openapi.yaml"
TARGET_DIR="docs/实现计划/deliverables/iter-1"

if [[ ! -f "${OPENAPI_FILE}" ]]; then
  echo "OpenAPI 文件不存在: ${OPENAPI_FILE}" >&2
  exit 1
fi

mkdir -p "${TARGET_DIR}"
cp "${OPENAPI_FILE}" "${TARGET_DIR}/hr-business-service.openapi.yaml"

echo "已同步 OpenAPI 到 ${TARGET_DIR}"
echo "企业 Wiki 同步请在 CI 中注入 WIKI_TOKEN 后执行 API 发布步骤。"
