package com.dongyang.mock.controller.api;

import com.dongyang.mock.model.DTO.RankingItemDTO;
import com.dongyang.mock.model.DTO.RealtimeDTO;
import com.dongyang.mock.service.api.RealtimeStockService;
import com.dongyang.mock.service.api.WebSocketService;
import com.dongyang.mock.service.api.fetch.RankingFetchService;
import com.dongyang.mock.util.api.InmemoryCacheManager;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/socket")
public class SocketController extends HttpServlet {
    InmemoryCacheManager cacheManager = InmemoryCacheManager.getInstance();
    RealtimeStockService realtimeStockService = new RealtimeStockService();
    Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<RankingItemDTO> cachedRanking = cacheManager.getList(RankingFetchService.RANKING_CACHE_KEY);
        List<String> list = new ArrayList<>();
        for (RankingItemDTO rankingItemDTO : cachedRanking) { list.add(rankingItemDTO.getStkCode()); }
        realtimeStockService.connectSocket(list, "1");

        String code = req.getParameter("code");

        Class<RealtimeDTO> type = RealtimeDTO.class;
        RealtimeDTO cachedReal = cacheManager.get(WebSocketService.REALTIME_CACHE_KEY + code + "_AL", type );
        String jsonResponse = gson.toJson(cachedReal);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(jsonResponse);
    }
}
