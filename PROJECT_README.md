# HR AI SaaS - Agent 核心系统

## 项目概述

HR 智能助手 SaaS 产品的多 Agent 协作系统,基于 Spring AI Alibaba 构建。

### 核心能力

- ✅ 多 Agent 协作 (HR政策、招聘、工单路由等)
- ✅ Agent 编排引擎 + 工作流引擎
- ✅ 记忆管理 (短期 + 长期)
- ✅ 工具调用能力 (Function Calling)
- ✅ RAG 知识检索
- ✅ 多模态支持 (文本 + 图片 + 文档)

### 技术栈

- **框架**: Spring Boot 3.2 + Spring AI Alibaba 1.0.0-M2
- **AI模型**: 通义千问 (qwen-plus, text-embedding-v2)
- **数据存储**: MySQL 8.0 + Redis + Qdrant (向量库)
- **消息队列**: RabbitMQ
- **工作流**: Spring State Machine

---

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+
- Qdrant 1.7+
- RabbitMQ 3.12+

### 2. 安装依赖服务

#### 使用 Docker Compose (推荐)

创建 `docker-compose.yml`:

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: your_password
      MYSQL_DATABASE: hr_ai_saas
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  qdrant:
    image: qdrant/qdrant:v1.7.0
    ports:
      - "6333:6333"
      - "6334:6334"
    volumes:
      - qdrant_data:/qdrant/storage

  rabbitmq:
    image: rabbitmq:3.12-management-alpine
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest

volumes:
  mysql_data:
  qdrant_data:
```

启动服务:
```bash
docker-compose up -d
```

### 3. 数据库初始化

```bash
# 连接 MySQL
mysql -u root -p

# 执行初始化脚本
source hr-ai-agent-core/src/main/resources/db/schema.sql
source hr-ai-agent-core/src/main/resources/db/init-data.sql
```

### 4. 配置文件

编辑 `hr-ai-agent-core/src/main/resources/application.yml`:

```yaml
# 数据库密码
spring:
  datasource:
    password: your_password

# 通义千问 API Key
spring:
  ai:
    dashscope:
      api-key: ${DASHSCOPE_API_KEY}
```

### 5. 启动应用

```bash
# 编译项目
mvn clean package -DskipTests

# 启动 Agent 核心服务
cd hr-ai-agent-core
mvn spring-boot:run
```

访问: http://localhost:8080/api/health

---

## 项目结构

```
hr-ai-saas/
├── hr-ai-common/                    # 公共模块
│   └── src/main/java/com/hrai/common/
│       ├── constant/                # 常量
│       ├── dto/                     # DTO
│       ├── exception/               # 异常
│       └── utils/                   # 工具类
│
└── hr-ai-agent-core/                # Agent 核心服务
    └── src/main/java/com/hrai/agent/
        ├── orchestrator/            # Agent 编排器
        ├── agents/                  # Agent 实现
        │   ├── base/                # Agent 基类
        │   └── impl/                # 具体 Agent
        ├── workflow/                # 工作流引擎
        ├── memory/                  # 记忆管理
        ├── tools/                   # 工具/Function
        ├── coordinator/             # 多 Agent 协调
        ├── rag/                     # RAG 检索
        ├── llm/                     # LLM 客户端
        ├── multimodal/              # 多模态处理
        ├── config/                  # 配置
        └── controller/              # API 接口
```

---

## 开发进度

### Phase 1: 项目骨架搭建 ✅ (已完成)

- [x] Maven 多模块项目结构
- [x] Spring Boot 基础配置
- [x] 数据库 Schema
- [x] Qdrant 向量库配置
- [x] 健康检查接口

### Phase 2: MVP - 单 Agent + RAG (进行中)

- [ ] RAG 服务实现
- [ ] Agent 基础框架
- [ ] HR Policy Agent
- [ ] 短期记忆管理
- [ ] SSE 聊天接口
- [ ] Function 工具实现

### Phase 3: 多 Agent 协作 (计划中)

- [ ] Router Agent
- [ ] 多专家 Agent
- [ ] 工作流引擎
- [ ] RabbitMQ 事件总线
- [ ] 长期记忆

### Phase 4: 高级能力 (计划中)

- [ ] ReAct 循环
- [ ] 复杂工具链
- [ ] 多模态支持
- [ ] Prompt 版本管理

---

## API 文档

### 健康检查

```bash
GET /api/health
```

响应:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "UP",
    "service": "hr-ai-agent-core",
    "timestamp": "2025-01-29T10:30:00"
  }
}
```

---

## 配置说明

### 通义千问 API Key

1. 访问 [阿里云百炼平台](https://bailian.console.aliyun.com/)
2. 创建应用并获取 API Key
3. 设置环境变量或修改 `application.yml`

```bash
export DASHSCOPE_API_KEY=sk-your-api-key
```

### Qdrant 向量库

默认配置:
- Host: localhost
- Port: 6334
- Collections: `knowledge_default`, `user_memory`

### Redis 会话存储

短期记忆存储格式:
```
session:{tenant_id}:{session_id}
```

---

## 监控与运维

### 健康检查

```bash
curl http://localhost:8080/api/actuator/health
```

### Prometheus 指标

```bash
curl http://localhost:8080/api/actuator/prometheus
```

### 日志级别调整

编辑 `application.yml`:
```yaml
logging:
  level:
    com.hrai: DEBUG
```

---

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 许可证

Copyright © 2025 HR AI Team

---

## 联系方式

- 项目负责人: HR AI Team
- 文档: 参见 `/SPECS` 目录
