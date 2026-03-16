package com.mdservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class CouponSeckillConfig {

    @Bean
    public DefaultRedisScript<Long> couponSeckillScript() {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setLocation(new ClassPathResource("lua/coupon_seckill.lua"));
        script.setResultType(Long.class);
        return script;
    }
}

