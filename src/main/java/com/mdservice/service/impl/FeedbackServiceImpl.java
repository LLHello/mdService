package com.mdservice.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mdservice.aop.Log;
import com.mdservice.entity.Feedback;
import com.mdservice.entity.FeedbackReply;
import com.mdservice.entity.OfflineMessage;
import com.mdservice.mapper.FeedbackMapper;
import com.mdservice.service.inter.FeedbackService;
import com.mdservice.utils.FileUploadUtil;
import com.mdservice.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class FeedbackServiceImpl implements FeedbackService {
    @Autowired
    private FeedbackMapper feedbackMapper;
    @Autowired
    private FileUploadUtil fileUploadUtil;
    //用户反馈

    @Override
    public Result push(Feedback feedback, List<MultipartFile> files) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0 ; i < files.size() ; i++) {
            MultipartFile file = files.get(i);
            try {
                String s = fileUploadUtil.uploadFile(file);
                sb.append(s);
                if(i != files.size() - 1) {
                    sb.append(",");
                }
            } catch (IOException e) {
                log.info("上传失败：{}", e.getMessage());
                throw new RuntimeException(e);
            }
        }
        feedback.setPic(sb.toString());
        Boolean b = feedbackMapper.push(feedback);
        if (!b) {
            return Result.error();
        }
        return Result.success();
    }
//管理员获取反馈数据

    @Override
    public Result pull(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Feedback> res = feedbackMapper.pull();
        return Result.success(PageInfo.of(res));
    }

    @Override
    public Result getFeedback(Long userId) {
        List<Feedback> res = feedbackMapper.getFeeback(userId);
        for (Feedback feedback : res) {
            log.info("反馈提交时间：{}",feedback.getCreateTime());
        }
        return Result.success(res);
    }

    @Override
    public Result getReply(Long replyId) {
        FeedbackReply res= feedbackMapper.getReply(replyId);
        return Result.success(res);
    }

    @Override
    public Result replyFeedback(Long feedbackId, String content) {
        Boolean b = feedbackMapper.replyFeedback(feedbackId, content);
        if (!b) {
            return Result.error();
        }
        return Result.success();
    }

    @Override
    public Result checkReply(Long feedbackId) {
        FeedbackReply res = feedbackMapper.checkReply(feedbackId);
        //
        Long id = res.getFeedbackId();
        Boolean b = feedbackMapper.setStatus(id, (byte)2);
        if (!b) {
            return Result.error("设置状态失败");
        }

        return Result.success(res);
    }
}
