package com.example.aquarkdemo.service.impl;

import com.example.aquarkdemo.service.RedisMetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMetricServiceImpl implements RedisMetricService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Map<String, Object> getMetricData() {
        Properties info = (Properties) redisTemplate.execute((RedisCallback<Object>) connection -> connection.info());
        Properties commandStats = (Properties) redisTemplate.execute((RedisCallback<Object>) connection -> connection.info("commandstats"));
        Object dbSize = redisTemplate.execute((RedisCallback<Object>) connection -> connection.dbSize());
        Properties memoryInfo = (Properties) redisTemplate.execute((RedisCallback<Object>) connection -> connection.info("memory"));
        Properties clientsInfo = (Properties) redisTemplate.execute((RedisCallback<Object>) connection -> connection.info("clients"));
        Properties persistenceInfo = (Properties) redisTemplate.execute((RedisCallback<Object>) connection -> connection.info("persistence"));
        Properties cpuInfo = (Properties) redisTemplate.execute((RedisCallback<Object>) connection -> connection.info("cpu"));
        Properties statsInfo = (Properties) redisTemplate.execute((RedisCallback<Object>) connection -> connection.info("stats"));

        Map<String, Object> result = new HashMap<>(7);
        result.put("info", info);
        result.put("dbSize", dbSize);
        result.put("memoryInfo", memoryInfo);
        result.put("clientsInfo", clientsInfo);
        result.put("persistenceInfo", persistenceInfo);
        result.put("cpuInfo", cpuInfo);
        result.put("statsInfo", statsInfo);

        log.debug(" commandStats: {}, info: {}, dbSize: {}, memoryInfo: {}, clientsInfo: {}, " +
                  "persistenceInfo: {}, cpuInfo: {}, statsInfo: {}",
                commandStats, info, dbSize, memoryInfo, clientsInfo, persistenceInfo, cpuInfo, statsInfo);


        // 處理commandStats以便前端展示
        List<Map<String, String>> pieList = new ArrayList<>();
        if (commandStats != null) {
            commandStats.stringPropertyNames().forEach(key -> {
                Map<String, String> data = new HashMap<>(2);
                String property = commandStats.getProperty(key);
                data.put("name", StringUtils.removeStart(key, "cmdstat_"));
                data.put("value", StringUtils.substringBetween(property, "calls=", ",usec"));
                pieList.add(data);
            });
        }
        result.put("commandStats", pieList);

        return result;
    }
}
