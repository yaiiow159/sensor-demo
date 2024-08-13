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

        // 尖峰时间: 週一～週三 : 7:30 ~17:30。週四 週五 全天
        boolean isPeakTime = (
                (dayOfWeek == DayOfWeek.MONDAY || dayOfWeek == DayOfWeek.TUESDAY || dayOfWeek == DayOfWeek.WEDNESDAY) &&
                        (time.isAfter(LocalTime.of(7, 30)) && time.isBefore(LocalTime.of(17, 31)))
        ) ||
                (dayOfWeek == DayOfWeek.THURSDAY || dayOfWeek == DayOfWeek.FRIDAY);

        // 離峰时间: 週一～週三 : 00:00 ~7:30 以及 17:30~00:00。週六 週日 全天
        boolean isOffPeakTime = !isPeakTime ||
                (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY);

        return isPeakTime ? TimePeriod.PEAK : TimePeriod.OFF_PEAK;
    }

    public static boolean isPeakTime(LocalDateTime dateTime) {
        return checkPeakOffPeakPeriod(dateTime) == TimePeriod.PEAK;
    }

    public static boolean isOffPeakTime(LocalDateTime dateTime) {
        return checkPeakOffPeakPeriod(dateTime) == TimePeriod.OFF_PEAK;
    }

    //是否是週五或是週四
    public static boolean isCaculatePeekFullDay(LocalDateTime dateTime) {
        return dateTime.getDayOfWeek() == DayOfWeek.FRIDAY || dateTime.getDayOfWeek() == DayOfWeek.THURSDAY;
    }


    public static boolean isCaculateOffPeekFullDay(LocalDateTime dateTime) {
        return dateTime.getDayOfWeek() == DayOfWeek.SATURDAY || dateTime.getDayOfWeek() == DayOfWeek.SUNDAY;
    }


}
