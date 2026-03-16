package com.mdservice;

import com.mdservice.entity.Coupon;
import com.mdservice.service.inter.CouponService;
import com.mdservice.utils.UserLocal;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.mdservice.utils.Result;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
public class CouponSchemaAndSeckillSeedTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private CouponService couponService;

    @Test
    public void createTablesAndSeedCoupon() {
        createTables();

        Coupon coupon = new Coupon();
        coupon.setName("新人秒杀券");
        coupon.setDiscountAmount(new BigDecimal("10.00"));
        coupon.setThresholdAmount(new BigDecimal("100.00"));
        coupon.setTotalStock(50);
        coupon.setAvailableStock(50);
        coupon.setStatus((byte) 1);
        coupon.setStartTime(LocalDateTime.now().minusMinutes(10));
        coupon.setEndTime(LocalDateTime.now().plusHours(2));
        Object couponId = couponService.create(coupon).getData();
        log.info("couponId={}", couponId);
    }

    @Test
    public void concurrentSeckill() throws Exception {
        List<Long> userIds = jdbcTemplate.queryForList("select id from user order by id asc limit 80", Long.class);
        if (userIds == null || userIds.isEmpty()) {
            return;
        }
        Long couponId = jdbcTemplate.queryForObject("select id from coupon order by id desc limit 1", Long.class);
        if (couponId == null) {
            return;
        }
        couponService.preheat(couponId);

        int n = userIds.size();
        CountDownLatch latch = new CountDownLatch(n);
        ExecutorService pool = Executors.newFixedThreadPool(16);
        AtomicInteger ok = new AtomicInteger(0);
        AtomicInteger dup = new AtomicInteger(0);
        AtomicInteger soldOut = new AtomicInteger(0);
        ConcurrentLinkedQueue<String> errors = new ConcurrentLinkedQueue<>();
        for (Long userId : userIds) {
            pool.submit(() -> {
                UserLocal.setUser(String.valueOf(userId));
                try {
                    Result r = couponService.seckill(couponId);
                    if (r.getCode() != null && r.getCode() == 200) {
                        ok.incrementAndGet();
                    } else if ("已领取过".equals(r.getMsg())) {
                        dup.incrementAndGet();
                    } else if ("已抢光".equals(r.getMsg())) {
                        soldOut.incrementAndGet();
                    } else if (r.getMsg() != null) {
                        errors.add(r.getMsg());
                    } else {
                        errors.add("unknown");
                    }
                } catch (Exception e) {
                    errors.add("ex:" + e.getClass().getSimpleName());
                } finally {
                    UserLocal.removeUser();
                    latch.countDown();
                }
            });
        }
        latch.await();
        pool.shutdown();

        Integer claims = waitForClaims(couponId, ok.get());
        String stock = jdbcTemplate.queryForObject("select available_stock from coupon where id = ?", String.class, couponId);
        String redisStock = redisTemplate.opsForValue().get("coupon:stock:" + couponId);
        log.info("ok={}, dup={}, soldOut={}, claims={}, dbStock={}, errors={}", ok.get(), dup.get(), soldOut.get(), claims, stock, errors.size());
        log.info("redisStock={}", redisStock);
        int printed = 0;
        for (String err : errors) {
            log.info("err={}", err);
            printed++;
            if (printed >= 10) {
                break;
            }
        }
    }

    private Integer waitForClaims(Long couponId, int expected) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 8000;
        Integer claims = 0;
        while (System.currentTimeMillis() < deadline) {
            claims = jdbcTemplate.queryForObject("select count(1) from coupon_claim where coupon_id = ?", Integer.class, couponId);
            if (claims != null && claims >= expected) {
                return claims;
            }
            Thread.sleep(100);
        }
        return claims;
    }

    private void createTables() {
        jdbcTemplate.execute("""
                create table if not exists `coupon` (
                  `id` bigint not null auto_increment,
                  `name` varchar(64) not null,
                  `discount_amount` decimal(10,2) not null default 0.00,
                  `threshold_amount` decimal(10,2) not null default 0.00,
                  `total_stock` int not null,
                  `available_stock` int not null,
                  `status` tinyint not null default 1,
                  `start_time` datetime default null,
                  `end_time` datetime default null,
                  `create_time` datetime not null default current_timestamp,
                  `update_time` datetime default null on update current_timestamp,
                  primary key (`id`),
                  key `idx_status_time` (`status`, `start_time`, `end_time`)
                ) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci;
                """);

        jdbcTemplate.execute("""
                create table if not exists `coupon_claim` (
                  `id` bigint not null auto_increment,
                  `coupon_id` bigint not null,
                  `user_id` bigint not null,
                  `status` tinyint not null default 0,
                  `create_time` datetime not null default current_timestamp,
                  primary key (`id`),
                  unique key `uk_coupon_user` (`coupon_id`, `user_id`),
                  key `idx_user_id` (`user_id`),
                  constraint `fk_claim_coupon` foreign key (`coupon_id`) references `coupon` (`id`) on delete restrict on update cascade,
                  constraint `fk_claim_user` foreign key (`user_id`) references `user` (`id`) on delete restrict on update cascade
                ) engine=innodb default charset=utf8mb4 collate=utf8mb4_unicode_ci;
                """);
    }
}
