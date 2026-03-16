# 私聊 WebSocket 与离线消息

## 连接

- URL：`ws://localhost:8080/ws/{userId}?token={JWT}`
- `token` 为可选参数：如果带 token，会校验 token 内 userId 与路径 userId 必须一致，否则服务端断开连接。

## 服务端自动推送

- 连接成功后，服务端会自动补发该用户的未送达聊天消息，并将这些消息标记为已送达（status=1）。
- 仍保留原有离线消息（offline_message）与系统公告补发逻辑。

## 消息协议（JSON）

所有 JSON 消息使用统一 envelope：

```json
{
  "type": "xxx",
  "data": {}
}
```

### 发送消息：chat.send

客户端 -> 服务端：

```json
{
  "type": "chat.send",
  "toUserId": 2002,
  "content": "你好",
  "msgType": 0,
  "clientMsgId": "c-uuid-001",
  "orderId": 123,
  "goodsId": 456
}
```

服务端 -> 发送方（确认已落库）：

```json
{
  "type": "chat.ack",
  "data": {
    "clientMsgId": "c-uuid-001",
    "messageId": 90001,
    "conversationId": 3001
  }
}
```

服务端 -> 接收方（在线则实时推送；离线则等上线补发）：

```json
{
  "type": "chat.message",
  "data": {
    "id": 90001,
    "conversationId": 3001,
    "senderId": 1001,
    "receiverId": 2002,
    "msgType": 0,
    "content": "你好",
    "orderId": 123,
    "goodsId": 456,
    "clientMsgId": "c-uuid-001",
    "status": 0,
    "createTime": "2026-03-13T12:00:00"
  }
}
```

### 拉取历史：chat.history

客户端 -> 服务端（支持 conversationId 或 peerId 二选一）：

```json
{ "type": "chat.history", "peerId": 2002, "beforeId": 90001, "limit": 20 }
```

服务端 -> 客户端：

```json
{
  "type": "chat.history",
  "data": { "conversationId": 3001, "messages": [] }
}
```

### 已读回执：chat.read

客户端 -> 服务端（支持 conversationId 或 peerId 二选一）：

```json
{ "type": "chat.read", "conversationId": 3001, "lastReadMessageId": 90001 }
```

服务端 -> 客户端：

```json
{
  "type": "chat.read.ack",
  "data": { "conversationId": 3001, "lastReadMessageId": 90001 }
}
```

## “识别该商家下单商品”

### HTTP：/chat/orderItems

- GET `/chat/orderItems?userId={买家ID}&merchantId={商家ID}`
- 权限：仅允许当前登录用户为买家或该商家（否则返回“无权限”）

### WebSocket：chat.orderItems

客户端 -> 服务端（可显式传 userId/merchantId；也可用 peerId 辅助 merchantId）：

```json
{ "type": "chat.orderItems", "userId": 1001, "merchantId": 2002 }
```

服务端 -> 客户端：

```json
{
  "type": "chat.orderItems",
  "data": { "userId": 1001, "merchantId": 2002, "items": [] }
}
```

## 状态字段

- chat_message.status：0=已保存未送达，1=已送达，2=已读

## 数据库脚本

- `src/main/resources/sql/002_chat_tables.sql`
