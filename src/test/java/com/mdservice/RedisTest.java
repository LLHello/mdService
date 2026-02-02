package com.mdservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdservice.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
@Slf4j
public class RedisTest {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    private static final ObjectMapper mapper = new ObjectMapper();
    @Test
    public void testRedis1() throws JsonProcessingException {
        User user = new User();
        //stringRedisTemplate需要手动序列化和反序列化
        String jsonUser = mapper.writeValueAsString(user);
        stringRedisTemplate.opsForValue().set("user:200", jsonUser);
        String s = stringRedisTemplate.opsForValue().get("user:200");
        user = mapper.readValue(s, User.class);
        log.info("user: {}", user);
    }
    @Test
    public void testRedis2(){
        stringRedisTemplate.opsForValue().set("key", "value");
        String s = stringRedisTemplate.opsForValue().get("key");
        log.info("key: {}", s);
    }
}
