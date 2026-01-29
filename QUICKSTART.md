# 🚀 HR AI SaaS - 5分钟快速上手

> 从零到运行,只需5个步骤!

---

## ✅ 前置条件检查

在开始之前,请确保已安装:

```bash
# 检查 Java 版本 (需要 17+)
java -version

# 检查 Maven 版本 (需要 3.8+)
mvn -version

# 检查 Docker 是否运行
docker ps

# 检查 Docker Compose
docker-compose --version
```

---

## 📋 快速启动步骤

### 第1步: 克隆项目 (如果还没有)

```bash
cd /Users/guanjunhui/code/ai/HR-AI-SAAS
```

### 第2步: 启动依赖服务 (MySQL, Redis, Qdrant, RabbitMQ)

```bash
./scripts/start-dev.sh
```

**预期输出**:
```
📦 第1步: 启动依赖服务...
⏳ 等待服务就绪...
✅ 依赖服务启动完成!
```

**验证服务**:
```bash
docker-compose ps

# 应该看到4个服务都是 Up 状态
```

### 第3步: 编译项目

```bash
mvn clean package -DskipTests
```

**预期输出**:
```
[INFO] BUILD SUCCESS
[INFO] Total time: ~1-2分钟
```

### 第4步: 启动应用

```bash
cd hr-ai-agent-core
mvn spring-boot:run
```

**预期输出**:
```
╔═══════════════════════════════════════════════════╗
║                                                   ║
║    HR AI Agent 核心服务启动成功!                    ║
║                                                   ║
║    多Agent协作系统已就绪                            ║
║    访问 http://localhost:8080/api/health         ║
║                                                   ║
╚═══════════════════════════════════════════════════╝
```

### 第5步: 验证服务

打开新的终端窗口:

```bash
# 健康检查
curl http://localhost:8080/api/health

# Ping测试
curl http://localhost:8080/api/health/ping

# Actuator健康检查
curl http://localhost:8080/api/actuator/health
```

**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "UP",
    "service": "hr-ai-agent-core",
    "timestamp": "2025-01-29T11:00:00"
  }
}
```

---

## 🎉 成功!

如果看到以上输出,恭喜你已经成功启动了HR AI SaaS项目!

---

## 🔧 配置通义千问 API (可选,Phase 2需要)

### 获取API Key

1. 访问 [阿里云百炼平台](https://bailian.console.aliyun.com/)
2. 创建应用
3. 获取API Key (格式: `sk-xxxxx`)

### 配置方式1: 环境变量 (推荐)

```bash
export DASHSCOPE_API_KEY=sk-your-api-key

# 重启应用
cd hr-ai-agent-core
mvn spring-boot:run
```

### 配置方式2: 修改配置文件

编辑 `hr-ai-agent-core/src/main/resources/application.yml`:

```yaml
spring:
  ai:
    dashscope:
      api-key: sk-your-api-key
```

---

## 🌐 访问各服务

启动成功后,可以访问以下地址:

| 服务 | 地址 | 凭证 |
|------|------|------|
| **应用健康检查** | http://localhost:8080/api/health | - |
| **Prometheus监控** | http://localhost:8080/api/actuator/prometheus | - |
| **Qdrant Dashboard** | http://localhost:6333/dashboard | - |
| **RabbitMQ管理界面** | http://localhost:15672 | guest / guest |

---

## 🛑 停止服务

### 停止Spring Boot应用

在运行 `mvn spring-boot:run` 的终端按 `Ctrl+C`

### 停止Docker服务

```bash
./scripts/stop-dev.sh
```

### 清理所有数据 (慎用!)

```bash
docker-compose down -v
```

---

## 🔍 故障排查

### 问题1: Docker服务启动失败

**症状**: `docker-compose up -d` 报错

**解决**:
```bash
# 检查Docker是否运行
docker info

# 查看错误日志
docker-compose logs

# 强制重启
docker-compose down
docker-compose up -d --force-recreate
```

### 问题2: 端口冲突

**症状**: `Address already in use`

**解决**:
```bash
# 检查端口占用
lsof -i :3306  # MySQL
lsof -i :6379  # Redis
lsof -i :6334  # Qdrant
lsof -i :8080  # Spring Boot

# 关闭占用的进程,或修改 docker-compose.yml 中的端口映射
```

### 问题3: Maven编译失败

**症状**: `BUILD FAILURE`

**解决**:
```bash
# 清理并重新下载依赖
mvn clean install -U -DskipTests

# 如果还是失败,删除本地仓库缓存
rm -rf ~/.m2/repository/com/hrai
mvn clean install -DskipTests
```

### 问题4: Spring Boot启动失败

**症状**: `Application run failed`

**解决**:
```bash
# 检查MySQL是否启动
docker-compose ps mysql

# 检查数据库连接配置
# 编辑 hr-ai-agent-core/src/main/resources/application.yml
# 确认密码是 hr_ai_2025

# 查看详细错误日志
cd hr-ai-agent-core
mvn spring-boot:run -X
```

---

## 📚 下一步

### Phase 1 (已完成) ✅
- ✅ 项目骨架搭建
- ✅ 基础配置
- ✅ Docker环境

### Phase 2 (进行中)

**目标**: 实现基础的HR政策问答Agent

**任务**:
1. 配置通义千问API Key
2. 实现RAG检索服务
3. 实现HRPolicyAgent
4. 实现SSE聊天接口

**详细指南**: 参见 `PROGRESS.md` Phase 2章节

---

## 📖 相关文档

- [PROJECT_README.md](PROJECT_README.md) - 完整项目说明
- [PHASE1_COMPLETED.md](PHASE1_COMPLETED.md) - Phase 1详细报告
- [PROGRESS.md](PROGRESS.md) - 开发进度
- [STRUCTURE.md](STRUCTURE.md) - 项目结构

---

## 💡 提示

### 开发模式热重载

使用Spring Boot DevTools实现代码热重载:

```bash
cd hr-ai-agent-core
mvn spring-boot:run -Dspring-boot.run.fork=false
```

### 查看日志

```bash
# 应用日志
tail -f hr-ai-agent-core/logs/spring.log

# Docker服务日志
docker-compose logs -f mysql
docker-compose logs -f qdrant
```

### 数据库管理

```bash
# 连接MySQL
docker exec -it hr-ai-mysql mysql -uroot -phr_ai_2025

# 使用数据库
USE hr_ai_saas;

# 查看表
SHOW TABLES;

# 查看Agent定义
SELECT * FROM agents;
```

---

**准备好了吗? 让我们开始Phase 2的开发!** 🚀

*最后更新: 2025-01-29*
