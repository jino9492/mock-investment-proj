package com.dongyang.mock.controller.api;

import com.dongyang.mock.model.DTO.LeagueDTO;
import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.service.LeagueService;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 전체 포트폴리오 API Controller
 * GET /api/v1/league/portfolio/details
 */
@WebServlet("/api/v1/league/portfolio/details")
public class LeaguePortfolioDetailsApiController extends HttpServlet {
    private final LeagueService leagueService = new LeagueService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("member") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                sendErrorResponse(response, "로그인이 필요합니다.");
                return;
            }

            MemberDTO member = (MemberDTO) session.getAttribute("member");
            int memId = member.getMemId();

            List<LeagueDTO.PortfolioStock> holdings = leagueService.getPortfolioDetails(memId);
            
            long totalProfitLoss = holdings.stream()
                    .mapToLong(LeagueDTO.PortfolioStock::getProfitLoss)
                    .sum();

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("holdings", holdings);
            responseMap.put("total_profit_loss", totalProfitLoss);
            response.getWriter().write(gson.toJson(responseMap));
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            sendErrorResponse(response, "서버 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            sendErrorResponse(response, "알 수 없는 오류가 발생했습니다.");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        response.getWriter().write(gson.toJson(errorResponse));
    }
}

