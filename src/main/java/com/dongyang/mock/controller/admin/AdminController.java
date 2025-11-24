package com.dongyang.mock.controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/admin/*")
public class AdminController extends HttpServlet {

    /**
     * 루트 페이지 및 로그아웃만 담당: 나머지 세부 경로는 별도 컨트롤러에서 처리.
     */
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

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * 로그인 POST 처리.
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            String adminId = req.getParameter("adminId");
            String password = req.getParameter("password");

            if ("admin".equals(adminId) && "admin1234".equals(password)) {
                HttpSession session = req.getSession(true);
                session.setAttribute("adminUser", adminId);
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                return;
            }

            req.setAttribute("errorMsg", "아이디 또는 비밀번호가 올바르지 않습니다.");
            req.getRequestDispatcher("/WEB-INF/views/admin/login_content.jsp").forward(req, resp);
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}