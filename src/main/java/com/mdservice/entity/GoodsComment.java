package com.mdservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsComment {
    private Long id;
    private Long goodsId;         // 商品ID
    private Long userId;           // 评论用户ID
    private Long parentId;         // 父评论ID（为null则是一级评论）
    private Long replyToUserId;    // 被回复的用户ID（二级评论中标记@谁）
    private String content;        // 评论内容
    private Byte rating;           // 评分 1-5（仅一级评论有效）
    private Byte status;           // 0 正常；1 已屏蔽
    private LocalDateTime createTime;

    // 非数据库字段，查询时联表填充
    private String username;           // 评论人昵称
    private String userIcon;           // 评论人头像
    private String replyToUsername;    // 被回复人昵称
    private List<GoodsComment> replies; // 子评论（仅一级评论携带）
}
