package com.example.aquarkdemo.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.stereotype.Component;

import java.util.Objects;


/**
 * @author Timmy
 *
 * 生產者監聽
 */
@Slf4j
@Component
public class MessageSendReqListener implements ProducerListener {

    @Override
    public void onSuccess(ProducerRecord producerRecord, RecordMetadata recordMetadata) {
        log.info("recordMetadata 寄送成功 內容: 主題:{} , 內容:{}, 偏移量:{}", recordMetadata.topic(), recordMetadata, recordMetadata.offset());
    }
    @Override
    public void onError(ProducerRecord producerRecord, RecordMetadata recordMetadata, Exception exception) {
        log.error("生產者 寄送消息 : {} 出現異常 ,寄送失敗錯誤原因: {}", Objects.requireNonNull(recordMetadata) ,exception.getMessage());
    }
}
