package com.dongyang.mock.controller.stock;

import com.dongyang.mock.model.DTO.*;
import com.dongyang.mock.service.WatchlistService;
import com.dongyang.mock.service.api.StockDetailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;


@WebServlet("/stock_info")
public class StockInfoController extends HttpServlet {

    private final String viewPage = "/WEB-INF/views/stock/info.jsp";
    private final StockDetailService stockDetailService = new StockDetailService();
    private final WatchlistService watchlistService = new WatchlistService();

    /**
     * 종목 코드 파라미터를 파싱합니다.
     * @param req HTTP 요청
     * @return 파싱된 종목 코드
     * @throws ServletException 파라미터가 없거나 잘못된 형식인 경우
     */
    private String parseStockCode(HttpServletRequest req) throws ServletException {
        String codeParam = req.getParameter("code");
        if (codeParam == null || codeParam.trim().isEmpty()) {
            throw new ServletException("종목 코드가 필요합니다.");
        }

        try {
            return codeParam;
        } catch (NumberFormatException e) {
            throw new ServletException("올바른 종목 코드 형식이 아닙니다.");
        }
    }

    /**
     * 종목 상세 정보를 조회하여 request에 설정합니다.
     * @param stkCode 종목 코드
     * @param req HTTP 요청
     * @throws Exception 조회 실패 시
     */
    private void loadStockDetails(String stkCode, HttpServletRequest req) throws Exception {
        StockDTO stockDTO = stockDetailService.getStockByCode(stkCode);
        Long stkId = stockDTO.getStockId();

        req.setAttribute("stock", stockDTO);
        req.setAttribute("detail", stockDetailService.getStockDetailById(stkId));
        req.setAttribute("indicator", stockDetailService.getFinancialIndicatorById(stkId));
        req.setAttribute("summary", stockDetailService.getFinancialSummaryById(stkId));
        req.setAttribute("daily", stockDetailService.getStockPriceDailyById(stkId));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            String  stkCode = parseStockCode(req);
            loadStockDetails(stkCode, req);

            // 로그인한 사용자의 관심종목 여부 확인
            StockDTO stock = (StockDTO) req.getAttribute("stock");
            if (stock != null) {
                HttpSession session = req.getSession(false);
                if (session != null && session.getAttribute("member") != null) {
                    MemberDTO member = (MemberDTO) session.getAttribute("member");
                    int memId = member.getMemId();
                    try {
                        boolean isWatchlist = watchlistService.isWatchlist(memId, stock.getStockId());
                        req.setAttribute("isWatchlist", isWatchlist);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "종목 정보를 조회하는 중 오류가 발생했습니다.");
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