package com.example.aquarkdemo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.io.Serializable;
import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SensorData  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "v1")
    @ColumnDefault("0")
    private Double v1;

    @Column(name = "v5")
    @ColumnDefault("0")
    private Double v5;

    @Column(name = "v6")
    @ColumnDefault("0")
    private Double v6;

    @Column(name = "rh")
    @ColumnDefault("0")
    private Double rh;

    @Column(name = "tx")
    @ColumnDefault("0")
    private Double tx;

    @Column(name = "echo")
    @ColumnDefault("0")
    private Double echo;

    @Column(name = "rain_d")
    @ColumnDefault("0")
    private Double rain_d;

    @Column(name = "speed")
    @ColumnDefault("0")
    private Double speed;

    @Column(name = "timestamp")
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime timestamp;
}
