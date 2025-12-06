package com.dongyang.mock.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

@WebFilter("/*")
public class PathAttributeFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

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

        String currentPath = requestURI.replaceFirst(contextPath, "");

        httpRequest.setAttribute("currentUrlPath", currentPath);

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

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}