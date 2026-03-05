package com.mdservice.controller;

import com.mdservice.entity.SystemNotice;
import com.mdservice.service.inter.NoticeService;
import com.mdservice.utils.Result;
import com.mdservice.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notice")
public class NoticeController {
    @Autowired
    private NoticeService noticeService;
    /**
     * 推送消息给指定用户
     * http://localhost:8080/notice/push/1001?msg=消息
     */
    @GetMapping("/push/{userId}")
    public String pushToUser(@PathVariable String userId, String msg) {
        try {
            WebSocketServer.sendInfo(msg, userId);
        } catch (Exception e) {
            e.printStackTrace();
            return "推送失败";
        }
        return "推送成功";
    }
    /**
     * 推送给所有人
     */
    @GetMapping("/pushAll")
    public String pushAll(String msg) {
        WebSocketServer.sendAll(msg);
        return "群发成功";
    }
//    删除系统公告信息
    @DeleteMapping("/delete/{id}")
    public Result deleteNotice(@PathVariable String id) {
        return noticeService.deleteSystemNotice(id);
    }
//    修改系统公告信息
    @PutMapping("/modify")
    public Result modifySystemNotice(SystemNotice systemNotice) {
        return noticeService.modifySystemNotice(systemNotice);
    }
//    获取所有系统公告信息
    @GetMapping("/getALl")
    public Result getALlSystemNotice() {
        return noticeService.getAllSystemNotice();
    }
//    获取系统发给当前用户的所有消息
    @GetMapping("/getAllSystemToUser/{userId}")
    public Result getAllSystemToUser(@PathVariable String userId) {
        return noticeService.getAllSystemToUser(userId);
    }
}
