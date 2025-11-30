package com.dongyang.mock.controller.auth;

import com.dongyang.mock.service.auth.IndexDataService;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/index/sparkline")
public class IndexDataController extends HttpServlet {

    private final IndexDataService indexService = new IndexDataService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String indexCode = request.getParameter("code"); // 예: "KOSPI"

        if (indexCode == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Code parameter missing\"}");
            return;
        }

        try {
            Map<String, List<Double>> data = indexService.getRecentSparklineData(indexCode);

            Gson gson = new Gson();
            String jsonResponse = gson.toJson(data);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(jsonResponse);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Server error: " + e.getMessage() + "\"}");
        }
    }
}