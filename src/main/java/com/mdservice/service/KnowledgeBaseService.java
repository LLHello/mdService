package com.mdservice.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KnowledgeBaseService {
    @Autowired
    private EmbeddingModel embeddingModel;
    @Autowired
    private EmbeddingStore<TextSegment> embeddingStore;
    /**
     * 将长文本存入 Redis 向量库
     */
    public void importKnowledge(String text) {
        Document document = Document.from(text);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(300, 30)) // 分片
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(document);
        log.info("文本已成功向量化并存入向量数据库！");
    }
    /**
     * 将一段文本/文章存入向量数据库
     * @param text 比如一篇很长的商品退换货规则
    public void saveToVectorDb(String text) {
        Document document = Document.from(text);

        // 创建一个摄入器 (Ingestor)
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                // 文本切片：把长文切成最多 300 字符的小段，段与段之间有 30 字符重叠(防止切断上下文)
                .documentSplitter(DocumentSplitters.recursive(300, 30))
                .embeddingModel(embeddingModel) // 用什么模型把文字变成数字
                .embeddingStore(embeddingStore) // 存到哪个数据库
                .build();

        // 执行存入操作
        ingestor.ingest(document);
        log.info("文本已成功向量化并存入向量数据库！");
    }*/
}
