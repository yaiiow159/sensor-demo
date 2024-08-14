package com.example.aquarkdemo.service;

import com.example.aquarkdemo.entity.SensorData;
import com.example.aquarkdemo.result.ApiResult;

import java.util.List;
import java.util.Map;

public interface HttpRequestService {
    ApiResult<Map<Integer, List<SensorData>>> callApi();
}
