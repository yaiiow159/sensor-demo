package com.example.aquarkdemo.repository;

import com.example.aquarkdemo.dto.*;

import com.example.aquarkdemo.entity.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    /**
     * 查詢出 peak或是off-peak 的總值
     * @param startTime 起始時間
     * @param endTime 結束時間
     * @return PeakOffPeakSumDTO
     */
    @Query("SELECT new com.example.aquarkdemo.dto.PeakOffPeakSumDTO(" +
            "SUM(sd.sensor.volt.v1), SUM(sd.sensor.volt.v5), SUM(sd.sensor.volt.v6), SUM(sd.rainD), " +
            "SUM(sd.sensor.stickTxRh.rh), SUM(sd.sensor.stickTxRh.tx), " +
            "SUM(sd.sensor.ultrasonicLevel.echo), SUM(sd.sensor.waterSpeedAquark.speed)) " +
            "FROM com.example.aquarkdemo.entity.SensorData sd " +
            "WHERE sd.obsTime >= :startTime AND sd.obsTime < :endTime")
    PeakOffPeakSumDTO findPeekSumByTimeRange(@Param("startTime") LocalDateTime startTime,
                                             @Param("endTime") LocalDateTime endTime);


    /**
     * 查詢出 peak 或是 off peak 的平均值
     * @param startTime 起始時間
     * @param endTime 結束時間
     * @return PeakOffPeakAverageDTO
     */
    @Query("SELECT new com.example.aquarkdemo.dto.PeakOffPeakAverageDTO(" +
            "AVG(sd.sensor.volt.v1), AVG(sd.sensor.volt.v5), AVG(sd.sensor.volt.v6), AVG(sd.rainD), " +
            "AVG(sd.sensor.stickTxRh.rh), AVG(sd.sensor.stickTxRh.tx), " +
            "AVG(sd.sensor.ultrasonicLevel.echo), AVG(sd.sensor.waterSpeedAquark.speed)) " +
            "FROM com.example.aquarkdemo.entity.SensorData sd " +
            "WHERE sd.obsTime >= :startTime AND sd.obsTime < :endTime")
    PeakOffPeakAverageDTO findPeakOffPeakAvgByTimeRange(@Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime);


    /**
     * 查詢出 每日平均值
     * @param startTime 起始時間
     * @param endTime 結束時間
     * @return DailyAverageDTO
     */
    @Query("SELECT new com.example.aquarkdemo.dto.DailyAverageDTO(" +
            "AVG(sd.sensor.volt.v1), AVG(sd.sensor.volt.v5), AVG(sd.sensor.volt.v6), AVG(sd.rainD), " +
            "AVG(sd.sensor.stickTxRh.rh), AVG(sd.sensor.stickTxRh.tx), " +
            "AVG(sd.sensor.ultrasonicLevel.echo), AVG(sd.sensor.waterSpeedAquark.speed)) " +
            "FROM com.example.aquarkdemo.entity.SensorData sd " +
            "WHERE sd.obsTime >= :startTime AND sd.obsTime < :endTime")
    DailyAverageDTO findDailyAverages(@Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);



    /**
     * 查詢出 每小時平均值
     * @param startTime 起始時間
     * @param endTime 結束時間
     * @return HourlyAverageDTO
     */
    @Query("SELECT new com.example.aquarkdemo.dto.HourlyAverageDTO(" +
            "HOUR(sd.obsTime), " +
            "AVG(sd.sensor.volt.v1), AVG(sd.sensor.volt.v5), AVG(sd.sensor.volt.v6), " +
            "AVG(sd.sensor.stickTxRh.rh), AVG(sd.sensor.stickTxRh.tx), " +
            "AVG(sd.sensor.ultrasonicLevel.echo), AVG(sd.rainD), AVG(sd.sensor.waterSpeedAquark.speed)) " +
            "FROM com.example.aquarkdemo.entity.SensorData sd " +
            "WHERE sd.obsTime >= :startTime AND sd.obsTime < :endTime " +
            "GROUP BY HOUR(sd.obsTime)")
    List<HourlyAverageDTO> findHourlyAverages(@Param("startTime") LocalDateTime startTime,
                                              @Param("endTime") LocalDateTime endTime);


    /**
     * 查詢出每小時加總值
     * @param startTime 起始時間
     * @param endTime   結束時間
     * @return HourlySumDto
     */
    @Query("SELECT new com.example.aquarkdemo.dto.HourlySumDTO(HOUR(sd.obsTime), " +
            "SUM(sd.sensor.volt.v1), SUM(sd.sensor.volt.v5), SUM(sd.sensor.volt.v6), SUM(sd.rainD), " +
            "SUM(sd.sensor.stickTxRh.rh), SUM(sd.sensor.stickTxRh.tx), " +
            "SUM(sd.sensor.ultrasonicLevel.echo), SUM(sd.sensor.waterSpeedAquark.speed)) " +
            "FROM com.example.aquarkdemo.entity.SensorData sd WHERE sd.obsTime >= :startTime AND sd.obsTime < :endTime " +
            "GROUP BY HOUR(sd.obsTime)")
    List<HourlySumDTO> findHourlySums(@Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);



    /**
     * 查詢出每日總值
     * @param startTime 起始時間
     * @param endTime 結束時間
     * @return DailySumDTO
     */
    @Query("SELECT new com.example.aquarkdemo.dto.DailySumDTO(" +
            "SUM(sd.sensor.volt.v1), SUM(sd.sensor.volt.v5), SUM(sd.sensor.volt.v6), SUM(sd.rainD), " +
            "SUM(sd.sensor.stickTxRh.rh), SUM(sd.sensor.stickTxRh.tx), " +
            "SUM(sd.sensor.ultrasonicLevel.echo), SUM(sd.sensor.waterSpeedAquark.speed)) " +
            "FROM com.example.aquarkdemo.entity.SensorData sd " +
            "WHERE sd.obsTime >= :startTime AND sd.obsTime < :endTime")
    DailySumDTO findDailySums(@Param("startTime") LocalDateTime startTime,
                              @Param("endTime") LocalDateTime endTime);


}