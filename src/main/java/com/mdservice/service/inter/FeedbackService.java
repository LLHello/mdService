package com.mdservice.service.inter;

import com.mdservice.entity.Feedback;
import com.mdservice.utils.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FeedbackService {
    Result push(Feedback feedback, List<MultipartFile> files);

    Result pull(Integer pageNum, Integer pageSize);

    Result getFeedback(Long userId);

    Result getReply(Long replyId);

    Result replyFeedback(Long feedbackId, String content);

    Result checkReply(Long feedbackId);
}
