package com.mdservice.controller;

import com.mdservice.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/goods")
public class GoodsController {
/*
* 获取商品分类列表
* */
    @GetMapping("/categoryList")
    public Result categoryList(){

        return null;
    }
}
