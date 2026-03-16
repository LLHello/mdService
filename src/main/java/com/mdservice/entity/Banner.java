package com.mdservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Banner {
    private Long id;
    private Long goodsId;
    private Integer sort;
    private Integer isActive;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
