package com.mdservice.entity;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class UserFavorite {
    private Long id;//数据库主键id
    private Long userId;//收藏的user_id
    private Byte targetType;//收藏的类型：0,收藏的商家；1，收藏的商品
    private Long targetId;//收藏的商家/商品的id
    private LocalDateTime createTime;//收藏时间
}
