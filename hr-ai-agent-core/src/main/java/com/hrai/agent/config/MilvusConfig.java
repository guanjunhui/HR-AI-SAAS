package com.hrai.agent.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Milvus 向量数据库配置
 * 替代原有的 Qdrant 配置
 *
 * @author HR AI Team
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "milvus")
public class MilvusConfig {

    private static final Logger log = LoggerFactory.getLogger(MilvusConfig.class);

    /**
     * Milvus 服务地址
     */
    private String host = "localhost";

    /**
     * Milvus 端口
     */
    private Integer port = 19530;

    /**
     * 用户名 (可选)
     */
    private String username;

    /**
     * 密码 (可选)
     */
    private String password;

    /**
     * Collection 配置
     */
    private Map<String, CollectionConfig> collections;

    @Data
    public static class CollectionConfig {
        private String name;
        private Integer dimension = 768;
        private String indexType = "IVF_FLAT";
        private String metricType = "COSINE";
        private String description;
    }

    @Bean
    public MilvusClientV2 milvusClient() {
        log.info("初始化 Milvus 客户端: {}:{}", host, port);

        ConnectConfig.ConnectConfigBuilder builder = ConnectConfig.builder()
                .uri("http://" + host + ":" + port);

        // 如果配置了认证信息
        if (username != null && !username.isEmpty()) {
            builder.token(username + ":" + password);
        }

        MilvusClientV2 client = new MilvusClientV2(builder.build());
        log.info("Milvus 客户端初始化成功");
        return client;
    }

    /**
     * 初始化 Collection
     */
    @PostConstruct
    public void initCollections() {
        if (collections == null || collections.isEmpty()) {
            log.warn("未配置 Milvus Collection，跳过初始化");
            return;
        }

        // 延迟初始化，等待 Bean 创建完成
        // 实际的 Collection 创建在 MilvusInitializer 中执行
        log.info("Milvus Collection 配置加载完成，共 {} 个 Collection", collections.size());
    }

    /**
     * 创建 hr_knowledge Collection Schema
     * 用于存储知识库向量
     */
    public CreateCollectionReq buildKnowledgeCollectionSchema(String collectionName, int dimension) {
        // 定义字段
        List<CreateCollectionReq.FieldSchema> fields = new ArrayList<>();

        // 主键
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name("id")
                .dataType(DataType.Int64)
                .isPrimaryKey(true)
                .autoID(true)
                .build());

        // 租户 ID (用于多租户隔离)
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name("tenant_id")
                .dataType(DataType.VarChar)
                .maxLength(64)
                .build());

        // 文档 ID
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name("doc_id")
                .dataType(DataType.VarChar)
                .maxLength(64)
                .build());

        // 分块 ID
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name("chunk_id")
                .dataType(DataType.VarChar)
                .maxLength(64)
                .build());

        // 文本内容
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name("content")
                .dataType(DataType.VarChar)
                .maxLength(8192)
                .build());

        // 元数据 (JSON)
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name("metadata")
                .dataType(DataType.JSON)
                .build());

        // 向量字段
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name("embedding")
                .dataType(DataType.FloatVector)
                .dimension(dimension)
                .build());

        // 创建时间
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name("created_at")
                .dataType(DataType.Int64)
                .build());

        // 定义 Schema
        CreateCollectionReq.CollectionSchema schema = CreateCollectionReq.CollectionSchema.builder()
                .fieldSchemaList(fields)
                .build();

        // 定义索引
        List<IndexParam> indexes = new ArrayList<>();
        indexes.add(IndexParam.builder()
                .fieldName("embedding")
                .indexType(IndexParam.IndexType.IVF_FLAT)
                .metricType(IndexParam.MetricType.COSINE)
                .extraParams(Map.of("nlist", 1024))
                .build());

        // tenant_id 索引 (用于过滤)
        indexes.add(IndexParam.builder()
                .fieldName("tenant_id")
                .indexType(IndexParam.IndexType.TRIE)
                .build());

        return CreateCollectionReq.builder()
                .collectionName(collectionName)
                .collectionSchema(schema)
                .indexParams(indexes)
                .description("HR 知识库向量存储")
                .build();
    }

    /**
     * 创建 user_memory Collection Schema
     * 用于存储用户长期记忆向量
     */
    public CreateCollectionReq buildMemoryCollectionSchema(String collectionName, int dimension) {
        // 定义字段
        List<CreateCollectionReq.FieldSchema> fields = new ArrayList<>();

        // 主键
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name("id")
                .dataType(DataType.Int64)
                .isPrimaryKey(true)
                .autoID(true)
                .build());

        // 租户 ID
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name("tenant_id")
                .dataType(DataType.VarChar)
                .maxLength(64)
                .build());

        // 用户 ID
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name("user_id")
                .dataType(DataType.VarChar)
                .maxLength(64)
                .build());

        // 会话 ID
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name("session_id")
                .dataType(DataType.VarChar)
                .maxLength(64)
                .build());

        // 记忆摘要
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name("summary")
                .dataType(DataType.VarChar)
                .maxLength(4096)
                .build());

        // 记忆类型 (short_term / long_term)
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name("memory_type")
                .dataType(DataType.VarChar)
                .maxLength(32)
                .build());

        // 元数据 (JSON)
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name("metadata")
                .dataType(DataType.JSON)
                .build());

        // 向量字段
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name("embedding")
                .dataType(DataType.FloatVector)
                .dimension(dimension)
                .build());

        // 创建时间
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name("created_at")
                .dataType(DataType.Int64)
                .build());

        // 定义 Schema
        CreateCollectionReq.CollectionSchema schema = CreateCollectionReq.CollectionSchema.builder()
                .fieldSchemaList(fields)
                .build();

        // 定义索引
        List<IndexParam> indexes = new ArrayList<>();
        indexes.add(IndexParam.builder()
                .fieldName("embedding")
                .indexType(IndexParam.IndexType.IVF_FLAT)
                .metricType(IndexParam.MetricType.COSINE)
                .extraParams(Map.of("nlist", 1024))
                .build());

        // tenant_id + user_id 复合过滤索引
        indexes.add(IndexParam.builder()
                .fieldName("tenant_id")
                .indexType(IndexParam.IndexType.TRIE)
                .build());

        indexes.add(IndexParam.builder()
                .fieldName("user_id")
                .indexType(IndexParam.IndexType.TRIE)
                .build());

        return CreateCollectionReq.builder()
                .collectionName(collectionName)
                .collectionSchema(schema)
                .indexParams(indexes)
                .description("用户长期记忆向量存储")
                .build();
    }
}
