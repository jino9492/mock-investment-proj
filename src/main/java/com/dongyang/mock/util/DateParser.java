package com.dongyang.mock.util;

import java.sql.Date;
import java.sql.Timestamp;

public class DateParser {
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
}
