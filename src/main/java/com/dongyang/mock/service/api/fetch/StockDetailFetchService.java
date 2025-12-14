package com.dongyang.mock.service.api.fetch;

import com.dongyang.mock.model.DAO.*;
import com.dongyang.mock.model.DTO.StockDetailDTO;
import com.dongyang.mock.model.DTO.StockFinancialIndicatorDTO;
import com.dongyang.mock.model.DTO.StockFinancialSummaryDTO;
import com.dongyang.mock.model.DTO.StockPriceDailyDTO;
import com.dongyang.mock.service.auth.AuthService;
import com.dongyang.mock.util.api.ApiConstants;
import com.dongyang.mock.util.db.DBManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebListener
public class StockDetailFetchService extends FetchService implements ServletContextListener {
    int ERROR = 100;
    AuthService authService = new AuthService();
    private Map<Long, String > stockMap;

    StockDAO stockDAO = new StockDAO();
    StockDetailDAO stockDetailDAO = new StockDetailDAO();
    StockFinancialSummaryDAO summaryDAO = new StockFinancialSummaryDAO();
    StockFinancialIndicatorDAO indicatorDAO = new StockFinancialIndicatorDAO();
    StockPriceDailyDAO stockPriceDailyDAO = new StockPriceDailyDAO();

    List<StockDetailDTO> resultDetail = new ArrayList<>();
    List<StockFinancialIndicatorDTO> resultFinancialIndicator = new ArrayList<>();
    List<StockFinancialSummaryDTO> resultFinancialSummary = new ArrayList<>();
    List<StockPriceDailyDTO> resultPriceDaily = new ArrayList<>();

    public void contextInitialized(ServletContextEvent sce) {
        try {
            Thread initThread = new Thread(() -> {
                try {
                    // DB에 저장된 음수 close_price 확인
                    checkNegativeClosePrices();
                    
                    stockMap = DBManager.withConnection(conn->stockDAO.selectAllStockSymbolMap(conn));
                    runUpdate();
                    System.out.println("Stock Detail 데이터가 서버 메모리에 로드되었습니다. (비동기 완료)");
                } catch (Exception e) {
                    System.err.println("Stock Detail 초기 수집 중 치명적인 오류 발생: " + e.getMessage());
                }
            });
            initThread.setName("Stock-Detail-Init-Thread");
            initThread.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * DB에 저장된 음수 close_price 확인 및 수정
     */
    private void checkNegativeClosePrices() {
        try {
            DBManager.executeInTransaction(conn -> {
                // 1. 음수 값 확인
                String selectSql = "SELECT stock_id, price_date, close_price, open_price, high_price, low_price " +
                                 "FROM stock_price_daily WHERE close_price < 0 OR open_price < 0 OR high_price < 0 OR low_price < 0 " +
                                 "ORDER BY stock_id, price_date DESC LIMIT 20";
                
                int count = 0;
                try (PreparedStatement pstmt = conn.prepareStatement(selectSql);
                     ResultSet rs = pstmt.executeQuery()) {
                    
                    while (rs.next()) {
                        count++;
                    }
                }
                
                // 2. 음수 값들을 절댓값으로 수정
                if (count > 0) {
                    String updateSql = "UPDATE stock_price_daily SET " +
                                     "open_price = ABS(open_price), " +
                                     "high_price = ABS(high_price), " +
                                     "low_price = ABS(low_price), " +
                                     "close_price = ABS(close_price) " +
                                     "WHERE close_price < 0 OR open_price < 0 OR high_price < 0 OR low_price < 0";
                    
                    try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                        updatePstmt.executeUpdate();
                    }
                }
                
                return DBManager.TransactionResult.commit(null);
            });
        } catch (Exception e) {
            System.err.println("[StockDetailFetchService] 음수 close_price 확인/수정 중 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void runUpdate() throws Exception {
        String token = authService.issueAccessToken();
        if (token == null) {
            System.err.println("Access Token 발급 실패. 업데이트 중단.");
            return;
        }

        final int CHUNK_SIZE = 100; // 100개 종목마다 배치 커밋
        int counter = 0;

        for (Map.Entry<Long, String> entry : stockMap.entrySet()) {
            Long stkId = entry.getKey();
            String stkCd = entry.getValue();

            try {
                boolean exists = DBManager.withConnection(conn -> stockDetailDAO.existsByStockCode(stkId, conn));
                // boolean exists = DBManager.withConnection(conn->stockPriceDailyDAO.existsByStockId(stkId, conn));

                if (!exists) {
                    System.out.println("[StockDetailFetchService] 종목 코드 " + stkCd + " (ID: " + stkId + ") fetch 시작");
                    runFetch(token, stkCd, stkId);
                    counter++;
                    Thread.sleep(DELAY_TIME);
                } else {
                     // System.out.println(stkCd + " 종목은 이미 존재합니다. 건너갑니다.");
                }

                if (counter >= CHUNK_SIZE) {
                    commitAccumulatedData();
                    counter = 0;
                }

            } catch (InterruptedException e) {
                System.err.println(stkCd + " 종목 fetch 중 시스템 중단 신호 발생. 남은 데이터를 커밋합니다.");
                commitAccumulatedData(); // 중단 전에 마지막 남은 청크 커밋 시도
                Thread.currentThread().interrupt();
                return;

            } catch (Exception e) {
                System.err.println(stkCd + " 종목 정보 fetch 중 오류 발생: " + e.getMessage());
            }
        }

        if (counter > 0) {
            System.out.println(">>> 루프 종료 후 남은 " + counter + "건 데이터 최종 배치 커밋 시작.");
            commitAccumulatedData();
        } else {
            System.out.println("수집된 종목 상세 정보가 없습니다.");
        }
    }

    public String buildRequestBody(String stkCd) {
        return "{\"stk_cd\" : \"%s\"}".formatted(stkCd);
    }

    public void runFetch(String token, String stkCd, Long stkId) throws Exception {
        String tr = ApiConstants.TR_STOCK_DETAIL;
        String url = ApiConstants.PATH_STOCK_DETAIL;
        String requestBody = buildRequestBody(stkCd);
        Map<String, Object> responseMap = callApi(token, url, tr, requestBody, "POST");
        Object returnCodeObj = responseMap.get("return_code");
        if (returnCodeObj == null || (returnCodeObj instanceof String && !returnCodeObj.equals("0")) || (returnCodeObj instanceof Number && ((Number) returnCodeObj).doubleValue() != 0.0)) {
            System.err.println(stkCd + " 종목 API 호출 실패. 응답: " + responseMap);
            return;
        }

        parseResponse(responseMap, stkId);
    }

    private void parseResponse(Map<String, Object> response, Long stockId) {
        try {
            String name = getStringValue(response, "stk_nm");
            BigDecimal fav = getBigDecimalValue(response, "fav");
            String settlementMonth = getStringValue(response, "setl_mm");
            String marketCap = getStringValue(response, "mac");
            Date listingDate = DBManager.withConnection(conn -> stockDAO.selectListingDateById(stockId, conn));
            String description = "";

            resultDetail.add(new StockDetailDTO(stockId, name, fav, settlementMonth, marketCap, listingDate, description));
        } catch (Exception e) {
            System.err.println(stockId + " 종목: Stock Detail 파싱 실패. " + e.getMessage());
        }

        try {
            Date indicatorDate = Date.valueOf(LocalDate.now());
            BigDecimal per = getBigDecimalValue(response, "per");
            BigDecimal pbr = getBigDecimalValue(response, "pbr");
            BigDecimal roe = getBigDecimalValue(response, "roe");
            BigDecimal eps = getBigDecimalValue(response, "eps");
            BigDecimal bps = getBigDecimalValue(response, "bps");

            resultFinancialIndicator.add(new StockFinancialIndicatorDTO(stockId, indicatorDate, per, pbr, roe, eps, bps));
        } catch (Exception e) {
            System.err.println(stockId + " 종목: Financial Indicator 파싱 실패. " + e.getMessage());
        }

        try {
            Long revenue = getLongValue(response, "sale_amt");
            Long operatingProfit = getLongValue(response, "bus_pro");
            BigDecimal creditRatio = getBigDecimalValue(response, "crd_rt");

            resultFinancialSummary.add(new StockFinancialSummaryDTO(stockId, revenue, operatingProfit, creditRatio));
        } catch (Exception e) {
            System.err.println(stockId + " 종목: Financial Summary 파싱 실패. " + e.getMessage());
        }

        try {
            Date priceDate = Date.valueOf(LocalDate.now());
            BigDecimal openPrice = getBigDecimalValue(response, "open_pric");
            BigDecimal highPrice = getBigDecimalValue(response, "high_pric");
            BigDecimal lowPrice = getBigDecimalValue(response, "low_pric");
            BigDecimal closePrice = getBigDecimalValue(response, "cur_prc");
            
            // 가격 필드가 음수인 경우 절댓값으로 변환
            if (openPrice != null && openPrice.compareTo(BigDecimal.ZERO) < 0) {
                openPrice = openPrice.abs();
            }
            if (highPrice != null && highPrice.compareTo(BigDecimal.ZERO) < 0) {
                highPrice = highPrice.abs();
            }
            if (lowPrice != null && lowPrice.compareTo(BigDecimal.ZERO) < 0) {
                lowPrice = lowPrice.abs();
            }
            if (closePrice != null && closePrice.compareTo(BigDecimal.ZERO) < 0) {
                closePrice = closePrice.abs();
            }
            
            Long volume = getLongValue(response, "trde_qty");

            Long amount = closePrice != null ? closePrice.multiply(new BigDecimal(volume)).setScale(0, RoundingMode.HALF_UP).longValue() : 0L;

            BigDecimal prevClose = DBManager.withConnection(conn -> stockPriceDailyDAO.getPreviousClosePrice(stockId, conn));

            BigDecimal changePrice = getBigDecimalValue(response, "pred_pre");
            BigDecimal changeRate = getBigDecimalValue(response, "flu_rt");

            resultPriceDaily.add(new StockPriceDailyDTO(stockId, priceDate, openPrice, highPrice,
                    lowPrice, closePrice, volume, amount, prevClose, changePrice, changeRate));
        } catch (Exception e) {
            System.err.println(stockId + " 종목: Price Daily 파싱/계산 실패. " + e.getMessage());
        }
    }

    private String getStringValue(Map<String, Object> response, String key) {
        Object value = response.get(key);
        return (value instanceof String) ? (String) value : (value != null ? value.toString() : null);
    }

    private Long getLongValue(Map<String, Object> response, String key) {
        String strValue = getStringValue(response, key);
        if (strValue == null || strValue.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(strValue.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            System.err.println(key + " 필드의 Long 변환 실패: " + strValue);
            return null;
        }
    }

    private BigDecimal getBigDecimalValue(Map<String, Object> response, String key) {
        String strValue = getStringValue(response, key);
        if (strValue == null || strValue.trim().isEmpty()) {
            return null;
        }
        try {
            String cleanedValue = strValue.replaceAll("[^0-9\\.\\-]", "");
            BigDecimal result = new BigDecimal(cleanedValue);
            return result;
        } catch (NumberFormatException e) {
            System.err.println(key + " 필드의 BigDecimal 변환 실패: " + strValue);
            return null;
        }
    }

    private void commitAccumulatedData() {
        if (resultDetail.isEmpty() && resultFinancialSummary.isEmpty() && resultFinancialIndicator.isEmpty() && resultPriceDaily.isEmpty()) {
            return; // 커밋할 데이터가 없으면 종료
        }

        try {
            DBManager.executeInTransaction(conn -> {

                // 1. stock_detail 배치 삽입
                if (!resultDetail.isEmpty()) {
                    int[] detailResults = stockDetailDAO.insertStockDetailBatch(resultDetail, conn);
                    System.out.println(">>> stock_detail " + detailResults.length + "건 배치 처리 완료.");
                }

                // 2. stock_financial_summary 배치 삽입
                if (!resultFinancialSummary.isEmpty()) {
                    int[] summaryResults = summaryDAO.insertSummaryBatch(resultFinancialSummary, conn);
                    System.out.println(">>> stock_financial_summary " + summaryResults.length + "건 배치 처리 완료.");
                }

                // 3. stock_financial_indicator 배치 삽입
                if (!resultFinancialIndicator.isEmpty()) {
                    int[] indicatorResults = indicatorDAO.insertIndicatorBatch(resultFinancialIndicator, conn);
                    System.out.println(">>> stock_financial_indicator " + indicatorResults.length + "건 배치 처리 완료.");
                }

                // 4. stock_price_daily 배치 삽입
                if (!resultPriceDaily.isEmpty()) {
                    int[] priceDailyResults = stockPriceDailyDAO.insertPriceDailyBatch(resultPriceDaily, conn);
                    System.out.println(">>> stock_price_daily " + priceDailyResults.length + "건 배치 처리 완료.");
                }

                return DBManager.TransactionResult.commit(null);
            });

            // 커밋 성공 후, 메모리 리스트 비우기
            resultDetail.clear();
            resultFinancialSummary.clear();
            resultFinancialIndicator.clear();
            resultPriceDaily.clear();

        } catch (Exception e) {
            System.err.println("종목 상세 데이터 배치 저장/갱신 실패: " + e.getMessage());
            resultDetail.clear();
            resultFinancialSummary.clear();
            resultFinancialIndicator.clear();
            resultPriceDaily.clear();
        }
    }
}