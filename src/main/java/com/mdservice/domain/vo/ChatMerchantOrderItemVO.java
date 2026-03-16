package com.mdservice.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ChatMerchantOrderItemVO {
    private Long orderId;
    private String orderNo;
    private Byte orderStatus;
    private LocalDateTime orderCreateTime;
    private LocalDateTime payTime;
    private Long goodsId;
    private Long skuId;
    private String title;
    private String pic;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalAmount;
}
