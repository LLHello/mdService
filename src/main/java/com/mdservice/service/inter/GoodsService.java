package com.mdservice.service.inter;

import com.mdservice.domain.dto.GoodsDTO;
import com.mdservice.entity.Goods;
import com.mdservice.utils.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GoodsService {

    Result getCategoryList();

    Result getCategoryGoods(Long id);

    Result getMerchantGoods(Long id);

    Result updatePics(GoodsDTO goods, List<String> deleteImagePaths, List<MultipartFile> newImages, String oldImagePath);

    Result getGood(Long id);

    Result getTop10();

    Result getGoodsClickCount(Long goodId);

    Result addGood(GoodsDTO goodsDTO, List<MultipartFile> images);

    Result modifyStatus(Long goodsId, Integer status);

    Result categoryIdAdmin(Long id);
    List<Goods> findByName(String name);
}
