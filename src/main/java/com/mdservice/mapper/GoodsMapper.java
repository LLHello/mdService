package com.mdservice.mapper;

import com.mdservice.domain.dto.GoodsDTO;
import com.mdservice.domain.vo.GoodsVO;
import com.mdservice.entity.Category;
import com.mdservice.entity.Goods;
import com.mdservice.entity.GoodsImage;
import com.mdservice.entity.Sku;
import com.mdservice.utils.Result;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
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

    List<Goods> searchGoods(@Param("keyword") String keyword,
                            @Param("minPrice") BigDecimal minPrice,
                            @Param("maxPrice") BigDecimal maxPrice,
                            @Param("skuAttrs") List<String> skuAttrs,
                            @Param("sort") String sort);

    List<Sku> selectSkusByGoodsId(Long goodsId);

    int insertSku(Sku sku);

    int updateSku(Sku sku);

    int deductSkuStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    int addSkuStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    int deleteSkusByGoodsId(Long goodsId);

    int deleteSkusByGoodsIdAndNotIn(@Param("goodsId") Long goodsId, @Param("skuIds") List<Long> skuIds);

    Sku selectSkuBySkuId(@Param("skuId") Long skuId);
}
