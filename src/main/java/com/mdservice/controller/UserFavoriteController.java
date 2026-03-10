package com.mdservice.controller;

import com.mdservice.aop.Log;
import com.mdservice.entity.UserFavorite;
import com.mdservice.service.inter.UserFavoriteService;
import com.mdservice.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/favorite")
public class UserFavoriteController {
    @Autowired
    private UserFavoriteService userFavoriteService;
    //用户收藏
    @Log(module = "收藏", operation = "新增操作", desc = "用户新增一个收藏")
    @PostMapping("/star")
    public Result star(UserFavorite userFavorite) {
        return userFavoriteService.star(userFavorite);
    }
    //用户查看自己的收藏
    @Log(module = "收藏", operation = "查询操作", desc = "用户查询自己的收藏")
    @GetMapping("/getStar")
    public Result getStar(Long userId) {
        return userFavoriteService.getStar(userId);
    }
    //用户取消一个收藏
    @Log(module = "收藏", operation = "删除操作", desc = "用户删除一个收藏")
    @PutMapping("/unStar")
    public Result unStar(Long id) {//收藏主键id
        return userFavoriteService.unStar(id);
    }
    //商家查看自己的店铺粉丝
    @Log(module = "收藏", operation = "查询操作", desc = "商家查询自己的粉丝")
    @GetMapping("/getFans")
    public Result getFans(Long merchantId) {
        return userFavoriteService.getFans(merchantId);
    }
}
