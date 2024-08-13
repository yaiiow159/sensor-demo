package com.example.aquarkdemo.service.impl;

import com.example.aquarkdemo.config.ApiCallProps;
import com.example.aquarkdemo.entity.RawData;
import com.example.aquarkdemo.entity.SensorData;
import com.example.aquarkdemo.result.ApiResult;
import com.example.aquarkdemo.service.HttpRequestService;
import com.example.aquarkdemo.util.JsonUtil;
import com.example.aquarkdemo.util.RestTemplateUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Service
public class HttpRequestServiceImpl implements HttpRequestService {

    private final RestTemplateUtil restTemplateUtil;
    private final ApiCallProps apiCallProps;
    private final ThreadPoolExecutor threadPoolExecutor;

    public HttpRequestServiceImpl(RestTemplateUtil restTemplateUtil, ApiCallProps apiCallProps,
                                  @Qualifier("httpConnectionThreadPoolExecutor") ThreadPoolExecutor threadPoolExecutor) {
        this.restTemplateUtil = restTemplateUtil;
        this.apiCallProps = apiCallProps;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    @Override
    public ApiResult<List<SensorData>> callApi() {
        List<String> apiCallList = apiCallProps.getApiCallList();

        if(CollectionUtils.isEmpty(apiCallList)) {
            return ApiResult.error(Collections.EMPTY_LIST);
        }

        List<CompletableFuture<List<SensorData>>> futureList = apiCallList.stream()
                .map(api -> CompletableFuture.supplyAsync(() -> {
                    try {
                        String response = restTemplateUtil.get(api, null, String.class);
                        log.debug("Api回傳結果內容: {}", response);
                        RawData rawData = JsonUtil.deserialize(response, new TypeReference<RawData>(){});
                        log.debug("實際得到數據: {}", rawData);
                        return CollectionUtils.isEmpty(rawData.getRaw()) ? Collections.<SensorData>emptyList() : rawData.getRaw();
                    } catch (ConnectionRequestTimeoutException e) {
                        log.error("連線 {} api超時", api);
                        return Collections.<SensorData>emptyList();
                    } catch (Exception e) {
                        log.error("呼叫 {} api時出現異常", api, e);
                        return Collections.<SensorData>emptyList();
                    }
                }, threadPoolExecutor).exceptionally(ex -> {
                    log.error("異步處理 {} api時發生異常", api, ex);
                    return Collections.<SensorData>emptyList();
                }))
                .toList();

        try {
            // 等待所有處理完畢
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).get(10, TimeUnit.SECONDS);
        } catch (ExecutionException | InterruptedException e) {
            log.error("執行異步處理時發生異常 {}", e.getMessage());
            return ApiResult.error(Collections.EMPTY_LIST);
        } catch (TimeoutException e) {
            log.error("執行異步處理時超時 {}", e.getMessage());
            return ApiResult.timeout(Collections.EMPTY_LIST);
        }

        List<SensorData> sensorDataList = futureList.stream()
                .flatMap(future -> future.join().stream())
                .filter(sensorData -> !ObjectUtils.isEmpty(sensorData)).toList();

        // 回傳結果集
        if(ObjectUtils.isEmpty(sensorDataList)) {
           return ApiResult.error(Collections.EMPTY_LIST);
        }

        return ApiResult.success(sensorDataList);
    }
}
