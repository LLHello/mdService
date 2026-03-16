package com.mdservice;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

/**
 * 执行数据库迁移：
 *   1. coupon 表添加 goods_id
 *   2. orders 表添加 coupon_claim_id / discount_amount
 *   3. coupon_claim 表添加 used_order_id
 *   4. coupon 表添加缺失的 goods_id 列（幂等）
 */
@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class MigrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void runMigration003() {
        // ---- coupon: add goods_id ----
        if (!columnExists("coupon", "goods_id")) {
            jdbcTemplate.execute(
                "ALTER TABLE `coupon` ADD COLUMN `goods_id` bigint NULL DEFAULT NULL COMMENT '绑定商品ID，null为全场券' AFTER `name`"
            );
            log.info("[Migration] coupon.goods_id added");
        } else {
            log.info("[Migration] coupon.goods_id already exists, skip");
        }

        // ---- orders: add coupon_claim_id ----
        if (!columnExists("orders", "coupon_claim_id")) {
            jdbcTemplate.execute(
                "ALTER TABLE `orders` ADD COLUMN `coupon_claim_id` bigint NULL DEFAULT NULL COMMENT '使用的优惠券领取记录ID' AFTER `pay_amount`"
            );
            log.info("[Migration] orders.coupon_claim_id added");
        } else {
            log.info("[Migration] orders.coupon_claim_id already exists, skip");
        }

        // ---- orders: add discount_amount ----
        if (!columnExists("orders", "discount_amount")) {
            jdbcTemplate.execute(
                "ALTER TABLE `orders` ADD COLUMN `discount_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠抵扣金额' AFTER `coupon_claim_id`"
            );
            log.info("[Migration] orders.discount_amount added");
        } else {
            log.info("[Migration] orders.discount_amount already exists, skip");
        }

        // ---- coupon_claim: add used_order_id ----
        if (!columnExists("coupon_claim", "used_order_id")) {
            jdbcTemplate.execute(
                "ALTER TABLE `coupon_claim` ADD COLUMN `used_order_id` bigint NULL DEFAULT NULL COMMENT '使用该优惠券的订单ID' AFTER `status`"
            );
            log.info("[Migration] coupon_claim.used_order_id added");
        } else {
            log.info("[Migration] coupon_claim.used_order_id already exists, skip");
        }

        log.info("[Migration] migration-003 complete");
    }

    private boolean columnExists(String table, String column) {
        try {
            List<Map<String, Object>> cols = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                table, column
            );
            return !cols.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
