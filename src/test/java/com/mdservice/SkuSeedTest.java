package com.mdservice;

import com.mdservice.entity.Goods;
import com.mdservice.entity.Sku;
import com.mdservice.mapper.GoodsMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class SkuSeedTest {

    @Autowired
    private GoodsMapper goodsMapper;

    @Test
    public void seedSkus() {
        List<Goods> goodsList = goodsMapper.getAllGoods();
        log.info("goods count: {}", goodsList == null ? 0 : goodsList.size());
        if (goodsList == null || goodsList.isEmpty()) {
            return;
        }

        int seededGoods = 0;
        for (Goods goods : goodsList) {
            if (goods == null || goods.getId() == null) {
                continue;
            }
            List<Sku> existing = goodsMapper.selectSkusByGoodsId(goods.getId());
            if (existing != null && !existing.isEmpty()) {
                continue;
            }

            BigDecimal basePrice = parsePrice(goods.getPrice());
            Sku sku1 = newSku(goods.getId(), basePrice, "[]", 200);
            Sku sku2 = newSku(goods.getId(), basePrice.multiply(new BigDecimal("1.10")), "[{\"attr_name\":\"规格\",\"attr_value\":\"升级款\"}]", 120);
            goodsMapper.insertSku(sku1);
            goodsMapper.insertSku(sku2);

            seededGoods++;
            log.info("seeded skus for goodsId={}, title={}, skuIds=[{},{}]", goods.getId(), goods.getTitle(), sku1.getSkuId(), sku2.getSkuId());
        }
        log.info("seeded goods: {}", seededGoods);
    }

    private Sku newSku(Long goodsId, BigDecimal price, String skuAttrs, int stock) {
        Sku sku = new Sku();
        sku.setGoodsId(goodsId);
        sku.setPrice(price);
        sku.setMarketPrice(price.multiply(new BigDecimal("1.20")));
        sku.setCostPrice(price.multiply(new BigDecimal("0.60")));
        sku.setStock(stock);
        sku.setLockStock(0);
        sku.setSkuAttrs(skuAttrs);
        sku.setStatus((byte) 1);
        sku.setSaleCount(0);
        return sku;
    }

    private BigDecimal parsePrice(String price) {
        if (price == null || price.isBlank()) {
            return new BigDecimal("99.00");
        }
        try {
            return new BigDecimal(price.trim());
        } catch (Exception e) {
            return new BigDecimal("99.00");
        }
    }
}
