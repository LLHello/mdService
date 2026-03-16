package com.mdservice.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class Aiconfig {
    @Value("${custom.vector-db.redis.host}")
    private String redisHost;

    @Value("${custom.vector-db.redis.port}")
    private int redisPort;

    @Value("${custom.vector-db.redis.index-name}")
    private String indexName;

    @Value("${custom.vector-db.redis.dimension}")
    private int dimension;
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                // 【核心防御】：只保留最近 6 条消息 (即 3 轮对话：一问一答算两条)
                // 超出的旧消息会被自动丢弃，防止上下文污染
                .maxMessages(6)
                .build();
    }
//    注册 Redis 向量数据库
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return RedisEmbeddingStore.builder()
                .host(redisHost)
                .port(redisPort)
                .indexName(indexName)
                .dimension(dimension)
                .build();
    }
    // 注册 RAG 检索器
    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore,
                                             EmbeddingModel embeddingModel) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(3)
                .minScore(0.6)
                .build();
    }
}
