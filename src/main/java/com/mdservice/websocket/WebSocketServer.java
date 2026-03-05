package com.mdservice.websocket;

import com.mdservice.entity.OfflineMessage;
import com.mdservice.entity.SystemNotice;
import com.mdservice.mapper.OfflineMessageMapper;
import com.mdservice.mapper.SystemNoticeMapper;
import com.mdservice.utils.SpringContextUtil;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
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
        this.session.getBasicRemote().sendText(message);
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
