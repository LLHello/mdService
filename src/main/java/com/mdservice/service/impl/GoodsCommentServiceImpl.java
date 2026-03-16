package com.mdservice.service.impl;

import com.mdservice.domain.dto.GoodsCommentDTO;
import com.mdservice.domain.vo.GoodsCommentVO;
import com.mdservice.entity.GoodsComment;
import com.mdservice.mapper.GoodsCommentMapper;
import com.mdservice.service.inter.GoodsCommentService;
import com.mdservice.utils.Result;
import com.mdservice.utils.UserLocal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GoodsCommentServiceImpl implements GoodsCommentService {

    @Autowired
    private GoodsCommentMapper commentMapper;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Result addComment(GoodsCommentDTO dto) {
        String userIdStr = UserLocal.getUser();
        if (userIdStr == null) return Result.error("请先登录");
        long userId = Long.parseLong(userIdStr);

        if (dto.getParentId() != null) {
            dto.setRating(null);
        }
        if (dto.getParentId() == null && (dto.getRating() == null || dto.getRating() < 1 || dto.getRating() > 5)) {
            dto.setRating(5);
        }

        GoodsComment comment = new GoodsComment();
        comment.setGoodsId(dto.getGoodsId());
        comment.setUserId(userId);
        comment.setParentId(dto.getParentId());
        comment.setReplyToUserId(dto.getReplyToUserId());
        comment.setContent(dto.getContent());
        comment.setRating(dto.getRating() != null ? Byte.valueOf(dto.getRating().byteValue()) : null);

        int rows = commentMapper.insert(comment);
        if (rows < 1) return Result.error("评论失败");
        return Result.success(comment.getId());
    }

    @Override
    public Result listComments(Long goodsId) {
        List<GoodsComment> tops = commentMapper.selectTopLevelByGoodsId(goodsId);
        if (tops == null || tops.isEmpty()) return Result.success(new ArrayList<>());

        Map<Long, List<GoodsCommentVO>> replyMap = new HashMap<>();
        for (GoodsComment top : tops) {
            List<GoodsComment> replies = commentMapper.selectRepliesByParentId(top.getId());
            replyMap.put(top.getId(), replies.stream().map(this::toVO).collect(Collectors.toList()));
        }

        List<GoodsCommentVO> result = tops.stream().map(c -> {
            GoodsCommentVO vo = toVO(c);
            vo.setReplies(replyMap.getOrDefault(c.getId(), new ArrayList<>()));
            return vo;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @Override
    public Result merchantReply(GoodsCommentDTO dto) {
        String userIdStr = UserLocal.getUser();
        if (userIdStr == null) return Result.error("请先登录");
        long merchantUserId = Long.parseLong(userIdStr);

        if (dto.getParentId() == null) return Result.error("回复必须指定父评论ID");

        GoodsComment parent = commentMapper.selectById(dto.getParentId());
        if (parent == null) return Result.error("父评论不存在");
        if (!dto.getGoodsId().equals(parent.getGoodsId())) return Result.error("商品ID不匹配");

        GoodsComment reply = new GoodsComment();
        reply.setGoodsId(dto.getGoodsId());
        reply.setUserId(merchantUserId);
        reply.setParentId(dto.getParentId());
        reply.setReplyToUserId(dto.getReplyToUserId() != null ? dto.getReplyToUserId() : parent.getUserId());
        reply.setContent(dto.getContent());
        reply.setRating(null);

        int rows = commentMapper.insert(reply);
        if (rows < 1) return Result.error("回复失败");
        return Result.success(reply.getId());
    }

    @Override
    public Result listCommentsForMerchant(Long goodsId) {
        List<GoodsComment> tops = commentMapper.selectByGoodsIdForMerchant(goodsId);
        if (tops == null || tops.isEmpty()) return Result.success(new ArrayList<>());

        Map<Long, List<GoodsCommentVO>> replyMap = new HashMap<>();
        for (GoodsComment top : tops) {
            List<GoodsComment> replies = commentMapper.selectRepliesByParentId(top.getId());
            replyMap.put(top.getId(), replies.stream().map(this::toVO).collect(Collectors.toList()));
        }

        List<GoodsCommentVO> result = tops.stream().map(c -> {
            GoodsCommentVO vo = toVO(c);
            vo.setReplies(replyMap.getOrDefault(c.getId(), new ArrayList<>()));
            return vo;
        }).collect(Collectors.toList());

        return Result.success(result);
    }

    @Override
    public Result commentStat(Long goodsId) {
        int total = commentMapper.countByGoodsId(goodsId);
        Double avg = commentMapper.avgRatingByGoodsId(goodsId);
        Map<String, Object> stat = new HashMap<>();
        stat.put("total", total);
        stat.put("avgRating", avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        return Result.success(stat);
    }

    private GoodsCommentVO toVO(GoodsComment c) {
        GoodsCommentVO vo = new GoodsCommentVO();
        vo.setId(c.getId());
        vo.setGoodsId(c.getGoodsId());
        vo.setUserId(c.getUserId());
        vo.setParentId(c.getParentId());
        vo.setReplyToUserId(c.getReplyToUserId());
        vo.setContent(c.getContent());
        vo.setRating(c.getRating() != null ? (int) c.getRating() : null);
        vo.setCreateTime(c.getCreateTime() != null ? c.getCreateTime().format(FMT) : null);
        vo.setUsername(c.getUsername());
        vo.setUserIcon(c.getUserIcon());
        vo.setReplyToUsername(c.getReplyToUsername());
        return vo;
    }
}
