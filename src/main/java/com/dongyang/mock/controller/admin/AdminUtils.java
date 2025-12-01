package com.dongyang.mock.controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 어드민 페이지 유틸리티 클래스
 * - URL 빌더
 * - 리다이렉트 헬퍼
 * - 파라미터 검증
 */
public final class AdminUtils {
    
    private AdminUtils() {
        // 인스턴스화 방지
    }
    
    /**
     * URL 인코딩
     */
    public static String encode(String value) {
        if (value == null) {
            return "";
        }
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return value;
        }
    }
    
    /**
     * 상태 메시지와 함께 리다이렉트
     */
    public static void redirectWithStatus(HttpServletRequest req, HttpServletResponse resp, 
                                         String path, String status) throws IOException {
        StringBuilder url = new StringBuilder(req.getContextPath()).append(path);
        if (status != null && !status.isEmpty()) {
            url.append("?").append(AdminConstants.PARAM_STATUS).append("=").append(encode(status));
        }
        resp.sendRedirect(url.toString());
    }
    
    /**
     * 회원 목록으로 리다이렉트
     */
    public static void redirectToUsers(HttpServletRequest req, HttpServletResponse resp, 
                                      String status) throws IOException {
        redirectWithStatus(req, resp, AdminConstants.ADMIN_USERS_PATH, status);
    }
    
    /**
     * 회원 상세 페이지로 리다이렉트
     */
    public static void redirectToUserDetail(HttpServletRequest req, HttpServletResponse resp, 
                                           int memberId, String status) throws IOException {
        StringBuilder url = new StringBuilder(req.getContextPath())
                .append(AdminConstants.ADMIN_USERS_DETAIL_PATH)
                .append("?")
                .append(AdminConstants.PARAM_MEMBER_ID)
                .append("=")
                .append(memberId);
        if (status != null && !status.isEmpty()) {
            url.append("&")
               .append(AdminConstants.PARAM_STATUS)
               .append("=")
               .append(encode(status));
        }
        resp.sendRedirect(url.toString());
    }
    
    /**
     * 정수 파라미터 파싱 (안전)
     * @return 파싱된 정수값, 실패 시 null
     */
    public static Integer parseIntSafely(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 정수 파라미터 파싱 (기본값 포함)
     * @return 파싱된 정수값, 실패 시 defaultValue
     */
    public static int parseIntSafely(String value, int defaultValue) {
        Integer result = parseIntSafely(value);
        return result != null ? result : defaultValue;
    }
    
    /**
     * 문자열 앞뒤 공백 제거 (null-safe)
     */
    public static String trim(String value) {
        return value == null ? null : value.trim();
    }
    
    /**
     * 문자열이 null이거나 공백인지 확인
     */
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    /**
     * 문자열이 null이 아니고 공백이 아닌지 확인
     */
    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }
}

