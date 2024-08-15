package com.example.aquarkdemo.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 *  redis 缓存工具類
 *
 *  可設置 key 並且設值ttl過期時間
 */
@Component
@RequiredArgsConstructor
public class RedisCacheClient {

    private final StringRedisTemplate stringRedisTemplate;

    @Data
    private static class RedisCacheData {
        private Object data;
        private LocalDateTime expireTime;
    }

    public void set(String key, Object value) throws JsonProcessingException {
        stringRedisTemplate.opsForValue().set(key, JsonUtil.serialize(value));
    }
    public void set(String key, Object value, Long ttlTime ,TimeUnit timeUnit) throws JsonProcessingException {
        stringRedisTemplate.opsForValue().set(key, JsonUtil.serialize(value), ttlTime, timeUnit);
    }

    public void setToHash(String key, String hashKey, Object value) throws JsonProcessingException {
        stringRedisTemplate.opsForHash().put(key, hashKey, JsonUtil.serialize(value));
    }

    public void getFromHash(String key, String hashKey) {
        stringRedisTemplate.opsForHash().get(key, hashKey);
    }

    public void setToList(String key, Object value) throws JsonProcessingException {
        stringRedisTemplate.opsForList().leftPushAll(key, JsonUtil.serialize(value));
    }

    public void getFromList(String key) {
        stringRedisTemplate.opsForList().leftPop(key);
    }

    public void getFromList(String key, Long ttlTime, TimeUnit timeUnit) {
        stringRedisTemplate.opsForList().leftPop(key, ttlTime, timeUnit);
    }

    public void setToSet(String key, Object value) throws JsonProcessingException {
        stringRedisTemplate.opsForSet().add(key, JsonUtil.serialize(value));
    }

    public void getFromSet(String key) {
        stringRedisTemplate.opsForSet().pop(key);
    }

    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }
    // 設置並且設置過期時間
    public void setWithLogicExpire(String key, Object value, Long ttlTime,TimeUnit timeUnit) throws JsonProcessingException {
        RedisCacheData redisCacheData = new RedisCacheData();
        redisCacheData.setData(value);
        redisCacheData.setExpireTime(LocalDateTime.now().plusSeconds(timeUnit.toSeconds(ttlTime)));
        stringRedisTemplate.opsForValue().set(key, JsonUtil.serialize(value));
    }
    public <T,ID> T queryIfPresent(String keyPrefix, ID id, Class<T> clazz, Function<ID,T> callback, Long ttlTime, TimeUnit timeUnit) throws JsonProcessingException {
        final String key = keyPrefix + id;
        final String value = stringRedisTemplate.opsForValue().get(key);

        if(StringUtils.hasText(value)){
            return JsonUtil.deserialize(value, clazz);
        }

        // 如果不存在則執行callback 查詢db 利用id
        T t = callback.apply(id);
        if(t == null){
            stringRedisTemplate.opsForValue().set(key, "",ttlTime,timeUnit);
            throw new IllegalArgumentException("查無該筆資料, id: " + id);
        }
        // 存入redis
        stringRedisTemplate.opsForValue().set(key, JsonUtil.serialize(t),ttlTime,timeUnit);
        return t;
    }
}
