package com.mdservice.entity;

import lombok.Data;

import java.util.Date;

@Data
public class OfflineMessage {
    private Long id;
    private Long userId;
    private String content;
    private Integer status; // 0:未读, 1:已读
    private Date createTime;
}
