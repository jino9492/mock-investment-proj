package com.dongyang.mock.filter;

import com.dongyang.mock.model.DTO.MemberDTO;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

// 모든 요청에 대해 필터 적용
@WebFilter("/*")
public class LoginStatusFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // 1. 세션에서 "member" 객체 가져오기 (새 세션 생성 방지)
        HttpSession session = httpRequest.getSession(false);

        MemberDTO member = null;
        boolean isLoggedIn = false;

        if (session != null) {
            Object sessionAttr = session.getAttribute("member");

            if (sessionAttr instanceof MemberDTO) {
                member = (MemberDTO) sessionAttr;
                isLoggedIn = true;
            }
        }

        // 2. 로그인 상태와 MemberDTO 객체를 request 속성으로 설정
        // JSP 페이지에서 ${isLoggedIn} 와 ${member} 로 바로 사용 가능
        httpRequest.setAttribute("isLoggedIn", isLoggedIn);
        httpRequest.setAttribute("member", member);

        // 3. 다음 필터나 최종 대상(JSP/서블릿)으로 요청 전달
        chain.doFilter(request, response);
    }

    // init()과 destroy() 메서드는 생략 가능
}