package com.dongyang.mock.controller;

import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.service.LeagueService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/league")
public class LeagueController extends HttpServlet {
    private final LeagueService leagueService = new LeagueService();
    private static final String VIEW_PAGE = "/WEB-INF/views/league/league.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        HttpSession session = request.getSession(false);
        MemberDTO member = null;
        boolean isLoggedIn = false;

        if (session != null && session.getAttribute("member") != null) {
            member = (MemberDTO) session.getAttribute("member");
            isLoggedIn = true;

            // 로그인한 사용자의 경우 리그 데이터 로드
            try {
                int memId = member.getMemId();
                
                // 계좌 요약 정보
                request.setAttribute("accountSummary", leagueService.getAccountSummary(memId));
                
                // 수익률 상위 종목 (3개)
                request.setAttribute("topReturnStocks", leagueService.getTopReturnStocks(memId, 3));
                
                // 주변 랭킹
                request.setAttribute("nearbyRankings", leagueService.getNearbyRankings(memId));
                
                // 포트폴리오 상세
                request.setAttribute("portfolioDetails", leagueService.getPortfolioDetails(memId));
            } catch (SQLException e) {
                System.err.println("리그 데이터 조회 오류: " + e.getMessage());
                request.setAttribute("errorMsg", "데이터를 불러오는 중 오류가 발생했습니다.");
            }
        }

        request.setAttribute("member", member);
        request.setAttribute("isLoggedIn", isLoggedIn);

        String modifyRequest = request.getParameter("modify");
        request.setAttribute("modify", modifyRequest);

        request.getRequestDispatcher(VIEW_PAGE).forward(request, response);
    }
}
