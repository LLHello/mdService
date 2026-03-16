package com.mdservice.mapper;

import com.mdservice.domain.vo.BannerVO;
import com.mdservice.entity.Banner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BannerMapper {

    /** 查询所有启用的轮播图（含商品信息） */
    List<BannerVO> selectActiveBanners();

    /** 查询全部轮播图（管理员用，含商品信息） */
    List<BannerVO> selectAllBanners();

    /** 统计当前轮播图数量 */
    int countAll();

    /** 新增轮播图 */
    int insert(Banner banner);

    /** 删除轮播图 */
    int deleteById(@Param("id") Long id);

    /** 通过 goodsId 检查是否已存在 */
    int countByGoodsId(@Param("goodsId") Long goodsId);

    /** 更新排序 */
    int updateSort(@Param("id") Long id, @Param("sort") Integer sort);
}
