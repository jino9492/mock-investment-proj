package com.dongyang.mock.controller;

import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.model.DTO.WatchlistStockDTO;
import com.dongyang.mock.service.WatchlistService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;
import com.google.gson.Gson;

@WebServlet("/api/watchlist")
public class WatchlistController extends HttpServlet {
    private final WatchlistService watchlistService = new WatchlistService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("member") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.print("{\"success\":false,\"message\":\"로그인이 필요합니다.\"}");
            return;
        }

        MemberDTO member = (MemberDTO) session.getAttribute("member");
        int memId = member.getMemId();

        String stockIdParam = req.getParameter("stockId");
        if (stockIdParam == null || stockIdParam.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.print("{\"success\":false,\"message\":\"종목 ID가 필요합니다.\"}");
            return;
        }

        try {
            long stockId = Long.parseLong(stockIdParam);
            boolean success = watchlistService.addWatchlist(memId, stockId);
            
            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            if (success) {
                // 업데이트된 관심종목 목록 반환
                List<WatchlistStockDTO> watchlistStocks = watchlistService.getWatchlistStocksByMember(memId);
                String json = gson.toJson(new WatchlistResponse(true, "관심종목에 추가되었습니다.", watchlistStocks));
                out.print(json);
            } else {
                out.print("{\"success\":false,\"message\":\"관심종목 추가에 실패했습니다.\"}");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.print("{\"success\":false,\"message\":\"올바른 종목 ID 형식이 아닙니다.\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.print("{\"success\":false,\"message\":\"서버 오류가 발생했습니다.\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("member") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.print("{\"success\":false,\"message\":\"로그인이 필요합니다.\"}");
            return;
        }

        MemberDTO member = (MemberDTO) session.getAttribute("member");
        int memId = member.getMemId();

        String stockIdParam = req.getParameter("stockId");
        if (stockIdParam == null || stockIdParam.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.print("{\"success\":false,\"message\":\"종목 ID가 필요합니다.\"}");
            return;
        }

        try {
            long stockId = Long.parseLong(stockIdParam);
            boolean success = watchlistService.removeWatchlist(memId, stockId);
            
            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            if (success) {
                // 업데이트된 관심종목 목록 반환
                List<WatchlistStockDTO> watchlistStocks = watchlistService.getWatchlistStocksByMember(memId);
                String json = gson.toJson(new WatchlistResponse(true, "관심종목에서 제거되었습니다.", watchlistStocks));
                out.print(json);
            } else {
                out.print("{\"success\":false,\"message\":\"관심종목 제거에 실패했습니다.\"}");
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.print("{\"success\":false,\"message\":\"올바른 종목 ID 형식이 아닙니다.\"}");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.print("{\"success\":false,\"message\":\"서버 오류가 발생했습니다.\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("member") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.print("{\"success\":false,\"message\":\"로그인이 필요합니다.\"}");
            return;
        }

        MemberDTO member = (MemberDTO) session.getAttribute("member");
        int memId = member.getMemId();

        try {
            List<WatchlistStockDTO> watchlistStocks = watchlistService.getWatchlistStocksByMember(memId);
            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            String json = gson.toJson(new WatchlistResponse(true, "조회 성공", watchlistStocks));
            out.print(json);
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.setContentType("application/json;charset=UTF-8");
            PrintWriter out = resp.getWriter();
            out.print("{\"success\":false,\"message\":\"서버 오류가 발생했습니다.\"}");
        }
    }

    /**
     * 응답 객체
     */
    private static class WatchlistResponse {
        boolean success;
        String message;
        List<WatchlistStockDTO> watchlistStocks;

        WatchlistResponse(boolean success, String message, List<WatchlistStockDTO> watchlistStocks) {
            this.success = success;
            this.message = message;
            this.watchlistStocks = watchlistStocks;
        }
    }
}
