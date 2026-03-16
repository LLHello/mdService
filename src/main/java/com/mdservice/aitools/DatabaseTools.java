package com.mdservice.aitools;

import com.mdservice.entity.Goods;
import com.mdservice.mapper.GoodsMapper;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DatabaseTools {
    @Autowired
    private GoodsMapper goodsMapper;
    @Tool("当用户询问商品价格、库存、商品信息时，调用此方法。参数传入商品的名称")
    public String getProductInfo(String productName) {
        log.info(">>> AI 正在调用查商品工具，参数：" + productName);
        List<Goods> goods = goodsMapper.findByName(productName);
        if(goods.isEmpty()) return "数据库中未找到相关的商品信息。";
        // 将数据库结果转为字符串，供 AI 阅读
        StringBuilder sb = new StringBuilder("找到以下商品：\n");

        for (Goods good : goods) {
            String goodUrl = "http://localhost:5173/product/" + good.getId();
            sb.append(String.format("商品名：%s, 价格：%s元，链接：%s\n",
                    good.getTitle(), good.getPrice(), goodUrl));
        }
        String result = sb.toString();
        return result;
    }
}
