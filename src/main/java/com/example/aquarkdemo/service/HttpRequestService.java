package com.example.aquarkdemo.service;

import com.example.aquarkdemo.result.ApiResult;

public interface HttpRequestService {
    ApiResult<?> callApi();
}
