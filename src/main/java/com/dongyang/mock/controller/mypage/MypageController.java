package com.dongyang.mock.controller.mypage;

import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.model.DTO.MemberAssetDTO;
import com.dongyang.mock.service.MemberService;
import com.dongyang.mock.service.TradeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@WebServlet("/mypage")
public class MypageController extends HttpServlet {
    private  final MemberService memberService = new MemberService();
    private final TradeService tradeService = new TradeService();
    private static final String VIEW_PAGE = "/WEB-INF/views/mypage/mypage.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        HttpSession session = request.getSession(false);
        MemberDTO sessionMember = null;
        if (session != null) {
            sessionMember = (MemberDTO) session.getAttribute("member");
        }

        if (sessionMember == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // 세션에서 member 정보를 request에 설정
        request.setAttribute("member", sessionMember);

        // 현재 월의 시작일과 종료일 계산
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());
        
        String startDate = startOfMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String endDate = endOfMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // 월별 수익 데이터 조회
        try {
            int monthlyProfit = tradeService.getTotalRealizedProfit(sessionMember.getMemId(), startDate, endDate);
            request.setAttribute("monthlyProfit", monthlyProfit);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("monthlyProfit", 0);
        }

        // 회원 자산 정보 조회 (총 자산, 주문 가능 금액, 투자 중인 금액)
        try {
            MemberAssetDTO memberAsset = memberService.getMemberAsset(sessionMember.getMemId());
            if (memberAsset != null) {
                long totalAsset = memberAsset.getTotalAsset(); // 총 자산
                long investedAmount = memberAsset.getStockValue(); // 투자 중인 금액
                long availableCash = totalAsset - investedAmount; // 주문 가능 금액 = 총 자산 - 투자 중인 금액
                
                request.setAttribute("totalAsset", totalAsset);
                request.setAttribute("availableCash", availableCash);
                request.setAttribute("investedAmount", investedAmount);
            } else {
                request.setAttribute("totalAsset", 0L);
                request.setAttribute("availableCash", 0L);
                request.setAttribute("investedAmount", 0L);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("totalAsset", 0L);
            request.setAttribute("availableCash", 0L);
            request.setAttribute("investedAmount", 0L);
        }

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

        String action = request.getParameter("action");
        
        // 파산 신청 처리
        if (action != null && action.equals("bankruptcy")) {
            try {
                boolean success = memberService.declareBankruptcy(sessionMember.getMemId());
                if (success) {
                    // 세션의 회원 정보 업데이트 (virtual_cash가 100만원으로 리셋됨)
                    MemberDTO updatedMember = memberService.getMemberById(sessionMember.getMemId());
                    if (updatedMember != null) {
                        updatedMember.setMemPassword(null); // 비밀번호는 세션에 저장하지 않음
                        session.setAttribute("member", updatedMember);
                    }
                    response.sendRedirect(request.getContextPath() + "/mypage?bankruptcy=success");
                } else {
                    response.sendRedirect(request.getContextPath() + "/mypage?bankruptcy=fail");
                }
                return;
            } catch (SQLException e) {
                e.printStackTrace();
                response.sendRedirect(request.getContextPath() + "/mypage?bankruptcy=error");
                return;
            }
        }
        
        // 회원 탈퇴 처리
        if (action != null && action.equals("withdrawal")) {
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
