package com.mdservice.mapper;

import com.mdservice.domain.dto.GoodsDTO;
import com.mdservice.domain.vo.GoodsVO;
import com.mdservice.entity.Category;
import com.mdservice.entity.Goods;
import com.mdservice.entity.GoodsImage;
import com.mdservice.utils.Result;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface GoodsMapper {

    List<Category> getCategoryList();

    List<Goods> getCategoryGoods(Long id);

    List<Goods> getMerchantGoods(Long id);

    Boolean update(GoodsDTO goods);

    Goods getGood(Long id);

    void updatePicStr(Long id, String picStr);

    List<GoodsVO> getGoods(Set<String> top10);

    void updateClickCount(@Param("params") Map<Long, Long> map);

    Long addGood(Goods goods);

    Boolean modifyStatus(Long goodsId, Integer status);

    List<Goods> categoryIdAdmin(Long id);

    List<Goods> findByName(String name);

    List<Goods> getAllGoods();
}
