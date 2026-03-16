package com.mdservice.controller;

import com.mdservice.aop.Log;
import com.mdservice.entity.Coupon;
import com.mdservice.service.inter.CouponService;
import com.mdservice.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/coupon")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @Log(module = "优惠券", operation = "创建优惠券", desc = "创建优惠券并预热到Redis")
    @PostMapping("/create")
    public Result create(@RequestBody Coupon coupon) {
        return couponService.create(coupon);
    }

    @Log(module = "优惠券", operation = "优惠券列表", desc = "获取优惠券列表")
    @GetMapping("/list")
    public Result list() {
        return couponService.list();
    }

    @Log(module = "优惠券", operation = "预热优惠券", desc = "将优惠券库存预热到Redis")
    @PostMapping("/preheat/{id}")
    public Result preheat(@PathVariable("id") Long id) {
        return couponService.preheat(id);
    }

    @Log(module = "优惠券", operation = "优惠券秒杀", desc = "用户秒杀领取优惠券")
    @PostMapping("/seckill/{id}")
    public Result seckill(@PathVariable("id") Long id) {
        return couponService.seckill(id);
    }

    @Log(module = "优惠券", operation = "我的优惠券", desc = "查询当前用户未使用的优惠券")
    @GetMapping("/my")
    public Result my() {
        return couponService.myCoupons();
    }

    @Log(module = "优惠券", operation = "商品优惠券", desc = "查询指定商品可用的优惠券")
    @GetMapping("/byGoods")
    public Result byGoods(@RequestParam Long goodsId) {
        return couponService.listByGoods(goodsId);
    }
}

