package com.mdservice.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GoodsCommentDTO {
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;

    private Long parentId;       // 为null表示一级评论

    private Long replyToUserId;  // 被回复用户ID（二级评论时使用）

    @NotBlank(message = "评论内容不能为空")
    private String content;

    private Integer rating;      // 仅一级评论需要，1-5，服务层校验
}
