package com.dongyang.mock.controller;

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

// 체크리스트
// 로그인 안 했을 때 어드민 페이지 못들어가게 차단


@WebServlet("/admin/*")
public class AdminController extends HttpServlet {

    private final MemberService memberService = new MemberService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        String pathInfo = req.getPathInfo();

        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String currentPath = requestURI.replace(contextPath, "");
        req.setAttribute("currentPath", currentPath);

        if (pathInfo == null || pathInfo.equals("/")) {
            if (session != null && session.getAttribute("adminUser") != null) {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                return;
            }
            req.getRequestDispatcher("/WEB-INF/views/admin/login_content.jsp").forward(req, resp);
            return;
        }

        if ("/logout".equals(pathInfo)) {
            if (session != null) {
                session.invalidate();
            }
            resp.sendRedirect(req.getContextPath() + "/admin");
            return;
        }

        if (session == null || session.getAttribute("adminUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/admin");
            return;
        }

        switch(pathInfo) {
            case "/dashboard":
                req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);
                return;  // break 대신 return!
            case "/users":
                handleUsersPage(req, resp);
                return;
            case "/users/detail":
                handleUserDetail(req, resp);
                return;
            case "/settings":
                req.getRequestDispatcher("/WEB-INF/views/admin/settings.jsp").forward(req, resp);
                return;
            case "/trades":
                req.getRequestDispatcher("/WEB-INF/views/admin/settings.jsp").forward(req, resp);
                return;
            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
        }
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String pathInfo = req.getPathInfo();

        // 로그인 처리
        if (pathInfo == null || pathInfo.equals("/")) {
            String adminId = req.getParameter("adminId");     // ✅ adminId로 수정
            String password = req.getParameter("password");

            // 하드코딩 테스트
            if ("admin".equals(adminId) && "admin1234".equals(password)) {
                HttpSession session = req.getSession(true);
                session.setAttribute("adminUser", adminId);
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                return;
            }

            // 로그인 실패
            req.setAttribute("errorMsg", "아이디 또는 비밀번호가 올바르지 않습니다.");  // ✅ errorMsg로 수정
            req.getRequestDispatcher("/WEB-INF/views/admin/login_content.jsp").forward(req, resp);  // ✅ 파일명 수정
            return;
        }

        if ("/users".equals(pathInfo)) {
            handleCreateMember(req, resp);
            return;
        }

        if ("/users/detail".equals(pathInfo)) {
            handleDetailAction(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void handleCreateMember(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("adminUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/admin");
            return;
        }

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

        MemberStatus.JoinResult status = MemberStatus.JoinResult.FAIL_INSERT;
        try {
            status = memberService.join(newMember);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        redirectWithStatus(req, resp, status.name().toLowerCase());
    }

    private void redirectWithStatus(HttpServletRequest req, HttpServletResponse resp, String status) throws IOException {
        StringBuilder target = new StringBuilder(req.getContextPath()).append("/admin/users");
        if (status != null && !status.isEmpty()) {
            target.append("?status=")
                    .append(URLEncoder.encode(status, StandardCharsets.UTF_8));
        }
        resp.sendRedirect(target.toString());
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void handleUserDetail(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String memberIdParam = req.getParameter("memberId");
        MemberDTO member = null;
        if (!isBlank(memberIdParam)) {
            try {
                int memberId = Integer.parseInt(memberIdParam);
                member = memberService.getMemberById(memberId);
            } catch (NumberFormatException e) {
                // ignore invalid format; member stays null
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

    private void handleUsersPage(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String keyword = trim(req.getParameter("keyword"));
        String statusFilter = trim(req.getParameter("statusFilter"));
        String profitFilter = trim(req.getParameter("profitFilter"));
        String joinFilter = trim(req.getParameter("joinFilter"));

        try {
            List<MemberDTO> members = memberService.getMembers(keyword, statusFilter, profitFilter, joinFilter);
            MemberStatsDTO stats = memberService.getMemberStats();

            req.setAttribute("memberList", members);
            req.setAttribute("filteredCount", members != null ? members.size() : 0);
            req.setAttribute("memberStats", stats);
        } catch (SQLException e) {
            throw new ServletException("Failed to load member list", e);
        }

        req.setAttribute("keyword", keyword);
        req.setAttribute("statusFilter", statusFilter);
        req.setAttribute("profitFilter", profitFilter);
        req.setAttribute("joinFilter", joinFilter);

        req.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(req, resp);
    }

    private void handleDetailAction(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("adminUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/admin");
            return;
        }

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

    private void handleMemberUpdate(HttpServletRequest req, HttpServletResponse resp, int memberId) throws IOException {
        String email = trim(req.getParameter("memberEmail"));
        String nickname = trim(req.getParameter("memberNickname"));
        String password = req.getParameter("memberPassword");

        String baseRedirect = req.getContextPath() + "/admin/users/detail?memberId=" + memberId + "&status=";
        if (isBlank(email) || isBlank(nickname)) {
            resp.sendRedirect(baseRedirect + encode("update_fail"));
            return;
        }

        try {
            MemberStatus.UpdateResult result = memberService.updateMemberAccount(memberId, email, nickname, password);
            String status;
            switch (result) {
                case SUCCESS -> status = "update_success";
                case DUPLICATE_EMAIL -> status = "update_duplicate_email";
                case DUPLICATE_NICKNAME -> status = "update_duplicate_nickname";
                default -> status = "update_fail";
            }
            resp.sendRedirect(baseRedirect + encode(status));
        } catch (SQLException e) {
            e.printStackTrace();
            resp.sendRedirect(baseRedirect + encode("update_fail"));
        }
    }

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

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}