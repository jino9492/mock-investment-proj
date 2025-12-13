package com.dongyang.mock.controller.stock;

import com.dongyang.mock.model.DTO.*;
import com.dongyang.mock.service.api.StockDetailService;
import com.dongyang.mock.util.StockCodeResolver;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;


@WebServlet("/stock_info")
public class StockInfoController extends HttpServlet {

    private final String viewPage = "/WEB-INF/views/stock/info.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String codeParam = req.getParameter("code");
        if (codeParam == null || codeParam.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "종목 코드가 필요합니다.");
            return;
        }

        codeParam = codeParam.trim();
        StockDetailService stockDetailService = new StockDetailService();
        StockCodeResolver codeResolver = new StockCodeResolver(stockDetailService);

        StockDTO stockDTO;
        StockDetailDTO stockDetailDTO;
        StockFinancialIndicatorDTO stockFinancialIndicatorDTO;
        StockFinancialSummaryDTO stockFinancialSummaryDTO;
        StockPriceDailyDTO stockPriceDailyDTO;

        try{
            // 종목 코드로 조회 (stock_symbol 우선, 실패 시 stock_code_id)
            stockDTO = codeResolver.resolveStock(codeParam);
            
            if (stockDTO == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "종목을 찾을 수 없습니다: " + codeParam);
                return;
            }
            
            Long stkId = stockDTO.getStockId();
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
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
            return;
        }

        req.getRequestDispatcher(viewPage).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.getRequestDispatcher(viewPage).forward(req, resp);
    }
}