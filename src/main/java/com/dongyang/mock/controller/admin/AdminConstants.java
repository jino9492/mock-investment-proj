package com.dongyang.mock.controller.admin;

/**
 * 어드민 페이지 상수 정의
 * - 매직 넘버/문자열 상수화
 * - URL 경로
 * - 상태 메시지
 * - 기본값
 */
public final class AdminConstants {
    
    private AdminConstants() {
        // 인스턴스화 방지
    }
    
    // ==================== URL 경로 ====================
    public static final String ADMIN_BASE_PATH = "/admin";
    public static final String ADMIN_LOGIN_PATH = "/admin";
    public static final String ADMIN_DASHBOARD_PATH = "/admin/dashboard";
    public static final String ADMIN_USERS_PATH = "/admin/users";
    public static final String ADMIN_USERS_DETAIL_PATH = "/admin/users/detail";
    public static final String ADMIN_TRADES_PATH = "/admin/trades";
    public static final String ADMIN_SETTINGS_PATH = "/admin/settings";
    
    // ==================== JSP 경로 ====================
    public static final String JSP_ADMIN_LOGIN = "/WEB-INF/views/admin/login_content.jsp";
    public static final String JSP_ADMIN_DASHBOARD = "/WEB-INF/views/admin/dashboard.jsp";
    public static final String JSP_ADMIN_USERS = "/WEB-INF/views/admin/users.jsp";
    public static final String JSP_ADMIN_USER_DETAIL = "/WEB-INF/views/admin/user_detail.jsp";
    public static final String JSP_ADMIN_TRADES = "/WEB-INF/views/admin/trades.jsp";
    public static final String JSP_ADMIN_SETTINGS = "/WEB-INF/views/admin/settings.jsp";
    
    // ==================== 세션 속성 ====================
    public static final String SESSION_ADMIN_USER = "adminUser";
    public static final String SESSION_ADMIN_INFO = "adminInfo";
    
    // ==================== 요청 파라미터 ====================
    public static final String PARAM_MEMBER_ID = "memberId";
    public static final String PARAM_ACTION = "action";
    public static final String PARAM_STATUS = "status";
    public static final String PARAM_KEYWORD = "keyword";
    public static final String PARAM_PAGE = "page";
    
    // ==================== 액션 타입 ====================
    public static final String ACTION_UPDATE = "update";
    public static final String ACTION_DELETE = "delete";
    
    // ==================== 상태 메시지 ====================
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_INVALID = "invalid";
    public static final String STATUS_NOT_FOUND = "not_found";
    public static final String STATUS_UPDATE_SUCCESS = "update_success";
    public static final String STATUS_UPDATE_FAIL = "update_fail";
    public static final String STATUS_UPDATE_DUPLICATE_NICKNAME = "update_duplicate_nickname";
    public static final String STATUS_DELETE_SUCCESS = "delete_success";
    public static final String STATUS_DELETE_FAIL = "delete_fail";
    
    // ==================== 기본값 ====================
    public static final int DEFAULT_VIRTUAL_CASH = 1_000_000; // 기본 예치금
    public static final int DEFAULT_PAGE_SIZE = 20; // 페이지당 항목 수
    public static final int DEFAULT_TOP_STOCKS_COUNT = 5; // 상위 종목 개수
    
    // ==================== 에러 메시지 ====================
    public static final String ERROR_LOAD_MEMBER_LIST = "회원 목록을 불러오는 중 오류가 발생했습니다.";
    public static final String ERROR_LOAD_MEMBER_DETAIL = "회원 상세 정보를 불러오는 중 오류가 발생했습니다.";
    public static final String ERROR_LOAD_DASHBOARD = "대시보드 데이터를 불러오는 중 오류가 발생했습니다.";
    public static final String ERROR_LOAD_TRADE_LIST = "거래 목록을 불러오는 중 오류가 발생했습니다.";
    public static final String ERROR_INVALID_PARAMETER = "잘못된 요청 파라미터입니다.";
}

