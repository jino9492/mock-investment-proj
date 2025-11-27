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
        this.authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String token = authService.issueAccessToken();
        if (token != null) {
            response.getWriter().write("{\"success\": true, \"accessToken\": \"" + token + "\"}");
        } else {
            // 토큰 발급 실패
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\": false, \"message\": \"Access Token 발급에 실패했습니다. 키 또는 네트워크를 확인하세요.\"}");
        }
    }
}