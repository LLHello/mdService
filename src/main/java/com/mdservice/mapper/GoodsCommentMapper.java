package com.mdservice.mapper;

import com.mdservice.entity.GoodsComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoodsCommentMapper {

    /** 插入评论 */
    int insert(GoodsComment comment);

    /** 查询商品的所有一级评论（含用户信息，不含子评论） */
    List<GoodsComment> selectTopLevelByGoodsId(@Param("goodsId") Long goodsId);

    /** 查询某条一级评论下的所有二级评论 */
    List<GoodsComment> selectRepliesByParentId(@Param("parentId") Long parentId);

    /** 查询单条评论 */
    GoodsComment selectById(@Param("id") Long id);

    /** 统计商品评论总数 */
    int countByGoodsId(@Param("goodsId") Long goodsId);

    /** 统计商品平均评分（仅一级评论） */
    Double avgRatingByGoodsId(@Param("goodsId") Long goodsId);

    /** 屏蔽/恢复评论（管理员） */
    int updateStatus(@Param("id") Long id, @Param("status") Byte status);

    /** 商家查看某商品的所有一级评论（含二级） */
    List<GoodsComment> selectByGoodsIdForMerchant(@Param("goodsId") Long goodsId);
}
