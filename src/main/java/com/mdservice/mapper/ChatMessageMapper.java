package com.mdservice.mapper;

import com.mdservice.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatMessageMapper {
    int insert(ChatMessage message);

    List<ChatMessage> selectUndeliveredByReceiver(@Param("receiverId") Long receiverId, @Param("limit") Integer limit);

    int markDelivered(@Param("id") Long id);

    int markReadUpTo(@Param("conversationId") Long conversationId,
                     @Param("receiverId") Long receiverId,
                     @Param("lastReadMessageId") Long lastReadMessageId);

    List<ChatMessage> selectHistory(@Param("conversationId") Long conversationId,
                                    @Param("beforeId") Long beforeId,
                                    @Param("limit") Integer limit,
                                    @Param("daysAgo") Integer daysAgo);
}
