package com.example.aquarkdemo.mq;

import com.example.aquarkdemo.config.AlertProps;
import com.example.aquarkdemo.entity.SensorData;
import com.example.aquarkdemo.exception.ThresHoldException;
import com.example.aquarkdemo.repository.SensorDataRepository;
import com.example.aquarkdemo.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.NoResultException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.TimeoutException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import static com.example.aquarkdemo.constant.CommonConstant.BATCH_SIZE;
import static com.example.aquarkdemo.constant.CommonConstant.MAX_BATCH_SIZE;

@Slf4j
@Component
public class MessageConsumer {

    private final JavaMailSender javaMailSender;
    private final SensorDataRepository sensorDataRepository;
    private final AlertProps alertProps;
    private final ThreadPoolExecutor threadPoolExecutor;
    private static final String sendMailUser = "S5xkx@example.com";

    @Value("${spring.mail.username}")
    private String emailReceiver;

    public MessageConsumer(JavaMailSender javaMailSender, SensorDataRepository sensorDataRepository, AlertProps alertProps,@Qualifier("batchThreadPoolExecutor") ThreadPoolExecutor threadPoolExecutor) {
        this.javaMailSender = javaMailSender;
        this.sensorDataRepository = sensorDataRepository;
        this.alertProps = alertProps;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    @Transactional
    @KafkaListener(topics = "sensor-data-topic", containerFactory = "kafkaListenerContainerFactory",
            groupId = "sensor-data-consumer-group", topicPartitions = @TopicPartition(topic = "sensor-data-topic", partitions = { "0", "1", "2" }))
    @RetryableTopic(dltStrategy = DltStrategy.ALWAYS_RETRY_ON_ERROR, timeout = "30000",
            include = {ThresHoldException.class, JsonProcessingException.class, NoResultException.class, TimeoutException.class, RuntimeException.class})
    public void handleSensorData(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.debug("收到 sensor data: {}, 主題: {}, 分區: {}, 當前時間: {}",
                record.value(), record.topic(), record.partition(), LocalDateTime.now(ZoneId.of("Asia/Taipei")));

        try {
            List<SensorData> sensorDataList = JsonUtil.deserialize(record.value(), new TypeReference<List<SensorData>>() {});

            if (sensorDataList.size() > MAX_BATCH_SIZE) {
                handleLargeBatch(sensorDataList);
            } else {
                saveSensorData(sensorDataList);
            }
            // 手動確認ack message是否已正確執行完成 完成offset commit
            ack.acknowledge();
        } catch (JsonProcessingException e) {
            log.error("反序列化時發生異常: {}", e.getMessage());
            throw new RuntimeException("反序列化時發生異常", e);
        } catch (ThresHoldException e) {
            log.error("數據操作時發生異常: {}", e.getMessage());
            throw new RuntimeException("闊值警告:" + e.getMessage());
        }  catch (Exception e) {
            log.error("數據操作時發生異常: {}", e.getMessage());
            throw new RuntimeException("數據操作時發生異常", e);
        }
    }

    // 重試topic
    @Transactional
    @KafkaListener(topics = "sensor-data-topic-retry", containerFactory = "kafkaListenerContainerFactory", groupId = "sensor-data-consumer-group-retry")
    public void handleRetrySensorData(ConsumerRecord<String, String> record,Acknowledgment ack) {
        log.debug("收到 sensor data: {}, 主題: {}, 分區: {}, 當前時間: {}",
                record.value(), record.topic(), record.partition(), LocalDateTime.now(ZoneId.of("Asia/Taipei")));
        try {
            List<SensorData> sensorDataList = JsonUtil.deserialize(record.value(), new TypeReference<List<SensorData>>() {});
            if (sensorDataList.size() > MAX_BATCH_SIZE) {
                handleLargeBatch(sensorDataList);
            } else {
                saveSensorData(sensorDataList);
            }
            // 手動確認ack message是否已正確執行完成
            ack.acknowledge();
        } catch (JsonProcessingException e) {
            log.error("反序列化時發生異常: {}", e.getMessage());
            throw new RuntimeException("反序列化時發生異常", e);
        } catch (ThresHoldException e) {
            log.error("數據操作時發生異常: {}", e.getMessage());
            throw new RuntimeException("闊值警告:" + e.getMessage());
        }  catch (Exception e) {
            log.error("數據操作時發生異常: {}", e.getMessage());
            throw new RuntimeException("數據操作時發生異常", e);
        }
    }

    // 死信對列
    @KafkaListener(topics = "sensor-data-topic-dlt", containerFactory = "kafkaListenerContainerFactory", groupId = "sensor-data-consumer-group-dlt")
    public void handleDlt(ConsumerRecord<String, String> record,Acknowledgment ack) {
        log.debug("收到 sensor data: {}, 主題: {}, 分區: {}, 當前時間: {}",
                record.value(), record.topic(), record.partition(), LocalDateTime.now(ZoneId.of("Asia/Taipei")));
        // 寄送死信對列異常通知
        final String message = "寄送死信對列異常通知、多次將感應器存入db發生異常、發送此郵件告知系統管理員";
        final String subject = "寄送死信對列異常通知";
        sendAlertMail(message,subject);
        // 手動確認ack message是否已正確執行完成 完成offset commit
        ack.acknowledge();
    }

    /**
     * 批次插入(多執行緒)
     * @param sensorDataList 感應器集合
     */
    private void handleLargeBatch(List<SensorData> sensorDataList) throws ThresHoldException {
        List<List<SensorData>> partitions = partitionList(sensorDataList, BATCH_SIZE);

        List<CompletableFuture<Void>> futures = partitions.stream()
                .map(batch -> CompletableFuture.runAsync(() -> {
                    try {
                        validateThreshold(batch);
                        saveSensorData(batch);
                    } catch (Exception e) {
                        log.error("批次插入過程發生異常: {}", e.getMessage());
                        throw new RuntimeException("批次插入失敗", e);
                    }
                }, threadPoolExecutor))
                .toList();

        try {
            // 等待所有處理完畢
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception e) {
            log.error("處理批次插入過程中發生異常: {}", e.getMessage());
            throw new ThresHoldException("批次插入處理失敗", e);
        }
    }

    /**
     * 批次插入
     * @param sensorDataList 感應器集合
     */
    private void saveSensorData(List<SensorData> sensorDataList) throws ThresHoldException {
        try {
            validateThreshold(sensorDataList);
            List<SensorData> dbRecords = sensorDataRepository.saveAll(sensorDataList);
            if (!CollectionUtils.isEmpty(dbRecords)) {
                log.debug("成功存入db");
            }
        } catch (Exception e) {
            log.error("存入資料庫時發生異常: {}", e.getMessage());
            throw new RuntimeException("存入資料庫時發生異常", e);
        }
    }

    /**
     *  拆分主List 為 子List
     */
    private <T> List<List<T>> partitionList(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    /**
     * 驗證資料是否有超過闊值
     * @param sensorDataList 感應器集合
     */
    private void validateThreshold(List<SensorData> sensorDataList) throws ThresHoldException {
        StringBuilder message = new StringBuilder();
        final String subject = "閥值告警通知";
        for(SensorData sensorData : sensorDataList) {
            int alerCount = 0;
            message.append("<p style='color:red;'>當前時間: ")
                   .append(LocalDateTime.now(ZoneId.of("Asia/Taipei"))).append("</p>");
            if(sensorData.getSensor() != null) {
                if (sensorData.getSensor().getVolt() != null && alertProps.getAlertV1() <= sensorData.getSensor().getVolt().getV1()) {
                    log.info("超過閾值: {}", sensorData.getSensor().getVolt().getV1() + "需寄送郵件通知告警訊息");
                    alerCount++;
                    message.append("V1感應器超過閾值: ").append(sensorData.getSensor().getVolt().getV1()).append("需寄送郵件通知告警訊息");
                    message.append("<br/>");
                }

                if (sensorData.getSensor().getVolt() != null && alertProps.getAlertV5() <= sensorData.getSensor().getVolt().getV5()) {
                    log.info("超過閾值: {}", sensorData.getSensor().getVolt().getV5() + "需寄送郵件通知告警訊息");
                    alerCount++;
                    message.append("V5感應器超過閾值: ").append(sensorData.getSensor().getVolt().getV5()).append("需寄送郵件通知告警訊息");
                    message.append("<br/>");
                }

                if (sensorData.getSensor().getVolt() != null && alertProps.getAlertV6() <= sensorData.getSensor().getVolt().getV6()) {
                    log.info("超過閾值: {}", sensorData.getSensor().getVolt().getV6() + "需寄送郵件通知告警訊息");
                    alerCount++;
                    message.append("V6感應器超過閾值: ").append(sensorData.getSensor().getVolt().getV6()).append("需寄送郵件通知告警訊息");
                    message.append("<br/>");
                }

                if (sensorData.getSensor().getStickTxRh() != null && alertProps.getAlertRh() <= sensorData.getSensor().getStickTxRh().getRh()) {
                    log.info("超過閾值: {}", sensorData.getSensor().getStickTxRh().getRh() + "需寄送郵件通知告警訊息");
                    alerCount++;
                    message.append("RH感應器超過閾值: ").append(sensorData.getSensor().getStickTxRh().getRh()).append("需寄送郵件通知告警訊息");
                    message.append("<br/>");
                }

                if (sensorData.getSensor().getStickTxRh() != null && alertProps.getAlertTx() <= sensorData.getSensor().getStickTxRh().getTx()) {
                    log.info("超過閾值: {}", sensorData.getSensor().getStickTxRh().getTx() + "需寄送郵件通知告警訊息");
                    alerCount++;
                    message.append("TX感應器超過閾值: ").append(sensorData.getSensor().getStickTxRh().getTx()).append("需寄送郵件通知告警訊息");
                    message.append("<br/>");
                }

                if (sensorData.getRainD() != null && alertProps.getAlertRain_d() <= sensorData.getRainD()) {
                    log.info("超過閾值: {}", sensorData.getRainD() + "需寄送郵件通知告警訊息");
                    alerCount++;
                    message.append("雨量感應器超過閾值: ").append(sensorData.getRainD()).append("需寄送郵件通知告警訊息");
                    message.append("<br/>");
                }

                if (sensorData.getSensor().getUltrasonicLevel() != null && alertProps.getAlertEcho() <= sensorData.getSensor().getUltrasonicLevel().getEcho()) {
                    log.info("超過閾值: {}", sensorData.getSensor().getUltrasonicLevel().getEcho() + "需寄送郵件通知告警訊息");
                    alerCount++;
                    message.append("超聲波感應器超過閾值: ").append(sensorData.getSensor().getUltrasonicLevel().getEcho()).append("需寄送郵件通知告警訊息");
                    message.append("<br/>");
                }
                if (sensorData.getSensor().getWaterSpeedAquark() != null && alertProps.getAlertSpeed() <= sensorData.getSensor().getWaterSpeedAquark().getSpeed()) {
                    log.info("超過閾值: {}", sensorData.getSensor().getWaterSpeedAquark().getSpeed() + "需寄送郵件通知告警訊息");
                    alerCount++;
                    message.append("水位感應器超過閾值: ").append(sensorData.getSensor().getWaterSpeedAquark().getSpeed()).append("需寄送郵件通知告警訊息");
                }
            }
            message.append("</p>");
            if(alerCount > 0) {
                // 寄送告警郵件
                sendAlertMail(message.toString(), subject);
                throw new ThresHoldException("超過閾值");
            }
        }
    }

    /**
     * 寄送告警郵件
     */
    private void sendAlertMail(final String message, final String subject, final String to, final String from) {
        createMimeMessageAndSend(message, subject, from, to);
    }

    /**
     * 寄送告警郵件
     */
    private void sendAlertMail(final String message, final String subject) {
        createMimeMessageAndSend(message, subject, sendMailUser, emailReceiver);
    }

    /**
     * 創建信件並寄送
     * @param message      寄送訊息
     * @param subject      主題
     * @param sendMailUser 寄件人
     * @param emailReceiver 收件人
     */
    private void createMimeMessageAndSend(String message, String subject, String sendMailUser, String emailReceiver) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(sendMailUser);
            helper.setTo(emailReceiver);
            helper.setSubject(subject);
            helper.setText(message, true);
            javaMailSender.send(mimeMessage);

            log.debug("寄送告警郵件成功");
        } catch (Exception e) {
            log.error("寄送失敗 原因: {}", e.getMessage());
        }
    }


}
