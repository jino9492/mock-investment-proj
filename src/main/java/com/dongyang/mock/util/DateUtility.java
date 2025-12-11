package com.dongyang.mock.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtility {

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

    // 밀리초 타입으로 변환
    public static long convertToMillis(String date, String format) {
        String dateTimeString = String.valueOf(date);
        long expiryTimeMillis;

        try {
            String fullYearDateTime = dateTimeString;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);

            LocalDateTime localDateTime = LocalDateTime.parse(fullYearDateTime, formatter);

            expiryTimeMillis = localDateTime.atZone(ZoneId.of("Asia/Seoul"))
                    .toInstant()
                    .toEpochMilli();

            return expiryTimeMillis;
        } catch (Exception e) {
            System.err.println("시간 변환 중 오류 발생: " + e.getMessage());

            return 0;
        }
    }
}
