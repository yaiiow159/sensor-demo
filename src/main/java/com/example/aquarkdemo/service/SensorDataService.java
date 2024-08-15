package com.example.aquarkdemo.service;

import com.example.aquarkdemo.dto.SensorDataDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface SensorDataService {

    void callApiAndSaveData();

    void calculatePeakAndOffPeakAverages();

    void calculateDailySums();

    void calculateHourlyAverages(LocalDateTime startTime, LocalDateTime endTime);

    void calculateHourlySums(LocalDateTime startTime, LocalDateTime endTime);

    void calculateDailyAverages();

    List<SensorDataDTO> queryData(String timePeriod, String field, String startDate, String endDate, String selectType, Integer limit);

    Map<String, Object> queryDashboardData(String date);


    Map<String, Object> queryDailyAnalysis(String date);

    Map<String, Object> queryHourlyAnalysis(String date);

    Map<String, Object> queryOffPeakAnalysis(String date);

    Map<String, Object> queryPeakAnalysis(String date);
}
