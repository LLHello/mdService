package com.mdservice.controller;

import com.mdservice.aop.Log;
import com.mdservice.service.inter.OrderService;
import com.mdservice.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Log(module = "订单", operation = "创建订单", desc = "从购物车创建订单")
    @PostMapping("/createFromCart")
    public Result createFromCart(@RequestParam(required = false) Long couponClaimId) {
        return orderService.createFromCart(couponClaimId);
    }

    @Log(module = "订单", operation = "订单列表", desc = "用户查看订单列表")
    @GetMapping("/list")
    public Result list(@RequestParam(required = false) Byte status) {
        return orderService.list(status);
    }

    @Log(module = "订单", operation = "订单详情", desc = "用户查看订单详情")
    @GetMapping("/{id}")
    public Result detail(@PathVariable("id") Long id) {
        return orderService.detail(id);
    }

    @Log(module = "订单", operation = "订单支付", desc = "用户支付订单")
    @PostMapping("/pay/{id}")
    public Result pay(@PathVariable("id") Long id) {
        return orderService.pay(id);
    }

    @Log(module = "订单", operation = "订单退款", desc = "用户退款已支付订单")
    @PostMapping("/refund/{id}")
    public Result refund(@PathVariable("id") Long id) {
        return orderService.refund(id);
    }

    @Log(module = "订单", operation = "取消订单", desc = "用户取消待支付订单")
    @PostMapping("/cancel/{id}")
    public Result cancel(@PathVariable("id") Long id) {
        return orderService.cancel(id);
    }
}
