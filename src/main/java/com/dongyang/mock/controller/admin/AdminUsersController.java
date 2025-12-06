package com.dongyang.mock.controller.admin;

import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.model.DTO.MemberStatsDTO;
import com.dongyang.mock.model.status.MemberStatus;
import com.dongyang.mock.service.MemberService;
import com.dongyang.mock.util.DateParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * 관리자 회원 관리 컨트롤러
 * - 회원 목록 조회 및 필터링
 * - 회원 상세 정보 조회
 * - 신규 회원 생성
 * - 회원 정보 수정 (닉네임, 보유자금)
 * - 회원 삭제
 */
@WebServlet(urlPatterns = {"/admin/users", "/admin/users/detail"})
public class AdminUsersController extends HttpServlet {

    private final MemberService memberService = new MemberService();

    /**
     * GET 요청 처리
     * - /admin/users: 회원 목록 페이지
     * - /admin/users/detail: 회원 상세 페이지
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (!ensureAdminSession(req, resp)) {
            return;
        }
        String servletPath = req.getServletPath();
        if ("/admin/users/detail".equals(servletPath)) {
            req.setAttribute("currentPath", "/admin/users");
            handleUserDetail(req, resp);
        } else {
            req.setAttribute("currentPath", "/admin/users");
            handleUsersPage(req, resp);
        }
    }

    /**
     * POST 요청 처리
     * - /admin/users: 신규 회원 생성
     * - /admin/users/detail: 회원 정보 수정/삭제
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        if (!ensureAdminSession(req, resp)) {
            return;
        }
        String servletPath = req.getServletPath();
        if ("/admin/users/detail".equals(servletPath)) {
            handleDetailAction(req, resp);
        } else {
            handleCreateMember(req, resp);
        }
    }

    /**
     * 관리자 세션 확인
     * @return 관리자 세션이 존재하면 true, 없으면 false (로그인 페이지로 리다이렉트)
     */
    private boolean ensureAdminSession(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("adminUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/admin");
            return false;
        }
        return true;
    }

    /**
     * 회원 목록 페이지 처리
     * - 필터 조건에 맞는 회원 목록 조회 (키워드, 상태, 수익률, 가입일)
     * - 회원 통계 정보 조회
     * - JSP에 데이터 전달하여 목록 페이지 렌더링
     */
    private void handleUsersPage(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String keyword = trim(req.getParameter("keyword"));
        String statusFilter = trim(req.getParameter("statusFilter"));
        String profitFilter = trim(req.getParameter("profitFilter"));
        String joinFilter = trim(req.getParameter("joinFilter"));

        try {
            List<MemberDTO> members = memberService.getMembers(keyword, statusFilter, profitFilter, joinFilter);
            MemberStatsDTO stats = memberService.getMemberStats();
            
            // null 체크: stats가 null이면 기본값으로 초기화
            if (stats == null) {
                stats = new MemberStatsDTO();
            }

            req.setAttribute("memberList", members);
            req.setAttribute("filteredCount", members != null ? members.size() : 0);
            req.setAttribute("memberStats", stats);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServletException("Failed to load member list", e);
        }

        req.setAttribute("keyword", keyword);
        req.setAttribute("statusFilter", statusFilter);
        req.setAttribute("profitFilter", profitFilter);
        req.setAttribute("joinFilter", joinFilter);

        req.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(req, resp);
    }

    /**
     * 신규 회원 생성 처리
     * - 이메일, 닉네임, 비밀번호 검증
     * - 기본 예치금 1,000,000원 설정
     * - 약관 동의 정보 저장
     * - 중복 체크 후 회원 등록
     */
    private void handleCreateMember(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = trim(req.getParameter("memberEmail"));
        String nickname = trim(req.getParameter("memberNickname"));
        String password = req.getParameter("memberPassword");

        if (isBlank(email) || isBlank(nickname) || isBlank(password)) {
            redirectWithStatus(req, resp, "invalid");
            return;
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        MemberDTO newMember = new MemberDTO();
        newMember.setMemEmail(email);
        newMember.setMemPassword(password);
        newMember.setMemNickname(nickname);
        newMember.setMemLastLoginIP(req.getRemoteAddr());
        newMember.setMemLastLoginAt(null);
        newMember.setAgreePrivacyAt(DateParser.getCheckTime(req.getParameter("agreePrivacy"), now));
        newMember.setAgreeThirdAt(DateParser.getCheckTime(req.getParameter("agreeThird"), now));
        newMember.setAgreeNotificationAt(DateParser.getCheckTime(req.getParameter("agreeNotification"), now));
        newMember.setVirtualCash(1000000); // 기본 예치금 1,000,000원

        MemberStatus.JoinResult status = MemberStatus.JoinResult.FAIL_INSERT;
        try {
            status = memberService.join(newMember);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        redirectWithStatus(req, resp, status.name().toLowerCase());
    }

    /**
     * 상태 메시지와 함께 리다이렉트
     * @param status 리다이렉트 URL에 포함할 상태 값
     */
    private void redirectWithStatus(HttpServletRequest req, HttpServletResponse resp, String status) throws IOException {
        StringBuilder target = new StringBuilder(req.getContextPath()).append("/admin/users");
        if (status != null && !status.isEmpty()) {
            target.append("?status=")
                    .append(URLEncoder.encode(status, StandardCharsets.UTF_8));
        }
        resp.sendRedirect(target.toString());
    }

    /**
     * 문자열 앞뒤 공백 제거
     */
    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * 문자열이 null이거나 공백인지 확인
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * 회원 상세 정보 조회
     * - 회원 ID로 회원 정보 조회
     * - 회원이 없으면 에러 페이지로 리다이렉트
     * - 상세 정보 페이지로 포워딩
     */
    private void handleUserDetail(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String memberIdParam = req.getParameter("memberId");
        MemberDTO member = null;
        if (!isBlank(memberIdParam)) {
            try {
                int memberId = Integer.parseInt(memberIdParam);
                member = memberService.getMemberById(memberId);
            } catch (NumberFormatException ignored) {
            } catch (SQLException e) {
                throw new ServletException("Failed to load member detail", e);
            }
        }

        if (member == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/users?status=not_found");
            return;
        }

        req.setAttribute("member", member);
        req.getRequestDispatcher("/WEB-INF/views/admin/user_detail.jsp").forward(req, resp);
    }

    /**
     * 회원 상세 페이지에서의 액션 라우팅
     * - update: 회원 정보 수정
     * - delete: 회원 삭제
     */
    private void handleDetailAction(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");
        String memberIdParam = req.getParameter("memberId");
        if (isBlank(memberIdParam)) {
            resp.sendRedirect(req.getContextPath() + "/admin/users?status=invalid");
            return;
        }

        int memberId;
        try {
            memberId = Integer.parseInt(memberIdParam);
        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/admin/users?status=invalid");
            return;
        }

        if ("update".equals(action)) {
            handleMemberUpdate(req, resp, memberId);
        } else if ("delete".equals(action)) {
            handleMemberDelete(req, resp, memberId);
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * 회원 정보 수정 처리
     * - 닉네임 수정 가능
     * - 보유자금 수정 가능
     * - 이메일은 수정 불가 (읽기 전용)
     * - 비밀번호는 수정 불가
     * - 닉네임 중복 체크
     */
    private void handleMemberUpdate(HttpServletRequest req, HttpServletResponse resp, int memberId) throws IOException {
        String nickname = trim(req.getParameter("memberNickname"));
        String virtualCashStr = trim(req.getParameter("virtualCash"));

        String baseRedirect = req.getContextPath() + "/admin/users/detail?memberId=" + memberId + "&status=";
        if (isBlank(nickname)) {
            resp.sendRedirect(baseRedirect + encode("update_fail"));
            return;
        }

        Integer virtualCash = null;
        if (virtualCashStr != null && !virtualCashStr.isEmpty()) {
            try {
                virtualCash = Integer.parseInt(virtualCashStr);
                if (virtualCash < 0) {
                    resp.sendRedirect(baseRedirect + encode("update_fail"));
                    return;
                }
            } catch (NumberFormatException e) {
                resp.sendRedirect(baseRedirect + encode("update_fail"));
                return;
            }
        }

        try {
            // 이메일은 수정하지 않고, 비밀번호도 수정하지 않음 (null 전달)
            MemberDTO existingMember = memberService.getMemberById(memberId);
            if (existingMember == null) {
                resp.sendRedirect(baseRedirect + encode("update_fail"));
                return;
            }

            MemberDTO dto = new MemberDTO();
            dto.setMemId(memberId);
            dto.setMemEmail(existingMember.getMemEmail());
            dto.setMemNickname(nickname);
            dto.setVirtualCash(virtualCash);
            
            // 닉네임과 보유자금 수정 (이메일은 기존 값 유지, 비밀번호는 null로 전달하여 변경하지 않음)
            MemberStatus.UpdateResult result = memberService.updateMemberAccount(dto);
            String status;
            switch (result) {
                case SUCCESS -> status = "update_success";
                case DUPLICATE_NICKNAME -> status = "update_duplicate_nickname";
                default -> status = "update_fail";
            }
            resp.sendRedirect(baseRedirect + encode(status));
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(baseRedirect + encode("update_fail"));
        }
    }

    /**
     * 회원 삭제 처리
     * - 회원 ID로 회원 삭제
     * - 삭제 성공 시 회원 목록으로 리다이렉트
     * - 삭제 실패 시 상세 페이지로 리다이렉트
     */
    private void handleMemberDelete(HttpServletRequest req, HttpServletResponse resp, int memberId) throws IOException {
        try {
            boolean deleted = memberService.deleteMember(memberId);
            if (deleted) {
                resp.sendRedirect(req.getContextPath() + "/admin/users?status=delete_success");
            } else {
                resp.sendRedirect(req.getContextPath() + "/admin/users/detail?memberId=" + memberId + "&status=" + encode("delete_fail"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/users/detail?memberId=" + memberId + "&status=" + encode("delete_fail"));
        }
    }

    /**
     * URL 인코딩 유틸리티
     */
    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

