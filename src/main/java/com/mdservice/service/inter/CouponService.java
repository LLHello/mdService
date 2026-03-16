package com.mdservice.service.inter;

import com.mdservice.entity.Coupon;
import com.mdservice.utils.Result;

public interface CouponService {
    Result create(Coupon coupon);

    Result list();

    Result preheat(Long couponId);

    Result seckill(Long couponId);

    /** 查询与某商品相关的可用优惠券（全场券 + 该商品专属券） */
    Result listByGoods(Long goodsId);

    /** 查询当前登录用户已领取且未使用的优惠券 */
    Result myCoupons();
}

