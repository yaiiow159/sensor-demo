package com.example.aquarkdemo.schedule;

import com.example.aquarkdemo.service.SensorDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 排程
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SensorScheduler {

    private final SensorDataService sensorDataService;

    /**
     * 每天早上7點抓取數據並保存到資料庫
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void callApiAndSaveData() {
        log.info("執行抓取API數據,執行時間: " + LocalDateTime.now(ZoneId.of("Asia/Taipei")));

        sensorDataService.callApiAndSaveData();

        log.info("抓取數據 完成...");
    }

    /**
     * 每天午夜計算當天的加總成果和每日平均值
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void calculateDailyStatistics() {
        log.info("計算 每天的加總成果、每小時平均值和每日平均值 開始... 執行時間: " + LocalDateTime.now(ZoneId.of("Asia/Taipei")));

        // 計算每天的加總成果
        sensorDataService.calculateDailySums();

        // 計算每日的平均值
        sensorDataService.calculateDailyAverages();

        // 計算 每天尖峰時段還有離峰時段的平均值 還有加總值
        sensorDataService.calculatePeakAndOffPeakAverages();

        log.info("計算 每天的加總成果、每小時平均值和每日平均值 完成...");
    }

    /**
     * 每小時計算當小時的加總成果
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void calculateHourlyStatistics() {
        log.info("計算 每小時的加總成果、每小時平均值開始... 執行時間: " + LocalDateTime.now(ZoneId.of("Asia/Taipei")));

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(1);

        // 計算每小時的平均值
        sensorDataService.calculateHourlyAverages(startTime, endTime);

        // 計算每小時加總
        sensorDataService.calculateHourlySums(startTime, endTime);
    }


}
