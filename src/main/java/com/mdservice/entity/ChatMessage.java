package com.mdservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private Long receiverId;
    private Byte msgType;
    private String content;
    private Long orderId;
    private Long goodsId;
    private String clientMsgId;
    private Byte status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime deliverTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime readTime;
}
