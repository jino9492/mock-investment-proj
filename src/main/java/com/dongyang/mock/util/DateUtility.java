package com.dongyang.mock.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateUtility {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    // 안전하게 Date로 변환
    public static Date getSqlDate(String dateStr) {
        if (dateStr != null && !dateStr.trim().isEmpty()) {
            try {
                return Date.valueOf(dateStr);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    // checkbox 타입 변환
    public static Timestamp getCheckTime(String checkParam, Timestamp now) {
        return (checkParam != null) ? now : null;
    }

    /**
     * 현재 날짜를 기준으로 N 영업일 전의 날짜를 yyyyMMdd 형식으로 반환합니다.
     * 주말은 건너뛰고 계산합니다. (daysAgo=1 이면 전 영업일)
     */
    public static String getTradingDateXDaysAgo(int daysAgo) {
        LocalDate date = LocalDate.now();
        int daysCounted = 0;

        while (daysCounted < daysAgo) {
            date = date.minusDays(1);
            if (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
                daysCounted++;
            }
        }
        return date.format(DATE_FORMAT);
    }

    public static String getTodayDate() {
        return LocalDate.now().format(DATE_FORMAT);
    }
}
