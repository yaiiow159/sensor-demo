package com.example.aquarkdemo.service.impl;

import com.example.aquarkdemo.aspect.CountTime;
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
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    @CountTime
    @Transactional(readOnly = true, rollbackFor = CaculateException.class)
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
        PeakOffPeakSumDTO offPeakSumDTO1;
        PeakOffPeakAverageDTO offPeakAverageDTO1;
        LocalDateTime offPeakStartTime1;
        LocalDateTime offPeakEndTime1;

        // 驗證是否是週五 周五尖峰值段為全天
        if (PeakOffPeakTimeChecker.isCalculatePeakFullDay(now)) {
            peekStartTime = LocalDate.now(ZoneId.of("Asia/Taipei")).minusDays(1).atStartOfDay();
            peekEndTime = now;

            peakSumDTO = sensorDataRepository.findPeekSumByTimeRange(peekStartTime, peekEndTime);
            peakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(peekStartTime, peekEndTime);
        } else if (PeakOffPeakTimeChecker.isCalculateOffPeakFullDay(now)) {
            offPeakStartTime = now.minusDays(1);
            offPeakEndTime = now;

            offPeakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(offPeakStartTime, offPeakEndTime);
            offPeakSumDTO = sensorDataRepository.findPeekSumByTimeRange(offPeakStartTime, offPeakEndTime);
        } else {
            // 7:30 到 17:30
            peekStartTime = now.minusDays(1).withHour(7).withMinute(30).withSecond(0);
            peekEndTime = now.minusDays(1).withHour(17).withMinute(30).withSecond(0);
            // 00:00 到 7:30
            offPeakStartTime = now.minusDays(1).withHour(0).withMinute(0).withSecond(0);
            offPeakEndTime = now.minusDays(1).withHour(7).withMinute(30).withSecond(0);

            // 17:30 到 24:00
            offPeakStartTime1 = now.minusDays(1).withHour(17).withMinute(30).withSecond(0);
            offPeakEndTime1 = now;

            peakSumDTO = sensorDataRepository.findPeekSumByTimeRange(peekStartTime, peekEndTime);
            peakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(peekStartTime, peekEndTime);

            offPeakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(offPeakStartTime, offPeakEndTime);
            offPeakSumDTO = sensorDataRepository.findPeekSumByTimeRange(offPeakStartTime, offPeakEndTime);
            offPeakAverageDTO1 = sensorDataRepository.findPeakOffPeakAvgByTimeRange(offPeakStartTime1, offPeakEndTime1);
            offPeakSumDTO1 = sensorDataRepository.findPeekSumByTimeRange(offPeakStartTime1, offPeakEndTime1);

            offPeakAverageDTO = sumPeakOffPeakAverageDTO(offPeakAverageDTO, offPeakAverageDTO1);
            offPeakSumDTO = sumPeakOffPeakSumDTO(offPeakSumDTO, offPeakSumDTO1);
        }
        try {
            if (peakAverageDTO != null)
                redisCacheClient.set(REDIS_PEAK_AVERAGE_KEY + "_" + now.toLocalDate().minusDays(1), peakAverageDTO, 1L, TimeUnit.DAYS);
            if (peakSumDTO != null)
                redisCacheClient.set(REDIS_PEAK_SUM_KEY + "_" + now.toLocalDate().minusDays(1), peakSumDTO, 1L, TimeUnit.DAYS);
            if (offPeakAverageDTO != null)
                redisCacheClient.set(REDIS_OFF_PEAK_AVERAGE_KEY + "_" + now.toLocalDate().minusDays(1), offPeakAverageDTO, 1L, TimeUnit.DAYS);
            if (offPeakSumDTO != null)
                redisCacheClient.set(REDIS_OFF_PEAK_SUM_KEY + "_" + now.toLocalDate().minusDays(1), offPeakSumDTO, 1L, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            log.error("計算平均值時發生異常 {}", e.getMessage());
            throw new CaculateException("計算尖峰、離峰時間平均值時發生異常, 原因:" + e.getMessage());
        }
    }

    /**
     * 計算 每日各項數據總和 並添加到redis cache當中
     */
    @Override
    @CountTime
    @Transactional(readOnly = true, rollbackFor = CaculateException.class)
    public void calculateDailySums() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startTime = yesterday.atStartOfDay().atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();
        LocalDateTime endTime = LocalDate.now().atStartOfDay().atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();

        DailySumDTO dailySums = sensorDataRepository.findDailySums(startTime, endTime);
        // 保存到 redis cache當中
        try {
            if (dailySums != null)
                redisCacheClient.set(REDIS_DAILY_SUM_KEY + "_" + yesterday, dailySums, 1L, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            log.error("計算總和時發生異常 {}", e.getMessage());
            throw new CaculateException("計算每日加總時發生異常, 原因:" + e.getMessage());
        }
    }

    /**
     * 計算 每小時各項數據總和 並添加到redis cache當中
     */
    @Override
    @CountTime
    @Transactional(readOnly = true, rollbackFor = CaculateException.class)
    public void calculateHourlyAverages(LocalDateTime startTime, LocalDateTime endTime) {
        // 確保時區正確
        startTime = startTime.atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();
        endTime = endTime.atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();

        List<HourlyAverageDTO> hourlyAverages = sensorDataRepository.findHourlyAverages(startTime, endTime);
        // 保存到redis cache中
        try {
            if (hourlyAverages != null && !hourlyAverages.isEmpty())
                redisCacheClient.setToList(REDIS_HOURLY_AVERAGE_KEY + "_" + startTime.toLocalDate(), hourlyAverages, 1L, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            log.error("計算每小時平均值時發生異常 {}", e.getMessage());
            throw new CaculateException("計算每小時平均值時發生異常, 原因:" + e.getMessage());
        }
    }

    @Override
    @CountTime
    @Transactional(readOnly = true, rollbackFor = CaculateException.class)
    public void calculateHourlySums(LocalDateTime startTime, LocalDateTime endTime) {
        // 確保時區正確
        startTime = startTime.atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();
        endTime = endTime.atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();

        List<HourlySumDTO> hourlySums = sensorDataRepository.findHourlySums(startTime, endTime);
        // 保存到redis cache中
        try {
            if (hourlySums != null && !hourlySums.isEmpty())
                redisCacheClient.setToList(REDIS_HOURLY_SUM_KEY + "_" + startTime.toLocalDate().minusDays(1), hourlySums, 1L, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            log.error("計算每小時加總值時發生異常 {}", e.getMessage());
            throw new CaculateException("計算每小時加總值時發生異常, 原因:" + e.getMessage());
        }
    }


    /**
     * 計算 每日各項數據總和 並添加到redis cache當中
     */
    @Override
    @CountTime
    @Transactional(readOnly = true, rollbackFor = CaculateException.class)
    public void calculateDailyAverages() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startTime = yesterday.atStartOfDay().atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();
        LocalDateTime endTime = LocalDate.now().atStartOfDay().atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();

        DailyAverageDTO dailyAverageDTO = sensorDataRepository.findDailyAverages(startTime, endTime);
        // 保存到 redis cache當中
        try {
            if (dailyAverageDTO != null)
                redisCacheClient.set(REDIS_DAILY_AVERAGE_KEY + "_" + yesterday.minusDays(1), dailyAverageDTO, 1L, TimeUnit.DAYS);
        } catch (JsonProcessingException e) {
            log.error("計算每日平均時發生異常");
            throw new CaculateException("每日數據平均值加入緩存時發生異常,原因:" + e.getMessage());
        }
    }

    /**
     * 查詢符合資料類型還有欄位的數據結果集合
     *
     * @param queryType 查詢類型(尖峰、離峰)
     * @param field     資料欄位
     * @param startTime 起始時間(字串)
     * @param endTime   結束時間(字串)
     * @return Object
     */
    @Override
    @CountTime
    @Transactional(readOnly = true, rollbackFor = CaculateException.class)
    public Object queryData(String queryType, String field, String startTime, String endTime, Integer limit) {
        log.debug("queryData: queryType={}, fields={}, startTime={}, endTime={}", queryType, field, startTime, endTime);

        if (limit == null || limit == 0) {
            limit = 10;
        }

        // 從redis 獲取
        Object list = redisCacheClient.get(REDIS_QUERY_KEY_PREFIX + queryType + "_" + field + "_" + startTime + "-" + endTime);
        if (list != null) {
            return list;
        }

        LocalDateTime start =LocalDate.parse(startTime).atStartOfDay().atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();
        LocalDateTime end = LocalDate.parse(endTime).atStartOfDay().atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object> query = cb.createQuery(Object.class);
        Root<SensorData> root = query.from(SensorData.class);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.between(root.get("obsTime"), start, end));

        // 動態添加查詢條件 (尖峰、離峰)
        if (TIME_PERIOD_PEAK.equals(queryType)) {
            predicates.add(cb.or(
                    cb.and(cb.equal(cb.function("DAYOFWEEK", Integer.class, root.get("obsTime")), Calendar.MONDAY),
                            cb.between(cb.function("HOUR", Integer.class, root.get("obsTime")), 7, 17)),
                    cb.and(cb.equal(cb.function("DAYOFWEEK", Integer.class, root.get("obsTime")), Calendar.TUESDAY),
                            cb.between(cb.function("HOUR", Integer.class, root.get("obsTime")), 7, 17)),
                    cb.and(cb.equal(cb.function("DAYOFWEEK", Integer.class, root.get("obsTime")), Calendar.WEDNESDAY),
                            cb.between(cb.function("HOUR", Integer.class, root.get("obsTime")), 7, 17)),
                    cb.equal(cb.function("DAYOFWEEK", Integer.class, root.get("obsTime")), Calendar.THURSDAY),
                    cb.equal(cb.function("DAYOFWEEK", Integer.class, root.get("obsTime")), Calendar.FRIDAY)
            ));
        } else if (TIME_PERIOD_OFF_PEAK.equals(queryType)) {
            predicates.add(cb.or(
                    cb.and(cb.equal(cb.function("DAYOFWEEK", Integer.class, root.get("obsTime")), Calendar.MONDAY),
                            cb.lessThan(cb.function("HOUR", Integer.class, root.get("obsTime")), 7)),
                    cb.and(cb.equal(cb.function("DAYOFWEEK", Integer.class, root.get("obsTime")), Calendar.MONDAY),
                            cb.greaterThan(cb.function("HOUR", Integer.class, root.get("obsTime")), 17)),
                    cb.and(cb.equal(cb.function("DAYOFWEEK", Integer.class, root.get("obsTime")), Calendar.TUESDAY),
                            cb.lessThan(cb.function("HOUR", Integer.class, root.get("obsTime")), 7)),
                    cb.and(cb.equal(cb.function("DAYOFWEEK", Integer.class, root.get("obsTime")), Calendar.TUESDAY),
                            cb.greaterThan(cb.function("HOUR", Integer.class, root.get("obsTime")), 17)),
                    cb.and(cb.equal(cb.function("DAYOFWEEK", Integer.class, root.get("obsTime")), Calendar.WEDNESDAY),
                            cb.lessThan(cb.function("HOUR", Integer.class, root.get("obsTime")), 7)),
                    cb.and(cb.equal(cb.function("DAYOFWEEK", Integer.class, root.get("obsTime")), Calendar.WEDNESDAY),
                            cb.greaterThan(cb.function("HOUR", Integer.class, root.get("obsTime")), 17)),
                    cb.equal(cb.function("DAYOFWEEK", Integer.class, root.get("obsTime")), Calendar.SATURDAY),
                    cb.equal(cb.function("DAYOFWEEK", Integer.class, root.get("obsTime")), Calendar.SUNDAY)
            ));
        }

        List<Selection<?>> selections = new ArrayList<>();
        addFieldSelections(field,cb,root,selections);

        query.multiselect(selections);
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get("obsTime")));

        try {
            String redisKey = REDIS_QUERY_KEY_PREFIX + queryType + "_" + field + "_" + startTime + "-" + endTime;
            Object resultList = entityManager.createQuery(query).setMaxResults(limit).getResultList();
            redisCacheClient.set(redisKey, resultList, 60L, TimeUnit.SECONDS);
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
    @CountTime
    @Transactional(readOnly = true, rollbackFor = CaculateException.class)
    public Map<String, Object> queryDashboardData(String date) {
        LocalDate localDate = LocalDate.parse(date);
        HashMap<String, Object> result = new HashMap<>();

        LocalDateTime startTime = localDate.atStartOfDay(ZoneId.of("Asia/Taipei")).toLocalDateTime();
        LocalDateTime endTime = localDate.plusDays(1).atStartOfDay(ZoneId.of("Asia/Taipei")).toLocalDateTime();

        try {
            // 統一 Redis 緩存鍵的拼接
            String dateSuffix = "_" + localDate;

            // 從 Redis 緩存中取得資料
            DailySumDTO dailySumDTO = getFromCache(REDIS_DAILY_SUM_KEY + dateSuffix, DailySumDTO.class);
            DailyAverageDTO dailyAverageDTO = getFromCache(REDIS_DAILY_AVERAGE_KEY + dateSuffix, DailyAverageDTO.class);
            List<HourlySumDTO> hourlySumDTO = getListFromCache(REDIS_HOURLY_SUM_KEY + dateSuffix, new TypeReference<HourlySumDTO>() {});
            List<HourlyAverageDTO> hourlyAverageDTO = getListFromCache(REDIS_HOURLY_AVERAGE_KEY + dateSuffix, new TypeReference<HourlyAverageDTO>() {});

            PeakOffPeakSumDTO peakSumDTO = getFromCache(REDIS_PEAK_SUM_KEY + dateSuffix, PeakOffPeakSumDTO.class);
            PeakOffPeakAverageDTO peakAverageDTO = getFromCache(REDIS_PEAK_AVERAGE_KEY + dateSuffix, PeakOffPeakAverageDTO.class);
            PeakOffPeakSumDTO offPeakSumDTO = getFromCache(REDIS_OFF_PEAK_SUM_KEY + dateSuffix, PeakOffPeakSumDTO.class);
            PeakOffPeakAverageDTO offPeakAverageDTO = getFromCache(REDIS_OFF_PEAK_AVERAGE_KEY + dateSuffix, PeakOffPeakAverageDTO.class);

            // 如果 Redis 中沒有資料，則從資料庫中查詢並存入 Redis
            if (dailySumDTO == null) {
                dailySumDTO = sensorDataRepository.findDailySums(startTime, endTime);
                cacheData(REDIS_DAILY_SUM_KEY + dateSuffix, dailySumDTO);
            }
            if (dailyAverageDTO == null) {
                dailyAverageDTO = sensorDataRepository.findDailyAverages(startTime, endTime);
                cacheData(REDIS_DAILY_AVERAGE_KEY + dateSuffix, dailyAverageDTO);
            }
            if (hourlySumDTO == null || hourlySumDTO.isEmpty()) {
                hourlySumDTO = sensorDataRepository.findHourlySums(startTime, endTime);
                cacheDataToList(REDIS_HOURLY_SUM_KEY + dateSuffix, hourlySumDTO);
            }
            if (hourlyAverageDTO == null || hourlyAverageDTO.isEmpty()) {
                hourlyAverageDTO = sensorDataRepository.findHourlyAverages(startTime, endTime);
                cacheDataToList(REDIS_HOURLY_AVERAGE_KEY + dateSuffix, hourlyAverageDTO);
            }

            if(peakSumDTO == null && offPeakSumDTO == null) {

                if (PeakOffPeakTimeChecker.isCalculatePeakFullDay(startTime)) {
                    peakSumDTO = sensorDataRepository.findPeekSumByTimeRange(startTime, endTime);
                    cacheData(REDIS_PEAK_SUM_KEY + dateSuffix, peakSumDTO);
                    peakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(startTime, endTime);
                    cacheData(REDIS_PEAK_AVERAGE_KEY + dateSuffix, peakAverageDTO);

                } else if (PeakOffPeakTimeChecker.isCalculateOffPeakFullDay(startTime)) {
                    offPeakSumDTO = sensorDataRepository.findPeekSumByTimeRange(startTime, endTime);
                    cacheData(REDIS_OFF_PEAK_SUM_KEY + dateSuffix, offPeakSumDTO);
                    offPeakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(startTime, endTime);
                    cacheData(REDIS_OFF_PEAK_AVERAGE_KEY + dateSuffix, offPeakAverageDTO);

                } else {
                    LocalDateTime peakStartTime = localDate.atTime(7, 30);
                    LocalDateTime peakEndTime = localDate.atTime(17, 30);
                    LocalDateTime offPeakStartTime = localDate.atTime(17, 30);
                    LocalDateTime offPeakEndTime = localDate.plusDays(1).atStartOfDay();

                    peakSumDTO = sensorDataRepository.findPeekSumByTimeRange(peakStartTime, peakEndTime);
                    cacheData(REDIS_PEAK_SUM_KEY + dateSuffix, peakSumDTO);

                    if (peakAverageDTO == null) {
                        peakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(peakStartTime, peakEndTime);
                        cacheData(REDIS_PEAK_AVERAGE_KEY + dateSuffix, peakAverageDTO);
                    }
                    offPeakSumDTO = sensorDataRepository.findPeekSumByTimeRange(offPeakStartTime, offPeakEndTime);

                    PeakOffPeakSumDTO offPeakSumDTO2 = sensorDataRepository.findPeekSumByTimeRange(localDate.atStartOfDay(), peakStartTime);
                    offPeakSumDTO = sumPeakOffPeakSumDTO(offPeakSumDTO, offPeakSumDTO2);
                    cacheData(REDIS_OFF_PEAK_SUM_KEY + dateSuffix, offPeakSumDTO);

                    if (offPeakAverageDTO == null) {
                        offPeakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(offPeakStartTime, offPeakEndTime);
                        PeakOffPeakAverageDTO offPeakAverageDTO2 = sensorDataRepository.findPeakOffPeakAvgByTimeRange(localDate.atStartOfDay(), peakStartTime);
                        offPeakAverageDTO = sumPeakOffPeakAverageDTO(offPeakAverageDTO, offPeakAverageDTO2);
                        cacheData(REDIS_OFF_PEAK_AVERAGE_KEY + dateSuffix, offPeakAverageDTO);
                    }
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

        } catch (IOException e) {
            throw new CaculateException("反序列化時發生異常", e);
        }

        return result;
    }

    /**
     * 取得每日平均跟加總的相關數據
     *
     * @param date 查詢時段 yyyy-MM-dd
     * @return Map<String, Object>
     */
    @Override
    @CountTime
    @Transactional(readOnly = true, rollbackFor = CaculateException.class)
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
     *
     * @param date 查詢時段 yyyy-MM-dd
     * @return Map<String, Object>
     */
    @Override
    @CountTime
    @Transactional(readOnly = true, rollbackFor = CaculateException.class)
    public Map<String, Object> queryHourlyAnalysis(String date) {
        LocalDate localDate = LocalDate.parse(date);
        HashMap<String, Object> result = new HashMap<>();

        // 取得當時時間的一整天
        LocalDateTime startTime = localDate.atStartOfDay().atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();
        LocalDateTime endTime = localDate.plusDays(1).atStartOfDay(ZoneId.of("Asia/Taipei")).toLocalDateTime();

        try {
            List<HourlySumDTO> hourlySumDTO = redisCacheClient.getFromList(REDIS_HOURLY_SUM_KEY + "_" + localDate, new TypeReference<HourlySumDTO>() {
            });
            List<HourlyAverageDTO> hourlyAverageDTO = redisCacheClient.getFromList(REDIS_HOURLY_AVERAGE_KEY + "_" + localDate, new TypeReference<HourlyAverageDTO>() {
            });

            if (hourlySumDTO == null || hourlySumDTO.isEmpty()) {
                hourlySumDTO = sensorDataRepository.findHourlySums(startTime, endTime);
                redisCacheClient.setToList(REDIS_HOURLY_SUM_KEY + "_" + localDate, hourlySumDTO, 1L, TimeUnit.MINUTES);
            }
            if (hourlyAverageDTO == null || hourlyAverageDTO.isEmpty()) {
                hourlyAverageDTO = sensorDataRepository.findHourlyAverages(startTime, endTime);
                redisCacheClient.setToList(REDIS_HOURLY_AVERAGE_KEY + "_" + localDate, hourlyAverageDTO, 1L, TimeUnit.MINUTES);
            }

            result.put("hourlySumDTO", hourlySumDTO);
            result.put("hourlyAverageDTO", hourlyAverageDTO);
        } catch (JsonProcessingException e) {
            throw new CaculateException("反序列化時發生異常", e);
        }

        return result;
    }

    @Override
    @CountTime
    @Transactional(readOnly = true, rollbackFor = CaculateException.class)
    public Map<String, Object> queryOffPeakAnalysis(String date) {
        LocalDate localDate = LocalDate.parse(date);
        HashMap<String, Object> result = new HashMap<>();

        // 取得當時時間的一整天
        LocalDateTime startTime = localDate.atStartOfDay().atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();
        LocalDateTime endTime = localDate.plusDays(1).atStartOfDay(ZoneId.of("Asia/Taipei")).toLocalDateTime();

        try {
            PeakOffPeakSumDTO offPeakSumDTO = JsonUtil.deserialize(redisCacheClient.get(REDIS_OFF_PEAK_SUM_KEY + "_" + localDate), PeakOffPeakSumDTO.class);
            PeakOffPeakAverageDTO offPeakAverageDTO = JsonUtil.deserialize(redisCacheClient.get(REDIS_OFF_PEAK_AVERAGE_KEY + "_" + localDate), PeakOffPeakAverageDTO.class);

            if (offPeakSumDTO == null || offPeakAverageDTO == null) {
                if (PeakOffPeakTimeChecker.isCalculateOffPeakFullDay(startTime)) {
                    offPeakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(startTime, endTime);
                    redisCacheClient.set(REDIS_OFF_PEAK_AVERAGE_KEY + "_" + localDate, JsonUtil.serialize(offPeakAverageDTO), 1L, TimeUnit.MINUTES);

                    offPeakSumDTO = sensorDataRepository.findPeekSumByTimeRange(startTime, endTime);
                    redisCacheClient.set(REDIS_OFF_PEAK_SUM_KEY + "_" + localDate, JsonUtil.serialize(offPeakSumDTO), 1L, TimeUnit.MINUTES);
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

                    if (offPeakAverageDTO == null) {
                        offPeakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(offPeakStartTime, offPeakEndTime);
                        // 查詢 00:00 到早上 7:30 的數據
                        PeakOffPeakAverageDTO offPeakAverageDTO2 = sensorDataRepository.findPeakOffPeakAvgByTimeRange(offPeakStartTime2, offPeakEndTime2);
                        // 把 00:00 到 早上7:30 還有 5:30 到 00:00的數據相加
                        offPeakAverageDTO = sumPeakOffPeakAverageDTO(offPeakAverageDTO, offPeakAverageDTO2);
                        redisCacheClient.set(REDIS_OFF_PEAK_AVERAGE_KEY + "_" + localDate, JsonUtil.serialize(offPeakAverageDTO), 1L, TimeUnit.MINUTES);
                    }
                    if (offPeakSumDTO == null) {
                        offPeakSumDTO = sensorDataRepository.findPeekSumByTimeRange(offPeakStartTime, offPeakEndTime);
                        PeakOffPeakSumDTO offPeakSumDTO2 = sensorDataRepository.findPeekSumByTimeRange(offPeakStartTime2, offPeakEndTime2);
                        // 把 00:00 到 早上7:30 還有 5:30 到 00:00的數據相加
                        offPeakSumDTO = sumPeakOffPeakSumDTO(offPeakSumDTO, offPeakSumDTO2);
                        redisCacheClient.set(REDIS_OFF_PEAK_SUM_KEY + "_" + localDate, JsonUtil.serialize(offPeakSumDTO), 1L, TimeUnit.MINUTES);
                    }
                }
            }

            result.put("offPeakSumDTO", offPeakSumDTO);
            result.put("offPeakAverageDTO", offPeakAverageDTO);
        } catch (JsonProcessingException e) {
            throw new CaculateException("反序列化時發生異常", e);
        }

        return result;
    }

    @Override
    @CountTime
    @Transactional(readOnly = true, rollbackFor = CaculateException.class)
    public Map<String, Object> queryPeakAnalysis(String date) {
        LocalDate localDate = LocalDate.parse(date);
        HashMap<String, Object> result = new HashMap<>();

        // 取得當時時間的一整天
        LocalDateTime startTime = localDate.atStartOfDay().atZone(ZoneId.of("Asia/Taipei")).toLocalDateTime();
        LocalDateTime endTime = localDate.plusDays(1).atStartOfDay(ZoneId.of("Asia/Taipei")).toLocalDateTime();

        try {
            PeakOffPeakSumDTO peakSumDTO = JsonUtil.deserialize(redisCacheClient.get(REDIS_PEAK_SUM_KEY + "_" + localDate), PeakOffPeakSumDTO.class);
            PeakOffPeakAverageDTO peakAverageDTO = JsonUtil.deserialize(redisCacheClient.get(REDIS_PEAK_AVERAGE_KEY + "_" + localDate), PeakOffPeakAverageDTO.class);

            if (peakSumDTO == null || peakAverageDTO == null) {
                if (PeakOffPeakTimeChecker.isCalculatePeakFullDay(startTime)) {
                    peakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(startTime, endTime);
                    redisCacheClient.set(REDIS_PEAK_AVERAGE_KEY + "_" + localDate, JsonUtil.serialize(peakAverageDTO), 1L, TimeUnit.MINUTES);

                    peakSumDTO = sensorDataRepository.findPeekSumByTimeRange(startTime, endTime);
                    redisCacheClient.set(REDIS_PEAK_SUM_KEY + "_" + localDate, JsonUtil.serialize(peakSumDTO), 1L, TimeUnit.MINUTES);
                } else {
                    // 上午七點半到下午五點半
                    LocalDateTime peekStartTime = localDate.atTime(7, 30);
                    LocalDateTime peekEndTime = localDate.atTime(17, 30);

                    ZoneId zoneId = ZoneId.of("Asia/Taipei");
                    peekStartTime = peekStartTime.atZone(zoneId).toLocalDateTime();
                    peekEndTime = peekEndTime.atZone(zoneId).toLocalDateTime();

                    if (peakSumDTO == null) {
                        peakSumDTO = sensorDataRepository.findPeekSumByTimeRange(peekStartTime, peekEndTime);
                        redisCacheClient.set(REDIS_PEAK_SUM_KEY + "_" + localDate, JsonUtil.serialize(peakSumDTO), 1L, TimeUnit.MINUTES);
                    }
                    if (peakAverageDTO == null) {
                        peakAverageDTO = sensorDataRepository.findPeakOffPeakAvgByTimeRange(peekStartTime, peekEndTime);
                        redisCacheClient.set(REDIS_PEAK_AVERAGE_KEY + "_" + localDate, JsonUtil.serialize(peakAverageDTO), 1L, TimeUnit.MINUTES);
                    }
                }
            }
            result.put("peakSumDTO", peakSumDTO);
            result.put("peakAverageDTO", peakAverageDTO);
        } catch (JsonProcessingException e) {
            throw new CaculateException("反序列化時發生異常", e);
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

    private <T> T getFromCache(String key, Class<T> clazz) throws IOException {
        return JsonUtil.deserialize(redisCacheClient.get(key), clazz);
    }

    private <T> List<T> getListFromCache(String key, TypeReference<T> typeReference) throws IOException {
        return redisCacheClient.getFromList(key, typeReference);
    }

    private void cacheData(String key, Object data) throws JsonProcessingException {
        redisCacheClient.set(key, JsonUtil.serialize(data), 1L, TimeUnit.MINUTES);
    }

    private void cacheDataToList(String key, List<?> data) throws JsonProcessingException {
        redisCacheClient.setToList(key, data, 1L, TimeUnit.MINUTES);
    }

    private void addFieldSelections(String field, CriteriaBuilder cb, Root<SensorData> root, List<Selection<?>> selections) {
        switch (field) {
            case SENSOR_ALL_FIELD:
                selections.add(root.get("sensor").get("volt").get("v1"));
                selections.add(root.get("sensor").get("volt").get("v5"));
                selections.add(root.get("sensor").get("volt").get("v6"));
                selections.add(root.get("sensor").get("stickTxRh").get("tx"));
                selections.add(root.get("sensor").get("stickTxRh").get("rh"));
                selections.add(root.get("sensor").get("ultrasonicLevel").get("echo"));
                selections.add(root.get("sensor").get("waterSpeedAquark").get("speed"));
                selections.add(root.get("rainD"));
                break;
            case SENSOR_FIELD_NAME_V1:
                selections.add(root.get("sensor").get("volt").get("v1"));
                break;
            case SENSOR_FIELD_NAME_V5:
                selections.add(root.get("sensor").get("volt").get("v5"));
                break;
            case SENSOR_FIELD_NAME_V6:
                selections.add(root.get("sensor").get("volt").get("v6"));
                break;
            case SENSOR_FIELD_NAME_TX:
                selections.add(root.get("sensor").get("stickTxRh").get("tx"));
                break;
            case SENSOR_FIELD_NAME_RH:
                selections.add(root.get("sensor").get("stickTxRh").get("rh"));
                break;
            case SENSOR_FIELD_NAME_ECHO:
                selections.add(root.get("sensor").get("ultrasonicLevel").get("echo"));
                break;
            case SENSOR_FIELD_NAME_SPEED:
                selections.add(root.get("sensor").get("waterSpeedAquark").get("speed"));
                break;
            case SENSOR_FIELD_NAME_RAIN_D:
                selections.add(root.get("rainD"));
                break;
            default:
                throw new IllegalArgumentException("不支持的查詢欄位: " + field);
        }
    }


}
