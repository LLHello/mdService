package com.mdservice.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatConversation {
    private Long id;
    private Long user1Id;
    private Long user2Id;
    private Long lastMessageId;
    private LocalDateTime lastMessageTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
