package com.dongyang.mock.filter;

import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.service.MemberService;
import com.dongyang.mock.service.WatchlistService;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

// 모든 요청에 대해 필터 적용
@WebFilter("/*")
public class LoginStatusFilter implements Filter {

    private final MemberService memberService = new MemberService();
    private final WatchlistService watchlistService = new WatchlistService();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();

        // 정적 리소스는 필터를 건너뛰기
        if (isStaticResource(requestURI, contextPath)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);

        MemberDTO member = null;
        boolean isLoggedIn = false;

        if (session != null) {
            Object sessionAttr = session.getAttribute("member");

            if (sessionAttr instanceof MemberDTO) {
                member = (MemberDTO) sessionAttr;
                isLoggedIn = true;
                
                // 로그인한 사용자의 보유 종목 및 관심종목 조회
                try {
                    int memId = member.getMemId();
                    
                    // 보유 종목 목록
                    var holdings = memberService.getMemberHoldings(memId);
                    httpRequest.setAttribute("holdings", holdings);
                    
                    // 관심종목 목록
                    var watchlistStocks = watchlistService.getWatchlistStocksByMember(memId);
                    httpRequest.setAttribute("watchlistStocks", watchlistStocks);
                } catch (SQLException e) {
                    e.printStackTrace();
                    // 에러 발생 시 빈 리스트 설정
                    httpRequest.setAttribute("holdings", java.util.Collections.emptyList());
                    httpRequest.setAttribute("watchlistStocks", java.util.Collections.emptyList());
                }
            }
        }

        httpRequest.setAttribute("isLoggedIn", isLoggedIn);
        httpRequest.setAttribute("member", member);

        chain.doFilter(request, response);
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