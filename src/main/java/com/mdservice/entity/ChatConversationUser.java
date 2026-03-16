package com.mdservice.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatConversationUser {
    private Long id;
    private Long conversationId;
    private Long userId;
    private Long peerId;
    private Integer unreadCount;
    private Long lastReadMessageId;
    private LocalDateTime lastReadTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
