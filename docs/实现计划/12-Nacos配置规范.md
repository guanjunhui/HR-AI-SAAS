# Nacos 配置与服务发现规范

> **版本**: v1.0.0
> **创建日期**: 2026-02-02
> **文档状态**: 使用指南

---

## 一、Nacos 架构概览

### 1.1 Nacos 在架构中的作用

```
┌─────────────────────────────────────────────────────────────┐
│                    Nacos Server (8848)                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌────────────────────────┐  ┌────────────────────────┐    │
│  │    配置中心 (Config)    │  │   注册中心 (Registry)   │    │
│  ├────────────────────────┤  ├────────────────────────┤    │
│  │ - 动态配置管理          │  │ - 服务注册             │    │
│  │ - 配置热更新            │  │ - 服务发现             │    │
│  │ - 配置版本管理          │  │ - 健康检查             │    │
│  │ - 灰度发布              │  │ - 负载均衡             │    │
│  │ - 配置监听              │  │ - 服务元数据管理        │    │
│  └────────────────────────┘  └────────────────────────┘    │
│                                                             │
│  ┌────────────────────────────────────────────────────┐    │
│  │              MCP/A2A 扩展能力                       │    │
│  ├────────────────────────────────────────────────────┤    │
│  │ - MCP工具动态注册                                   │    │
│  │ - Agent能力发现                                     │    │
│  │ - A2A任务路由                                       │    │
│  └────────────────────────────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                           ▲
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   ┌────▼────┐       ┌────▼────┐       ┌────▼────┐
   │ Gateway │       │ Service │       │ Service │
   │         │       │    A    │       │    B    │
   └─────────┘       └─────────┘       └─────────┘
```

---

## 二、命名空间与分组规划

### 2.1 命名空间设计（环境隔离）

```yaml
命名空间规划：
├─ dev (开发环境)
│  └─ namespace-id: hr-ai-dev
├─ test (测试环境)
│  └─ namespace-id: hr-ai-test
├─ staging (预发布环境)
│  └─ namespace-id: hr-ai-staging
└─ prod (生产环境)
   └─ namespace-id: hr-ai-prod
```

**创建命名空间**：

```bash
# 方式1: Nacos 控制台创建
http://localhost:8848/nacos
命名空间管理 → 新建命名空间

# 方式2: OpenAPI创建
curl -X POST 'http://localhost:8848/nacos/v1/console/namespaces' \
  -d 'customNamespaceId=hr-ai-dev&namespaceName=开发环境&namespaceDesc=HR AI SaaS 开发环境'
```

### 2.2 配置分组设计（业务隔离）

```yaml
分组规划：
├─ HRAI_GROUP (主业务组)
│  ├─ hr-gateway
│  ├─ hr-org-service
│  ├─ hr-core-service
│  └─ ...
├─ HRAI_COMMON_GROUP (公共配置组)
│  ├─ common.yml (全局配置)
│  ├─ db.yml (数据库配置)
│  └─ redis.yml (Redis配置)
└─ HRAI_MCP_GROUP (MCP/A2A配置组)
   ├─ mcp-tools.yml
   └─ a2a-agents.yml
```

---

## 三、配置中心使用规范

### 3.1 配置文件命名规范

```
格式：{spring.application.name}-{profile}.{file-extension}

示例：
hr-org-service.yml          # 默认配置
hr-org-service-dev.yml      # 开发环境
hr-org-service-test.yml     # 测试环境
hr-org-service-prod.yml     # 生产环境
```

### 3.2 配置优先级

```
优先级（高 → 低）：
1. Nacos 配置中心（远程配置）
   hr-org-service-prod.yml
2. Nacos 配置中心（默认配置）
   hr-org-service.yml
3. 本地配置文件
   application-prod.yml
4. 本地默认配置
   application.yml
```

### 3.3 共享配置（扩展配置）

```yaml
# bootstrap.yml
spring:
  application:
    name: hr-org-service
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:hr-ai-dev}
        group: HRAI_GROUP
        file-extension: yml

        # 扩展配置（共享配置）
        extension-configs:
          - data-id: common.yml
            group: HRAI_COMMON_GROUP
            refresh: true
          - data-id: db.yml
            group: HRAI_COMMON_GROUP
            refresh: true
          - data-id: redis.yml
            group: HRAI_COMMON_GROUP
            refresh: true

        # 共享配置（推荐使用 extension-configs）
        shared-configs:
          - data-id: logging.yml
            group: HRAI_COMMON_GROUP
            refresh: true
```

### 3.4 配置热更新

#### 3.4.1 使用 @RefreshScope

```java
@RestController
@RefreshScope  // 支持动态刷新
public class ConfigController {

    @Value("${system.notice:默认公告}")
    private String systemNotice;

    @GetMapping("/config/notice")
    public String getNotice() {
        return systemNotice;
    }
}
```

#### 3.4.2 使用 @ConfigurationProperties

```java
@Data
@Component
@ConfigurationProperties(prefix = "system")
@RefreshScope
public class SystemConfig {
    private String notice;
    private Integer maxUploadSize;
    private List<String> allowedIps;
}
```

#### 3.4.3 监听配置变化

```java
@Component
@Slf4j
public class ConfigChangeListener {

    @Autowired
    private NacosConfigManager nacosConfigManager;

    @PostConstruct
    public void init() throws NacosException {
        String dataId = "hr-org-service.yml";
        String group = "HRAI_GROUP";

        nacosConfigManager.getConfigService().addListener(
            dataId,
            group,
            new Listener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("配置已更新: {}", configInfo);
                    // 处理配置变化逻辑
                }

                @Override
                public Executor getExecutor() {
                    return null;
                }
            }
        );
    }
}
```

### 3.5 配置示例

#### 3.5.1 公共配置（common.yml）

```yaml
# Data ID: common.yml
# Group: HRAI_COMMON_GROUP

# 日志配置
logging:
  level:
    root: INFO
    com.hrai: DEBUG
  pattern:
    console: '%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{traceId}] - %msg%n'

# 分页配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl
  global-config:
    db-config:
      logic-delete-field: deleted
      logic-delete-value: 1
      logic-not-delete-value: 0

# 监控配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

#### 3.5.2 数据库配置（db.yml）

```yaml
# Data ID: db.yml
# Group: HRAI_COMMON_GROUP

# 数据源公共配置
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    druid:
      initial-size: 5
      min-idle: 5
      max-active: 20
      max-wait: 60000
      time-between-eviction-runs-millis: 60000
      min-evictable-idle-time-millis: 300000
      validation-query: SELECT 1
      test-while-idle: true
      test-on-borrow: false
      test-on-return: false
      pool-prepared-statements: true
      max-pool-prepared-statement-per-connection-size: 20
      filters: stat,wall
      stat-view-servlet:
        enabled: true
        url-pattern: /druid/*
        login-username: admin
        login-password: admin123
```

#### 3.5.3 Redis配置（redis.yml）

```yaml
# Data ID: redis.yml
# Group: HRAI_COMMON_GROUP

spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: 0
    timeout: 3000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
```

#### 3.5.4 服务专属配置（hr-org-service.yml）

```yaml
# Data ID: hr-org-service.yml
# Group: HRAI_GROUP

server:
  port: 8081

spring:
  application:
    name: hr-org-service

  datasource:
    url: jdbc:mysql://${MYSQL_HOST:localhost}:3307/hr_org?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
    username: ${MYSQL_USER:root}
    password: ${MYSQL_PASSWORD:hr_ai_2025}

  redis:
    database: 1  # hr-org-service专属Redis库

# 业务配置
org:
  tree:
    max-depth: 10
    cache-ttl: 3600
  user:
    password:
      bcrypt-strength: 10
  jwt:
    secret: ${JWT_SECRET:hr-ai-secret-key-2025}
    expiration: 86400  # 24小时
```

---

## 四、服务注册与发现规范

### 4.1 服务注册配置

```yaml
# bootstrap.yml
spring:
  application:
    name: hr-org-service
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
        namespace: ${NACOS_NAMESPACE:hr-ai-dev}
        group: HRAI_GROUP

        # 服务实例配置
        ip: ${SERVICE_IP:}  # 留空自动获取
        port: ${SERVER_PORT:8081}
        weight: 1.0  # 权重（0.01-100），用于负载均衡

        # 元数据（可用于灰度发布、版本路由）
        metadata:
          version: 1.0.0
          env: ${SPRING_PROFILES_ACTIVE:dev}
          region: cn-hangzhou
          service-type: business

        # 健康检查
        heart-beat-interval: 5000  # 心跳间隔 5秒
        heart-beat-timeout: 15000  # 心跳超时 15秒
        ip-delete-timeout: 30000   # 实例删除超时 30秒

        # 是否注册
        register-enabled: true

        # 集群名称
        cluster-name: DEFAULT
```

### 4.2 服务发现配置

```yaml
spring:
  cloud:
    nacos:
      discovery:
        # 是否启用服务发现
        enabled: true

        # 订阅服务列表
        subscribe-services: hr-core-service,hr-ai-service

        # 只订阅健康实例
        naming-load-cache-at-start: true
```

### 4.3 服务元数据应用

#### 4.3.1 灰度发布

```java
@Configuration
public class GrayscaleConfig {

    @Bean
    public ReactorLoadBalancer<ServiceInstance> grayscaleLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory loadBalancerClientFactory) {

        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);

        return new RoundRobinLoadBalancer(
            loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class),
            name) {

            @Override
            public Mono<Response<ServiceInstance>> choose(Request request) {
                // 从请求头获取灰度标识
                String grayscaleVersion = getGrayscaleVersion(request);

                if (grayscaleVersion != null) {
                    // 筛选匹配版本的实例
                    return super.choose(request)
                        .map(response -> {
                            List<ServiceInstance> instances = response.getServer().stream()
                                .filter(instance ->
                                    grayscaleVersion.equals(instance.getMetadata().get("version")))
                                .collect(Collectors.toList());

                            if (!instances.isEmpty()) {
                                return new DefaultResponse(instances.get(0));
                            }
                            return response;
                        });
                }

                return super.choose(request);
            }
        };
    }
}
```

#### 4.3.2 根据元数据路由

```java
@Component
public class MetadataRouter {

    @Autowired
    private NacosDiscoveryProperties nacosProperties;

    public ServiceInstance selectByRegion(List<ServiceInstance> instances, String region) {
        return instances.stream()
            .filter(instance -> region.equals(instance.getMetadata().get("region")))
            .findFirst()
            .orElse(instances.get(0));
    }
}
```

---

## 五、MCP/A2A 与 Nacos 集成

### 5.1 MCP 工具动态注册

```yaml
# Data ID: mcp-tools.yml
# Group: HRAI_MCP_GROUP

spring:
  ai:
    alibaba:
      mcp:
        nacos:
          enabled: true
          server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
          namespace: ${NACOS_NAMESPACE:hr-ai-dev}
          group: HRAI_MCP_GROUP

          # MCP Server 配置
          servers:
            - name: hr-knowledge-search
              description: HR知识库搜索工具
              tools:
                - name: search_knowledge
                  description: 搜索HR政策知识库
                  parameters:
                    query: 搜索关键词
              endpoint: http://hr-rag-service:8090/mcp/tools/search

            - name: employee-query
              description: 员工信息查询工具
              tools:
                - name: query_employee
                  description: 根据工号或姓名查询员工
                  parameters:
                    employee_code: 工号
                    name: 姓名
              endpoint: http://hr-core-service:8082/mcp/tools/employee
```

### 5.2 A2A Agent 注册发现

```yaml
# Data ID: a2a-agents.yml
# Group: HRAI_MCP_GROUP

spring:
  ai:
    alibaba:
      a2a:
        nacos:
          enabled: true
          server-addr: ${NACOS_SERVER_ADDR:localhost:8848}
          namespace: ${NACOS_NAMESPACE:hr-ai-dev}
          group: HRAI_MCP_GROUP

          # Agent 能力注册
          agents:
            - name: hr-policy-agent
              description: HR政策专家Agent
              service-name: hr-ai-service
              capabilities:
                - hr-policy-query
                - leave-policy-explain
                - salary-policy-query
              endpoints:
                task: /a2a/tasks
                messages: /a2a/messages

            - name: recruiting-agent
              description: 招聘助手Agent
              service-name: hr-recruit-service
              capabilities:
                - resume-parsing
                - candidate-matching
                - interview-scheduling
              endpoints:
                task: /a2a/tasks
                messages: /a2a/messages
```

### 5.3 Java代码中使用 A2A

```java
@Service
@Slf4j
public class A2ATaskDelegationService {

    @Autowired
    private NacosDiscoveryProperties nacosProperties;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 委派任务到其他Agent
     */
    public void delegateTask(String targetAgent, A2ATaskRequest taskRequest) {
        // 1. 从Nacos获取目标Agent实例
        List<ServiceInstance> instances = nacosProperties
            .namingServiceInstance()
            .selectInstances(targetAgent, true);

        if (instances.isEmpty()) {
            throw new BizException("目标Agent不可用: " + targetAgent);
        }

        ServiceInstance instance = instances.get(0);
        String a2aEndpoint = instance.getMetadata().get("a2a.task.endpoint");

        if (a2aEndpoint == null) {
            a2aEndpoint = "/a2a/tasks";  // 默认端点
        }

        // 2. 发送A2A任务请求
        String url = String.format("http://%s:%d%s",
            instance.getHost(),
            instance.getPort(),
            a2aEndpoint);

        restTemplate.postForObject(url, taskRequest, A2ATaskResponse.class);

        log.info("A2A任务已委派: {} -> {}",
            nacosProperties.getService(), targetAgent);
    }
}
```

---

## 六、最佳实践

### 6.1 配置敏感信息加密

```yaml
# 使用占位符，敏感信息通过环境变量注入
spring:
  datasource:
    username: ${MYSQL_USER}
    password: ${MYSQL_PASSWORD}
  redis:
    password: ${REDIS_PASSWORD}

# 或使用 Nacos 加密配置（KMS）
spring:
  cloud:
    nacos:
      config:
        kms:
          enabled: true
          key-id: ${KMS_KEY_ID}
```

### 6.2 配置版本管理

```bash
# Nacos支持配置历史版本
# 在控制台可回滚到任意历史版本

# 推荐做法：
1. 每次修改配置前先备份
2. 在配置中添加版本注释
3. 重大变更前通知相关服务

# 配置示例
# Version: 1.2.0
# Date: 2026-02-02
# Author: DevOps
# Changes: 调整数据库连接池参数
spring:
  datasource:
    druid:
      max-active: 30  # 从20调整为30
```

### 6.3 多环境配置隔离

```
推荐方案：命名空间隔离

开发环境:
  namespace: hr-ai-dev
  配置文件: hr-org-service-dev.yml

测试环境:
  namespace: hr-ai-test
  配置文件: hr-org-service-test.yml

生产环境:
  namespace: hr-ai-prod
  配置文件: hr-org-service-prod.yml
```

### 6.4 健康检查优化

```yaml
spring:
  cloud:
    nacos:
      discovery:
        # 优化心跳检查
        heart-beat-interval: 5000   # 5秒心跳
        heart-beat-timeout: 15000   # 15秒超时（3次心跳失败）
        ip-delete-timeout: 30000    # 30秒后删除实例

        # 开启健康检查
        health-check-enabled: true
```

---

## 七、常见问题排查

### 7.1 服务注册失败

```bash
# 问题现象
服务启动正常，但Nacos控制台看不到服务实例

# 排查步骤
1. 检查Nacos服务是否运行
curl http://localhost:8848/nacos/v1/console/health/liveness

2. 检查namespace配置是否正确
curl http://localhost:8848/nacos/v1/console/namespaces

3. 检查服务日志
grep "register service" application.log

4. 检查网络连通性
telnet localhost 8848
```

### 7.2 配置不生效

```bash
# 问题现象
修改了Nacos配置，但服务未刷新

# 排查步骤
1. 检查是否添加 @RefreshScope 注解
2. 检查配置监听是否正常
curl http://localhost:808x/actuator/nacos-config
3. 检查Data ID和Group是否匹配
4. 检查命名空间是否正确
```

### 7.3 服务调用失败

```bash
# 问题现象
Feign调用报错：No instances available for hr-org-service

# 排查步骤
1. 确认目标服务已注册
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=hr-org-service

2. 确认服务实例健康
curl http://localhost:8081/actuator/health

3. 检查命名空间是否一致
4. 检查服务发现配置
```

---

## 八、附录

### 8.1 Nacos OpenAPI常用命令

```bash
# 查询服务列表
curl -X GET 'http://localhost:8848/nacos/v1/ns/service/list?pageNo=1&pageSize=20'

# 查询服务实例
curl -X GET 'http://localhost:8848/nacos/v1/ns/instance/list?serviceName=hr-org-service'

# 发布配置
curl -X POST 'http://localhost:8848/nacos/v1/cs/configs' \
  -d 'dataId=hr-org-service.yml&group=HRAI_GROUP&content=...'

# 获取配置
curl -X GET 'http://localhost:8848/nacos/v1/cs/configs?dataId=hr-org-service.yml&group=HRAI_GROUP'

# 删除配置
curl -X DELETE 'http://localhost:8848/nacos/v1/cs/configs?dataId=hr-org-service.yml&group=HRAI_GROUP'

# 监听配置
curl -X POST 'http://localhost:8848/nacos/v1/cs/configs/listener' \
  -d 'Listening-Configs=hr-org-service.yml%02HRAI_GROUP%01'
```

### 8.2 Nacos控制台地址

```
本地开发: http://localhost:8848/nacos
默认账号: nacos / nacos

生产环境建议修改默认密码并启用权限控制
```

---

> **重要提示**: 生产环境务必修改Nacos默认密码，启用权限控制，配置敏感信息加密。
