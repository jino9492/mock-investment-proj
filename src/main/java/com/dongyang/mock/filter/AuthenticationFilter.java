package com.dongyang.mock.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(urlPatterns = {"/mypage/*", "/league"})
public class AuthenticationFilter implements Filter {
    private static final String LOGIN_PAGE_PATH = "/login";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);

        boolean isLoggedIn = (session != null && session.getAttribute("member") != null);

        if (isLoggedIn) {
            chain.doFilter(request, response);
        } else {
            String redirectUrl = httpRequest.getContextPath() + LOGIN_PAGE_PATH;
            httpResponse.sendRedirect(redirectUrl);
            return;
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }
}