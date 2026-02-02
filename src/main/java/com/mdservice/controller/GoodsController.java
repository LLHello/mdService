package com.mdservice.controller;

import com.mdservice.domain.dto.GoodsDTO;
import com.mdservice.entity.Goods;
import com.mdservice.service.inter.GoodsService;
import com.mdservice.utils.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Resource
    private GoodsService goodsService;
/*
* 获取商品分类列表
* */
    @GetMapping("/categoryList")
    public Result categoryList(){
        return goodsService.getCategoryList();
    }
    @GetMapping("/categoryId/{id}")
    public Result categoryId(@PathVariable Long id){
        return goodsService.getCategoryGoods(id);
    }
    @GetMapping("/merchantGoods/{id}")
    public Result merchantGoods(@PathVariable Long id){
        return goodsService.getMerchantGoods(id);
    }
    @PutMapping("/update")
    public Result update(@Validated @RequestPart("goods") GoodsDTO goods,//商品字段信息
                         @RequestPart(value = "deleteImagePaths", required = false) List<String> deleteImagePaths,//要删除的图片路径，格式：/upload/demo.png
                         @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,//新上传的图片
                         String oldImagePath
                         ){
        // /upload/3f4374e9-c83a-45fd-a554-2a13f135cd3a.png,/upload/94a8cd3b-9450-4b6c-a860-6f71e96d6db7.png,/upload/f823c442-c845-4c72-b0d8-771be4933385.jpg
        log.info("oldImagePath:{}",oldImagePath);
        String[] split = oldImagePath.split(",");
        Integer num = split.length;
        log.info("oldImagePath包含的图片个数:{}",num);
        if(!CollectionUtils.isEmpty(newImages)) log.info("newImages图片个数:{}",newImages.size());
        //先避免空指针，再判断
        if(!CollectionUtils.isEmpty(newImages) && newImages.size()+num > 5) return Result.error("最多上传五张图片");
        return goodsService.updatePics(goods, deleteImagePaths, newImages, oldImagePath);
    }
    @GetMapping("/good/{id}")
    public Result getGood(@PathVariable Long id){
        return goodsService.getGood(id);
    }
}
