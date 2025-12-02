package com.dongyang.mock.controller.admin;

import com.dongyang.mock.model.DTO.MemberStatsDTO;
import com.dongyang.mock.model.DTO.ServerStatsDTO;
import com.dongyang.mock.model.DTO.TradeStatsDTO;
import com.dongyang.mock.service.MemberService;
import com.dongyang.mock.service.TradeService;
import com.dongyang.mock.util.ServerStatsUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/admin/dashboard")
public class AdminDashboardController extends BaseAdminController {

    private final MemberService memberService = new MemberService();
    private final TradeService tradeService = new TradeService();

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            // 회원 통계 조회
            MemberStatsDTO memberStats = memberService.getMemberStats();
            if (memberStats == null) {
                memberStats = new MemberStatsDTO();
            }

            // 거래 통계 조회
            TradeStatsDTO tradeStats = tradeService.getTradeStats();
            if (tradeStats == null) {
                tradeStats = new TradeStatsDTO();
            }

            // 서버 상태 조회
            ServerStatsDTO serverStats = ServerStatsUtil.getServerStats();

            req.setAttribute("memberStats", memberStats);
            req.setAttribute("tradeStats", tradeStats);
            req.setAttribute("serverStats", serverStats);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServletException("Failed to load dashboard statistics", e);
        }

        setCurrentPath(req, "/admin/dashboard");
        req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);
    }
}

