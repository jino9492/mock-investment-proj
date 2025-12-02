package com.dongyang.mock.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.function.Supplier;

/**
 * 관리자 컨트롤러들의 공통 기능을 제공하는 베이스 클래스
 * - 관리자 세션 확인
 * - 공통 유틸리티 메서드
 */
public abstract class BaseAdminController extends HttpServlet {

    /**
     * 기본 예치금 상수
     */
    protected static final int DEFAULT_VIRTUAL_CASH = 1_000_000;

    /**
     * 관리자 세션 확인
     * @return 관리자 세션이 존재하면 true, 없으면 false (로그인 페이지로 리다이렉트)
     */
    protected boolean ensureAdminSession(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("adminUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/admin");
            return false;
        }
        return true;
    }

    /**
     * 문자열 앞뒤 공백 제거
     */
    protected String trim(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * 문자열이 null이거나 공백인지 확인
     */
    protected boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * URL 인코딩 유틸리티
     */
    protected String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return value;
        }
    }

    /**
     * 현재 경로를 request attribute로 설정 (사이드바 활성화용)
     */
    protected void setCurrentPath(HttpServletRequest req, String path) {
        req.setAttribute("currentPath", path);
    }

    /**
     * null 체크 후 기본값 반환 (DTO null 체크 패턴 통합)
     * @param value 체크할 값
     * @param defaultValue null일 경우 반환할 기본값
     * @param <T> 타입
     * @return value가 null이 아니면 value, null이면 defaultValue
     */
    protected <T> T orDefault(T value, Supplier<T> defaultValue) {
        return value != null ? value : defaultValue.get();
    }

    /**
     * SQLException을 ServletException으로 변환하여 throw
     * @param e SQLException
     * @param message 에러 메시지
     * @throws ServletException 변환된 예외
     */
    protected void handleSQLException(SQLException e, String message) throws ServletException {
        e.printStackTrace();
        throw new ServletException(message, e);
    }

    /**
     * 상태 파라미터와 함께 리다이렉트
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param basePath 기본 경로
     * @param status 상태 값 (null이면 파라미터 추가 안 함)
     * @throws IOException 리다이렉트 실패 시
     */
    protected void redirectWithStatus(HttpServletRequest req, HttpServletResponse resp, String basePath, String status) throws IOException {
        StringBuilder target = new StringBuilder(req.getContextPath()).append(basePath);
        if (status != null && !status.isEmpty()) {
            target.append("?status=").append(encode(status));
        }
        resp.sendRedirect(target.toString());
    }

    /**
     * 쿼리 파라미터와 함께 리다이렉트
     * @param req HttpServletRequest
     * @param resp HttpServletResponse
     * @param basePath 기본 경로
     * @param queryParams 쿼리 파라미터 (예: "memberId=123&status=success")
     * @throws IOException 리다이렉트 실패 시
     */
    protected void redirectWithQuery(HttpServletRequest req, HttpServletResponse resp, String basePath, String queryParams) throws IOException {
        StringBuilder target = new StringBuilder(req.getContextPath()).append(basePath);
        if (queryParams != null && !queryParams.isEmpty()) {
            target.append("?").append(queryParams);
        }
        resp.sendRedirect(target.toString());
    }

    /**
     * 정수 파라미터 파싱 (안전한 파싱)
     * @param param 파싱할 파라미터 값
     * @return 파싱된 정수, 실패 시 null
     */
    protected Integer parseIntSafely(String param) {
        if (isBlank(param)) {
            return null;
        }
        try {
            return Integer.parseInt(param);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * GET 요청 전에 공통 처리 (세션 체크 등)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!ensureAdminSession(req, resp)) {
            return;
        }
        handleGet(req, resp);
    }

    /**
     * POST 요청 전에 공통 처리 (세션 체크, 인코딩 등)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!ensureAdminSession(req, resp)) {
            return;
        }
        handlePost(req, resp);
    }

    /**
     * GET 요청을 처리하는 추상 메서드
     */
    protected abstract void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException;

    /**
     * POST 요청을 처리하는 추상 메서드
     * 기본 구현은 405 Method Not Allowed를 반환
     */
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}

