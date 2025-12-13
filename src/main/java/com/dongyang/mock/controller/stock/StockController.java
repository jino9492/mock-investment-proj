package com.dongyang.mock.controller.stock;

import com.dongyang.mock.model.DTO.*;
import com.dongyang.mock.model.DAO.MemberDAO;
import com.dongyang.mock.service.OrderService;
import com.dongyang.mock.service.api.StockDetailService;
import com.dongyang.mock.util.StockCodeResolver;
import com.dongyang.mock.util.db.DBManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;


@WebServlet("/stock")
public class StockController extends HttpServlet {

    private final String viewPage = "/WEB-INF/views/stock/chart.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String codeParam = req.getParameter("code");
        if (codeParam == null || codeParam.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "종목 코드가 필요합니다.");
            return;
        }

        codeParam = codeParam.trim();
        System.out.println("[StockController] 요청된 종목 코드: '" + codeParam + "'");

        StockDetailService stockDetailService = new StockDetailService();
        StockCodeResolver codeResolver = new StockCodeResolver(stockDetailService);
        OrderService orderService = new OrderService();
        MemberDAO memberDAO = new MemberDAO();

        StockDTO stockDTO;
        StockDetailDTO stockDetailDTO;
        StockFinancialIndicatorDTO stockFinancialIndicatorDTO;
        StockFinancialSummaryDTO stockFinancialSummaryDTO;
        StockPriceDailyDTO stockPriceDailyDTO;

        try{
            // 종목 코드로 조회 (stock_symbol 우선, 실패 시 stock_code_id)
            stockDTO = codeResolver.resolveStock(codeParam);
            
            if (stockDTO == null) {
                System.err.println("[StockController] 종목을 찾을 수 없습니다. code: '" + codeParam + "'");
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "종목을 찾을 수 없습니다: " + codeParam);
                return;
            }
            
            Long stkCode = stockDTO.getStockCodeId();
            System.out.println("[StockController] 종목 조회 성공: stock_id=" + stockDTO.getStockId() + ", stock_symbol=" + stockDTO.getStockSymbol() + ", stock_code_id=" + stkCode);
            
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
            
            // 로그인한 사용자의 보유 주식 정보 및 대기 주문 목록 조회
            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("member") != null) {
                MemberDTO member = (MemberDTO) session.getAttribute("member");
                int memId = member.getMemId();
                
                try {
                    // 현재 종목에 대한 보유 정보 조회
                    MemberHoldingDTO holding = DBManager.withConnection(conn -> 
                        memberDAO.findHoldingByStock(memId, stkId, conn));
                    req.setAttribute("holding", holding);
                    
                    // 대기 주문 목록 조회
                    java.util.List<OrderDTO> pendingOrders = orderService.getPendingOrders(memId);
                    req.setAttribute("pendingOrders", pendingOrders);
                    
                    // 대기 중인 판매 주문 수량 조회
                    long pendingSellQuantity = orderService.getPendingSellOrderQuantity(memId, stkId);
                    req.setAttribute("pendingSellQuantity", pendingSellQuantity);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
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