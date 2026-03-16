package com.mdservice.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Coupon {
    private Long id;
    private String name;
    private Long goodsId;          // null=全场券，非null=指定商品券
    private BigDecimal discountAmount;
    private BigDecimal thresholdAmount;
    private Integer totalStock;
    private Integer availableStock;
    private Byte status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

