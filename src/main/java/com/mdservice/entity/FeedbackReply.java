package com.mdservice.entity;

import lombok.Data;

@Data
public class FeedbackReply {
    private Long id;
    private String content;
    private Long feedbackId;
}
