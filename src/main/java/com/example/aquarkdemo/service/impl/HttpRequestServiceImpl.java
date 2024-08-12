package com.example.aquarkdemo.service.impl;

import com.example.aquarkdemo.config.ApiCallProps;
import com.example.aquarkdemo.result.ApiResult;
import com.example.aquarkdemo.result.Apimessage;
import com.example.aquarkdemo.result.SensorDataResponse;
import com.example.aquarkdemo.service.Caller;
import com.example.aquarkdemo.service.HttpRequestService;
import com.example.aquarkdemo.util.RestTemplateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class HttpRequestServiceImpl implements HttpRequestService {

    private final RestTemplateUtil restTemplateUtil;
    private final ApiCallProps apiCallProps;

    @Override
    public ApiResult<?> callApi() {
        List<String> callList = apiCallProps.getApiCallList();

        return null;
    }
}
