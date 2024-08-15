package com.example.aquarkdemo.util;

import com.example.aquarkdemo.dto.BaseHourDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
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


    public void setToList(String key, List<?> list) throws JsonProcessingException {
        List<String> serializedList = new LinkedList<>();
        for (Object obj : list) {
            serializedList.add(JsonUtil.serialize(obj));
        }
        stringRedisTemplate.opsForList().leftPushAll(key, serializedList);
    }

    public void setToList(String key, List<?> list, Long timeout, TimeUnit unit) throws JsonProcessingException {
        List<String> serializedList = new LinkedList<>();
        for (Object obj : list) {
            serializedList.add(JsonUtil.serialize(obj));
        }
        stringRedisTemplate.opsForList().leftPushAll(key, serializedList);
        stringRedisTemplate.expire(key, timeout, unit);
    }

    public <T> List<T> getFromList(String key, Class<T> clazz) throws JsonProcessingException {
        List<String> serializedList = stringRedisTemplate.opsForList().range(key, 0, -1);
        List<T> resultList = new LinkedList<>();
        if (serializedList != null && !serializedList.isEmpty()) {
            for (String serialized : serializedList) {
                T obj = JsonUtil.getInstance().readValue(serialized, clazz);
                resultList.add(obj);
            }
        }
        return resultList;
    }

    public <T> List<T> getFromList(String key, TypeReference<T> typeReference) throws JsonProcessingException {
        List<String> serializedList = stringRedisTemplate.opsForList().range(key, 0, -1);
        List<T> resultList = new LinkedList<>();
        if (serializedList != null && !serializedList.isEmpty()) {
            for (String serialized : serializedList) {
                T obj = JsonUtil.getInstance().readValue(serialized, typeReference);
                resultList.add(obj);
            }
        }
        return resultList;
    }

    public void setToSet(String key, Object value) throws JsonProcessingException {
        stringRedisTemplate.opsForSet().add(key, JsonUtil.serialize(value));
    }

    public String getFromSet(String key) {
        return stringRedisTemplate.opsForSet().pop(key);
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
