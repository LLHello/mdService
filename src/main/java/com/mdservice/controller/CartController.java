package com.mdservice.controller;

import com.mdservice.aop.Log;
import com.mdservice.service.inter.CartService;
import com.mdservice.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Log(module = "购物车", operation = "加入购物车", desc = "用户加入购物车")
    @PostMapping("/add")
    public Result add(Long skuId, Integer quantity) {
        return cartService.add(skuId, quantity);
    }

    @Log(module = "购物车", operation = "查看购物车", desc = "用户查看购物车列表")
    @GetMapping("/list")
    public Result list() {
        return cartService.list();
    }

    @Log(module = "购物车", operation = "修改数量", desc = "用户修改购物车数量")
    @PutMapping("/quantity")
    public Result updateQuantity(Long cartItemId, Integer quantity) {
        return cartService.updateQuantity(cartItemId, quantity);
    }

    @Log(module = "购物车", operation = "勾选商品", desc = "用户勾选/取消勾选购物车商品")
    @PutMapping("/checked")
    public Result updateChecked(Long cartItemId, Byte checked) {
        return cartService.updateChecked(cartItemId, checked);
    }

    @Log(module = "购物车", operation = "删除商品", desc = "用户删除购物车商品")
    @DeleteMapping("/{id}")
    public Result remove(@PathVariable("id") Long id) {
        return cartService.remove(id);
    }
}

