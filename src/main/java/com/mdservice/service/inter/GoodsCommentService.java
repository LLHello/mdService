package com.mdservice.service.inter;

import com.mdservice.domain.dto.GoodsCommentDTO;
import com.mdservice.utils.Result;

public interface GoodsCommentService {

    /** 发布评论（用户） */
    Result addComment(GoodsCommentDTO dto);

    /** 获取商品评论列表（树形：一级+二级） */
    Result listComments(Long goodsId);

    /** 商家回复评论 */
    Result merchantReply(GoodsCommentDTO dto);

    /** 商家查看某商品的全部评论 */
    Result listCommentsForMerchant(Long goodsId);

    /** 统计评论数和平均分 */
    Result commentStat(Long goodsId);
}
