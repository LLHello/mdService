package com.mdservice.mapper;

import com.mdservice.domain.vo.MyCouponVO;
import com.mdservice.entity.Coupon;
import com.mdservice.entity.CouponClaim;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CouponMapper {
    int insertCoupon(Coupon coupon);

    int updateAvailableStock(@Param("id") Long id, @Param("delta") Integer delta);

    Coupon selectById(@Param("id") Long id);

    List<Coupon> selectAll();

    int insertClaim(CouponClaim claim);

    int existsClaim(@Param("couponId") Long couponId, @Param("userId") Long userId);

    List<Long> selectClaimUserIds(@Param("couponId") Long couponId);

    /** 查询所有优惠券ID（用于启动时批量预热） */
    List<Long> selectAllIds();

    /** 按商品ID查询可用优惠券（status=1 且未过期） */
    List<Coupon> selectByGoodsId(@Param("goodsId") Long goodsId);

    /** 查询用户已领取且未使用的优惠券（关联 coupon 信息） */
    List<MyCouponVO> selectUnusedClaimsByUserId(@Param("userId") Long userId);

    /** 通过 claimId 查单条领取记录 */
    CouponClaim selectClaimById(@Param("claimId") Long claimId);

    /** 标记优惠券已使用 */
    int markClaimUsed(@Param("claimId") Long claimId, @Param("orderId") Long orderId);

    /** 归还优惠券（退款时） */
    int markClaimUnused(@Param("claimId") Long claimId);
}
