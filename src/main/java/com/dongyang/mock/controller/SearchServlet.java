//package com.dongyang.mock.controller;
//
//import com.dongyang.mock.model.DTO.StockDTO;
//import com.dongyang.mock.service.SearchService;
//
//import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
//import javax.servlet.http.HttpServlet;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
//import java.io.IOException;
//import java.util.LinkedList;
//import java.util.List;
//
//@WebServlet("/search")
//public class SearchServlet extends HttpServlet {
//
//    private static final int RECENT_LIMIT = 5;
//    private final SearchService searchService = new SearchService();
//
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
//            throws ServletException, IOException {
//
//        System.out.println(">>> [SearchServlet] doGet 진입");
//
//        String query = req.getParameter("q");
//        System.out.println(">>> [SearchServlet] q = " + query);
//
//        if (query == null || query.trim().isEmpty()) {
//            req.setAttribute("searchQuery", "");
//            req.setAttribute("searchResults", null);
//            req.getRequestDispatcher("/WEB-INF/views/search/searchResult.jsp")
//                    .forward(req, resp);
//            return;
//        }
//
//        query = query.trim();
//
//        // ========= 최근 검색어 세션에 저장 =========
//        HttpSession session = req.getSession();
//        @SuppressWarnings("unchecked")
//        LinkedList<String> recent =
//                (LinkedList<String>) session.getAttribute("recentSearches");
//
//        if (recent == null) {
//            recent = new LinkedList<>();
//        }
//
//        // 중복 제거 후 맨 앞에 추가
//        recent.remove(query);
//        recent.addFirst(query);
//
//        // 최대 개수 제한
//        while (recent.size() > RECENT_LIMIT) {
//            recent.removeLast();
//        }
//
//        session.setAttribute("recentSearches", recent);
//        System.out.println(">>> [SearchServlet] recentSearches in session = " + recent);
//
//        // =========  실제 검색 실행 =========
//        List<StockDTO> resultList = searchService.searchStock(query);
//
//        req.setAttribute("searchQuery", query);
//        req.setAttribute("searchResults", resultList);
//
//        // ========= 결과 페이지로 포워드 =========
//        req.getRequestDispatcher("/WEB-INF/views/search/searchResult.jsp")
//                .forward(req, resp);
//    }
//}
