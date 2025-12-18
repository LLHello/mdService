package com.mdservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    private Long id;
    private String name;
    private Byte isShow;//管理员管理是否展示
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
