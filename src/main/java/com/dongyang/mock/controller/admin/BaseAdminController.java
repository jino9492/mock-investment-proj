package com.dongyang.mock.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;

/**
 * 관리자 컨트롤러들의 공통 기능을 제공하는 베이스 클래스
 * - 관리자 세션 확인
 * - 공통 유틸리티 메서드
 */
public abstract class BaseAdminController extends HttpServlet {

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
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * 현재 경로를 request attribute로 설정 (사이드바 활성화용)
     */
    protected void setCurrentPath(HttpServletRequest req, String path) {
        req.setAttribute("currentPath", path);
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

