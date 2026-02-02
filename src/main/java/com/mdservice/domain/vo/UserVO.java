package com.mdservice.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserVO {
    private Long id;
    private String account;
    private Byte gender;//0，女；1，男
    private String username;
    private Byte role;//0,普通用户；1，商家；2，管理员
    private Byte isShow;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String icon;
    private String des;
    private String token;
}
