package com.mdservice.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemVO {
    private Long id;
    private Long goodsId;
    private Long skuId;
    private String title;
    private String pic;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalAmount;
}

