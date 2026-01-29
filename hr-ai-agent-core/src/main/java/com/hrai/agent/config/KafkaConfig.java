package com.hrai.agent.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka 消息队列配置
 * 替代原有的 RabbitMQ 配置
 *
 * @author HR AI Team
 */
@Slf4j
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    /**
     * Topic 名称常量
     */
    public static final class Topics {
        /**
         * Agent 任务 Topic
         * 用于 Agent 间任务分发和协调
         */
        public static final String AGENT_TASK = "agent-task";

        /**
         * 工作流事件 Topic
         * 用于工作流状态变更通知
         */
        public static final String WORKFLOW_EVENT = "workflow-event";

        /**
         * 对话历史 Topic
         * 用于异步持久化对话记录
         */
        public static final String CONVERSATION_HISTORY = "conversation-history";

        /**
         * 知识库更新 Topic
         * 用于知识库文档变更通知
         */
        public static final String KNOWLEDGE_UPDATE = "knowledge-update";

        /**
         * A2A 任务委派 Topic
         * 用于 Agent 间任务委派
         */
        public static final String A2A_TASK_DELEGATION = "a2a-task-delegation";

        /**
         * MCP 工具调用 Topic
         * 用于 MCP 工具异步调用
         */
        public static final String MCP_TOOL_INVOCATION = "mcp-tool-invocation";

        private Topics() {}
    }

    /**
     * Kafka Admin 配置
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * Agent 任务 Topic (3 分区)
     */
    @Bean
    public NewTopic agentTaskTopic() {
        return TopicBuilder.name(Topics.AGENT_TASK)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "604800000") // 7天保留
                .build();
    }

    /**
     * 工作流事件 Topic (3 分区)
     */
    @Bean
    public NewTopic workflowEventTopic() {
        return TopicBuilder.name(Topics.WORKFLOW_EVENT)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "259200000") // 3天保留
                .build();
    }

    /**
     * 对话历史 Topic (6 分区，高吞吐)
     */
    @Bean
    public NewTopic conversationHistoryTopic() {
        return TopicBuilder.name(Topics.CONVERSATION_HISTORY)
                .partitions(6)
                .replicas(1)
                .config("retention.ms", "2592000000") // 30天保留
                .build();
    }

    /**
     * 知识库更新 Topic (3 分区)
     */
    @Bean
    public NewTopic knowledgeUpdateTopic() {
        return TopicBuilder.name(Topics.KNOWLEDGE_UPDATE)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "604800000") // 7天保留
                .build();
    }

    /**
     * A2A 任务委派 Topic (3 分区)
     */
    @Bean
    public NewTopic a2aTaskDelegationTopic() {
        return TopicBuilder.name(Topics.A2A_TASK_DELEGATION)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "86400000") // 1天保留
                .build();
    }

    /**
     * MCP 工具调用 Topic (3 分区)
     */
    @Bean
    public NewTopic mcpToolInvocationTopic() {
        return TopicBuilder.name(Topics.MCP_TOOL_INVOCATION)
                .partitions(3)
                .replicas(1)
                .config("retention.ms", "86400000") // 1天保留
                .build();
    }
}
