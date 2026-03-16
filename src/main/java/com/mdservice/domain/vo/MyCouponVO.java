package com.mdservice.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 我的优惠券 VO —— 包含领取记录 ID，供下单时传入 couponClaimId 使用
 */
@Data
public class MyCouponVO {
    /** 领取记录 ID（下单时传此值作为 couponClaimId）*/
    private Long claimId;
    /** 优惠券 ID */
    private Long id;
    private String name;
    /** null = 全场券，非 null = 指定商品专属券 */
    private Long goodsId;
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
