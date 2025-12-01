package com.dongyang.mock.controller.stock;

import com.dongyang.mock.model.DTO.*;
import com.dongyang.mock.service.api.StockDetailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;


@WebServlet("/stock")
public class StockController extends HttpServlet {

    private final String viewPage = "/WEB-INF/views/stock/chart.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Long stkId = Long.parseLong(req.getParameter("stockID"));

        StockDetailService stockDetailService = new StockDetailService();

        StockDTO stockDTO;
        StockDetailDTO stockDetailDTO;
        StockFinancialIndicatorDTO stockFinancialIndicatorDTO;
        StockFinancialSummaryDTO stockFinancialSummaryDTO;
        StockPriceDailyDTO stockPriceDailyDTO;

        try{
            stockDTO = stockDetailService.getStockById(stkId);
            stockDetailDTO = stockDetailService.getStockDetailById(stkId);
            stockFinancialIndicatorDTO = stockDetailService.getFinancialIndicatorById(stkId);
            stockFinancialSummaryDTO = stockDetailService.getFinancialSummaryById(stkId);
            stockPriceDailyDTO = stockDetailService.getStockPriceDailyById(stkId);

            req.setAttribute("stock", stockDTO);
            req.setAttribute("detail", stockDetailDTO);
            req.setAttribute("indicator", stockFinancialIndicatorDTO);
            req.setAttribute("summary", stockFinancialSummaryDTO);
            req.setAttribute("daily", stockPriceDailyDTO);
        } catch (Exception e){
            e.printStackTrace();
        }

        req.getRequestDispatcher(viewPage).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {


        req.getRequestDispatcher(viewPage).forward(req, resp);
    }
}