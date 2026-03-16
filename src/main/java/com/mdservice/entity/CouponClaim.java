package com.mdservice.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CouponClaim {
    private Long id;
    private Long couponId;
    private Long userId;
    private Byte status;
    private Long usedOrderId;       // 使用该优惠券的订单ID
    private LocalDateTime createTime;
}

