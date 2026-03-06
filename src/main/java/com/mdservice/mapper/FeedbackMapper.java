package com.mdservice.mapper;

import com.mdservice.entity.Feedback;
import com.mdservice.entity.FeedbackReply;
import com.mdservice.entity.OfflineMessage;
import com.mdservice.utils.Result;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FeedbackMapper {
    Boolean push(Feedback feedback);

    List<Feedback> pull();

    List<Feedback> getFeeback(Long userId);

    FeedbackReply getReply(Long replyId);

    Boolean replyFeedback(Long feedbackId, String content);

    Boolean setStatus(Long id, Byte status);

    FeedbackReply checkReply(Long feedbackId);
}
