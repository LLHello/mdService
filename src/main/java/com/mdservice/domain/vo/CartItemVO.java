package com.mdservice.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartItemVO {
    private Long id;
    private Long goodsId;
    private Long skuId;
    private Integer quantity;
    private Byte checked;
    private String title;
    private String pic;
    private BigDecimal price;
    private Integer stock;
    private String skuAttrs;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

