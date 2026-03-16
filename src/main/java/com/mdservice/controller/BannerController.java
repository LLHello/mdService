package com.mdservice.controller;

import com.mdservice.service.inter.BannerService;
import com.mdservice.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/banner")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    /**
     * 公开接口：获取首页轮播图（前台调用）
     */
    @GetMapping("/active")
    public Result listActive() {
        return bannerService.listActive();
    }

    /**
     * 管理员：获取全部轮播图列表
     */
    @GetMapping("/list")
    public Result listAll() {
        return bannerService.listAll();
    }

    /**
     * 管理员：添加轮播图
     * @param goodsId 商品ID
     * @param sort    排序（可选，默认追加到末尾）
     */
    @PostMapping("/add")
    public Result add(@RequestParam Long goodsId,
                      @RequestParam(required = false) Integer sort) {
        return bannerService.add(goodsId, sort);
    }

    /**
     * 管理员：删除轮播图
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        return bannerService.delete(id);
    }

    /**
     * 管理员：更新排序
     */
    @PutMapping("/sort")
    public Result updateSort(@RequestParam Long id,
                             @RequestParam Integer sort) {
        return bannerService.updateSort(id, sort);
    }
}
