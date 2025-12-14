package com.dongyang.mock.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * 간단한 JSON 생성 유틸리티
 * JSON 라이브러리 없이 기본적인 JSON 문자열 생성
 */
public class JsonUtil {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 문자열을 JSON 안전하게 이스케이프
     */
    private static String escapeJsonString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * JSON 문자열을 JavaScript 문자열로 안전하게 이스케이프
     * (작은따옴표, 큰따옴표, 백슬래시 등을 이스케이프)
     */
    public static String escapeForJavaScript(String jsonStr) {
        if (jsonStr == null) {
            return "";
        }
        return jsonStr.replace("\\", "\\\\")
                      .replace("'", "\\'")
                      .replace("\"", "\\\"")
                      .replace("\n", "\\n")
                      .replace("\r", "\\r")
                      .replace("\t", "\\t")
                      .replace("</script>", "<\\/script>");
    }
    
    /**
     * Timestamp를 문자열로 변환
     */
    private static String formatTimestamp(Timestamp ts) {
        if (ts == null) {
            return null;
        }
        try {
            return DATE_FORMAT.format(ts);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 주문 객체를 JSON 문자열로 변환
     */
    public static String orderToJson(Object orderId, Object stockName, Object side, Object orderType,
                                     Object price, Object quantity, Object hasCondition, Timestamp placedAt,
                                     Object conditionJson) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"orderId\":").append(orderId).append(",");
        sb.append("\"stockName\":\"").append(escapeJsonString(String.valueOf(stockName != null ? stockName : ""))).append("\",");
        sb.append("\"side\":\"").append(side != null ? side : "BUY").append("\",");
        sb.append("\"orderType\":\"").append(orderType != null ? orderType : "LIMIT").append("\",");
        sb.append("\"price\":").append(price != null ? price : "null").append(",");
        sb.append("\"quantity\":").append(quantity).append(",");
        sb.append("\"hasCondition\":").append(hasCondition).append(",");
        String placedAtStr = formatTimestamp(placedAt);
        sb.append("\"placedAt\":").append(placedAtStr != null ? "\"" + placedAtStr + "\"" : "null").append(",");
        sb.append("\"condition\":").append(conditionJson != null ? conditionJson : "null");
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * 완료된 주문 객체를 JSON 문자열로 변환
     */
    public static String completedOrderToJson(Object orderId, Object stockName, Object side, Object status,
                                              Object orderType, Object price, Object quantity,
                                              Timestamp placedAt, Timestamp cancelledAt, Timestamp tradedAt) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"orderId\":").append(orderId).append(",");
        sb.append("\"stockName\":\"").append(escapeJsonString(String.valueOf(stockName != null ? stockName : ""))).append("\",");
        sb.append("\"side\":\"").append(side != null ? side : "BUY").append("\",");
        sb.append("\"status\":\"").append(status != null ? status : "FILLED").append("\",");
        sb.append("\"orderType\":\"").append(orderType != null ? orderType : "LIMIT").append("\",");
        sb.append("\"price\":").append(price != null ? price : "null").append(",");
        sb.append("\"quantity\":").append(quantity).append(",");
        String placedAtStr = formatTimestamp(placedAt);
        sb.append("\"placedAt\":").append(placedAtStr != null ? "\"" + placedAtStr + "\"" : "null").append(",");
        String cancelledAtStr = formatTimestamp(cancelledAt);
        sb.append("\"cancelledAt\":").append(cancelledAtStr != null ? "\"" + cancelledAtStr + "\"" : "null").append(",");
        String tradedAtStr = formatTimestamp(tradedAt);
        sb.append("\"tradedAt\":").append(tradedAtStr != null ? "\"" + tradedAtStr + "\"" : "null");
        sb.append("}");
        return sb.toString();
    }
    
    /**
     * 조건 객체를 JSON 문자열로 변환
     */
    public static String conditionToJson(Object triggerPrice, Timestamp validUntil) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"triggerPrice\":").append(triggerPrice != null ? triggerPrice : "null").append(",");
        String validUntilStr = formatTimestamp(validUntil);
        sb.append("\"validUntil\":").append(validUntilStr != null ? "\"" + validUntilStr + "\"" : "null");
        sb.append("}");
        return sb.toString();
    }
}
