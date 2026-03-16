package com.mdservice.task;

import com.mdservice.entity.CouponClaim;
import com.mdservice.mapper.CouponMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Component;
import org.springframework.context.SmartLifecycle;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
public class CouponClaimConsumer implements SmartLifecycle {

    private static final String STREAM_KEY = "coupon:claim:stream";
    private static final String GROUP = "coupon-claim-group";

    @Value("${coupon.seckill.consume-batch-size:200}")
    private int batchSize;

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private CouponMapper couponMapper;

    private final String consumer = UUID.randomUUID().toString().replace("-", "");
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean draining = new AtomicBoolean(false);
    private volatile boolean running;

    @Override
    public void start() {
        ensureGroup();
        running = true;
    }

    @Override
    public void stop() {
        running = false;
        executor.shutdownNow();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    public void triggerConsume() {
        if (!running) {
            return;
        }
        // If already draining, the running loop will naturally pick up the new message;
        // no need to spawn another task.
        if (!draining.compareAndSet(false, true)) {
            return;
        }
        executor.submit(() -> {
            try {
                // Small delay to let the just-written stream message become visible
                Thread.sleep(50);
                drainLoop();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                draining.set(false);
            }
        });
    }

    private void ensureGroup() {
        StreamOperations<String, String, String> ops = redisTemplate.opsForStream();
        try {
            ops.createGroup(STREAM_KEY, ReadOffset.latest(), GROUP);
        } catch (Exception ignored) {
            try {
                RecordId id = ops.add(STREAM_KEY, Map.of("init", "1"));
                ops.createGroup(STREAM_KEY, ReadOffset.latest(), GROUP);
                if (id != null) {
                    ops.delete(STREAM_KEY, id);
                }
            } catch (Exception ignored2) {
            }
        }
    }

    private void drainLoop() {
        StreamOperations<String, String, String> ops = redisTemplate.opsForStream();
        Consumer c = Consumer.from(GROUP, consumer);
        StreamReadOptions options = StreamReadOptions.empty().count(batchSize).block(Duration.ofMillis(200));
        int emptyRounds = 0;
        while (running) {
            List<MapRecord<String, String, String>> records = new ArrayList<>();
            try {
                List<MapRecord<String, String, String>> newRecords = ops.read(c, options, StreamOffset.create(STREAM_KEY, ReadOffset.lastConsumed()));
                if (newRecords != null && !newRecords.isEmpty()) {
                    records.addAll(newRecords);
                }
            } catch (Exception e) {
                log.error("coupon claim read new failed", e);
            }
            try {
                List<MapRecord<String, String, String>> pendingRecords = ops.read(c, StreamReadOptions.empty().count(batchSize), StreamOffset.create(STREAM_KEY, ReadOffset.from("0")));
                if (pendingRecords != null && !pendingRecords.isEmpty()) {
                    records.addAll(pendingRecords);
                }
            } catch (Exception e) {
                log.error("coupon claim read pending failed", e);
            }

            if (records.isEmpty()) {
                emptyRounds++;
                if (emptyRounds >= 3) {
                    return;
                }
                try { Thread.sleep(100); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); return; }
                continue;
            }
            emptyRounds = 0;

            for (MapRecord<String, String, String> record : records) {
                if (!running) {
                    return;
                }
                try {
                    persist(record.getValue());
                    ops.acknowledge(STREAM_KEY, GROUP, record.getId());
                    ops.delete(STREAM_KEY, record.getId());
                } catch (DuplicateKeyException e) {
                    ops.acknowledge(STREAM_KEY, GROUP, record.getId());
                    ops.delete(STREAM_KEY, record.getId());
                } catch (Exception e) {
                    log.error("coupon claim persist failed, recordId={}", record.getId(), e);
                }
            }
        }
    }

    private void persist(Map<String, String> value) {
        Long couponId = parseLong(value.get("couponId"));
        Long userId = parseLong(value.get("userId"));
        if (couponId == null || userId == null) {
            return;
        }
        CouponClaim claim = new CouponClaim();
        claim.setCouponId(couponId);
        claim.setUserId(userId);
        claim.setStatus((byte) 0);
        couponMapper.insertClaim(claim);
    }

    private Long parseLong(String v) {
        if (v == null || v.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(v.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
