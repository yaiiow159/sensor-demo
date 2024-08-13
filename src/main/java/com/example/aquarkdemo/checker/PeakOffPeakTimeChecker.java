package com.example.aquarkdemo.checker;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 尖峰與離峰時間檢查驗證器
 */
public class PeakOffPeakTimeChecker {

    /**
     * 尖峰或離峰枚舉
     */
    public enum TimePeriod {
        PEAK,
        OFF_PEAK
    }

    public static TimePeriod checkPeakOffPeakPeriod(LocalDateTime dateTime) {
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        LocalTime time = dateTime.toLocalTime();

        boolean isPeakTime = (
                (dayOfWeek == DayOfWeek.MONDAY || dayOfWeek == DayOfWeek.TUESDAY || dayOfWeek == DayOfWeek.WEDNESDAY) &&
                        time.isAfter(LocalTime.of(7, 29)) && time.isBefore(LocalTime.of(17, 31))
        ) ||
                (dayOfWeek == DayOfWeek.THURSDAY || dayOfWeek == DayOfWeek.FRIDAY);

        boolean isOffPeakTime = (
                (dayOfWeek == DayOfWeek.MONDAY || dayOfWeek == DayOfWeek.TUESDAY || dayOfWeek == DayOfWeek.WEDNESDAY) &&
                        (time.isBefore(LocalTime.of(7, 30)) || time.isAfter(LocalTime.of(17, 30)))
        ) ||
                (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY);

        if (isPeakTime) {
            return TimePeriod.PEAK;
        } else if (isOffPeakTime) {
            return TimePeriod.OFF_PEAK;
        } else {
            return null;
        }
    }

    public static boolean isPeakTime(LocalDateTime dateTime) {
        return checkPeakOffPeakPeriod(dateTime) == TimePeriod.PEAK;
    }

    public static boolean isOffPeakTime(LocalDateTime dateTime) {
        return checkPeakOffPeakPeriod(dateTime) == TimePeriod.OFF_PEAK;
    }

    // 是否是週四或週五的全天
    public static boolean isCalculatePeakFullDay(LocalDateTime dateTime) {
        return dateTime.getDayOfWeek() == DayOfWeek.THURSDAY || dateTime.getDayOfWeek() == DayOfWeek.FRIDAY;
    }

    // 是否是週六或週日的全天
    public static boolean isCalculateOffPeakFullDay(LocalDateTime dateTime) {
        return dateTime.getDayOfWeek() == DayOfWeek.SATURDAY || dateTime.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
