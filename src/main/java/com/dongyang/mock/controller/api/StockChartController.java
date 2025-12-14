package com.dongyang.mock.controller.api;

import com.dongyang.mock.model.DAO.StockChartDAO;
import com.dongyang.mock.model.DAO.StockDAO;
import com.dongyang.mock.model.DAO.StockDetailDAO;
import com.dongyang.mock.model.DTO.StockChartDTO;
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
import java.util.List;

@WebServlet("/api/stock/chart")
public class StockChartController extends HttpServlet {
    Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest reqest, HttpServletResponse response) throws ServletException, IOException {
        StockChartDAO stockChartDAO = new StockChartDAO();
        StockDAO stockDAO = new StockDAO();
        List<StockChartDTO> stockChartDTOs;
        String code = reqest.getParameter("code");

        try {
            stockChartDTOs = DBManager.withConnection(conn->{
                Long stockId = stockDAO.getStockIdByCode(code, conn);
                return stockChartDAO.findRecentStockChartData(stockId, 24*5, conn);
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        String jsonResponse = gson.toJson(stockChartDTOs);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse);
    }
}
