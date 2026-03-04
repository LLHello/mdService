package com.mdservice.task;

import com.google.common.collect.Lists;
import com.mdservice.mapper.GoodsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.Executor;

import static com.mdservice.constant.RedisConstant.DIRTY_KEY;
import static com.mdservice.constant.RedisConstant.RANK_KEY;

@Component
@Slf4j
public class ClickSyncTask implements SmartLifecycle {
    private boolean running;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    @Qualifier("clickSyncExecutor")
    private Executor executor;
    public void syncToDb() {
        // 1. 获取所有脏数据 ID (SPOP: 弹出并删除，原子操作，防止重复处理)
        // 注意：如果数据量巨大，不要一次全弹出来，可以用 SPOP key count 分批弹
        List<String> dirtyIds = redisTemplate.opsForSet().pop(DIRTY_KEY, 10000); // 每次最多处理 1万 个

        if (dirtyIds == null || dirtyIds.isEmpty()) {
            return;
        }

        log.info("开始同步点击量，涉及商品数: {}", dirtyIds.size());

        // 2. 将这批 ID 分片 (Partition)，比如每 500 个一组，交给线程池并发处理
        // 使用 Guava 的 Lists.partition 或者手动拆分
        List<String> idList = new ArrayList<>(dirtyIds);
        List<List<String>> partitions = Lists.partition(idList, 500); // 需要引入 Guava 包

        // 3. 提交给线程池
        for (List<String> batchIds : partitions) {
            executor.execute(() -> processBatch(batchIds));
        }
    }

    /**
     * 实际执行批量更新的逻辑
     */
    private void processBatch(List<String> batchIds) {
        try {
            // 3.1 批量查询 Redis 获取最新点击量 (Pipeline 管道技术，减少网络 RTT)
            List<Object> scores = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (String id : batchIds) {
                    connection.zSetCommands().zScore(RANK_KEY.getBytes(), id.getBytes());
                }
                return null;
            });

            // 3.2 组装数据，准备写 DB
//            List<String> ids = new ArrayList<>(batchIds.size());
            Map<Long, Long> map = new HashMap<>();
            for (int i = 0; i < batchIds.size(); i++) {
                String goodsIdStr = batchIds.get(i);
                Object scoreObj = scores.get(i);
                if (scoreObj != null) {
                    // redisTemplate pipelined 返回的是 Double
                    long clickCount = ((Double) scoreObj).longValue();
                    map.put(Long.parseLong(goodsIdStr), clickCount);
                }
            }
            if(!map.isEmpty()) goodsMapper.updateClickCount(map);
            log.info("线程 {} 完成了一批同步，数量: {}", Thread.currentThread().getName(), batchIds.size());

        } catch (Exception e) {
            try {
                String[] idsArray = batchIds.toArray(new String[0]);
                // 将失败的 ID 重新塞回 Redis 的 Set 集合
                redisTemplate.opsForSet().add(DIRTY_KEY, idsArray);

                log.info("已将 {} 个失败的 ID 放回 Redis 等待下次重试", batchIds.size());
            } catch (Exception redisEx) {
                log.error("严重错误：回滚 Redis 失败！数据可能丢失，IDs: {}", batchIds, redisEx);
            }
        }
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void start() {
        this.running = true;
    }

    @Override
    public void stop() {
        log.info("应用关闭中，执行最后一次点击量同步...");
        try {
            this.syncToDb(); // 调用你的同步逻辑
        } catch (Exception e) {
            log.error("关闭时同步失败", e);
        }
        this.running = false;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }
}
