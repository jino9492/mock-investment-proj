package com.dongyang.mock.controller.mypage;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/mypage/orders")
public class OrderDetailsController extends HttpServlet {

    private final String viewPage = "/WEB-INF/views/mypage/order_details.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.getRequestDispatcher(viewPage).forward(req, resp);
    }
}
