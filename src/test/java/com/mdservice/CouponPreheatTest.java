package com.mdservice;

import com.mdservice.mapper.CouponMapper;
import com.mdservice.service.inter.CouponService;
import com.mdservice.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class CouponPreheatTest {

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private CouponService couponService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 1. 先执行数据库迁移（幂等添加 goods_id / coupon_claim_id / discount_amount / used_order_id 列）
     * 2. 再对所有优惠券执行 Redis 预热
     * 运行后即可在领券中心正常领取优惠券（不再报「活动未预热」）。
     */
    @Test
    public void preheatAllCoupons() {
        // --- Step 1: migrate ---
        addColumnIfMissing("coupon", "goods_id",
                "ALTER TABLE `coupon` ADD COLUMN `goods_id` bigint NULL DEFAULT NULL COMMENT '绑定商品ID，null为全场券' AFTER `name`");
        addColumnIfMissing("orders", "coupon_claim_id",
                "ALTER TABLE `orders` ADD COLUMN `coupon_claim_id` bigint NULL DEFAULT NULL COMMENT '使用的优惠券领取记录ID' AFTER `pay_amount`");
        addColumnIfMissing("orders", "discount_amount",
                "ALTER TABLE `orders` ADD COLUMN `discount_amount` decimal(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠抵扣金额' AFTER `coupon_claim_id`");
        addColumnIfMissing("coupon_claim", "used_order_id",
                "ALTER TABLE `coupon_claim` ADD COLUMN `used_order_id` bigint NULL DEFAULT NULL COMMENT '使用该优惠券的订单ID' AFTER `status`");
        log.info("[PreheatTest] 数据库迁移完成");

        // --- Step 2: preheat ---
        List<Long> ids = couponMapper.selectAllIds();
        assertNotNull(ids, "coupon id list should not be null");
        if (ids.isEmpty()) {
            log.info("[PreheatTest] 数据库中没有优惠券，跳过预热");
            return;
        }
        log.info("[PreheatTest] 共 {} 张优惠券需要预热", ids.size());
        int ok = 0, fail = 0;
        for (Long id : ids) {
            try {
                Result result = couponService.preheat(id);
                if (result != null && result.getCode() != null && result.getCode().intValue() == 200) {
                    log.info("[PreheatTest] 预热成功 couponId={}", id);
                    ok++;
                } else {
                    log.warn("[PreheatTest] 预热返回非成功 couponId={}, result={}", id, result);
                    fail++;
                }
            } catch (Exception e) {
                log.error("[PreheatTest] 预热异常 couponId={}", id, e);
                fail++;
            }
        }
        log.info("[PreheatTest] 完成：成功={}, 失败={}", ok, fail);
    }

    private void addColumnIfMissing(String table, String column, String ddl) {
        try {
            List<Map<String, Object>> cols = jdbcTemplate.queryForList(
                    "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS "
                    + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                    table, column);
            if (cols.isEmpty()) {
                jdbcTemplate.execute(ddl);
                log.info("[PreheatTest] 已添加列 {}.{}", table, column);
            } else {
                log.info("[PreheatTest] 列 {}.{} 已存在，跳过", table, column);
            }
        } catch (Exception e) {
            log.error("[PreheatTest] 添加列 {}.{} 失败", table, column, e);
        }
    }
}
