package com.mdservice.entity;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class Feedback {
    private Long id;//主键
    private String content;//反馈内容
    private String userId;//反馈的人
    private LocalDateTime createTime;//反馈时间
    private String pic;//反馈附带的图片
    private Byte status;//0,未回复（默认）;1，已回复;2,用户已查看回复
    private String title;//反馈的标题
}
