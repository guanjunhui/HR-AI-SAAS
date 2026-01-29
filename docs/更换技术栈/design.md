# 更换技术栈 - 设计

状态: draft
负责人:
最后更新: 2026-01-29

## 总览
- 将消息队列实现替换为 Kafka，统一 Topic、Producer/Consumer 规范与配置。
- 将向量库实现替换为 Milvus，提供建库、写入、检索与索引策略。
- 升级 Spring AI Alibaba 至 1.1.0.0-RC2，并引入 agent-framework 与 graph 能力。
- 接入 Nacos 作为注册中心，完成 MCP 注册发现，并预留 A2A 元数据扩展。

## 方案
- 以适配层方式替换 MQ 与向量库：保留业务接口，内部实现切换到 Kafka/Milvus。
- 使用配置开关控制新旧实现（灰度/回滚），必要时支持双写/双读。
- 统一基础设施配置（地址、认证、TLS、超时、重试策略）。
- 将 MCP 注册与服务发现抽象为注册模块，启动/下线自动更新 Nacos。

## 架构/模块
- messaging 模块：KafkaProducer/KafkaConsumer 封装、Topic/Schema 管理、重试与死信策略。
- vector 模块：Milvus Client 封装、Collection/Index 管理、查询接口。
- ai 模块：Spring AI Alibaba 升级与 agent-framework/graph 能力装配。
- registry 模块：Nacos 注册、健康检查、元数据同步（含 MCP/A2A 扩展字段）。

## 数据模型
- Kafka：Topic 命名规范、消息 key/headers、序列化格式（沿用现有或统一为 JSON/Avro）。
- Milvus：Collection 名称、向量维度、字段 schema、索引类型与参数。
- Nacos：服务名、实例元数据（版本、能力、MCP 端点、A2A 预留字段）。

## 接口/协议
- 内部接口保持不变（业务服务调用 MQ/向量库的抽象接口）。
- MCP 注册协议：定义元数据键（如 mcp.endpoint、mcp.version、capabilities）。
- A2A 预留：定义扩展字段（如 a2a.endpoint、a2a.version），先不强依赖。

## 交互/流程
- 服务启动：读取配置 -> 初始化 Kafka/Milvus 客户端 -> 注册 Nacos -> 上报 MCP 元数据。
- 服务运行：消息消费/生产与向量检索走新实现；必要时双写/双读。
- 服务下线：注销 Nacos 实例，确保发现列表更新。

## 边界/异常
- Kafka/Milvus/Nacos 不可用时：启动失败或降级策略（可配置）。
- 消息堆积/消费失败：重试 + 死信队列（如需）。
- 向量检索失败：错误上报与降级返回。

## 安全/权限/隐私
- Kafka/Milvus/Nacos 访问凭据统一管理（配置中心/环境变量）。
- 支持 TLS/SSL 连接与敏感信息脱敏日志。

## 观测性
- 关键指标：Kafka 消费/生产延迟、Milvus 检索耗时、注册状态。
- 日志与分布式追踪：统一 traceId，便于问题定位。

## 迁移/发布
- 分阶段：开发环境验证 -> 测试环境回归 -> 灰度/双写 -> 全量切换。
- 若切换失败：一键开关回退旧实现。

## 风险与缓解
- RC 版本风险：预留回滚到上一稳定版本；隔离升级影响范围。
- 数据兼容风险：在测试环境做样本迁移与一致性验证。
- 运维复杂度提升：补充部署与监控文档。
