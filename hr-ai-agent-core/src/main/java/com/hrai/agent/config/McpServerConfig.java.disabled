package com.hrai.agent.config;

import com.hrai.common.context.TenantContext;
import io.milvus.v2.client.MilvusClientV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * MCP Server 配置
 * 实现工具动态注册与发现
 *
 * 支持三大场景:
 * 1. Agent 工具动态注册 - 各 Agent 的 Tools/Functions 通过 Nacos 动态发现
 * 2. 多服务 Agent 协调 - 不同微服务中的 Agent 互相发现和调用
 * 3. 外部 MCP Server 接入 - 接入第三方 MCP Server (IDE 插件、浏览器扩展等)
 *
 * @author HR AI Team
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class McpServerConfig {

    private final MilvusClientV2 milvusClient;
    private final MilvusConfig milvusConfig;

    /**
     * 知识库搜索工具
     * 在知识库中搜索相关文档
     */
    @Bean
    public Function<SearchKnowledgeRequest, SearchKnowledgeResponse> searchKnowledge(
            EmbeddingModel embeddingModel) {
        return request -> {
            log.info("MCP Tool 调用: searchKnowledge, query={}", request.query());

            String tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                tenantId = "default";
            }

            try {
                // 1. 生成查询向量
                float[] queryVector = embeddingModel.embed(request.query());

                // 2. 在 Milvus 中搜索
                // 实际实现需要根据 Milvus SDK 完成向量检索
                // 这里是框架代码，具体实现依赖业务需求

                log.info("知识库搜索完成: tenantId={}, query={}", tenantId, request.query());

                return new SearchKnowledgeResponse(
                        List.of(),  // 实际返回搜索结果
                        0,
                        "搜索完成"
                );
            } catch (Exception e) {
                log.error("知识库搜索失败: {}", e.getMessage(), e);
                return new SearchKnowledgeResponse(
                        List.of(),
                        0,
                        "搜索失败: " + e.getMessage()
                );
            }
        };
    }

    /**
     * HR 政策查询工具
     */
    @Bean
    public Function<PolicyQueryRequest, PolicyQueryResponse> queryHrPolicy(ChatModel chatModel) {
        return request -> {
            log.info("MCP Tool 调用: queryHrPolicy, question={}", request.question());

            try {
                // 调用 AI 模型回答 HR 政策问题
                // 实际实现需要结合 RAG 检索

                return new PolicyQueryResponse(
                        "这是对 HR 政策问题的回答",
                        0.95,
                        List.of("来源1", "来源2")
                );
            } catch (Exception e) {
                log.error("HR 政策查询失败: {}", e.getMessage(), e);
                return new PolicyQueryResponse(
                        "查询失败: " + e.getMessage(),
                        0.0,
                        List.of()
                );
            }
        };
    }

    /**
     * 招聘流程查询工具
     */
    @Bean
    public Function<RecruitingQueryRequest, RecruitingQueryResponse> queryRecruitingStatus() {
        return request -> {
            log.info("MCP Tool 调用: queryRecruitingStatus, candidateId={}", request.candidateId());

            // 实际实现需要查询数据库

            return new RecruitingQueryResponse(
                    request.candidateId(),
                    "面试中",
                    Map.of(
                            "currentStage", "技术面试",
                            "nextStep", "HR 面试"
                    )
                );
        };
    }

    /**
     * 工单创建工具
     */
    @Bean
    public Function<CreateTicketRequest, CreateTicketResponse> createTicket() {
        return request -> {
            log.info("MCP Tool 调用: createTicket, title={}, category={}",
                    request.title(), request.category());

            String tenantId = TenantContext.getTenantId();
            String userId = TenantContext.getUserId();

            // 实际实现需要创建工单记录

            String ticketId = "TKT-" + System.currentTimeMillis();

            return new CreateTicketResponse(
                    ticketId,
                    "pending",
                    "工单创建成功"
            );
        };
    }

    // ========== Request/Response Records ==========

    public record SearchKnowledgeRequest(
            String query,
            int topK,
            double scoreThreshold
    ) {}

    public record SearchKnowledgeResponse(
            List<Map<String, Object>> results,
            int totalCount,
            String message
    ) {}

    public record PolicyQueryRequest(
            String question,
            String category
    ) {}

    public record PolicyQueryResponse(
            String answer,
            double confidence,
            List<String> sources
    ) {}

    public record RecruitingQueryRequest(
            String candidateId,
            String positionId
    ) {}

    public record RecruitingQueryResponse(
            String candidateId,
            String status,
            Map<String, Object> details
    ) {}

    public record CreateTicketRequest(
            String title,
            String description,
            String category,
            String priority
    ) {}

    public record CreateTicketResponse(
            String ticketId,
            String status,
            String message
    ) {}
}
