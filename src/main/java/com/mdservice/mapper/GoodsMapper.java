package com.mdservice.mapper;

import com.mdservice.domain.dto.GoodsDTO;
import com.mdservice.entity.Category;
import com.mdservice.entity.Goods;
import com.mdservice.entity.GoodsImage;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GoodsMapper {

    List<Category> getCategoryList();

    List<Goods> getCategoryGoods(Long id);

    List<Goods> getMerchantGoods(Long id);

    Boolean update(GoodsDTO goods);

    Goods getGood(Long id);

    void updatePicStr(Long id, String picStr);
}
