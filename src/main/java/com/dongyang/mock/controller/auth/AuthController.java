package com.dongyang.mock.controller.auth;

import com.dongyang.mock.service.AuthService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/auth/issue-token")
public class AuthController extends HttpServlet {

    private AuthService authService;

    @Override
    public void init() throws ServletException {
        // 서블릿 초기화 시 Service 인스턴스 생성
        this.authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 1. Model(Service) 호출
        String token = authService.issueAccessToken();

        // 2. 결과에 따라 응답 구성
        if (token != null) {
            // 토큰 발급 성공 (캐시된 토큰이 반환될 수도 있음)
            response.getWriter().write("{\"success\": true, \"accessToken\": \"" + token + "\"}");
        } else {
            // 토큰 발급 실패
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
            response.getWriter().write("{\"success\": false, \"message\": \"Access Token 발급에 실패했습니다. 키 또는 네트워크를 확인하세요.\"}");
        }
    }
}