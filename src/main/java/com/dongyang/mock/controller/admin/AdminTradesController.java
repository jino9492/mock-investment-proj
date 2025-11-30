package com.dongyang.mock.controller.admin;

import com.dongyang.mock.model.DTO.TradeDTO;
import com.dongyang.mock.model.DTO.TradeStatsDTO;
import com.dongyang.mock.service.TradeService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * 관리자 거래 관리 컨트롤러
 * - 거래 목록 조회 및 필터링
 * - 거래 통계 조회
 */
@WebServlet("/admin/trades")
public class AdminTradesController extends BaseAdminController {

    private final TradeService tradeService = new TradeService();

    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 필터 파라미터 추출
        String startDate = trim(req.getParameter("startDate"));
        String endDate = trim(req.getParameter("endDate"));
        String typeFilter = trim(req.getParameter("typeFilter"));
        String statusFilter = trim(req.getParameter("statusFilter"));
        String stockKeyword = trim(req.getParameter("stockKeyword"));
        String userKeyword = trim(req.getParameter("userKeyword"));

        try {
            // 거래 목록 조회
            List<TradeDTO> tradeList = tradeService.getTrades(
                    startDate, endDate, typeFilter, statusFilter, stockKeyword, userKeyword);

            // 거래 통계 조회
            TradeStatsDTO tradeStats = tradeService.getTradeStats();
            if (tradeStats == null) {
                tradeStats = new TradeStatsDTO();
            }

            // JSP에 데이터 전달
            req.setAttribute("tradeList", tradeList);
            req.setAttribute("tradeStats", tradeStats);
            req.setAttribute("startDate", startDate);
            req.setAttribute("endDate", endDate);
            req.setAttribute("typeFilter", typeFilter);
            req.setAttribute("statusFilter", statusFilter);
            req.setAttribute("stockKeyword", stockKeyword);
            req.setAttribute("userKeyword", userKeyword);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServletException("Failed to load trade list", e);
        }

        setCurrentPath(req, "/admin/trades");
        req.getRequestDispatcher("/WEB-INF/views/admin/trades.jsp").forward(req, resp);
    }
}

