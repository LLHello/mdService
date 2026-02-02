package com.mdservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsImage {
    private Long id;

    /** 商品ID */
    private Long goodsId;

    /** 图片存储路径 */
    private String imagePath;

    /** 图片访问URL */
    private String imageUrl;
}
