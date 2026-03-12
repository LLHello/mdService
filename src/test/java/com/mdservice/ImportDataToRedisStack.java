package com.mdservice;

import com.mdservice.config.WebSocketConfig;
import com.mdservice.entity.Goods;
import com.mdservice.mapper.GoodsMapper;
import com.mdservice.service.KnowledgeBaseService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test") // 激活test环境，让@Profile("!test")的bean不加载
@Slf4j
public class ImportDataToRedisStack {
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    @Autowired
    private GoodsMapper goodsMapper;
    @Test
    public void importDataToRedis() {
        List<Goods> goodsList = goodsMapper.getAllGoods();
        for (Goods goods : goodsList) {
            String string = goods.getId()+ "," + goods.getTitle()+ "," + goods.getPrice() + "," + goods.getDes();
            log.info(string);
            knowledgeBaseService.importKnowledge(string);
        }
    }
}
