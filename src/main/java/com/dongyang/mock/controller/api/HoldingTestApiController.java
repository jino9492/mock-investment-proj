package com.dongyang.mock.controller.api;

import com.dongyang.mock.model.DAO.AccountSummaryDAO;
import com.dongyang.mock.model.DAO.HoldingDAO;
import com.dongyang.mock.model.DAO.PortfolioDAO;
import com.dongyang.mock.model.DAO.RankingDAO;
import com.dongyang.mock.model.DTO.LeagueDTO;
import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.util.db.DBManager;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holding 데이터 테스트 API Controller
 * - Holding 데이터 입력 및 검증
 * - 리그 페이지 기능 테스트
 * 
 * POST /api/v1/test/holding/insert
 * GET /api/v1/test/holding/verify
 */
@WebServlet("/api/v1/test/holding/*")
public class HoldingTestApiController extends HttpServlet {
    private final HoldingDAO holdingDAO = new HoldingDAO();
    private final AccountSummaryDAO accountSummaryDAO = new AccountSummaryDAO();
    private final PortfolioDAO portfolioDAO = new PortfolioDAO();
    private final RankingDAO rankingDAO = new RankingDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || !pathInfo.equals("/insert")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            sendErrorResponse(response, "잘못된 요청 경로입니다.");
            return;
        }

        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("member") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                sendErrorResponse(response, "로그인이 필요합니다.");
                return;
            }

            MemberDTO member = (MemberDTO) session.getAttribute("member");
            int memId = member.getMemId();

            // 요청 파라미터 파싱
            String stockIdParam = request.getParameter("stock_id");
            String quantityParam = request.getParameter("quantity");
            String buyPriceParam = request.getParameter("buy_price");

            if (stockIdParam == null || quantityParam == null || buyPriceParam == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendErrorResponse(response, "필수 파라미터가 누락되었습니다. (stock_id, quantity, buy_price)");
                return;
            }

            int stockId = Integer.parseInt(stockIdParam);
            int quantity = Integer.parseInt(quantityParam);
            long buyPrice = Long.parseLong(buyPriceParam);

            if (quantity <= 0 || buyPrice <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                sendErrorResponse(response, "수량과 매수 가격은 0보다 커야 합니다.");
                return;
            }

            // Holding 데이터 입력
            int result = DBManager.executeInTransaction(conn -> {
                int insertResult = holdingDAO.insertOrUpdateHolding(memId, stockId, quantity, buyPrice, conn);
                if (insertResult > 0) {
                    return DBManager.TransactionResult.commit(insertResult);
                } else {
                    return DBManager.TransactionResult.rollback(0);
                }
            });

            Map<String, Object> responseMap = new HashMap<>();
            if (result > 0) {
                responseMap.put("success", true);
                responseMap.put("message", "Holding 데이터가 성공적으로 입력되었습니다.");
                responseMap.put("mem_id", memId);
                responseMap.put("stock_id", stockId);
                responseMap.put("quantity", quantity);
                responseMap.put("buy_price", buyPrice);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                responseMap.put("success", false);
                responseMap.put("message", "Holding 데이터 입력에 실패했습니다.");
            }

            response.getWriter().write(gson.toJson(responseMap));

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            sendErrorResponse(response, "숫자 형식이 올바르지 않습니다.");
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            sendErrorResponse(response, "서버 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            sendErrorResponse(response, "알 수 없는 오류가 발생했습니다.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || !pathInfo.equals("/verify")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            sendErrorResponse(response, "잘못된 요청 경로입니다.");
            return;
        }

        try {
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("member") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                sendErrorResponse(response, "로그인이 필요합니다.");
                return;
            }

            MemberDTO member = (MemberDTO) session.getAttribute("member");
            int memId = member.getMemId();

            // Holding 데이터 조회
            List<HoldingDAO.HoldingInfo> holdings = DBManager.withConnection(conn -> 
                holdingDAO.selectHoldings(memId, conn)
            );

            // 리그 페이지 기능 검증
            LeagueDTO.AccountSummary accountSummary = DBManager.withConnection(conn -> 
                accountSummaryDAO.selectAccountSummary(memId, conn)
            );

            List<LeagueDTO.TopReturnStock> topReturnStocks = DBManager.withConnection(conn -> 
                portfolioDAO.selectTopReturnStocks(memId, 3, conn)
            );

            List<LeagueDTO.PortfolioStock> portfolioDetails = DBManager.withConnection(conn -> 
                portfolioDAO.selectPortfolioDetails(memId, conn)
            );

            List<LeagueDTO.Ranking> nearbyRankings = DBManager.withConnection(conn -> 
                rankingDAO.selectNearbyRankings(memId, conn)
            );

            // 검증 결과 구성
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("message", "리그 페이지 기능 검증 완료");
            responseMap.put("mem_id", memId);
            
            // Holding 데이터
            Map<String, Object> holdingData = new HashMap<>();
            holdingData.put("count", holdings.size());
            holdingData.put("holdings", holdings);
            responseMap.put("holding_data", holdingData);

            // 계좌 요약 정보
            Map<String, Object> accountData = new HashMap<>();
            accountData.put("total_asset", accountSummary.getTotalAsset());
            accountData.put("stock_value", accountSummary.getStockValue());
            accountData.put("cash_balance", accountSummary.getCashBalance());
            accountData.put("total_return", accountSummary.getTotalReturn());
            accountData.put("percentile", accountSummary.getPercentile());
            responseMap.put("account_summary", accountData);

            // 수익률 상위 종목
            Map<String, Object> topReturnData = new HashMap<>();
            topReturnData.put("count", topReturnStocks.size());
            topReturnData.put("stocks", topReturnStocks);
            responseMap.put("top_return_stocks", topReturnData);

            // 포트폴리오 상세
            Map<String, Object> portfolioData = new HashMap<>();
            portfolioData.put("count", portfolioDetails.size());
            portfolioData.put("stocks", portfolioDetails);
            responseMap.put("portfolio_details", portfolioData);

            // 주변 랭킹
            Map<String, Object> rankingData = new HashMap<>();
            rankingData.put("count", nearbyRankings.size());
            rankingData.put("rankings", nearbyRankings);
            responseMap.put("nearby_rankings", rankingData);

            // 검증 상태
            boolean isValid = true;
            List<String> issues = new java.util.ArrayList<>();
            
            if (holdings.isEmpty()) {
                isValid = false;
                issues.add("보유 주식이 없습니다.");
            }
            
            if (accountSummary.getStockValue() == 0 && !holdings.isEmpty()) {
                isValid = false;
                issues.add("보유 주식이 있지만 평가액이 0입니다. stock_price_daily 데이터를 확인하세요.");
            }
            
            if (topReturnStocks.isEmpty() && !holdings.isEmpty()) {
                isValid = false;
                issues.add("보유 주식이 있지만 수익률 상위 종목이 조회되지 않습니다.");
            }

            Map<String, Object> validationMap = new HashMap<>();
            validationMap.put("is_valid", isValid);
            validationMap.put("issues", issues);
            responseMap.put("validation", validationMap);

            response.getWriter().write(gson.toJson(responseMap));

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            sendErrorResponse(response, "서버 오류가 발생했습니다: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            sendErrorResponse(response, "알 수 없는 오류가 발생했습니다.");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        response.getWriter().write(gson.toJson(errorResponse));
    }
}

