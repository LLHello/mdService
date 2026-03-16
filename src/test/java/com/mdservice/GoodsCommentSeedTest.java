package com.mdservice;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class GoodsCommentSeedTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    public void createTableAndSeed() {
        // 1. 建表
        jdbc.execute("""
            CREATE TABLE IF NOT EXISTS `goods_comment` (
              `id`               BIGINT       NOT NULL AUTO_INCREMENT,
              `goods_id`         BIGINT       NOT NULL COMMENT '商品ID',
              `user_id`          BIGINT       NOT NULL COMMENT '评论用户ID',
              `parent_id`        BIGINT       DEFAULT NULL COMMENT '父评论ID，NULL=一级评论',
              `reply_to_user_id` BIGINT       DEFAULT NULL COMMENT '被回复用户ID',
              `content`          VARCHAR(500) NOT NULL COMMENT '评论内容',
              `rating`           TINYINT      DEFAULT NULL COMMENT '评分1-5，仅一级评论',
              `status`           TINYINT      NOT NULL DEFAULT 0 COMMENT '0正常 1屏蔽',
              `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
              PRIMARY KEY (`id`),
              KEY `idx_goods_id`  (`goods_id`),
              KEY `idx_user_id`   (`user_id`),
              KEY `idx_parent_id` (`parent_id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
              COMMENT='商品评论表';
        """);
        log.info("goods_comment 表已就绪");

        // 2. 查询现有商品与用户
        List<Map<String, Object>> goods = jdbc.queryForList(
                "SELECT id FROM goods ORDER BY id ASC LIMIT 5");
        List<Map<String, Object>> users = jdbc.queryForList(
                "SELECT id FROM `user` WHERE role = 0 ORDER BY id ASC LIMIT 10");
        List<Map<String, Object>> merchants = jdbc.queryForList(
                "SELECT id FROM `user` WHERE role = 1 ORDER BY id ASC LIMIT 3");

        if (goods.isEmpty() || users.isEmpty()) {
            log.warn("没有商品或用户数据，跳过种子");
            return;
        }

        // 3. 为每件商品插入样例评论
        String[][] reviewContents = {
            {"质量非常好，物超所值！快递很快，包装完整。",         "5"},
            {"外观漂亮，用起来也顺手，推荐购买！",              "5"},
            {"性价比很高，朋友也买了一个，大家都很满意。",        "4"},
            {"收到实物和图片一致，很喜欢，下次还会来这家店。",     "5"},
            {"总体还不错，就是物流慢了点，商品本身没问题。",      "4"},
            {"用了一周了，感觉很好用，质感不错。",              "4"},
            {"有点小瑕疵，但整体可以接受，客服态度很好。",       "3"},
            {"非常满意，已经是第二次购买了，依然好评！",         "5"},
        };

        String[] replyContents = {
            "感谢您的支持，有任何问题随时联系我们！",
            "非常感谢您的好评，期待您下次光临~",
            "感谢惠顾，如有任何问题欢迎联系客服！",
        };

        String[] userReplies = {
            "确实，我也觉得很划算！",
            "同意楼上，用着挺舒服的。",
            "我也买了，感觉一样好~",
        };

        int commentCount = 0;
        for (Map<String, Object> good : goods) {
            long goodsId = ((Number) good.get("id")).longValue();

            for (int i = 0; i < reviewContents.length && i < users.size(); i++) {
                long userId = ((Number) users.get(i).get("id")).longValue();
                String content = reviewContents[i][0];
                int rating = Integer.parseInt(reviewContents[i][1]);

                // 插入一级评论
                jdbc.update(
                    "INSERT INTO goods_comment (goods_id, user_id, parent_id, reply_to_user_id, content, rating, status, create_time) "
                  + "VALUES (?, ?, NULL, NULL, ?, ?, 0, DATE_SUB(NOW(), INTERVAL ? HOUR))",
                    goodsId, userId, content, rating, (i + 1) * 3
                );

                Long parentId = jdbc.queryForObject(
                    "SELECT id FROM goods_comment WHERE goods_id=? AND user_id=? ORDER BY id DESC LIMIT 1",
                    Long.class, goodsId, userId
                );
                commentCount++;

                if (parentId == null) continue;

                // 商家回复部分评论
                if (!merchants.isEmpty() && i % 3 == 0) {
                    long merchantId = ((Number) merchants.get(0).get("id")).longValue();
                    String reply = replyContents[i % replyContents.length];
                    jdbc.update(
                        "INSERT INTO goods_comment (goods_id, user_id, parent_id, reply_to_user_id, content, rating, status, create_time) "
                      + "VALUES (?, ?, ?, ?, ?, NULL, 0, DATE_SUB(NOW(), INTERVAL ? HOUR))",
                        goodsId, merchantId, parentId, userId, reply, i + 1
                    );
                    commentCount++;
                }

                // 其他用户追加二级评论
                if (i % 2 == 1 && users.size() > i + 1) {
                    long replyUserId = ((Number) users.get((i + 1) % users.size()).get("id")).longValue();
                    String userReply = userReplies[i % userReplies.length];
                    jdbc.update(
                        "INSERT INTO goods_comment (goods_id, user_id, parent_id, reply_to_user_id, content, rating, status, create_time) "
                      + "VALUES (?, ?, ?, ?, ?, NULL, 0, DATE_SUB(NOW(), INTERVAL ? MINUTE))",
                        goodsId, replyUserId, parentId, userId, userReply, (i + 1) * 30
                    );
                    commentCount++;
                }
            }
        }
        log.info("种子完成：共插入 {} 条评论数据", commentCount);
    }
}
