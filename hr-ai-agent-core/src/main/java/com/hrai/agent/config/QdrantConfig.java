package com.hrai.agent.config;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Qdrant 向量数据库配置
 *
 * @author HR AI Team
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "qdrant")
public class QdrantConfig {

    /**
     * Qdrant 服务地址
     */
    private String host = "localhost";

    /**
     * Qdrant 端口
     */
    private Integer port = 6334;

    /**
     * API Key (可选)
     */
    private String apiKey;

    /**
     * 是否使用TLS
     */
    private Boolean useTls = false;

    @Bean
    public QdrantClient qdrantClient() {
        System.out.println("初始化 Qdrant 客户端: " + host + ":" + port);

        QdrantGrpcClient.Builder builder = QdrantGrpcClient.newBuilder(host, port, useTls);

        // 如果配置了 API Key
        if (apiKey != null && !apiKey.isEmpty()) {
            builder.withApiKey(apiKey);
        }

        QdrantClient client = new QdrantClient(builder.build());

        System.out.println("Qdrant 客户端初始化成功");
        return client;
    }
}
