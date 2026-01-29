# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

HR AI SaaS - 基于 Spring AI Alibaba 的多 Agent 协作系统，用于构建智能 HR 助手 SaaS 产品。

**技术栈核心**：
- Spring Boot 3.2.2 + Spring AI Alibaba 1.0.0-M2（通义千问）
- MySQL 8.0 + Redis + Qdrant（向量库）+ RabbitMQ
- Maven 多模块架构（hr-ai-common, hr-ai-agent-core）

## 开发环境设置

### 启动依赖服务
```bash
# 启动 Docker 依赖（MySQL, Redis, Qdrant, RabbitMQ）
./scripts/start-dev.sh

# 停止服务
./scripts/stop-dev.sh
```

### 构建和运行
```bash
# 根目录编译整个项目
mvn clean package -DskipTests

# 启动应用（在 hr-ai-agent-core 目录）
cd hr-ai-agent-core
mvn spring-boot:run

# 热重载开发模式
mvn spring-boot:run -Dspring-boot.run.fork=false
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
- **hr-ai-common**：公共组件（常量、DTO、异常、工具类）
- **hr-ai-agent-core**：Agent 核心服务（编排器、工作流、RAG、记忆管理）

### Agent 编排架构（未来实现）
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
```

### 关键设计模式
1. **Agent 定义**：配置化（agent-config.yml），支持动态加载
2. **工具调用**：Function Calling，每个 Agent 配置独立工具集
3. **记忆管理**：
   - 短期：Redis（10 轮对话，1 小时 TTL）
   - 长期：Qdrant 向量化存储 + MySQL
4. **RAG 检索**：Qdrant（768 维向量）+ Top-5 + 0.6 相似度阈值
5. **多租户**：请求头传递（X-Tenant-Id, X-User-Id, X-Session-Id）

### 数据库 Schema 重点
- **agents**：Agent 定义表（type, config JSON）
- **workflows**：工作流定义（状态机配置）
- **conversation_history**：长期记忆
- **knowledge_docs/chunks**：知识库（向量化分块）
- **function_invocations**：Function 调用日志（审计 + 调试）

## 配置文件关键点

### application.yml
- **数据库密码**：`hr_ai_2025`（Docker 默认）
- **API 基础路径**：`/api`（服务端口 8080）
- **AI 配置**：
  - API Key：`DASHSCOPE_API_KEY` 环境变量
  - 默认模型：`qwen-plus`（可选 qwen-turbo, qwen-max）
  - 嵌入模型：`text-embedding-v2`（768 维）
- **Qdrant 集合**：
  - `knowledge_default`：知识库向量
  - `user_memory`：用户记忆向量

### agent-config.yml
定义了 3 个预置 Agent：
1. **hr_policy**（HR 政策专家）- qwen-plus, temp=0.7
2. **recruiting**（招聘助手）- qwen-plus, temp=0.7
3. **ticket_router**（工单路由）- qwen-turbo, temp=0.5（更确定性）

每个 Agent 包含：type, name, description, model, system_prompt, tools

## 开发规范

### 代码组织
- **包路径**：`com.hrai.{模块}.{功能}`
- **MapperScan**：`com.hrai.agent.**.mapper`
- **Lombok**：使用 `@Data`, `@Slf4j`，编译时会排除
- **异常处理**：使用 `BizException`（自定义错误码）
- **统一响应**：使用 `Result<T>` 包装（code, message, data, requestId, timestamp）

### 租户上下文传递
```java
// 常量定义在 TenantConstants
public static final String HEADER_TENANT_ID = "X-Tenant-Id";
public static final String HEADER_USER_ID = "X-User-Id";
public static final String HEADER_SESSION_ID = "X-Session-Id";
public static final String REDIS_SESSION_PREFIX = "hrai:session:";
```

### MyBatis Plus 配置
- 逻辑删除字段：`deleted`（1=删除，0=正常）
- 驼峰转换：自动开启
- Mapper XML：`classpath*:/mapper/**/*.xml`

### Qdrant 使用
```java
// QdrantConfig.java 提供 Bean
@Autowired
private QdrantClient qdrantClient;

// 配置注入
@Value("${qdrant.host}") private String host;
@Value("${qdrant.port}") private int port;
```

## 健康检查和监控

### 接口验证
```bash
# 自定义健康检查
curl http://localhost:8080/api/health
curl http://localhost:8080/api/health/ping

# Actuator 端点
curl http://localhost:8080/api/actuator/health
curl http://localhost:8080/api/actuator/prometheus
```

### 日志查看
```bash
# 应用日志
tail -f hr-ai-agent-core/logs/spring.log

# Docker 服务日志
docker-compose logs -f mysql
docker-compose logs -f qdrant
docker-compose logs -f rabbitmq
```

### 管理界面
- **RabbitMQ 管理界面**：http://localhost:15672（guest/guest）
- **Qdrant Dashboard**：http://localhost:6333/dashboard

## 常见问题排查

### 端口冲突
```bash
# 检查端口占用
lsof -i :3306   # MySQL
lsof -i :6379   # Redis
lsof -i :6334   # Qdrant gRPC
lsof -i :8080   # Spring Boot
```

### Maven 依赖问题
```bash
# 清理并重新下载
mvn clean install -U -DskipTests

# 删除本地仓库缓存
rm -rf ~/.m2/repository/com/hrai
```

### Docker 服务重启
```bash
# 强制重建
docker-compose down
docker-compose up -d --force-recreate

# 查看服务状态
docker-compose ps
```

## 项目进度

- **Phase 1** ✅（已完成）：项目骨架、数据库设计、Docker 环境
- **Phase 2** ⏳（进行中，4-6 周）：RAG 服务、Agent 框架、ChatController
- **Phase 3-5** 📅（待启动）：多 Agent 协作、高级能力、生产优化

详见：`PROGRESS.md`, `PROJECT_README.md`, `QUICKSTART.md`

## 重要文件路径

- **主配置**：`hr-ai-agent-core/src/main/resources/application.yml`
- **Agent 配置**：`hr-ai-agent-core/src/main/resources/config/agent-config.yml`
- **数据库 Schema**：`hr-ai-agent-core/src/main/resources/db/schema.sql`
- **Docker 编排**：`docker-compose.yml`
- **启动类**：`hr-ai-agent-core/src/main/java/com/hrai/agent/AgentApplication.java`
