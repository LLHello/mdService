package com.mdservice.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Sku {
    private Long skuId;
    private Long goodsId;
    private BigDecimal price;
    private BigDecimal marketPrice;
    private BigDecimal costPrice;
    private Integer stock;
    private Integer lockStock;
    private String skuAttrs;
    private Byte status;
    private Integer saleCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
