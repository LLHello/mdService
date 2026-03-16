package com.mdservice.controller;

import com.mdservice.domain.dto.GoodsCommentDTO;
import com.mdservice.service.inter.GoodsCommentService;
import com.mdservice.utils.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/comment")
public class GoodsCommentController {

    @Resource
    private GoodsCommentService commentService;

    /**
     * 获取商品评论列表（公开，无需登录）
     * GET /comment/list/{goodsId}
     */
    @GetMapping("/list/{goodsId}")
    public Result list(@PathVariable Long goodsId) {
        return commentService.listComments(goodsId);
    }

    /**
     * 获取评论统计信息（公开）
     * GET /comment/stat/{goodsId}
     */
    @GetMapping("/stat/{goodsId}")
    public Result stat(@PathVariable Long goodsId) {
        return commentService.commentStat(goodsId);
    }

    /**
     * 用户发布评论（需登录）
     * POST /comment/add
     */
    @PostMapping("/add")
    public Result add( @RequestBody GoodsCommentDTO dto) {
        return commentService.addComment(dto);
    }

    /**
     * 商家回复评论（需登录）
     * POST /comment/merchantReply
     */
    @PostMapping("/merchantReply")
    public Result merchantReply(@RequestBody GoodsCommentDTO dto) {
        return commentService.merchantReply(dto);
    }

    /**
     * 商家查看某商品全部评论（需登录）
     * GET /comment/merchantList/{goodsId}
     */
    @GetMapping("/merchantList/{goodsId}")
    public Result merchantList(@PathVariable Long goodsId) {
        return commentService.listCommentsForMerchant(goodsId);
    }
}
