package com.dongyang.mock.controller;

import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.service.HomeService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns = {"/", "/home"}, loadOnStartup = 1)
public class HomeController extends HttpServlet {
    private final HomeService homeService = new HomeService();
    private static final String VIEW_PAGE = "/WEB-INF/views/home/home.jsp";

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
                // 기본 서블릿을 찾을 수 없으면 404
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
                request.setAttribute("accountSummary", homeService.getAccountSummary(memId));
                
                // 수익률 상위 종목 (3개)
                request.setAttribute("topReturnStocks", homeService.getTopReturnStocks(memId, 3));
            } catch (SQLException e) {
                System.err.println("계좌 데이터 조회 오류: " + e.getMessage());
                request.setAttribute("errorMsg", "데이터를 불러오는 중 오류가 발생했습니다.");
            }
        }

        request.setAttribute("member", member);
        request.setAttribute("isLoggedIn", isLoggedIn);

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

