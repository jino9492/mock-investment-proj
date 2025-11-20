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
import java.sql.Date;
import java.sql.SQLException;

@WebServlet("/member")
public class MemberController extends HttpServlet {
    private MemberService memberService = new MemberService(); // Service 객체 생성

    // ... (doGet() 메소드 생략 - 폼 페이지 이동 담당)

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String cmd = request.getParameter("cmd");
        String viewPage = null;

        if (cmd.equals("join")) {
            String nickname = request.getParameter("nickname");
            String password = request.getParameter("password");
            String name = request.getParameter("name");
            String birth = request.getParameter("birth");
            String phone = request.getParameter("phone");
            String email = request.getParameter("email");
            String open = request.getParameter("memOpen"); // 공개 여부 ('Y' 또는 'N' 가정)
            String agreeTerms = request.getParameter("agreeTerms");     // 약관 동의 체크박스 (on/null)
            String agreePrivacy = request.getParameter("agreePrivacy"); // 개인정보 동의 체크박스 (on/null)
            String agreeThird = request.getParameter("agreeThird");     // 제3자 제공 동의 체크박스 (on/null)

            MemberDTO newMember = new MemberDTO();
            newMember.setMemNickName(nickname);
            newMember.setMemPassword(password);
            newMember.setMemName(name);
            newMember.setMemPhone(birth);
            newMember.setMemPhone(phone);
            newMember.setMemEmail(email);
            newMember.setMemOpen(Boolean.parseBoolean(open));
            newMember.setAgreeTermsAt(Date.valueOf(agreeTerms));
            newMember.setAgreePrivacyAt(Date.valueOf(agreePrivacy));
            newMember.setAgreeThirdAt(Date.valueOf(agreeThird));

            boolean success = false;
            try {
                success = memberService.join(newMember);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            if (success) {
                request.setAttribute("msg", "회원 가입 성공!");
                viewPage = "/views/loginForm.jsp";
            } else {
                request.setAttribute("errorMsg", "회원 가입 실패 또는 닉네임 중복.");
                viewPage = "/views/joinForm.jsp";
            }
        } else if (cmd.equals("login")) {
            String nickname = request.getParameter("nickname");
            String password = request.getParameter("password");

            // Service 호출
            MemberDTO member = null;
            try {
                member = memberService.login(nickname, password);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }

            if (member != null) {
                HttpSession session = request.getSession();
                session.setAttribute("member", member);
                viewPage = "/views/home.jsp";
            } else {
                request.setAttribute("errorMsg", "로그인 정보가 일치하지 않습니다.");
                viewPage = "/views/loginForm.jsp";
            }
        }

        request.getRequestDispatcher(viewPage).forward(request, response);
    }
}