package com.mdservice.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BannerVO {
    /** banner 记录主键 */
    private Long id;
    /** 关联商品ID */
    private Long goodsId;
    /** 商品名称 */
    private String title;
    /** 商品第一张图片路径 */
    private String pic;
    /** 排序 */
    private Integer sort;
    /** 是否启用 */
    private Integer isActive;
}
