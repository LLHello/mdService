package com.mdservice.service.impl;

import com.mdservice.domain.vo.MyCouponVO;
import com.mdservice.entity.Coupon;
import com.mdservice.mapper.CouponMapper;
import com.mdservice.service.inter.CouponService;
import com.mdservice.task.CouponClaimConsumer;
import com.mdservice.utils.Result;
import com.mdservice.utils.UserLocal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CouponServiceImpl implements CouponService {

    private static final String STOCK_KEY_PREFIX = "coupon:stock:";
    private static final String USER_KEY_PREFIX = "coupon:users:";
    private static final String META_KEY_PREFIX = "coupon:meta:";
    private static final String CLAIM_STREAM_KEY = "coupon:claim:stream";
    private static final String CLAIM_GROUP = "coupon-claim-group";

    @Value("${coupon.seckill.consume-threshold:20}")
    private long consumeThreshold;

    @Autowired
    private CouponMapper couponMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private DefaultRedisScript<Long> couponSeckillScript;
    @Autowired
    private CouponClaimConsumer couponClaimConsumer;

    @Override
    public Result create(Coupon coupon) {
        if (coupon == null) {
            return Result.error("参数不能为空");
        }
        if (ObjectUtils.isEmpty(coupon.getName())) {
            return Result.error("优惠券名称不能为空");
        }
        if (coupon.getTotalStock() == null || coupon.getTotalStock() <= 0) {
            return Result.error("库存必须大于0");
        }
        if (coupon.getAvailableStock() == null) {
            coupon.setAvailableStock(coupon.getTotalStock());
        }
        if (coupon.getStatus() == null) {
            coupon.setStatus((byte) 1);
        }
        couponMapper.insertCoupon(coupon);
        // 创建后立即预热，确保可以秒杀
        preheat(coupon.getId());
        return Result.success(coupon.getId());
    }

    @Override
    public Result listByGoods(Long goodsId) {
        if (goodsId == null) {
            return Result.error("goodsId不能为空");
        }
        List<Coupon> list = couponMapper.selectByGoodsId(goodsId);
        if (list != null) {
            for (Coupon coupon : list) {
                if (coupon == null || coupon.getId() == null) continue;
                String stock = redisTemplate.opsForValue().get(STOCK_KEY_PREFIX + coupon.getId());
                if (!ObjectUtils.isEmpty(stock)) {
                    try { coupon.setAvailableStock(Integer.parseInt(stock)); } catch (Exception ignored) {}
                }
            }
        }
        return Result.success(list);
    }

    @Override
    public Result myCoupons() {
        String userIdStr = UserLocal.getUser();
        if (ObjectUtils.isEmpty(userIdStr)) {
            return Result.error("未登录");
        }
        Long userId = Long.parseLong(userIdStr);
        List<MyCouponVO> list = couponMapper.selectUnusedClaimsByUserId(userId);
        return Result.success(list);
    }

    @Override
    public Result list() {
        List<Coupon> list = couponMapper.selectAll();
        if (list != null && !list.isEmpty()) {
            for (Coupon coupon : list) {
                if (coupon == null || coupon.getId() == null) {
                    continue;
                }
                String stock = redisTemplate.opsForValue().get(STOCK_KEY_PREFIX + coupon.getId());
                if (!ObjectUtils.isEmpty(stock)) {
                    try {
                        coupon.setAvailableStock(Integer.parseInt(stock));
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return Result.success(list);
    }

    @Override
    public Result preheat(Long couponId) {
        if (couponId == null) {
            return Result.error("couponId不能为空");
        }
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            return Result.error("优惠券不存在");
        }
        String stockKey = STOCK_KEY_PREFIX + couponId;
        String userKey = USER_KEY_PREFIX + couponId;
        String metaKey = META_KEY_PREFIX + couponId;

        redisTemplate.opsForValue().set(stockKey, String.valueOf(coupon.getAvailableStock()));
        List<Long> claimedUserIds = couponMapper.selectClaimUserIds(couponId);
        if (claimedUserIds != null && !claimedUserIds.isEmpty()) {
            String[] values = new String[claimedUserIds.size()];
            for (int i = 0; i < claimedUserIds.size(); i++) {
                values[i] = String.valueOf(claimedUserIds.get(i));
            }
            redisTemplate.opsForSet().add(userKey, values);
        }
        long startMs = coupon.getStartTime() == null ? 0L : coupon.getStartTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endMs = coupon.getEndTime() == null ? 0L : coupon.getEndTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        redisTemplate.opsForHash().put(metaKey, "status", String.valueOf(coupon.getStatus() == null ? 0 : coupon.getStatus()));
        redisTemplate.opsForHash().put(metaKey, "startMs", String.valueOf(startMs));
        redisTemplate.opsForHash().put(metaKey, "endMs", String.valueOf(endMs));

        Long ttlSeconds = computeTtlSeconds(coupon.getEndTime());
        if (ttlSeconds != null) {
            redisTemplate.expire(stockKey, java.time.Duration.ofSeconds(ttlSeconds));
            redisTemplate.expire(userKey, java.time.Duration.ofSeconds(ttlSeconds));
            redisTemplate.expire(metaKey, java.time.Duration.ofSeconds(ttlSeconds));
        }
        return Result.success();
    }

    @Override
    public Result seckill(Long couponId) {
        String userIdStr = UserLocal.getUser();
        if (ObjectUtils.isEmpty(userIdStr)) {
            return Result.error("未登录");
        }
        if (couponId == null) {
            return Result.error("couponId不能为空");
        }
        Long userId = Long.parseLong(userIdStr);

        String stockKey = STOCK_KEY_PREFIX + couponId;
        String userKey = USER_KEY_PREFIX + couponId;
        String metaKey = META_KEY_PREFIX + couponId;
        List<String> keys = List.of(stockKey, userKey, metaKey);
        Long code = redisTemplate.execute(couponSeckillScript, keys, String.valueOf(userId), String.valueOf(System.currentTimeMillis()));
        if (code == null) {
            return Result.error("系统繁忙");
        }
        if (code == 1) {
            return Result.error("已抢光");
        }
        if (code == 2) {
            return Result.error("已领取过");
        }
        if (code == 3) {
            return Result.error("活动未预热");
        }
        if (code == 4) {
            return Result.error("活动未开启");
        }
        if (code == 5) {
            return Result.error("活动未开始");
        }
        if (code == 6) {
            return Result.error("活动已结束");
        }

        try {
            Map<String, String> fields = new HashMap<>();
            fields.put("couponId", String.valueOf(couponId));
            fields.put("userId", String.valueOf(userId));
            fields.put("ts", String.valueOf(System.currentTimeMillis()));
            RecordId recordId = redisTemplate.opsForStream().add(CLAIM_STREAM_KEY, fields);
            if (recordId == null) {
                rollbackRedis(couponId, userId);
                return Result.error("系统繁忙");
            }
            couponClaimConsumer.triggerConsume();
            return Result.success(couponId + ":" + userId);
        } catch (Exception e) {
            rollbackRedis(couponId, userId);
            return Result.error("系统繁忙");
        }
    }

    private void tryTriggerConsume() {
        StreamOperations<String, String, String> ops = redisTemplate.opsForStream();
        long size = 0;
        try {
            Long len = ops.size(CLAIM_STREAM_KEY);
            size = len == null ? 0 : len;
        } catch (Exception ignored) {
        }
        long pending = 0;
        try {
            pending = ops.pending(CLAIM_STREAM_KEY, CLAIM_GROUP).getTotalPendingMessages();
        } catch (Exception ignored) {
        }
        if (size + pending < consumeThreshold) {
            return;
        }
        couponClaimConsumer.triggerConsume();
    }

    private void rollbackRedis(Long couponId, Long userId) {
        String stockKey = STOCK_KEY_PREFIX + couponId;
        String userKey = USER_KEY_PREFIX + couponId;
        redisTemplate.opsForValue().increment(stockKey);
        redisTemplate.opsForSet().remove(userKey, String.valueOf(userId));
    }

    private Long computeTtlSeconds(LocalDateTime endTime) {
        if (endTime == null) {
            return null;
        }
        long ttl = Duration.between(LocalDateTime.now(), endTime.plusDays(1)).getSeconds();
        if (ttl <= 0) {
            return null;
        }
        return ttl;
    }
}
