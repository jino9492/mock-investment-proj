package com.dongyang.mock.controller.api;

import com.dongyang.mock.service.api.RealtimeStockService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/socket")
public class SocketController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RealtimeStockService realtimeStockService = new RealtimeStockService();
        realtimeStockService.connectSocket();

    }
}
