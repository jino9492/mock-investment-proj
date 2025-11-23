package com.dongyang.mock.controller;

import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.model.status.MemberStatus;
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

        String email = request.getParameter("email");
        String nickname = request.getParameter("nickname");
        String password = request.getParameter("password");
        String lastLoginIP = request.getRemoteAddr();
        Timestamp agreePrivacy = DateParser.getCheckTime(request.getParameter("agreePrivacy"), now);
        Timestamp agreeThird = DateParser.getCheckTime(request.getParameter("agreeThird"), now);
        Timestamp agreeNotification = DateParser.getCheckTime(request.getParameter("agreeNotification"), now);

        MemberDTO newMember = new MemberDTO();
        newMember.setMemEmail(email);
        newMember.setMemPassword(password);
        newMember.setMemNickname(nickname);
        newMember.setMemLastLoginIP(lastLoginIP);
        newMember.setMemLastLoginAt(null);
        newMember.setAgreePrivacyAt(agreePrivacy);
        newMember.setAgreeThirdAt(agreeThird);
        newMember.setAgreeNotificationAt(agreeNotification);

        MemberStatus.JoinResult status = MemberStatus.JoinResult.FAIL_INSERT;
        try {
            status = memberService.join(newMember);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        if (status == MemberStatus.JoinResult.SUCCESS) {
            request.setAttribute("msg", "회원 가입 성공!");
            response.sendRedirect(request.getContextPath() + "/login");
        } else if (status == MemberStatus.JoinResult.DUPLICATE_EMAIL) {
            request.setAttribute("errorMsg", "이메일 중복");
            request.getRequestDispatcher(viewPage).forward(request, response);
        } else if (status == MemberStatus.JoinResult.DUPLICATE_NICKNAME) {
            request.setAttribute("errorMsg", "닉네임 중복");
            request.getRequestDispatcher(viewPage).forward(request, response);
        } else {
            request.setAttribute("errorMsg", "회원가입 실패");
            request.getRequestDispatcher(viewPage).forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher(viewPage).forward(req, resp);
    }
}
