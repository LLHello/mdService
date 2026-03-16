package com.mdservice.controller;

import com.mdservice.domain.vo.ChatConversationVO;
import com.mdservice.domain.vo.ChatMerchantOrderItemVO;
import com.mdservice.mapper.ChatConversationUserMapper;
import com.mdservice.mapper.OrderMapper;
import com.mdservice.service.inter.ChatService;
import com.mdservice.utils.Result;
import com.mdservice.utils.UserLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ChatConversationUserMapper chatConversationUserMapper;

    @Autowired
    private ChatService chatService;

    @GetMapping("/orderItems")
    public Result<List<ChatMerchantOrderItemVO>> orderItems(@RequestParam Long userId, @RequestParam Long merchantId) {
        String currentUserIdStr = UserLocal.getUser();
        if (ObjectUtils.isEmpty(currentUserIdStr)) {
            return Result.error("未登录");
        }
        Long currentUserId = Long.parseLong(currentUserIdStr);
        if (!currentUserId.equals(userId) && !currentUserId.equals(merchantId)) {
            return Result.error("无权限");
        }
        List<ChatMerchantOrderItemVO> items = orderMapper.selectOrderItemsByUserAndMerchant(userId, merchantId);
        return Result.success(items);
    }

    @GetMapping("/conversations")
    public Result<List<ChatConversationVO>> conversations() {
        String currentUserIdStr = UserLocal.getUser();
        if (ObjectUtils.isEmpty(currentUserIdStr)) {
            return Result.error("未登录");
        }
        Long currentUserId = Long.parseLong(currentUserIdStr);
        List<ChatConversationVO> list = chatConversationUserMapper.listConversationVOByUserId(currentUserId);
        return Result.success(list);
    }

    @PostMapping("/conversations/ensure")
    public Result<Long> ensureConversation(@RequestParam Long peerId) {
        String currentUserIdStr = UserLocal.getUser();
        if (ObjectUtils.isEmpty(currentUserIdStr)) {
            return Result.error("未登录");
        }
        if (peerId == null || peerId <= 0) {
            return Result.error("peerId不能为空");
        }
        Long currentUserId = Long.parseLong(currentUserIdStr);
        if (currentUserId.equals(peerId)) {
            return Result.error("不支持与自己创建会话");
        }
        Long conversationId = chatService.getOrCreateConversation(currentUserId, peerId).getId();
        return Result.success(conversationId);
    }
}
