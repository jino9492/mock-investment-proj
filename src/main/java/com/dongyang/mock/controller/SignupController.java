package com.dongyang.mock.controller;

import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.service.MemberService;
import com.dongyang.mock.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;

@WebServlet("/signup")
public class SignupController extends HttpServlet {
    private final MemberService memberService = new MemberService();
    String viewPage = "/WEB-INF/views/sign_form/signup_form.jsp";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String nickname = request.getParameter("nickname");
        String password = request.getParameter("password");
        String name = request.getParameter("name");
        Date birth = DateParser.getSqlDate(request.getParameter("birth"));
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String open = request.getParameter("memOpen");
        String lastLoginIP = request.getRemoteAddr();
        Timestamp agreeTerms = DateParser.getCheckTime(request.getParameter("agreeTerms"), now);
        Timestamp agreePrivacy = DateParser.getCheckTime(request.getParameter("agreePrivacy"), now);
        Timestamp agreeThird = DateParser.getCheckTime(request.getParameter("agreeThird"), now);

        MemberDTO newMember = new MemberDTO();
        newMember.setMemNickName(nickname);
        newMember.setMemPassword(password);
        newMember.setMemName(name);
        newMember.setMemBirth(birth);
        newMember.setMemPhone(phone);
        newMember.setMemEmail(email);
        newMember.setMemLastLoginIP(lastLoginIP);
        newMember.setMemLastLoginAt(null);
        newMember.setMemOpen(Boolean.parseBoolean(open));
        newMember.setAgreeTermsAt(agreeTerms);
        newMember.setAgreePrivacyAt(agreePrivacy);
        newMember.setAgreeThirdAt(agreeThird);

        boolean success = false;
        try {
            success = memberService.join(newMember);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (success) {
            request.setAttribute("msg", "회원 가입 성공!");
            response.sendRedirect(request.getContextPath() + "/login");
        } else {
            request.setAttribute("errorMsg", "회원 가입 실패 또는 닉네임 중복.");
            request.getRequestDispatcher(viewPage).forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher(viewPage).forward(req, resp);
    }
}
