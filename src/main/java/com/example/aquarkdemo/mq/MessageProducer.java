package com.example.aquarkdemo.mq;

import com.example.aquarkdemo.entity.SensorData;
import com.example.aquarkdemo.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MessageSendReqListener kafkaSendReqHandler;

    public void sendSensorData(List<SensorData> sensorDataList) {
        kafkaTemplate.setProducerListener(kafkaSendReqHandler);
        try {
            kafkaTemplate.send("sensor-data-topic", JsonUtil.serialize(sensorDataList));
        } catch (JsonProcessingException e) {
            log.error("寄送失敗 原因: {}", e.getMessage());
        }
    }
}
