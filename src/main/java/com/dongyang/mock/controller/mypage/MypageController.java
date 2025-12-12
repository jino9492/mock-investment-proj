package com.dongyang.mock.controller.mypage;

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
import java.sql.Timestamp;

@WebServlet("/mypage")
public class MypageController extends HttpServlet {
    private  final MemberService memberService = new MemberService();
    private static final String VIEW_PAGE = "/WEB-INF/views/mypage/mypage.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        String modifyRequest = request.getParameter("modify");
        request.setAttribute("modify", modifyRequest);

        request.getRequestDispatcher(VIEW_PAGE).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        MemberDTO sessionMember = null;
        if (session != null) {
            sessionMember = (MemberDTO) session.getAttribute("member");
        }

        if (sessionMember == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if(request.getParameter("action")!=null && request.getParameter("action").equals("withdrawal")){
            try {
                memberService.deleteMember(sessionMember.getMemId());
                session.removeAttribute("member");
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/");
                return;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        request.setCharacterEncoding("UTF-8");

        String password = request.getParameter("password");
        String passwordCheck = request.getParameter("passwordCheck");
        String nickname = request.getParameter("nickname");
        String agreeNotification = request.getParameter("agreeNotification");

        if (password == null) password = "";
        if (passwordCheck == null) passwordCheck = "";
        if (nickname == null) nickname = "";

        if (!password.isEmpty() || !passwordCheck.isEmpty()) {
            if (password.isEmpty() != passwordCheck.isEmpty()) {
                request.setAttribute("errorMsg", "비밀번호와 비밀번호 확인을 모두 입력해주세요.");
                request.setAttribute("modify", "1");
                request.getRequestDispatcher(VIEW_PAGE).forward(request, response);
                return;
            }

            if (!password.equals(passwordCheck)) {
                request.setAttribute("errorMsg", "비밀번호 확인이 일치하지 않습니다.");
                request.setAttribute("modify", "1");
                request.getRequestDispatcher(VIEW_PAGE).forward(request, response);
                return;
            }
        }

        MemberDTO update = new MemberDTO();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        update.setMemId(sessionMember.getMemId());
        update.setMemEmail(sessionMember.getMemEmail());
        update.setMemPassword(password.isEmpty() ? null : password);
        update.setMemNickname(nickname.isEmpty() ? null : nickname);

        if("on".equals(agreeNotification)) {
            update.setAgreeNotificationAt(now);
        } else {
            update.setAgreeNotificationAt(null);
        }

        try {
            memberService.updateMemberAccount(update);

            if (update.getMemNickname() != null) {
                sessionMember.setMemNickname(update.getMemNickname());
            }

            sessionMember.setAgreeNotificationAt(update.getAgreeNotificationAt());

            session.setAttribute("member", sessionMember);

            request.getRequestDispatcher(VIEW_PAGE).forward(request, response);
        } catch (SQLException e) {
            System.err.println("DB 업데이트 오류: " + e.getMessage());
            request.setAttribute("errorMsg", "정보 수정 중 데이터베이스 오류가 발생했습니다.");
            request.getRequestDispatcher(VIEW_PAGE).forward(request, response);
        }
    }
}
