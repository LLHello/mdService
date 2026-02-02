package com.mdservice.service.impl;

import com.mdservice.domain.dto.GoodsDTO;
import com.mdservice.entity.Category;
import com.mdservice.entity.Goods;
import com.mdservice.entity.GoodsImage;
import com.mdservice.mapper.GoodsMapper;
import com.mdservice.service.inter.GoodsService;
import com.mdservice.utils.FileUploadUtil;
import com.mdservice.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private FileUploadUtil fileUploadUtil;

    @Override
    public Result getCategoryList() {
        List<Category> categoryList =goodsMapper.getCategoryList();
        ArrayList<Category> collect = categoryList.stream().filter(item -> item.getIsShow() == 1).collect(Collectors.toCollection(ArrayList::new));
        return Result.success(collect);
    }

    @Override
    public Result getCategoryGoods(Long id) {
        List<Goods> goods = goodsMapper.getCategoryGoods(id);
        log.info("goodsList: {}", goods);
        // 过滤能展示的商品
        ArrayList<Goods> collectGoods = goods.stream().filter(item -> item.getIsShow() == 1 && item.getStatus() == 1).collect(Collectors.toCollection(ArrayList::new));
        return Result.success(collectGoods);
    }

    @Override
    public Result getMerchantGoods(Long id) {
        List<Goods> goods = goodsMapper.getMerchantGoods(id);
        return Result.success(goods);
    }

    @Override
    public Result updatePics(GoodsDTO goods, List<String> deleteImagePaths, List<MultipartFile> newImages, String oldImagePath) {
        log.info("goods: {}, deleteImagePaths: {}, newImages: {}", goods, deleteImagePaths, newImages);
        log.info("oldImagePath: {}", oldImagePath);
        // 1. 更新商品基本信息
        goods.setUpdateTime(LocalDateTime.now());
        boolean updateGoodsFlag = goodsMapper.update(goods);
        if (!updateGoodsFlag) {
            return Result.error("操作失败！");
        }
        Long goodsId = goods.getId();
        log.info("goodsId: {}", goodsId);

        // 2. 处理需要删除的图片（接收文件路径列表）
        if (!CollectionUtils.isEmpty(deleteImagePaths)) {
            // 2.1 先删除本地文件（调用自定义deleteFile方法）
            for (String imagePath : deleteImagePaths) {
                log.info("deleteImagePath: {}", imagePath);
                fileUploadUtil.deleteFile(imagePath);
            }
            // 2.2 删除数据库中对应路径的图片记录
//            goodsMapper.deleteByImagePaths(deleteImagePaths);
        }
        StringBuilder sb = new StringBuilder();
        if(!ObjectUtils.isEmpty(oldImagePath)){
            sb.append(oldImagePath);
            if(!CollectionUtils.isEmpty(newImages)) sb.append(",");
        }
        // 3. 处理新上传的图片

        if (!CollectionUtils.isEmpty(newImages)) {
            for (int i = 0 ; i < newImages.size() ; i++) {
                MultipartFile file = newImages.get(i);
                try {
                    //图片已经存在，直接拼接到字符串中
                    String relativePath = file.getOriginalFilename();
                    if(!fileUploadUtil.isImageExists(relativePath)) {
                        // 3.1 上传文件，获取相对路径（如：/upload/xxx.png，或者不合法时代表是新上传的图片）
                        relativePath = fileUploadUtil.uploadFile(file);
                        log.info("新上传图片：{}", relativePath);
                        sb.append(relativePath);
                        if(i != newImages.size() - 1) {
                            sb.append(",");
                        }
                        continue;
                    }
                    // 图片已经存在，直接拼接
                    log.info("图片已存在！{}", relativePath);
                    sb.append(relativePath);
                    if(i != newImages.size() - 1) {
                        sb.append(",");
                    }
                } catch (Exception e) {
                    throw new RuntimeException("图片上传失败：" + e.getMessage());
                }
            }
        }
        String picStr = sb.toString();
        log.info("id: {}, picStr: {}", goodsId, picStr);
        goodsMapper.updatePicStr(goodsId, picStr);
        return Result.success();
    }

    @Override
    public Result getGood(Long id) {
        Goods good = goodsMapper.getGood(id);
        log.info("good: {}", good);
        return Result.success(good);
    }

}
