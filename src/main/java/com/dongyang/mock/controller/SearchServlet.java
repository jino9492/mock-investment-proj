package com.dongyang.mock.controller;

import com.dongyang.mock.model.DTO.SearchResultDTO;
import com.dongyang.mock.service.SearchService;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/search")
public class SearchServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SearchServlet.class);
    private final SearchService searchService = new SearchService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String query = req.getParameter("q");

        if (query == null || query.trim().isEmpty()) {
            logger.debug("빈 검색어 요청");
            Map<String, Object> response = new HashMap<>();
            response.put("results", List.of());
            resp.getWriter().write(gson.toJson(response));
            return;
        }

        query = query.trim();
        logger.info("검색 요청 - 검색어: '{}'", query);

        try {
            // 검색 실행
            List<SearchResultDTO> resultList = searchService.searchStock(query);
            logger.debug("검색 완료 - 검색어: '{}', 결과 수: {}", query, resultList.size());

            // 응답 데이터 구성
            Map<String, Object> response = new HashMap<>();
            response.put("results", resultList);
            
            String jsonResponse = gson.toJson(response);
            resp.getWriter().write(jsonResponse);

        } catch (Exception e) {
            logger.error("검색 중 오류 발생 - 검색어: '{}'", query, e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, String> errorResponse = new HashMap<>();
            // 프로덕션 환경에서는 상세 에러 메시지 노출하지 않음
            errorResponse.put("error", "검색 중 오류가 발생했습니다.");
            resp.getWriter().write(gson.toJson(errorResponse));
        }
    }
}
