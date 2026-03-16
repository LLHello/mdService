package com.mdservice.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MerchantPayAmountVO {
    private Long merchantId;
    private BigDecimal amount;
}
