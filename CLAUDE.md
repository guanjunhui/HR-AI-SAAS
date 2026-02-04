# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

HR AI SaaS - 基于 Spring AI Alibaba 的多 Agent 协作系统，用于构建智能 HR 助手 SaaS 产品。

**技术栈核心**：
- Java 17 + Spring Boot 3.3.6 + Spring Cloud 2023.0.3 + Spring Cloud Alibaba 2023.0.3.2
- Spring AI Alibaba 1.1.2.0（通义千问 + Agent Framework + Graph）
- 后端：Spring Cloud Gateway + Spring Security + MyBatis Plus + Druid + Redisson + Sentinel 1.8.8
- 存储：MySQL 8.0 + Redis + Milvus 2.5.4（向量库）+ Kafka（消息队列）
- 服务治理：Nacos 3.1（配置中心 + MCP 注册发现 + A2A + Sentinel 规则存储）
- 前端：React 19 + Ant Design 5 + Vite 7 + TypeScript 5
- Maven 多模块架构（hr-ai-common, hr-ai-agent-core, hr-gateway, hr-org-service）

## 开发环境设置

### 启动依赖服务
```bash
# 启动 Docker 依赖（MySQL, Redis, Nacos, Kafka, Milvus）
./scripts/start-dev.sh

# 停止服务
./scripts/stop-dev.sh
```

### 构建和运行

**后端服务**：
```bash
# 根目录编译整个项目
mvn clean package -DskipTests

# 启动各个服务（按依赖顺序）
# 1. 启动组织鉴权服务（端口 8081）
cd hr-org-service
mvn spring-boot:run

# 2. 启动 Agent 核心服务（端口 8080）
cd hr-ai-agent-core
mvn spring-boot:run

# 3. 启动 API 网关（端口 9000）
cd hr-gateway
mvn spring-boot:run

# 热重载开发模式（任一服务）
mvn spring-boot:run -Dspring-boot.run.fork=false
```

**前端应用**：
```bash
cd hr-ai-web

# 安装依赖（首次或依赖变更时）
npm install

# 启动开发服务器（端口 5173）
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview

# 代码检查
npm run lint
```

### 运行测试
```bash
# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=类名

# 运行单个测试方法
mvn test -Dtest=类名#方法名
```

### 数据库操作
```bash
# 连接 MySQL
docker exec -it hr-ai-mysql mysql -uroot -phr_ai_2025

# 在 MySQL 中
USE hr_ai_saas;
SHOW TABLES;

# 初始化数据库（如需要）
mysql -uroot -phr_ai_2025 < hr-ai-agent-core/src/main/resources/db/schema.sql
mysql -uroot -phr_ai_2025 < hr-ai-agent-core/src/main/resources/db/init-data.sql
```

## 架构设计核心

### 多模块结构
- **hr-ai-common**：公共组件（常量、DTO、异常、工具类、TenantContext）
- **hr-ai-agent-core**：Agent 核心服务（编排器、工作流、RAG、记忆管理、MCP、A2A）- 端口 8080
- **hr-gateway**：API 网关（Spring Cloud Gateway，认证鉴权、路由转发、限流熔断）- 端口 9000
- **hr-org-service**：组织鉴权服务（用户管理、角色权限、组织架构、审计日志、JWT）- 端口 8081
- **hr-ai-web**：前端 Web 应用（React 19 + Ant Design + Vite + TypeScript）- 开发端口 5173

### Agent 编排架构
```
用户请求 → Router Agent (工单路由)
         ↓
    Orchestrator (编排器)
         ↓
  ┌──────┴──────┬──────────┐
HR Policy    Recruiting   其他专家
Agent         Agent        Agents
  └──────┬──────┴──────────┘
         ↓
    统一响应流式输出

MCP 注册发现 (Nacos 3.x):
├─ Agent 工具动态注册
├─ 多服务 Agent 协调
└─ 外部 MCP Server 接入

A2A 协议:
└─ Agent 间任务委派 (Kafka)
```

### 关键设计模式
1. **Agent 定义**：配置化（agent-config.yml），支持动态加载
2. **工具调用**：Function Calling + MCP 动态发现
3. **记忆管理**：
   - 短期：Redis（10 轮对话，1 小时 TTL）
   - 长期：Milvus 向量化存储 + MySQL
4. **RAG 检索**：Milvus（768 维向量）+ Top-5 + 0.6 相似度阈值
5. **多租户**：TenantContext + TenantInterceptor（X-Tenant-Id, X-User-Id, X-Session-Id, X-Plan-Type）
6. **消息队列**：Kafka（agent-task, workflow-event, conversation-history, a2a-task-delegation）

### 数据库 Schema 重点
- **agents**：Agent 定义表（type, config JSON）
- **workflows**：工作流定义（状态机配置）
- **conversation_history**：长期记忆
- **knowledge_docs/chunks**：知识库（向量化分块）
- **function_invocations**：Function 调用日志（审计 + 调试）

### Milvus Collection Schema
- **hr_knowledge**：知识库向量（tenant_id, doc_id, chunk_id, content, embedding）
- **user_memory**：用户记忆向量（tenant_id, user_id, session_id, summary, embedding）

## 配置文件关键点

### bootstrap.yml (Nacos 配置)
```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
      discovery:
        server-addr: localhost:8848
```

### application.yml
- **数据库密码**：`hr_ai_2025`（Docker 默认）
- **API 基础路径**：`/api`（服务端口 8080）
- **AI 配置**：
  - API Key：`DASHSCOPE_API_KEY` 环境变量
  - 默认模型：`qwen-plus`（可选 qwen-turbo, qwen-max）
  - 嵌入模型：`text-embedding-v2`（768 维）
- **Milvus 集合**：
  - `hr_knowledge`：知识库向量
  - `user_memory`：用户记忆向量
- **Kafka Topics**：
  - `agent-task`：Agent 任务分发
  - `workflow-event`：工作流事件
  - `conversation-history`：对话历史
  - `a2a-task-delegation`：A2A 任务委派

### agent-config.yml
定义了 3 个预置 Agent：
1. **hr_policy**（HR 政策专家）- qwen-plus, temp=0.7
2. **recruiting**（招聘助手）- qwen-plus, temp=0.7
3. **ticket_router**（工单路由）- qwen-turbo, temp=0.5（更确定性）

每个 Agent 包含：type, name, description, model, system_prompt, tools

### JWT 配置（网关与组织服务共享）
- **密钥**：`JWT_SECRET` 环境变量（默认：hr-ai-saas-jwt-secret-key-must-be-at-least-32-characters-long）
- **Token 有效期**：8 小时（access token）
- **Refresh Token 有效期**：7 天（仅组织服务）
- **Issuer**：hr-ai-saas

## 开发规范

### 代码组织
- **包路径**：`com.hrai.{模块}.{功能}`
- **MapperScan**：`com.hrai.agent.**.mapper`
- **Lombok**：使用 `@Data`, `@Slf4j`，编译时会排除
- **异常处理**：使用 `BizException`（自定义错误码）
- **统一响应**：使用 `Result<T>` 包装（code, message, data, requestId, timestamp）

### 租户上下文传递
```java
// TenantContext.java (hr-ai-common)
TenantContext.getTenantId();    // 获取租户 ID
TenantContext.getUserId();      // 获取用户 ID
TenantContext.getSessionId();   // 获取会话 ID
TenantContext.getPlanType();    // 获取套餐类型 (free/pro/enterprise)
TenantContext.isEnterprise();   // 是否企业版
```

### MyBatis Plus 配置
- 逻辑删除字段：`deleted`（1=删除，0=正常）
- 驼峰转换：自动开启
- Mapper XML：`classpath*:/mapper/**/*.xml`

### Milvus 使用
```java
// MilvusConfig.java 提供 Bean
@Autowired
private MilvusClientV2 milvusClient;

// 配置注入
@Value("${milvus.host}") private String host;
@Value("${milvus.port}") private int port;
```

### Kafka 使用
```java
// KafkaConfig.Topics 常量
KafkaConfig.Topics.AGENT_TASK
KafkaConfig.Topics.WORKFLOW_EVENT
KafkaConfig.Topics.A2A_TASK_DELEGATION

// 发送消息
kafkaTemplate.send(topic, key, message);
```

## 健康检查和监控

### 接口验证
```bash
# 前端应用
curl http://localhost:5173

# API 网关（统一入口）
curl http://localhost:9000/health

# 组织鉴权服务
curl http://localhost:8081/api/v1/auth/health

# Agent 核心服务
curl http://localhost:8080/api/health
curl http://localhost:8080/api/health/ping

# Actuator 端点（各服务）
curl http://localhost:8080/api/actuator/health    # Agent 服务
curl http://localhost:8081/actuator/health        # 组织服务
curl http://localhost:9000/actuator/health        # 网关
curl http://localhost:8080/api/actuator/prometheus
```

### 日志查看
```bash
# 应用日志
tail -f hr-ai-agent-core/logs/spring.log

# Docker 服务日志
docker-compose logs -f mysql
docker-compose logs -f nacos
docker-compose logs -f kafka
docker-compose logs -f milvus
```

### 管理界面
- **Nacos 控制台**：http://localhost:8848/nacos（nacos/nacos）
- **Kafka UI**：http://localhost:9080
- **Milvus Attu**：http://localhost:8000
- **MinIO Console**：http://localhost:9001（minioadmin/minioadmin）

## 常见问题排查

### 端口冲突
```bash
# 检查端口占用
lsof -i :3306   # MySQL
lsof -i :6379   # Redis
lsof -i :8848   # Nacos
lsof -i :9092   # Kafka
lsof -i :19530  # Milvus
lsof -i :5173   # 前端 Vite Dev Server
lsof -i :9000   # API 网关
lsof -i :8081   # 组织鉴权服务
lsof -i :8080   # Agent 核心服务
```

### Maven 依赖问题
```bash
# 清理并重新下载
mvn clean install -U -DskipTests

# 删除本地仓库缓存
rm -rf ~/.m2/repository/com/hrai
rm -rf ~/.m2/repository/com/alibaba/cloud/ai
```

### Docker 服务重启
```bash
# 强制重建
docker-compose down
docker-compose up -d --force-recreate

# 查看服务状态
docker-compose ps

# 清理数据重新开始
docker-compose down -v
./scripts/start-dev.sh
```

## 项目进度

- **Phase 1** ✅（已完成）：项目骨架、数据库设计、Docker 环境
- **技术栈迁移** ✅：Qdrant→Milvus, RabbitMQ→Kafka, 新增 Nacos 3.x + MCP + A2A
- **Phase 2** ⏳（进行中）：RAG 服务、Agent 框架、ChatController
- **Phase 3-5** 📅（待启动）：多 Agent 协作、高级能力、生产优化

详见：`PROGRESS.md`, `PROJECT_README.md`, `QUICKSTART.md`

## 服务架构与访问路径

**请求流**：
```
前端 (5173) → API 网关 (9000) → 后端服务
                    ↓
        ┌───────────┴───────────┐
        ↓                       ↓
组织鉴权服务 (8081)      Agent 核心服务 (8080)
```

**网关路由规则**（本地开发，Nacos 禁用）：
- `/api/v1/auth/**`, `/api/v1/org/**`, `/api/v1/user/**` → hr-org-service (8081)
- `/api/v1/agent/**`, `/api/v1/chat/**`, `/api/v1/knowledge/**` → hr-ai-agent-core (8080)
- `/health/**` → 网关健康检查

**生产环境**（Nacos 启用后）：
- 网关启用服务发现（`spring.cloud.nacos.discovery.enabled=true`）
- 路由使用 `lb://service-name` 负载均衡
- 各服务注册到 Nacos（localhost:8848）

## 重要文件路径

**后端配置**：
- **父 POM**：`pom.xml`（依赖版本统一管理）
- **Agent 配置**：`hr-ai-agent-core/src/main/resources/application.yml`
- **Agent Bootstrap**：`hr-ai-agent-core/src/main/resources/bootstrap.yml`
- **Agent 定义**：`hr-ai-agent-core/src/main/resources/config/agent-config.yml`
- **网关配置**：`hr-gateway/src/main/resources/application.yml`
- **组织服务配置**：`hr-org-service/src/main/resources/application.yml`
- **数据库 Schema**：`hr-ai-agent-core/src/main/resources/db/schema.sql`
- **Docker 编排**：`docker-compose.yml`

**关键启动类**：
- `hr-ai-agent-core/src/main/java/com/hrai/agent/AgentApplication.java`
- `hr-gateway/src/main/java/com/hrai/gateway/GatewayApplication.java`
- `hr-org-service/src/main/java/com/hrai/org/OrgServiceApplication.java`

**核心配置类**：
- **Milvus**：`hr-ai-agent-core/src/main/java/com/hrai/agent/config/MilvusConfig.java`
- **Kafka**：`hr-ai-agent-core/src/main/java/com/hrai/agent/config/KafkaConfig.java`
- **MCP**：`hr-ai-agent-core/src/main/java/com/hrai/agent/config/McpServerConfig.java`
- **JWT 网关**：`hr-gateway/src/main/java/com/hrai/gateway/config/JwtConfig.java`
- **JWT 组织**：`hr-org-service/src/main/java/com/hrai/org/config/JwtConfig.java`
- **CORS**：`hr-gateway/src/main/java/com/hrai/gateway/config/CorsConfig.java`

**领域实体**：
- **A2A 任务**：`hr-ai-agent-core/src/main/java/com/hrai/agent/a2a/AgentTaskDelegation.java`
- **租户上下文**：`hr-ai-common/src/main/java/com/hrai/common/context/TenantContext.java`
- **用户实体**：`hr-org-service/src/main/java/com/hrai/org/entity/SysUser.java`
- **组织单元**：`hr-org-service/src/main/java/com/hrai/org/entity/OrgUnit.java`

**前端项目**：
- **配置**：`hr-ai-web/package.json`, `hr-ai-web/vite.config.ts`
- **入口**：`hr-ai-web/src/main.tsx`
- **路由**：`hr-ai-web/src/router/`
- **组件**：`hr-ai-web/src/components/`, `hr-ai-web/src/pages/`
