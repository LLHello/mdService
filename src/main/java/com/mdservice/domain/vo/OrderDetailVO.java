package com.mdservice.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDetailVO {
    private Long id;
    private String orderNo;
    private Byte status;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Long couponClaimId;
    private BigDecimal discountAmount;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime updateTime;
    private List<OrderItemVO> items;
}

