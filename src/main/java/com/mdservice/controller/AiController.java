package com.mdservice.controller;

import com.mdservice.ai.CustomerServiceAgent;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@Slf4j

public class AiController {
    @Autowired
    private CustomerServiceAgent customerServiceAgent;
    @PostMapping("/ask")
    public String ask(String message) {
        log.info("用户询问信息：{}", message);
        // 直接调用定义好的接口，复杂的 Tool 调用流程都在底层自动完成了
        String reinforcedMessage = message +
                "\n\n(系统强制约束：不能返回json字符串，需要调用tool工具返回数据库中存在的数据。请严格基于工具查询的真实数据回答，绝不允许编造、猜测或提供未经证实的信息。若查无数据，请直接告知用户未查到。。)";
        String chat = customerServiceAgent.chat(reinforcedMessage);
        if (chat.contains("```json") && chat.contains("getProductInfo")) {
            log.error("拦截到 AI 暴露工具调用：{}", chat);
            // 直接给用户一个友好的报错：
            return "亲，查询系统开小差了，您可以换个说法再问我一次哦~";
        }
        log.info("回答信息：{}", chat);
        return chat;
    }
}
