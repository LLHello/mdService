package com.mdservice.mapper;

import com.mdservice.domain.vo.ChatConversationVO;
import com.mdservice.entity.ChatConversationUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatConversationUserMapper {
    ChatConversationUser selectByConversationAndUser(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    int insert(ChatConversationUser row);

    int incrementUnread(@Param("conversationId") Long conversationId, @Param("userId") Long userId, @Param("delta") Integer delta);

    int resetUnread(@Param("conversationId") Long conversationId,
                    @Param("userId") Long userId,
                    @Param("lastReadMessageId") Long lastReadMessageId);

    List<ChatConversationUser> listByUserId(@Param("userId") Long userId);

    List<ChatConversationVO> listConversationVOByUserId(@Param("userId") Long userId);
}
