package com.example.aquarkdemo.service.impl;

import com.example.aquarkdemo.checker.PeakOffPeakTimeChecker;
import com.example.aquarkdemo.dto.*;
import com.example.aquarkdemo.entity.SensorData;
import com.example.aquarkdemo.exception.CaculateException;
import com.example.aquarkdemo.mq.MessageProducer;
import com.example.aquarkdemo.repository.SensorDataRepository;
import com.example.aquarkdemo.result.ApiResult;
import com.example.aquarkdemo.service.HttpRequestService;
import com.example.aquarkdemo.service.SensorDataService;
import com.example.aquarkdemo.util.JsonUtil;
import com.example.aquarkdemo.util.RedisCacheClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.example.aquarkdemo.constant.CommonConstant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorDataServiceImpl implements SensorDataService {

    private final HttpRequestService httpRequestService;
    private final SensorDataRepository sensorDataRepository;
    private final RedisCacheClient redisCacheClient;
    private final MessageProducer messageProducer;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 呼叫API並存入資料
     */
    @Override
    public void callApiAndSaveData() {
        // STEP1 呼叫API 取得資料
        ApiResult<Map<Integer, List<SensorData>>> apiResult = httpRequestService.callApi();
        // STEP2 將資料存入DB
        if (apiResult != null && apiResult.isSuccess() && apiResult.getData() != Collections.EMPTY_MAP) {
           // 按照station_id 傳遞
            for (Map.Entry<Integer, List<SensorData>> entry : apiResult.getData().entrySet()) {
                 messageProducer.sendSensorData(entry.getValue());
            }
        } else {
            assert apiResult != null;
            if (apiResult.isError() || apiResult.isFail() || apiResult.isTimeout()) {
                log.info("呼叫API 失敗 狀態碼: " + apiResult.getCode() + " 訊息: " + apiResult.getMessage()
                        + " 執行時間: " + LocalDateTime.now(ZoneId.of("Asia/Taipei")));
            }
        }
    }

    /**
     * 計算 當天尖峰時間段的 平均 並添加到redis cache當中
     * 計算 當天離峰時間段的 平均 並添加到redis cache當中
     */
    @Override
    public void calculatePeakAndOffPeakAverages() {

        // 計算當天的 尖峰時段的加總值 還有平均值 （週一～週三 : 7:30 ~17:30。 週四 週五全天）
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Taipei"));
        LocalDateTime peekStartTime;
        LocalDateTime peekEndTime;
        LocalDateTime offPeakStartTime;
        LocalDateTime offPeakEndTime;

        PeakOffPeakSumDTO peakSumDTO = new PeakOffPeakSumDTO();
        PeakOffPeakAverageDTO peakAverageDTO = new PeakOffPeakAverageDTO();

        PeakOffPeakSumDTO offPeakSumDTO = new PeakOffPeakSumDTO();
        PeakOffPeakAverageDTO offPeakAverageDTO = new PeakOffPeakAverageDTO();

        // 驗證是否是週五 周五尖峰值段為全天
        if (PeakOffPeakTimeChecker.isCalculatePeakFullDay(now)) {
            peekStartTime = LocalDate.now(ZoneId.of("Asia/Taipei")).minusDays(1).atStartOfDay();
            peekEndTime = LocalDate.now(ZoneId.of("Asia/Taipei")).minusDays(1).atTime(LocalTime.MAX);

            peakSumDTO = sensorDataRepository.findPeekSumByTimeRange(peekStartTime, peekEndTime);
            peakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(peekStartTime, peekEndTime);
        } else if (PeakOffPeakTimeChecker.isCalculateOffPeakFullDay(now)) {
            offPeakStartTime = LocalDate.now(ZoneId.of("Asia/Taipei")).minusDays(1).atStartOfDay();
            offPeakEndTime = LocalDate.now(ZoneId.of("Asia/Taipei")).minusDays(1).atTime(LocalTime.MAX);

            offPeakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(offPeakStartTime, offPeakEndTime);
            offPeakSumDTO = sensorDataRepository.findPeekSumByTimeRange(offPeakStartTime, offPeakEndTime);
        } else {
            peekStartTime = LocalDateTime.now(ZoneId.of("Asia/Taipei")).minusDays(1).withHour(7).withMinute(30).withSecond(0);
            peekEndTime = LocalDateTime.now(ZoneId.of("Asia/Taipei")).minusDays(1).withHour(17).withMinute(30).withSecond(0);

            offPeakStartTime = LocalDateTime.now(ZoneId.of("Asia/Taipei")).minusDays(1).withHour(17).withMinute(30).withSecond(0);
            offPeakEndTime = LocalDateTime.now(ZoneId.of("Asia/Taipei"));

            peakSumDTO = sensorDataRepository.findPeekSumByTimeRange(peekStartTime, peekEndTime);
            peakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(peekStartTime, peekEndTime);

            offPeakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(offPeakStartTime, offPeakEndTime);
            offPeakSumDTO = sensorDataRepository.findPeekSumByTimeRange(offPeakStartTime, offPeakEndTime);
        }
        try {
            if (peakAverageDTO != null) redisCacheClient.set(REDIS_PEAK_AVERAGE_KEY + "_" + now.toLocalDate(), peakAverageDTO);
            if (peakSumDTO != null) redisCacheClient.set(REDIS_PEAK_SUM_KEY + "_" + now.toLocalDate(), peakSumDTO);
            if (offPeakAverageDTO != null) redisCacheClient.set(REDIS_OFF_PEAK_AVERAGE_KEY + "_" + now.toLocalDate(), offPeakAverageDTO);
            if (offPeakSumDTO != null) redisCacheClient.set(REDIS_OFF_PEAK_SUM_KEY + "_" + now.toLocalDate(), offPeakSumDTO);
        } catch (JsonProcessingException e) {
            log.error("計算平均值時發生異常 {}", e.getMessage());
            throw new CaculateException("計算尖峰、離峰時間平均值時發生異常, 原因:" + e.getMessage());
        }
    }

    /**
     * 計算 每日各項數據總和 並添加到redis cache當中
     */
    @Override
    public void calculateDailySums() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startTime = yesterday.atStartOfDay().atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();
        LocalDateTime endTime = LocalDate.now().atStartOfDay().atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();

        DailySumDTO dailySums = sensorDataRepository.findDailySums(startTime, endTime);
        // 保存到 redis cache當中
        try {
            if (dailySums != null) redisCacheClient.set(REDIS_DAILY_SUM_KEY + "_" + yesterday, dailySums);
        } catch (JsonProcessingException e) {
            log.error("計算總和時發生異常 {}", e.getMessage());
            throw new CaculateException("計算每日加總時發生異常, 原因:" + e.getMessage());
        }
    }

    /**
     * 計算 每小時各項數據總和 並添加到redis cache當中
     */
    @Override
    public void calculateHourlyAverages(LocalDateTime startTime, LocalDateTime endTime) {
        // 確保時區正確
        startTime = startTime.atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();
        endTime = endTime.atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();

        List<HourlyAverageDTO> hourlyAverages = sensorDataRepository.findHourlyAverages(startTime, endTime);
        // 保存到redis cache中
        try {
            if (hourlyAverages != null) redisCacheClient.set(REDIS_HOURLY_AVERAGE_KEY + "_" + startTime.toLocalDate(), hourlyAverages);
        } catch (JsonProcessingException e) {
            log.error("計算每小時平均值時發生異常 {}", e.getMessage());
            throw new CaculateException("計算每小時平均值時發生異常, 原因:" + e.getMessage());
        }
    }

    @Override
    public void calculateHourlySums(LocalDateTime startTime, LocalDateTime endTime) {
        // 確保時區正確
        startTime = startTime.atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();
        endTime = endTime.atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();

        List<HourlySumDTO> hourlySums = sensorDataRepository.findHourlySums(startTime, endTime);
        // 保存到redis cache中
        try {
            if (hourlySums != null) redisCacheClient.set(REDIS_HOURLY_SUM_KEY + "_" + startTime.toLocalDate(), hourlySums);
        } catch (JsonProcessingException e) {
            log.error("計算每小時加總值時發生異常 {}", e.getMessage());
            throw new CaculateException("計算每小時加總值時發生異常, 原因:" + e.getMessage());
        }
    }


    /**
     * 計算 每日各項數據總和 並添加到redis cache當中
     */
    @Override
    public void calculateDailyAverages() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startTime = yesterday.atStartOfDay().atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();
        LocalDateTime endTime = LocalDate.now().atStartOfDay().atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();

        DailyAverageDTO dailyAverageDTO = sensorDataRepository.findDailyAverages(startTime, endTime);
        // 保存到 redis cache當中
        try {
            if (dailyAverageDTO != null) redisCacheClient.set(REDIS_DAILY_AVERAGE_KEY + "_" + yesterday, dailyAverageDTO);
        } catch (JsonProcessingException e) {
            log.error("計算每日平均時發生異常");
            throw new CaculateException("每日數據平均值加入緩存時發生異常,原因:" + e.getMessage());
        }
    }

    /**
     * 查詢符合資料類型還有欄位的數據結果集合
     *
     * @param queryType 查詢類型(尖峰、離峰)
     * @param field    資料欄位
     * @param startTime 起始時間(字串)
     * @param endTime   結束時間(字串)
     * @return List<SensorDataDTO>
     */
    public List<SensorDataDTO> queryData(String queryType, String field, String startTime, String endTime, String selectType, Integer limit) {
        // 轉換 字串時間 為 LocalDateTime
        log.debug("queryData: queryType={}, fields={}, startTime={}, endTime={}", queryType, field, startTime, endTime);

        // 避免前端limit 為null
        if (limit == null || limit == 0) {
            limit = 10;
        }

        LocalDate start = LocalDate.parse(startTime);
        LocalDate end = LocalDate.parse(endTime);
        // 確保時區正確
        start = start.atStartOfDay().atZone(ZoneId.of("Asia/Taipei")).toLocalDate();
        end = end.atStartOfDay().atZone(ZoneId.of("Asia/Taipei")).toLocalDate();

        // 先嘗試從redis cache獲取資料
        String jsonStr = redisCacheClient.get(REDIS_QUERY_KEY_PREFIX + queryType + "_" + selectType + "_" + field + "_" + start + "_" + end);
        if(StringUtils.hasText(jsonStr)) {
            try {
                log.debug("從redis cache獲取數據: {}", jsonStr);
                return JsonUtil.deserialize(jsonStr, new TypeReference<List<SensorDataDTO>>(){});
            } catch (JsonProcessingException e) {
                log.error("從redis cache獲取數據時發生異常 {}", e.getMessage());
            }
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SensorDataDTO> query = cb.createQuery(SensorDataDTO.class);
        Root<SensorData> root = query.from(SensorData.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.between(root.get("obsTime"), start, end));

        // 動態添加查詢條件 (尖峰、離峰)
        switch (queryType) {
            case TIME_PERIOD_PEAK :
                predicates.add(cb.or(
                        cb.and(cb.equal(cb.function("DAYOFWEEK", Integer.class, root.get("obsTime")), 5),
                                cb.equal(cb.function("DAYOFWEEK", Integer.class, root.get("obsTime")), 6)),
                        cb.and(cb.greaterThanOrEqualTo(cb.function("HOUR", Integer.class, root.get("obsTime")), 7),
                                cb.lessThanOrEqualTo(cb.function("HOUR", Integer.class, root.get("obsTime")), 17))
                ));
                break;
            case TIME_PERIOD_OFF_PEAK :
                predicates.add(cb.or(
                        cb.equal(cb.function("DAYOFWEEK", Integer.class, root.get("obsTime")), 1),
                        cb.equal(cb.function("DAYOFWEEK", Integer.class, root.get("obsTime")), 7),
                        cb.or(cb.lessThan(cb.function("HOUR", Integer.class, root.get("obsTime")), 7),
                                cb.greaterThan(cb.function("HOUR", Integer.class, root.get("obsTime")), 17))
                ));
                break;
        }

        // 創建一個 List 來動態構建選擇部分
        List<Selection<?>> selections = new ArrayList<>();

        // 根據 selectType 決定是加總還是平均
        switch (field) {
            case SENSOR_ALL_FIELD:
                if (SELECT_TYPE_SUM.equals(selectType)) {
                    selections.add(cb.sum(root.get("sensor").get("volt").get("v1")));
                    selections.add(cb.sum(root.get("sensor").get("volt").get("v5")));
                    selections.add(cb.sum(root.get("sensor").get("volt").get("v6")));
                    selections.add(cb.sum(root.get("sensor").get("stickTxRh").get("tx")));
                    selections.add(cb.sum(root.get("sensor").get("stickTxRh").get("rh")));
                    selections.add(cb.sum(root.get("sensor").get("ultrasonicLevel").get("echo")));
                    selections.add(cb.sum(root.get("sensor").get("waterSpeedAquark").get("speed")));
                    selections.add(cb.sum(root.get("rainD")));
                } else if (SELECT_TYPE_AVG.equals(selectType)) {
                    selections.add(cb.avg(root.get("sensor").get("volt").get("v1")));
                    selections.add(cb.avg(root.get("sensor").get("volt").get("v5")));
                    selections.add(cb.avg(root.get("sensor").get("volt").get("v6")));
                    selections.add(cb.avg(root.get("sensor").get("stickTxRh").get("tx")));
                    selections.add(cb.avg(root.get("sensor").get("stickTxRh").get("rh")));
                    selections.add(cb.avg(root.get("sensor").get("ultrasonicLevel").get("echo")));
                    selections.add(cb.avg(root.get("sensor").get("waterSpeedAquark").get("speed")));
                    selections.add(cb.avg(root.get("rainD")));
                }
                break;
            case SENSOR_FIELD_NAME_V1:
                selections.add(SELECT_TYPE_SUM.equals(selectType) ? cb.sum(root.get("sensor").get("volt").get("v1")) : cb.avg(root.get("sensor").get("volt").get("v1")));
                break;
            case SENSOR_FIELD_NAME_V5:
                selections.add(SELECT_TYPE_SUM.equals(selectType) ? cb.sum(root.get("sensor").get("volt").get("v5")) : cb.avg(root.get("sensor").get("volt").get("v5")));
                break;
            case SENSOR_FIELD_NAME_V6:
                selections.add(SELECT_TYPE_SUM.equals(selectType) ? cb.sum(root.get("sensor").get("volt").get("v6")) : cb.avg(root.get("sensor").get("volt").get("v6")));
                break;
            case SENSOR_FIELD_NAME_TX:
                selections.add(SELECT_TYPE_SUM.equals(selectType) ? cb.sum(root.get("sensor").get("stickTxRh").get("tx")) : cb.avg(root.get("sensor").get("stickTxRh").get("tx")));
                break;
            case SENSOR_FIELD_NAME_RH:
                selections.add(SELECT_TYPE_SUM.equals(selectType) ? cb.sum(root.get("sensor").get("stickTxRh").get("rh")) : cb.avg(root.get("sensor").get("stickTxRh").get("rh")));
                break;
            case SENSOR_FIELD_NAME_ECHO:
                selections.add(SELECT_TYPE_SUM.equals(selectType) ? cb.sum(root.get("sensor").get("ultrasonicLevel").get("echo")) : cb.avg(root.get("sensor").get("ultrasonicLevel").get("echo")));
                break;
            case SENSOR_FIELD_NAME_SPEED:
                selections.add(SELECT_TYPE_SUM.equals(selectType) ? cb.sum(root.get("sensor").get("waterSpeedAquark").get("speed")) : cb.avg(root.get("sensor").get("waterSpeedAquark").get("speed")));
                break;
            case SENSOR_FIELD_NAME_RAIN_D:
                selections.add(SELECT_TYPE_SUM.equals(selectType) ? cb.sum(root.get("rainD")) : cb.avg(root.get("rainD")));
                break;
            default:
                throw new IllegalArgumentException("不支持的查詢欄位: " + field);
        }


        query.multiselect(selections);
        // 構建查詢選擇的欄位
        query.select(cb.construct(
                SensorDataDTO.class,
                root.get("obsTime"),
                root.get("sensor").get("volt").get("v1"),
                root.get("sensor").get("volt").get("v5"),
                root.get("sensor").get("volt").get("v6"),
                root.get("rainD"),
                root.get("sensor").get("stickTxRh").get("tx"),
                root.get("sensor").get("stickTxRh").get("rh"),
                root.get("sensor").get("ultrasonicLevel").get("echo"),
                root.get("sensor").get("waterSpeedAquark").get("speed")
        ));

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get("obsTime")));
        try {
            final String redisKey = REDIS_QUERY_KEY_PREFIX + queryType + "_" + selectType + "_" + field + "_" + start + "_" + end;
            // 先存取資料 如果之後一樣的數據從redis 獲取
            List<SensorDataDTO> resultList = entityManager.createQuery(query).setMaxResults(limit).getResultList();
            redisCacheClient.set(redisKey, resultList,60L, TimeUnit.SECONDS);
            return resultList;
        } catch (NoResultException | JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    /**
     * 取得特定某天的數據值 做圖表分析
     *
     * @param date 特定天數
     * @return Map<String, Object>
     */
    @Override
    public Map<String, Object> queryDashboardData(String date) {
        // 轉換成 LocalDate
        LocalDate localDate = LocalDate.parse(date);
        HashMap<String, Object> result = new HashMap<>();

        // 取得當時時間的一整天
        LocalDateTime startTime = localDate.atStartOfDay().atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();
        LocalDateTime endTime = localDate.plusDays(1).atStartOfDay(ZoneId.of("Asia/Taipei")).toLocalDateTime();

        // 先從redis cache 取得對應資料 若沒有資料則再從db獲取
        try {
            // 每日
            DailySumDTO dailySumDTO = JsonUtil.deserialize(redisCacheClient.get(REDIS_DAILY_SUM_KEY + "_" + localDate), DailySumDTO.class);
            DailyAverageDTO dailyAverageDTO = JsonUtil.deserialize(redisCacheClient.get(REDIS_DAILY_AVERAGE_KEY + "_" + localDate), DailyAverageDTO.class);
            // 每小時
            List<HourlySumDTO> hourlySumDTO = JsonUtil.deserialize(redisCacheClient.get(REDIS_HOURLY_SUM_KEY + "_" + localDate), new TypeReference<List<HourlySumDTO>>() {
            });
            List<HourlyAverageDTO> hourlyAverageDTO = JsonUtil.deserialize(redisCacheClient.get(REDIS_HOURLY_AVERAGE_KEY + "_" + localDate), new TypeReference<List<HourlyAverageDTO>>() {
            });

            // 尖峰、離峰
            PeakOffPeakSumDTO peakSumDTO = JsonUtil.deserialize(redisCacheClient.get(REDIS_PEAK_SUM_KEY + "_" + localDate), PeakOffPeakSumDTO.class);
            PeakOffPeakAverageDTO peakAverageDTO = JsonUtil.deserialize(redisCacheClient.get(REDIS_PEAK_AVERAGE_KEY + "_" + localDate), PeakOffPeakAverageDTO.class);
            PeakOffPeakSumDTO offPeakSumDTO = JsonUtil.deserialize(redisCacheClient.get(REDIS_OFF_PEAK_SUM_KEY + "_" + localDate), PeakOffPeakSumDTO.class);
            PeakOffPeakAverageDTO offPeakAverageDTO = JsonUtil.deserialize(redisCacheClient.get(REDIS_OFF_PEAK_AVERAGE_KEY + "_" + localDate), PeakOffPeakAverageDTO.class);

            // 如果沒有值 則向db查詢
            if (dailySumDTO == null) {
                dailySumDTO = sensorDataRepository.findDailySums(startTime, endTime);
                redisCacheClient.set(REDIS_DAILY_SUM_KEY + "_" + localDate, dailySumDTO, 60L, TimeUnit.SECONDS);
            }
            if (dailyAverageDTO == null) {
                dailyAverageDTO = sensorDataRepository.findDailyAverages(startTime, endTime);
                redisCacheClient.set(REDIS_DAILY_AVERAGE_KEY + "_" + localDate, dailyAverageDTO, 60L, TimeUnit.SECONDS);
            }
            if (hourlySumDTO == null || hourlySumDTO.isEmpty()) {
                hourlySumDTO = sensorDataRepository.findHourlySums(startTime, endTime);
                redisCacheClient.set(REDIS_HOURLY_SUM_KEY + "_" + localDate, hourlySumDTO, 60L, TimeUnit.SECONDS);
            }
            if (hourlyAverageDTO == null || hourlyAverageDTO.isEmpty()) {
                hourlyAverageDTO = sensorDataRepository.findHourlyAverages(startTime, endTime);
                redisCacheClient.set(REDIS_HOURLY_AVERAGE_KEY + "_" + localDate, hourlyAverageDTO, 60L, TimeUnit.SECONDS);
            }
            // 驗證這天是禮拜幾 還有 時間段
            if (PeakOffPeakTimeChecker.isCalculatePeakFullDay(startTime)) {
                // 全天的尖峰時段
                if (peakSumDTO == null) {
                    peakSumDTO = sensorDataRepository.findPeekSumByTimeRange(startTime, endTime);
                    redisCacheClient.set(REDIS_PEAK_SUM_KEY + "_" + localDate, peakSumDTO, 60L, TimeUnit.SECONDS);
                }
                if (peakAverageDTO == null) {
                    peakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(startTime, endTime);
                    redisCacheClient.set(REDIS_PEAK_AVERAGE_KEY + "_" + localDate, peakAverageDTO, 60L, TimeUnit.SECONDS);
                }
            } else if (PeakOffPeakTimeChecker.isCalculateOffPeakFullDay(startTime)) {
                if(offPeakAverageDTO == null) {
                    offPeakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(startTime,endTime);
                    redisCacheClient.set(REDIS_OFF_PEAK_AVERAGE_KEY + "_" + localDate, offPeakAverageDTO, 60L, TimeUnit.SECONDS);
                }
                if(offPeakSumDTO == null) {
                    offPeakSumDTO =   sensorDataRepository.findPeekSumByTimeRange(startTime, endTime);
                    redisCacheClient.set(REDIS_OFF_PEAK_SUM_KEY + "_" + localDate, offPeakSumDTO, 60L, TimeUnit.SECONDS);
                }
            } else {
                // 上午七點半到下午五點半
                LocalDateTime peekStartTime = localDate.atTime(7, 30);
                LocalDateTime peekEndTime = localDate.atTime(17, 30);

                // 離峰時段 下午五點半到晚上12點
                // 還有 00:00 到早上 7:30
                LocalDateTime offPeakStartTime = localDate.atTime(17, 30);
                LocalDateTime offPeakEndTime = localDate.plusDays(1).atStartOfDay();

                LocalDateTime offPeakStartTime2 = localDate.minusDays(1).atStartOfDay();
                LocalDateTime offPeakEndTime2 = localDate.atTime(7, 30);

                ZoneId zoneId = ZoneId.of("Asia/Taipei");
                peekStartTime = peekStartTime.atZone(zoneId).toLocalDateTime();
                peekEndTime = peekEndTime.atZone(zoneId).toLocalDateTime();
                offPeakStartTime = offPeakStartTime.atZone(zoneId).toLocalDateTime();
                offPeakEndTime = offPeakEndTime.atZone(zoneId).toLocalDateTime();
                offPeakStartTime2 = offPeakStartTime2.atZone(zoneId).toLocalDateTime();
                offPeakEndTime2 = offPeakEndTime2.atZone(zoneId).toLocalDateTime();

                if(peakSumDTO == null) {
                    peakSumDTO = sensorDataRepository.findPeekSumByTimeRange(peekStartTime, peekEndTime);
                    redisCacheClient.set(REDIS_PEAK_SUM_KEY + "_" + localDate, JsonUtil.serialize(peakSumDTO), 1L, TimeUnit.MINUTES);
                }
                if(peakAverageDTO == null) {
                    peakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(peekStartTime, peekEndTime);
                    redisCacheClient.set(REDIS_PEAK_AVERAGE_KEY + "_" + localDate, JsonUtil.serialize(peakAverageDTO), 1L, TimeUnit.MINUTES);
                }
                if(offPeakAverageDTO == null) {
                    offPeakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(offPeakStartTime, offPeakEndTime);
                    // 查詢 00:00 到早上 7:30 的數據
                    PeakOffPeakAverageDTO offPeakAverageDTO2 = sensorDataRepository.findPeakOffPeakAvgByTimeRange(offPeakStartTime2, offPeakEndTime2);
                    // 把 00:00 到 早上7:30 還有 5:30 到 00:00的數據相加
                    offPeakAverageDTO = sumPeakOffPeakAverageDTO(offPeakAverageDTO, offPeakAverageDTO2);
                    redisCacheClient.set(REDIS_OFF_PEAK_AVERAGE_KEY + "_" + localDate, JsonUtil.serialize(offPeakAverageDTO), 1L, TimeUnit.MINUTES);
                }
                if(offPeakSumDTO == null) {
                    offPeakSumDTO = sensorDataRepository.findPeekSumByTimeRange(offPeakStartTime, offPeakEndTime);
                    PeakOffPeakSumDTO offPeakSumDTO2 = sensorDataRepository.findPeekSumByTimeRange(offPeakStartTime2, offPeakEndTime2);
                    // 把 00:00 到 早上7:30 還有 5:30 到 00:00的數據相加
                    offPeakSumDTO = sumPeakOffPeakSumDTO(offPeakSumDTO, offPeakSumDTO2);
                    redisCacheClient.set(REDIS_OFF_PEAK_SUM_KEY + "_" + localDate, JsonUtil.serialize(offPeakSumDTO), 1L, TimeUnit.MINUTES);
                }
            }

            result.put("dailySumDTO", dailySumDTO);
            result.put("dailyAverageDTO", dailyAverageDTO);
            result.put("hourlySumDTO", hourlySumDTO);
            result.put("hourlyAverageDTO", hourlyAverageDTO);
            result.put("peakSumDTO", peakSumDTO);
            result.put("peakAverageDTO", peakAverageDTO);
            result.put("offPeakSumDTO", offPeakSumDTO);
            result.put("offPeakAverageDTO", offPeakAverageDTO);
        } catch (JsonProcessingException e) {
            throw new CaculateException("反序列化時發生異常", e);
        }
        return result;
    }

    /**
     * 取得每日平均跟加總的相關數據
     * @param date 查詢時段 yyyy-MM-dd
     * @return Map<String, Object>
     */
    @Override
    public Map<String, Object> queryDailyAnalysis(String date) {
        LocalDate localDate = LocalDate.parse(date);
        HashMap<String, Object> result = new HashMap<>();

        // 取得當時時間的一整天
        LocalDateTime startTime = localDate.atStartOfDay().atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();
        LocalDateTime endTime = localDate.plusDays(1).atStartOfDay(ZoneId.of("Asia/Taipei")).toLocalDateTime();

        // 每日
        try {
            DailySumDTO dailySumDTO = JsonUtil.deserialize(redisCacheClient.get(REDIS_DAILY_SUM_KEY + "_" + localDate), DailySumDTO.class);
            DailyAverageDTO dailyAverageDTO = JsonUtil.deserialize(redisCacheClient.get(REDIS_DAILY_AVERAGE_KEY + "_" + localDate), DailyAverageDTO.class);

            if (dailySumDTO == null) {
                dailySumDTO = sensorDataRepository.findDailySums(startTime, endTime);
                redisCacheClient.set(REDIS_DAILY_SUM_KEY + "_" + localDate, JsonUtil.serialize(dailySumDTO), 60L, TimeUnit.SECONDS);
            }
            if (dailyAverageDTO == null) {
                dailyAverageDTO = sensorDataRepository.findDailyAverages(startTime, endTime);
                redisCacheClient.set(REDIS_DAILY_AVERAGE_KEY + "_" + localDate, JsonUtil.serialize(dailyAverageDTO), 60L, TimeUnit.SECONDS);
            }

            result.put("dailySumDTO", dailySumDTO);
            result.put("dailyAverageDTO", dailyAverageDTO);
        } catch (JsonProcessingException e) {
            throw new CaculateException("反序列化時發生異常", e);
        }

        return result;
    }

    /**
     * 取得每小時平均跟加總的相關數據
     * @param date 查詢時段 yyyy-MM-dd
     * @return Map<String, Object>
     */
    @Override
    public Map<String, Object> queryHourlyAnalysis(String date) {
        LocalDate localDate = LocalDate.parse(date);
        HashMap<String, Object> result = new HashMap<>();

        // 取得當時時間的一整天
        LocalDateTime startTime = localDate.atStartOfDay().atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();
        LocalDateTime endTime = localDate.plusDays(1).atStartOfDay(ZoneId.of("Asia/Taipei")).toLocalDateTime();

        try {
            List<HourlySumDTO> hourlySumDTO = JsonUtil.deserialize(redisCacheClient.get(REDIS_HOURLY_SUM_KEY + "_" + localDate), new TypeReference<List<HourlySumDTO>>() {
            });
            List<HourlyAverageDTO> hourlyAverageDTO = JsonUtil.deserialize(redisCacheClient.get(REDIS_HOURLY_AVERAGE_KEY + "_" + localDate), new TypeReference<List<HourlyAverageDTO>>() {
            });

            if (hourlySumDTO == null || hourlySumDTO.isEmpty()) {
                hourlySumDTO = sensorDataRepository.findHourlySums(startTime, endTime);
                redisCacheClient.set(REDIS_HOURLY_SUM_KEY + "_" + localDate, JsonUtil.serialize(hourlySumDTO),1L,TimeUnit.MINUTES);
            }
            if (hourlyAverageDTO == null || hourlyAverageDTO.isEmpty()) {
                hourlyAverageDTO = sensorDataRepository.findHourlyAverages(startTime, endTime);
                redisCacheClient.set(REDIS_HOURLY_AVERAGE_KEY + "_" + localDate, JsonUtil.serialize(hourlyAverageDTO),1L,TimeUnit.MINUTES);
            }

            result.put("hourlySumDTO", hourlySumDTO);
            result.put("hourlyAverageDTO", hourlyAverageDTO);
        } catch (JsonProcessingException e) {
            throw new CaculateException("反序列化時發生異常", e);
        }

        return result;
    }

    @Override
    public Map<String, Object> queryOffPeakAnalysis(String date) {
        LocalDate localDate = LocalDate.parse(date);
        HashMap<String, Object> result = new HashMap<>();

        // 取得當時時間的一整天
        LocalDateTime startTime = localDate.atStartOfDay().atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();
        LocalDateTime endTime = localDate.plusDays(1).atStartOfDay(ZoneId.of("Asia/Taipei")).toLocalDateTime();

        try {
            PeakOffPeakSumDTO offPeakSumDTO = JsonUtil.deserialize(redisCacheClient.get(REDIS_OFF_PEAK_SUM_KEY + "_" + localDate), PeakOffPeakSumDTO.class);
            PeakOffPeakAverageDTO offPeakAverageDTO = JsonUtil.deserialize(redisCacheClient.get(REDIS_OFF_PEAK_AVERAGE_KEY + "_" + localDate), PeakOffPeakAverageDTO.class);

            if (PeakOffPeakTimeChecker.isCalculateOffPeakFullDay(startTime)) {
                if(offPeakAverageDTO == null) {
                    offPeakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(startTime,endTime);
                    redisCacheClient.set(REDIS_OFF_PEAK_AVERAGE_KEY + "_" + localDate, JsonUtil.serialize(offPeakAverageDTO), 1L, TimeUnit.MINUTES);
                }
                if(offPeakSumDTO == null) {
                    offPeakSumDTO =   sensorDataRepository.findPeekSumByTimeRange(startTime, endTime);
                    redisCacheClient.set(REDIS_OFF_PEAK_SUM_KEY + "_" + localDate, JsonUtil.serialize(offPeakSumDTO), 1L, TimeUnit.MINUTES);
                }
            } else {

                ZoneId zoneId = ZoneId.of("Asia/Taipei");
                // 離峰時段 下午五點半到晚上12點
                // 還有 00:00 到早上 7:30
                LocalDateTime offPeakStartTime = localDate.atTime(17, 30);
                LocalDateTime offPeakEndTime = localDate.plusDays(1).atStartOfDay();

                LocalDateTime offPeakStartTime2 = localDate.minusDays(1).atStartOfDay();
                LocalDateTime offPeakEndTime2 = localDate.atTime(7, 30);

                offPeakStartTime = offPeakStartTime.atZone(zoneId).toLocalDateTime();
                offPeakEndTime = offPeakEndTime.atZone(zoneId).toLocalDateTime();
                offPeakStartTime2 = offPeakStartTime2.atZone(zoneId).toLocalDateTime();
                offPeakEndTime2 = offPeakEndTime2.atZone(zoneId).toLocalDateTime();

                if(offPeakAverageDTO == null) {
                    offPeakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(offPeakStartTime, offPeakEndTime);
                    // 查詢 00:00 到早上 7:30 的數據
                    PeakOffPeakAverageDTO offPeakAverageDTO2 = sensorDataRepository.findPeakOffPeakAvgByTimeRange(offPeakStartTime2, offPeakEndTime2);
                    // 把 00:00 到 早上7:30 還有 5:30 到 00:00的數據相加
                    offPeakAverageDTO = sumPeakOffPeakAverageDTO(offPeakAverageDTO, offPeakAverageDTO2);
                    redisCacheClient.set(REDIS_OFF_PEAK_AVERAGE_KEY + "_" + localDate, JsonUtil.serialize(offPeakAverageDTO), 1L, TimeUnit.MINUTES);
                }
                if(offPeakSumDTO == null) {
                    offPeakSumDTO = sensorDataRepository.findPeekSumByTimeRange(offPeakStartTime, offPeakEndTime);
                    PeakOffPeakSumDTO offPeakSumDTO2 = sensorDataRepository.findPeekSumByTimeRange(offPeakStartTime2, offPeakEndTime2);
                    // 把 00:00 到 早上7:30 還有 5:30 到 00:00的數據相加
                    offPeakSumDTO = sumPeakOffPeakSumDTO(offPeakSumDTO, offPeakSumDTO2);
                    redisCacheClient.set(REDIS_OFF_PEAK_SUM_KEY + "_" + localDate, JsonUtil.serialize(offPeakSumDTO), 1L, TimeUnit.MINUTES);
                }
            }
            result.put("offPeakSumDTO", offPeakSumDTO);
            result.put("offPeakAverageDTO", offPeakAverageDTO);
        } catch (JsonProcessingException e) {
            throw new CaculateException("反序列化時發生異常",e);
        }

        return result;
    }

    @Override
    public Map<String, Object> queryPeakAnalysis(String date) {
        LocalDate localDate = LocalDate.parse(date);
        HashMap<String, Object> result = new HashMap<>();

        // 取得當時時間的一整天
        LocalDateTime startTime = localDate.atStartOfDay().atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();
        LocalDateTime endTime = localDate.plusDays(1).atStartOfDay(ZoneId.of("Asia/Taipei")).toLocalDateTime();

        try {
            PeakOffPeakSumDTO peakSumDTO = JsonUtil.deserialize(redisCacheClient.get(REDIS_PEAK_SUM_KEY + "_" + localDate), PeakOffPeakSumDTO.class);
            PeakOffPeakAverageDTO peakAverageDTO = JsonUtil.deserialize(redisCacheClient.get(REDIS_PEAK_AVERAGE_KEY + "_" + localDate), PeakOffPeakAverageDTO.class);

            if (PeakOffPeakTimeChecker.isCalculatePeakFullDay(startTime)) {
                if(peakAverageDTO == null) {
                    peakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(startTime,endTime);
                    redisCacheClient.set(REDIS_PEAK_AVERAGE_KEY + "_" + localDate, JsonUtil.serialize(peakAverageDTO), 1L, TimeUnit.MINUTES);
                }
                if(peakSumDTO == null) {
                    peakSumDTO =   sensorDataRepository.findPeekSumByTimeRange(startTime, endTime);
                    redisCacheClient.set(REDIS_PEAK_SUM_KEY + "_" + localDate, JsonUtil.serialize(peakSumDTO), 1L, TimeUnit.MINUTES);
                }
            } else {
                // 上午七點半到下午五點半
                LocalDateTime peekStartTime = localDate.atTime(7, 30);
                LocalDateTime peekEndTime = localDate.atTime(17, 30);

                ZoneId zoneId = ZoneId.of("Asia/Taipei");
                peekStartTime = peekStartTime.atZone(zoneId).toLocalDateTime();
                peekEndTime = peekEndTime.atZone(zoneId).toLocalDateTime();

                if(peakSumDTO == null) {
                    peakSumDTO = sensorDataRepository.findPeekSumByTimeRange(peekStartTime, peekEndTime);
                    redisCacheClient.set(REDIS_PEAK_SUM_KEY + "_" + localDate, JsonUtil.serialize(peakSumDTO), 1L, TimeUnit.MINUTES);
                }
                if(peakAverageDTO == null) {
                    peakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(peekStartTime, peekEndTime);
                    redisCacheClient.set(REDIS_PEAK_AVERAGE_KEY + "_" + localDate, JsonUtil.serialize(peakAverageDTO), 1L, TimeUnit.MINUTES);
                }
            }
            result.put("peakSumDTO", peakSumDTO);
            result.put("peakAverageDTO", peakAverageDTO);
        } catch (JsonProcessingException e) {
            throw new CaculateException("反序列化時發生異常",e);
        }

        return result;
    }

    private PeakOffPeakSumDTO sumPeakOffPeakSumDTO(PeakOffPeakSumDTO dto1, PeakOffPeakSumDTO dto2) {
        PeakOffPeakSumDTO result = new PeakOffPeakSumDTO();
        result.setSumTx(dto1.getSumTx() + dto2.getSumTx());
        result.setSumRh(dto1.getSumRh() + dto2.getSumRh());
        result.setSumV1(dto1.getSumV1() + dto2.getSumV1());
        result.setSumV5(dto1.getSumV5() + dto2.getSumV5());
        result.setSumV6(dto1.getSumV6() + dto2.getSumV6());
        result.setSumEcho(dto1.getSumEcho() + dto2.getSumEcho());
        result.setSumRain_d(dto1.getSumRain_d() + dto2.getSumRain_d());
        result.setSumHourSpeed(dto1.getSumHourSpeed() + dto2.getSumHourSpeed());
        return result;
    }

    private PeakOffPeakAverageDTO sumPeakOffPeakAverageDTO(PeakOffPeakAverageDTO dto1, PeakOffPeakAverageDTO dto2) {
        PeakOffPeakAverageDTO result = new PeakOffPeakAverageDTO();
        result.setAvgEcho(dto1.getAvgEcho() + dto2.getAvgEcho());
        result.setAvgSpeed(dto1.getAvgSpeed() + dto2.getAvgSpeed());
        result.setAvgRainD(dto1.getAvgRainD() + dto2.getAvgRainD());
        result.setAvgV1(dto1.getAvgV1() + dto2.getAvgV1());
        result.setAvgV5(dto1.getAvgV5() + dto2.getAvgV5());
        result.setAvgV6(dto1.getAvgV6() + dto2.getAvgV6());
        result.setAvgTx(dto1.getAvgTx() + dto2.getAvgTx());
        result.setAvgRh(dto1.getAvgRh() + dto2.getAvgRh());
        return result;
    }

}
