package com.mdservice.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CartItem {
    private Long id;
    private Long userId;
    private Long goodsId;
    private Long skuId;
    private Integer quantity;
    private Byte checked;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

