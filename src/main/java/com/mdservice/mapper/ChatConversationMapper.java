package com.mdservice.mapper;

import com.mdservice.entity.ChatConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ChatConversationMapper {
    ChatConversation selectByUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    int insert(ChatConversation conversation);

    int updateLastMessage(@Param("id") Long id,
                          @Param("lastMessageId") Long lastMessageId,
                          @Param("lastMessageTime") java.time.LocalDateTime lastMessageTime);
}
