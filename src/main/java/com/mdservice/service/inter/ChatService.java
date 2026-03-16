package com.mdservice.service.inter;

import com.mdservice.entity.ChatConversation;
import com.mdservice.entity.ChatMessage;

import java.util.List;

public interface ChatService {
    ChatConversation getOrCreateConversation(Long userAId, Long userBId);

    ChatMessage createMessage(Long senderId,
                              Long receiverId,
                              Byte msgType,
                              String content,
                              Long orderId,
                              Long goodsId,
                              String clientMsgId);

    List<ChatMessage> listUndelivered(Long receiverId, Integer limit);

    void markDelivered(Long messageId);

    void markRead(Long conversationId, Long receiverId, Long lastReadMessageId);

    List<ChatMessage> history(Long conversationId, Long beforeId, Integer limit, Integer daysAgo);
}
