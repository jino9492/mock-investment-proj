package com.dongyang.mock.controller.api;

import com.dongyang.mock.model.DAO.StockDAO;
import com.dongyang.mock.model.DAO.StockDetailDAO;
import com.dongyang.mock.model.DTO.StockDetailDTO;
import com.dongyang.mock.util.db.DBManager;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/stock/detail")
public class StockDetailController extends HttpServlet {
    Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest reqest, HttpServletResponse response) throws ServletException, IOException {
        StockDetailDAO stockDetailDAO = new StockDetailDAO();
        StockDAO stockDAO = new StockDAO();
        StockDetailDTO stockDetailDTO;
        String code = reqest.getParameter("code");
        try {
            stockDetailDTO = DBManager.withConnection(conn->{
               Long stockId = Long.parseLong(stockDAO.getStockIdByCode(code, conn));
               return stockDetailDAO.selectStockDetailById(stockId, conn);
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String jsonResponse = gson.toJson(stockDetailDTO);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }
}
