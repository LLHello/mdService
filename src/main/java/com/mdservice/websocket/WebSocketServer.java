package com.mdservice.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdservice.domain.vo.ChatMerchantOrderItemVO;
import com.mdservice.entity.ChatMessage;
import com.mdservice.entity.OfflineMessage;
import com.mdservice.entity.SystemNotice;
import com.mdservice.mapper.OrderMapper;
import com.mdservice.mapper.OfflineMessageMapper;
import com.mdservice.mapper.SystemNoticeMapper;
import com.mdservice.service.inter.ChatService;
import com.mdservice.utils.JwtUtil;
import com.mdservice.utils.SpringContextUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@ServerEndpoint("/ws/{userId}")
public class WebSocketServer {
    //记录当前在线连接数
    private static AtomicInteger onlineCount = new AtomicInteger(0);
    //存放每个客户端对应的WebSocket对象
    private static ConcurrentHashMap<String, WebSocketServer> webSocketMap = new ConcurrentHashMap<>();
    //与某个客户端的连接会话，给客户端发送数据
    private Session session;
    //userId
    private String userId;
    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        this.session = session;
        this.userId = userId;
        if (!checkUserIdToken(session, userId)) {
            return;
        }
        if (webSocketMap.containsKey(userId)) {
            webSocketMap.remove(userId);
            webSocketMap.put(userId, this);
            // 加入 set 中
        } else {
            webSocketMap.put(userId, this);
            // 在线数加1
            addOnlineCount();
        }
        log.info("用户连接:" + userId + ",当前在线人数为:" + getOnlineCount());

        try {
            sendMessage("连接成功");
            // 1. 检查离线私信
            checkAndSendOfflineMessages();

            // 2. 检查未读系统公告 (新增逻辑)
            checkAndSendSystemNotices();

            // 3. 检查离线聊天消息
            checkAndSendChatMessages();
        } catch (IOException e) {
            log.error("用户:" + userId + ",网络异常!!!!!!");
        }
    }
    /**
     * 检查并发送未读系统公告
     */
    private void checkAndSendSystemNotices() {
        SystemNoticeMapper noticeMapper = SpringContextUtil.getBean(SystemNoticeMapper.class);

        // 1. 获取用户上次读到的位置
        Long lastReadId = noticeMapper.getLastReadNoticeId(this.userId);
        if (lastReadId == null) {
            lastReadId = 0L; // 从来没读过
        }

        // 2. 查询所有比这个 ID 大的公告
        List<SystemNotice> notices = noticeMapper.selectUnreadNotices(lastReadId);

        if (notices != null && !notices.isEmpty()) {
            Long maxId = lastReadId;

            for (SystemNotice notice : notices) {
                try {
                    sendMessage("[离线公告] " + notice.getContent());
                    if (notice.getId() > maxId) {
                        maxId = notice.getId();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // 3. 更新读取进度到最新一条
            noticeMapper.updateLastReadId(this.userId, maxId);
        }
    }
    /**
     * 检查并发送离线消息
     */
    private void checkAndSendOfflineMessages() {
        // 1. 手动获取 Mapper Bean
        OfflineMessageMapper msgMapper = SpringContextUtil.getBean(OfflineMessageMapper.class);

        // 2. 查询该用户的未读离线消息
        List<OfflineMessage> unreadList = msgMapper.selectUnreadByUserId(this.userId);

        if (unreadList != null && !unreadList.isEmpty()) {
            log.info("发现用户 {} 有 {} 条离线消息，准备推送...", userId, unreadList.size());

            for (OfflineMessage msg : unreadList) {
                try {
                    // 3. 推送消息给客户端
                    sendMessage("[离线消息] " + msg.getContent());

                    // 4. 标记为已读
                    msgMapper.markAsRead(msg.getId());

                } catch (IOException e) {
                    log.error("离线消息推送失败: {}", e.getMessage());
                }
            }
        }
    }

    private void checkAndSendChatMessages() {
        ChatService chatService = SpringContextUtil.getBean(ChatService.class);
        Long receiverId = Long.parseLong(this.userId);
        List<ChatMessage> undelivered = chatService.listUndelivered(receiverId, 200);
        if (undelivered == null || undelivered.isEmpty()) {
            return;
        }
        for (ChatMessage msg : undelivered) {
            try {
                sendJson("chat.message", msg);
                chatService.markDelivered(msg.getId());
            } catch (IOException e) {
                log.error("聊天离线消息推送失败: {}", e.getMessage());
                return;
            }
        }
    }
    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        if (webSocketMap.containsKey(userId)) {
            webSocketMap.remove(userId);
            // 从set中删除
            subOnlineCount();
        }
        log.info("用户退出:" + userId + ",当前在线人数为:" + getOnlineCount());
    }
    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("用户消息:" + userId + ",报文:" + message);
        ObjectMapper objectMapper = SpringContextUtil.getBean(ObjectMapper.class);
        JsonNode root;
        try {
            root = objectMapper.readTree(message);
        } catch (Exception e) {
            return;
        }

        String type = root.path("type").asText(null);
        if (type == null) {
            return;
        }
        Long currentUserId;
        try {
            currentUserId = Long.parseLong(this.userId);
        } catch (Exception e) {
            return;
        }

        if ("chat.send".equals(type)) {
            Long toUserId = readLong(root, "toUserId");
            String content = root.path("content").asText(null);
            Byte msgType = root.hasNonNull("msgType") ? (byte) root.path("msgType").asInt() : (byte) 0;
            Long orderId = readLong(root, "orderId");
            Long goodsId = readLong(root, "goodsId");
            String clientMsgId = root.path("clientMsgId").asText(null);

            if (toUserId == null || content == null) {
                return;
            }

            ChatService chatService = SpringContextUtil.getBean(ChatService.class);
            ChatMessage saved;
            try {
                saved = chatService.createMessage(currentUserId, toUserId, msgType, content, orderId, goodsId, clientMsgId);
            } catch (Exception e) {
                return;
            }

            try {
                Map<String, Object> ack = new HashMap<>();
                ack.put("clientMsgId", clientMsgId);
                ack.put("messageId", saved.getId());
                ack.put("conversationId", saved.getConversationId());
                sendJson("chat.ack", ack);
            } catch (IOException e) {
                return;
            }

            WebSocketServer receiverServer = webSocketMap.get(String.valueOf(toUserId));
            if (receiverServer != null) {
                try {
                    receiverServer.sendJson("chat.message", saved);
                    chatService.markDelivered(saved.getId());
                } catch (IOException e) {
                    log.error("聊天消息实时推送失败: {}", e.getMessage());
                }
            }
            return;
        }

        if ("chat.read".equals(type)) {
            Long conversationId = readLong(root, "conversationId");
            Long peerId = readLong(root, "peerId");
            Long lastReadMessageId = readLong(root, "lastReadMessageId");
            if (lastReadMessageId == null) {
                return;
            }
            ChatService chatService = SpringContextUtil.getBean(ChatService.class);
            if (conversationId == null) {
                if (peerId == null) {
                    return;
                }
                conversationId = chatService.getOrCreateConversation(currentUserId, peerId).getId();
            }
            chatService.markRead(conversationId, currentUserId, lastReadMessageId);
            try {
                Map<String, Object> ack = new HashMap<>();
                ack.put("conversationId", conversationId);
                ack.put("lastReadMessageId", lastReadMessageId);
                sendJson("chat.read.ack", ack);
            } catch (IOException e) {
                return;
            }
            return;
        }

        if ("chat.history".equals(type)) {
            Long conversationId = readLong(root, "conversationId");
            Long peerId = readLong(root, "peerId");
            Long beforeId = readLong(root, "beforeId");
            Integer limit = root.hasNonNull("limit") ? root.path("limit").asInt() : 20;
            Integer daysAgo = root.hasNonNull("daysAgo") ? root.path("daysAgo").asInt() : 15;

            ChatService chatService = SpringContextUtil.getBean(ChatService.class);
            if (conversationId == null) {
                if (peerId == null) {
                    return;
                }
                conversationId = chatService.getOrCreateConversation(currentUserId, peerId).getId();
            }
            List<ChatMessage> history = chatService.history(conversationId, beforeId, limit, daysAgo);
            List<ChatMessage> result = history == null ? Collections.emptyList() : new ArrayList<>(history);
            Collections.reverse(result);
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("conversationId", conversationId);
                payload.put("messages", result);
                sendJson("chat.history", payload);
            } catch (IOException e) {
                return;
            }
            return;
        }

        if ("chat.orderItems".equals(type)) {
            Long userId = readLong(root, "userId");
            Long merchantId = readLong(root, "merchantId");
            Long peerId = readLong(root, "peerId");
            if (userId == null && merchantId == null) {
                return;
            }
            if (userId == null) {
                userId = currentUserId;
            }
            if (merchantId == null && peerId != null) {
                merchantId = peerId;
            }
            if (merchantId == null) {
                return;
            }
            if (!currentUserId.equals(userId) && !currentUserId.equals(merchantId)) {
                return;
            }
            OrderMapper orderMapper = SpringContextUtil.getBean(OrderMapper.class);
            List<ChatMerchantOrderItemVO> items = orderMapper.selectOrderItemsByUserAndMerchant(userId, merchantId);
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("userId", userId);
                payload.put("merchantId", merchantId);
                payload.put("items", items);
                sendJson("chat.orderItems", payload);
            } catch (IOException e) {
                return;
            }
            return;
        }
    }
    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("用户错误:" + this.userId + ",原因:" + error.getMessage());
        error.printStackTrace();
    }
    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws IOException {
        Session s = this.session;
        if (s == null || !s.isOpen()) {
            throw new IOException("WebSocket session has been closed");
        }
        synchronized (s) {
            if (!s.isOpen()) {
                throw new IOException("WebSocket session has been closed");
            }
            try {
                s.getBasicRemote().sendText(message);
            } catch (IllegalStateException e) {
                throw new IOException(e.getMessage(), e);
            }
        }
    }

    private void sendJson(String type, Object data) throws IOException {
        ObjectMapper objectMapper = SpringContextUtil.getBean(ObjectMapper.class);
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", type);
        payload.put("data", data);
        sendMessage(objectMapper.writeValueAsString(payload));
    }

    private boolean checkUserIdToken(Session session, String pathUserId) {
        try {
            Map<String, List<String>> params = session.getRequestParameterMap();
            List<String> tokens = params == null ? null : params.get("token");
            if (tokens == null || tokens.isEmpty()) {
                return true;
            }
            String token = tokens.get(0);
            if (token == null || token.isEmpty()) {
                return true;
            }
            JwtUtil jwtUtil = SpringContextUtil.getBean(JwtUtil.class);
            String tokenUserId = String.valueOf(jwtUtil.parseToken(token).get("userId"));
            if (!pathUserId.equals(tokenUserId)) {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "userId mismatch"));
                return false;
            }
            return true;
        } catch (Exception e) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "invalid token"));
            } catch (Exception ignored) {
            }
            return false;
        }
    }

    private Long readLong(JsonNode root, String field) {
        if (root == null || field == null) {
            return null;
        }
        JsonNode node = root.get(field);
        if (node == null || node.isNull()) {
            return null;
        }
        try {
            long v = node.asLong();
            return v == 0 ? null : v;
        } catch (Exception e) {
            return null;
        }
    }
    /**
     * 发送自定义消息 (群发或点对点)
     */
    public static void sendInfo(String message, @PathParam("userId") String userId) throws IOException {
        log.info("发送消息 to:" + userId + "，内容:" + message);
        if (userId != null && webSocketMap.containsKey(userId)) {
            webSocketMap.get(userId).sendMessage(message);
        } else {
            // 2. 不在线 -> 存库 (离线消息)
            log.info("用户 {} 不在线，存入离线消息库", userId);
            try {
                // 手动获取 Mapper Bean
                OfflineMessageMapper msgMapper = SpringContextUtil.getBean(OfflineMessageMapper.class);

                OfflineMessage offlineMsg = new OfflineMessage();
                offlineMsg.setUserId(Long.parseLong(userId));
                offlineMsg.setContent(message);

                // 插入数据库
                msgMapper.insert(offlineMsg);
                log.info("写入离线消息成功!");
            } catch (Exception e) {
                log.error("保存离线消息失败: {}", e.getMessage());
            }
        }
    }
    // 群发所有人
    public static void sendAll(String message) {
        log.info("开始群发消息: {}", message);

        // 1. 先保存到系统公告表 (这是离线推送的核心)
        SystemNotice notice = new SystemNotice();
        notice.setContent(message);

        SystemNoticeMapper noticeMapper = SpringContextUtil.getBean(SystemNoticeMapper.class);
        noticeMapper.insert(notice); // 插入后 notice.getId() 有值

        Long newNoticeId = notice.getId();

        // 2. 推送给所有【在线】用户
        for (String userId : webSocketMap.keySet()) {
            try {
                WebSocketServer server = webSocketMap.get(userId);
                server.sendMessage("[系统公告] " + message);

                // 3. 顺便更新在线用户的读取进度 (避免他下次上线又拉取一遍)
                noticeMapper.updateLastReadId(userId, newNoticeId);

            } catch (IOException e) {
                log.error("群发给用户 {} 失败", userId);
            }
        }
    }
    public static synchronized int getOnlineCount() {
        return onlineCount.get();
    }

    public static synchronized void addOnlineCount() {
        WebSocketServer.onlineCount.getAndIncrement();
    }

    public static synchronized void subOnlineCount() {
        WebSocketServer.onlineCount.getAndDecrement();
    }
}
