package com.mdservice.controller;

import com.mdservice.service.KnowledgeBaseService;
import com.mdservice.service.inter.CustomerServiceAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@Slf4j
@Profile("!test")

public class AiController {
    @Autowired
    private CustomerServiceAgent customerServiceAgent;
    @Autowired
    private KnowledgeBaseService knowledgeBaseService;
    @PostMapping("/ask")
    public String ask(String message) {
        log.info("用户询问信息：{}", message);
        // 直接调用定义好的接口，复杂的 Tool 调用流程都在底层自动完成了
        String chat = customerServiceAgent.chat(message);
        if (chat.contains("```json") && chat.contains("getProductInfo")) {
            log.error("拦截到 AI 暴露工具调用：{}", chat);
            // 直接给用户一个友好的报错：
            return "亲，查询系统开小差了，您可以换个说法再问我一次哦~";
        }
        log.info("回答信息：{}", chat);
        return chat;
    }
    // 1. 先录入知识库 (模拟管理员上传了网站的退货规则)
    @PostMapping("/import")
    public String importData(@RequestParam(required = true) String message) {
        //if(message == null) message = "【双十一退换货规则】本店支持7天无理由退货。如果商品已拆封影响二次销售，则不支持退货。内衣、生鲜等特殊商品不适用7天无理由退货。退货运费由买家承担，如果是商品质量问题运费由卖家承担。";
        knowledgeBaseService.importKnowledge(message);
        return "知识库导入成功";
    }
}
