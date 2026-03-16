package com.mdservice.service.impl;

import com.mdservice.entity.ChatConversation;
import com.mdservice.entity.ChatConversationUser;
import com.mdservice.entity.ChatMessage;
import com.mdservice.mapper.ChatConversationMapper;
import com.mdservice.mapper.ChatConversationUserMapper;
import com.mdservice.mapper.ChatMessageMapper;
import com.mdservice.service.inter.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatConversationMapper chatConversationMapper;
    @Autowired
    private ChatConversationUserMapper chatConversationUserMapper;
    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Override
    @Transactional
    public ChatConversation getOrCreateConversation(Long userAId, Long userBId) {
        if (userAId == null || userBId == null) {
            throw new IllegalArgumentException("userId不能为空");
        }
        if (userAId.equals(userBId)) {
            throw new IllegalArgumentException("不支持与自己创建会话");
        }
        Long user1 = Math.min(userAId, userBId);
        Long user2 = Math.max(userAId, userBId);

        ChatConversation existing = chatConversationMapper.selectByUsers(user1, user2);
        if (existing != null) {
            ensureConversationUsers(existing.getId(), userAId, userBId);
            ensureConversationUsers(existing.getId(), userBId, userAId);
            return existing;
        }

        ChatConversation created = new ChatConversation();
        created.setUser1Id(user1);
        created.setUser2Id(user2);
        try {
            chatConversationMapper.insert(created);
        } catch (Exception e) {
            ChatConversation after = chatConversationMapper.selectByUsers(user1, user2);
            if (after == null) {
                throw e;
            }
            ensureConversationUsers(after.getId(), userAId, userBId);
            ensureConversationUsers(after.getId(), userBId, userAId);
            return after;
        }
        ensureConversationUsers(created.getId(), userAId, userBId);
        ensureConversationUsers(created.getId(), userBId, userAId);
        return created;
    }

    @Override
    @Transactional
    public ChatMessage createMessage(Long senderId,
                                     Long receiverId,
                                     Byte msgType,
                                     String content,
                                     Long orderId,
                                     Long goodsId,
                                     String clientMsgId) {
        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException("senderId/receiverId不能为空");
        }
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("不支持发送给自己");
        }
        if (ObjectUtils.isEmpty(content)) {
            throw new IllegalArgumentException("content不能为空");
        }

        ChatConversation conversation = getOrCreateConversation(senderId, receiverId);

        ChatMessage msg = new ChatMessage();
        msg.setConversationId(conversation.getId());
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setMsgType(msgType == null ? (byte) 0 : msgType);
        msg.setContent(content);
        msg.setOrderId(orderId);
        msg.setGoodsId(goodsId);
        msg.setClientMsgId(clientMsgId);
        msg.setStatus((byte) 0);
        msg.setCreateTime(LocalDateTime.now());
        chatMessageMapper.insert(msg);

        chatConversationMapper.updateLastMessage(conversation.getId(), msg.getId(), LocalDateTime.now());
        chatConversationUserMapper.incrementUnread(conversation.getId(), receiverId, 1);
        return msg;
    }

    @Override
    public List<ChatMessage> listUndelivered(Long receiverId, Integer limit) {
        int finalLimit = limit == null || limit <= 0 ? 200 : Math.min(limit, 500);
        return chatMessageMapper.selectUndeliveredByReceiver(receiverId, finalLimit);
    }

    @Override
    public void markDelivered(Long messageId) {
        if (messageId == null) {
            return;
        }
        chatMessageMapper.markDelivered(messageId);
    }

    @Override
    @Transactional
    public void markRead(Long conversationId, Long receiverId, Long lastReadMessageId) {
        if (conversationId == null || receiverId == null || lastReadMessageId == null) {
            return;
        }
        chatMessageMapper.markReadUpTo(conversationId, receiverId, lastReadMessageId);
        chatConversationUserMapper.resetUnread(conversationId, receiverId, lastReadMessageId);
    }

    @Override
    public List<ChatMessage> history(Long conversationId, Long beforeId, Integer limit, Integer daysAgo) {
        int finalLimit = limit == null || limit <= 0 ? 20 : Math.min(limit, 500);
        return chatMessageMapper.selectHistory(conversationId, beforeId, finalLimit, daysAgo);
    }

    private void ensureConversationUsers(Long conversationId, Long userId, Long peerId) {
        ChatConversationUser row = chatConversationUserMapper.selectByConversationAndUser(conversationId, userId);
        if (row != null) {
            return;
        }
        ChatConversationUser inserted = new ChatConversationUser();
        inserted.setConversationId(conversationId);
        inserted.setUserId(userId);
        inserted.setPeerId(peerId);
        inserted.setUnreadCount(0);
        chatConversationUserMapper.insert(inserted);
    }
}
