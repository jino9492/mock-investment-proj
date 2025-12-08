package com.dongyang.mock.controller.api;

import com.dongyang.mock.model.DTO.RankingItemDTO;
import com.dongyang.mock.service.api.fetch.RankingFetchService;
import com.dongyang.mock.util.api.InmemoryCacheManager;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/rank/items")
public class RankingController extends HttpServlet {
    private final InmemoryCacheManager cacheManager = InmemoryCacheManager.getInstance();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            List<RankingItemDTO> cachedRanking = cacheManager.getList(RankingFetchService.RANKING_CACHE_KEY);
            Gson gson = new Gson();
            String jsonResponse = gson.toJson(cachedRanking);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Server error: " + e.getMessage() + "\"}");
        }
    }
}
