package com.example.aquarkdemo.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/**
 * @author Timmy
 *
 * 感應器資料實體類
 */
@Getter
@Entity
@Table(name = "sensor_data", indexes = {
    @Index(name = "idx_station_id", columnList = "station_id"),
    @Index(name = "idx_obs_time", columnList = "obs_time"),
})
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SensorData implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "station_id", nullable = false)
    @JsonProperty("station_id")
    @ColumnDefault("0")
    private Integer stationId;

    @Column(name = "obs_time", nullable = false)
    @JsonProperty("obs_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "Asia/Taipei")
    private LocalDateTime obsTime;

    @Column(name = "rain_d", nullable = false)
    @ColumnDefault("0.0")
    @JsonProperty("rain_d")
    private Double rainD;

    @JsonProperty("sensor")
    @Embedded
    private Sensor sensor;

    @PrePersist
    private void prePersist() {
        if (stationId == null) {
            stationId = 0;
        }
        if (obsTime == null) {
            obsTime = LocalDateTime.now(ZoneId.of("Asia/Taipei"));
        }
        if (rainD == null) {
            rainD = 0.0;
        }
        if(sensor == null) {
            sensor = new Sensor();
        }
        if(sensor.getWaterSpeedAquark()!= null &&
           sensor.getWaterSpeedAquark().getSpeed() != null &&
           sensor.getWaterSpeedAquark().getSpeed() < 0.0) {
            // 轉換成絕對值
            sensor.getWaterSpeedAquark().setSpeed(Math.abs(sensor.getWaterSpeedAquark().getSpeed()));
        }
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStationId(Integer stationId) {
        this.stationId = stationId;
    }

    public void setObsTime(LocalDateTime obsTime) {
        this.obsTime = obsTime;
    }

    public void setRainD(Double rainD) {
        this.rainD = rainD;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        SensorData that = (SensorData) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}

