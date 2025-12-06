package com.dongyang.mock.controller.auth;

import com.dongyang.mock.service.AuthService;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.dongyang.mock.model.DTO.AuthResponseDTO;
import com.google.gson.Gson;

@WebServlet("/auth/issue-token")
public class AuthController extends HttpServlet {
    private AuthService authService;
    private final Gson gson = new Gson(); // Gson 인스턴스 생성

    @Override
    public void init() throws ServletException {
        this.authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String token = authService.issueAccessToken();

        AuthResponseDTO responseDTO;

        if (token != null) {
            responseDTO = new AuthResponseDTO(true, token, "Access Token Issued");
        } else {
            responseDTO = new AuthResponseDTO(false, null, "Access Token Issuing Failed");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        String jsonResponse = gson.toJson(responseDTO);
        response.getWriter().write(jsonResponse);
    }
}