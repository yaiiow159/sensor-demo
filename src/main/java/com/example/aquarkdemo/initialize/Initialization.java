package com.example.aquarkdemo.initialize;

import com.example.aquarkdemo.config.AlertProps;
import com.example.aquarkdemo.repository.SensorDataRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.example.aquarkdemo.constant.CommonConstant.DEFAULT_ALERT_VALUE;

@Slf4j
@Component
@RequiredArgsConstructor
public class Initialization {

    private final SensorDataRepository sensorDataRepository;
    private final AlertProps alertProps;

    @Value("${check.if.delete-data:false}")
    private boolean deleteData;

    @PostConstruct
    void init() {
        log.info("初始化 SensorData 數據 開始....");
        initialDataBase();
        checkAlertValue();
        log.info("初始化 SensorData 數據 完成....");
    }

    private void checkAlertValue() {
        if(alertProps.getAlertV1() == null) {
            alertProps.setAlertV1(DEFAULT_ALERT_VALUE);
        }
        if(alertProps.getAlertV5() == null) {
            alertProps.setAlertV5(DEFAULT_ALERT_VALUE);
        }
        if(alertProps.getAlertV6() == null) {
            alertProps.setAlertV6(DEFAULT_ALERT_VALUE);
        }
        if(alertProps.getAlertRh() == null) {
            alertProps.setAlertRh(DEFAULT_ALERT_VALUE);
        }
        if(alertProps.getAlertTx() == null) {
            alertProps.setAlertTx(DEFAULT_ALERT_VALUE);
        }
        if(alertProps.getAlertEcho() == null) {
            alertProps.setAlertEcho(DEFAULT_ALERT_VALUE);
        }
        if(alertProps.getAlertRain_d() == null) {
            alertProps.setAlertRain_d(DEFAULT_ALERT_VALUE);
        }
    }

    // 初始化資料
    private void initialDataBase() {
        if(sensorDataRepository.count() > 0 && deleteData) {
            log.info("刪除 SensorData 數據....");
            sensorDataRepository.deleteAll();
        }
    }
}
