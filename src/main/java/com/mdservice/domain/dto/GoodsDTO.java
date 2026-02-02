package com.mdservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsDTO {
    private Long id;
    private Long merchantId;//商家id
    private Long categoryId;//分类id
//    private List<MultipartFile> pic;//商品图片
    private String title;//商品名称
    private String price;//商品价格
    private Byte isShow;//是否展示：商家管理
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Byte status;//管理员管理商品
    private String des;//对商品的描述
}
