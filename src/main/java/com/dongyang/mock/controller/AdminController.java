package com.dongyang.mock.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/admin")
public class AdminController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        // 이미 로그인되어 있으면 대시보드로
        if (session != null && session.getAttribute("adminUser") != null) {
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
            return;
        }

        // 현재 경로를 JSP에 전달 -- 사이드바 버튼
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String currentPath = requestURI.replace(contextPath, "");
        req.setAttribute("currentPath", currentPath);

        // 로그인 페이지 표시
        String viewPage = "/WEB-INF/views/admin/login.jsp";
        req.getRequestDispatcher(viewPage).forward(req, resp);
    }
}