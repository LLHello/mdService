package com.mdservice.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatConversationVO {
    private Long conversationId;
    private Long peerId;
    private String peerName;
    private String peerIcon;
    private Integer unreadCount;
    private Long lastMessageId;
    private String lastMessageContent;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime lastMessageTime;
}
