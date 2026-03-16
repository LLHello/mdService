package com.mdservice.domain.vo;

import com.mdservice.entity.GoodsComment;
import lombok.Data;

import java.util.List;

@Data
public class GoodsCommentVO {
    private Long id;
    private Long goodsId;
    private Long userId;
    private Long parentId;
    private Long replyToUserId;
    private String content;
    private Integer rating;
    private String createTime;

    private String username;
    private String userIcon;
    private String replyToUsername;

    private List<GoodsCommentVO> replies;
}
