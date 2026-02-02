package com.mdservice.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsVO {
    private Long id;
    private Long merchantId;//商家id
    private Long categoryId;//分类id
    private String pic;//商品图片
    private String title;//商品名称
    private String price;//商品价格
    private Byte isShow;//是否展示：商家管理
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Byte status;//管理员管理商品
    private String des;//对商品的描述
}
