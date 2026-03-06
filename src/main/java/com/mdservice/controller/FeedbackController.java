package com.mdservice.controller;

import com.mdservice.aop.Log;
import com.mdservice.entity.Feedback;
import com.mdservice.service.inter.FeedbackService;
import com.mdservice.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {
    @Autowired
    private FeedbackService feedbackService;
    //用户反馈
    @Log(module = "反馈", operation = "用户反馈", desc = "用户反馈一条信息")
    @PostMapping("push")
    public Result push(Feedback feedback, List<MultipartFile> files) {
        feedback.setStatus((byte)0);
        return feedbackService.push(feedback, files);
    }
    //管理员获取反馈数据
    @Log(module = "反馈", operation = "获取反馈", desc = "管理员获取所有反馈信息")
    @GetMapping("pull")
    public Result pull(@RequestParam(defaultValue = "0") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize) {
        return feedbackService.pull(pageNum, pageSize);
    }
    //用户查看自己的反馈
    @Log(module = "反馈", operation = "用户获取反馈", desc = "用户查看自己的反馈")
    @GetMapping("/getFeedback/{userId}")
    public Result getFeedback(@PathVariable Long userId) {
        return feedbackService.getFeedback(userId);
    }
    //管理员查看反馈时得到从feedback中获得id，根据此id与feedback_reply中的feedback_id进行比较查看此反馈的回复
    @Log(module = "反馈", operation = "管理员查看反馈", desc = "管理员查看反馈的回复")
    @GetMapping("/getReply/{replyId}")
    public Result getReply(@PathVariable Long replyId) {
        return feedbackService.getReply(replyId);
    }
//    管理员回复反馈
    @Log(module = "反馈", operation = "回复反馈", desc = "管理员回复一条反馈")
    @PostMapping("/replyFeedback")
    public Result replyFeedback(Long feedbackId, String content) {
        return feedbackService.replyFeedback(feedbackId, content);
    }
    //用户查看反馈时得到从feedback中获得id，根据此id与feedback_reply中的feedback_id进行比较查看此反馈的回复
    @GetMapping("/checkReply")
    public Result checkReply(Long feedbackId) {
        return feedbackService.checkReply(feedbackId);
    }
}
