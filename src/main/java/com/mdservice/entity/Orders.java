package com.mdservice.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Orders {
    private Long id;
    private String orderNo;
    private Long userId;
    private Byte status;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Long couponClaimId;     // 使用的优惠券领取记录ID
    private BigDecimal discountAmount; // 优惠抵扣金额
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime updateTime;
}

