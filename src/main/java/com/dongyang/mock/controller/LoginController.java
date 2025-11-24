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
import java.sql.SQLException;

@WebServlet("/login")
public class LoginController extends HttpServlet {
    private final MemberService memberService = new MemberService();
    String viewPage = "/WEB-INF/views/sign_form/login_form.jsp";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        MemberDTO member = null;
        try {
            member = memberService.login(email, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (member != null) {
            HttpSession session = request.getSession();
            session.setAttribute("member", member);
            response.sendRedirect("/");
        } else {
            request.setAttribute("errorMsg", "로그인 정보가 일치하지 않습니다.");
            request.getRequestDispatcher(viewPage).forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher(viewPage).forward(req, resp);
    }
}
