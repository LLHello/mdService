package com.mdservice.runner;

import com.mdservice.mapper.CouponMapper;
import com.mdservice.service.inter.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 应用启动后自动将所有优惠券预热到 Redis，
 * 解决「活动未预热」导致秒杀返回 code=3 的问题。
 */
@Component
@Slf4j
public class CouponPreheatRunner implements ApplicationRunner {

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private CouponService couponService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<Long> ids = couponMapper.selectAllIds();
            if (ids == null || ids.isEmpty()) {
                log.info("[CouponPreheat] 无优惠券需要预热");
                return;
            }
            int ok = 0, fail = 0;
            for (Long id : ids) {
                try {
                    couponService.preheat(id);
                    ok++;
                } catch (Exception e) {
                    log.warn("[CouponPreheat] 预热失败 couponId={}", id, e);
                    fail++;
                }
            }
            log.info("[CouponPreheat] 完成：成功={}, 失败={}", ok, fail);
        } catch (Exception e) {
            log.error("[CouponPreheat] 启动预热异常", e);
        }
    }
}
