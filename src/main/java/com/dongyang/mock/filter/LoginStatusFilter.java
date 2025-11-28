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

        httpRequest.setAttribute("isLoggedIn", isLoggedIn);
        httpRequest.setAttribute("member", member);

        chain.doFilter(request, response);
    }
}