package com.dongyang.mock.controller;

import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.service.MemberService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// 체크리스트
// 로그인 안 했을 때 어드민 페이지 못들어가게 차단


@WebServlet("/admin/*")
public class AdminController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        String pathInfo = req.getPathInfo();

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
                req.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(req, resp);
                return;
            case "/settings":
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
        }
    }
}