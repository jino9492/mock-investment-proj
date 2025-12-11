package com.dongyang.mock.service.api.fetch;

import com.dongyang.mock.model.DAO.IndexDAO;
import com.dongyang.mock.model.DAO.StockChartDAO;
import com.dongyang.mock.model.DAO.StockDAO;
import com.dongyang.mock.model.DTO.IndexDTO;
import com.dongyang.mock.model.DTO.RankingItemDTO;
import com.dongyang.mock.model.DTO.StockChartDTO;
import com.dongyang.mock.service.auth.AuthService;
import com.dongyang.mock.util.api.ApiConstants;
import com.dongyang.mock.util.api.InmemoryCacheManager;
import com.dongyang.mock.util.db.DBManager;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StockChartFetchService extends FetchService {
    private final AuthService authService = new AuthService();
    private final StockChartDAO stockChartDAO = new StockChartDAO();
    private final StockDAO stockDAO = new StockDAO();
    private final InmemoryCacheManager cacheManager = InmemoryCacheManager.getInstance();

    @Override
    public void runUpdate() {
        String token = authService.issueAccessToken();
        if (token == null) {
            System.err.println("Access Token 발급 실패. 업데이트 중단.");
            return;
        }

        try {
            List<RankingItemDTO> rank = cacheManager.getList(RankingFetchService.RANKING_CACHE_KEY);
            rank.forEach(r -> {
                try {
                    runFetch(token, 5, r.getStkCode().replace("_AL", ""), r.getStkName());
                    Thread.sleep(DELAY_TIME);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            System.err.println("배치 업데이트 중 오류 발생: " + e.getMessage());
        }
    }

    private void runFetch(String token, int tick, String code, String name) throws Exception {
        String tr = ApiConstants.TR_STOCK_CHART;
        String url = ApiConstants.PATH_STOCK_CHART;
        String requestBody = buildRequestBody(code, tick, 1);
        Map<String, Object> responseMap = callApi(token, url, tr, requestBody, "POST");

        DBManager.executeInTransaction(conn -> {
            Long stkId = stockDAO.getStockIdByCode(code, conn);
            String latestTimeStr = stockChartDAO.findLatestChartTime(stkId, conn);

            List<StockChartDTO> dataList = parseResponse(responseMap, name, stkId, latestTimeStr);
            System.out.println(name + "  : API 획득 후 신규 데이터 " + dataList.size() + "개 저장");

            if (!dataList.isEmpty()) {
                int[] results = stockChartDAO.insertStockChartBatch(dataList, conn);
                System.out.println(name + " 종목: " + results.length + "개 데이터 Batch 처리 완료.");
            }

            return DBManager.TransactionResult.commit(null);
        });
    }

    private String buildRequestBody(String stockCode, int tick, int updateStockType) {
        return "{\"stk_cd\" : \"%s\",\"tic_scope\" : \"%d\", \"upd_stkpc_tp\" : \"%d\"}".formatted(stockCode, tick, updateStockType);
    }

    @SuppressWarnings("unchecked")
    private List<StockChartDTO> parseResponse(Map<String, Object> response, String name, Long stkId, String latestTimeStr) {
        List<StockChartDTO> result = new ArrayList<>();
        Timestamp latestTime = null;
        if (latestTimeStr != null) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                latestTime = Timestamp.valueOf(LocalDateTime.parse(latestTimeStr, formatter));
            } catch (Exception e) {
                System.err.println("DB 최신 시간 파싱 오류: " + latestTimeStr);
            }
        }

        List<Map<String, String>> outputList = (List<Map<String, String>>) response.get("stk_min_pole_chart_qry");

        if (outputList == null) {
            System.err.println(name + " 주가 파싱 실패: 주가 상세 리스트(stk_min_pole_chart_qry)가 없습니다.");
            return result;
        }

        for (Map<String, String> item : outputList) {
            try {
                String timeStr = item.get("cntr_tm");
                String priceStr = item.get("cur_prc");
                String openStr = item.get("open_pric");
                String highStr = item.get("high_pric");
                String lowStr = item.get("low_pric");
                String volumeStr = item.get("trde_qty");

                if (timeStr == null || priceStr == null || timeStr.trim().isEmpty() || priceStr.trim().isEmpty()) {
                    System.err.println(name + " 파싱 실패: 날짜/종가 필드 누락. Date: " + timeStr + ", Price: " + priceStr);
                    continue;
                }

                // 시간 변환
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                LocalDateTime localDateTime = LocalDateTime.parse(timeStr, formatter);
                Timestamp time = Timestamp.valueOf(localDateTime);

                // 중복 데이터 필터링
                if (latestTime != null && (time.equals(latestTime) || time.before(latestTime))) {
                    break;
                }

                // price는 항상 양수
                BigDecimal price = new BigDecimal(priceStr).abs();
                BigDecimal open = new BigDecimal(openStr).abs();
                BigDecimal high = new BigDecimal(highStr).abs();
                BigDecimal low = new BigDecimal(lowStr).abs();
                Long volume = Long.parseLong(volumeStr);


                result.add(new StockChartDTO(
                        stkId,
                        time,
                        price,
                        open,
                        high,
                        low,
                        volume,
                        new BigDecimal(0)));
            } catch (Exception e) {
                System.err.println(name + " 주가 파싱 중 오류 발생: " + e.getMessage());
            }
        }
        return result;
    }
}