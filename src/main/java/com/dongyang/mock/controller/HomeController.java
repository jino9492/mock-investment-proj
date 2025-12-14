package com.dongyang.mock.controller;

import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.model.DTO.RankingItemDTO;
import com.dongyang.mock.model.DTO.RealtimeDTO;
import com.dongyang.mock.service.HomeService;
import com.dongyang.mock.service.api.RealtimeStockService;
import com.dongyang.mock.service.api.fetch.RankingFetchService;
import com.dongyang.mock.util.api.InmemoryCacheManager;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/", "/home"}, loadOnStartup = 1)
public class HomeController extends HttpServlet {
    private final HomeService homeService = new HomeService();
    private static final String VIEW_PAGE = "/default_layout.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
        
        // 정적 리소스는 기본 서블릿이 처리하도록 위임
        if (isStaticResource(requestURI, contextPath)) {
            RequestDispatcher dispatcher = getServletContext().getNamedDispatcher("default");
            if (dispatcher != null) {
                dispatcher.forward(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            return;
        }
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        HttpSession session = request.getSession(false);
        MemberDTO member = null;
        boolean isLoggedIn = false;

        if (session != null && session.getAttribute("member") != null) {
            member = (MemberDTO) session.getAttribute("member");
            isLoggedIn = true;

            // 로그인한 사용자의 경우 계좌 데이터 로드
            try {
                int memId = member.getMemId();
                
                // 계좌 요약 정보
                var accountSummary = homeService.getAccountSummary(memId);
                request.setAttribute("accountSummary", accountSummary);
                
                // 수익률 상위 종목 (3개)
                var topReturnStocks = homeService.getTopReturnStocks(memId, 3);
                request.setAttribute("topReturnStocks", topReturnStocks);
            } catch (SQLException e) {
                System.err.println("[HomeController] 계좌 데이터 조회 오류: " + e.getMessage());
                e.printStackTrace();
                request.setAttribute("errorMsg", "데이터를 불러오는 중 오류가 발생했습니다.");
            }
        }

        request.setAttribute("member", member);
        request.setAttribute("isLoggedIn", isLoggedIn);
        
        // default_layout.jsp로 forward하고 contentPage를 attribute로 전달
        request.setAttribute("contentPage", "/WEB-INF/views/home/home_content.jsp");
        request.getRequestDispatcher(VIEW_PAGE).forward(request, response);
    }
    
    private boolean isStaticResource(String requestURI, String contextPath) {
        String path = requestURI;
        if (contextPath != null && !contextPath.isEmpty()) {
            path = requestURI.replaceFirst(contextPath, "");
        }
        
        // 정적 리소스 경로 체크
        return path.startsWith("/static/") ||
               path.startsWith("/WEB-INF/") ||
               path.endsWith(".js") ||
               path.endsWith(".css") ||
               path.endsWith(".png") ||
               path.endsWith(".jpg") ||
               path.endsWith(".jpeg") ||
               path.endsWith(".gif") ||
               path.endsWith(".svg") ||
               path.endsWith(".ico") ||
               path.endsWith(".woff") ||
               path.endsWith(".woff2") ||
               path.endsWith(".ttf") ||
               path.endsWith(".eot");
    }
}

