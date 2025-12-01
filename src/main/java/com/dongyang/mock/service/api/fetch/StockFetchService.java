package com.dongyang.mock.service.api.fetch;

import com.dongyang.mock.model.DAO.StockCodeDAO;
import com.dongyang.mock.model.DAO.StockDAO;
import com.dongyang.mock.model.DTO.StockCodeDTO;
import com.dongyang.mock.model.DTO.StockDTO;
import com.dongyang.mock.service.auth.AuthService;
import com.dongyang.mock.util.api.ApiConstants;
import com.dongyang.mock.util.db.DBManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebListener
public class StockFetchService extends  FetchService implements ServletContextListener {
    AuthService authService = new AuthService();
    StockDAO stockDAO = new StockDAO();
    StockCodeDAO stockCodeDAO = new StockCodeDAO();
    private Map<String, Long> stockCodeMap;

    public void contextInitialized(ServletContextEvent sce) {
        try {
            System.out.println("====================== StockCode 로드 중 ======================");
            stockCodeMap = DBManager.withConnection(stockCodeDAO::getAllStockCodeIdMap);
            runUpdate();
            System.out.println("Stock 데이터가 서버 메모리에 로드되었습니다.");
            System.out.println("====================== StockCode 로드 종료 ======================");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void runUpdate() throws Exception {
        String token = authService.issueAccessToken();
        if (token == null) {
            System.err.println("Access Token 발급 실패. 업데이트 중단.");
            return;
        }

        try{
            runFetch(token, "0");
            Thread.sleep(DELAY_TIME);
            runFetch(token, "10");
            Thread.sleep(DELAY_TIME);
        } catch(Exception e){
            System.err.println("배치 업데이트 중 오류 발생: " + e.getMessage());
        }
    }

    public String buildRequestBody(String market) {
        return "{\"mrkt_tp\" : \"%s\"}".formatted(market);
    }

    public void runFetch(String token, String market) throws Exception {
        String tr = ApiConstants.TR_STOCK;
        String url = ApiConstants.PATH_STOCK;
        String requestBody = buildRequestBody(market);
        Map<String, Object> responseMap = callApi(token, url, tr, requestBody, "POST");

        DBManager.executeInTransaction(conn -> {
            List<StockDTO> dataList = parseResponse(responseMap);

            if (!dataList.isEmpty()) {
                int[] results = stockDAO.insertStockBatch(dataList, conn);
                System.out.println(market + " 종목: " + results.length + "개 데이터 Batch 처리 완료.");
            }

            return DBManager.TransactionResult.commit(null);
        });
    }

    private List<StockDTO> parseResponse(Map<String, Object> response) {
        List<StockDTO> result = new ArrayList<>();

        List<Map<String, String>> outputList = (List<Map<String, String>>) response.get("list");

        if (outputList == null) {
            System.err.println("종목 파싱 실패: 응답 상세 리스트(list)가 없습니다.");
            return result;
        }

        for (Map<String, String> item : outputList) {
            try {
                String code = item.get("code");
                String name = item.get("name");
                String marketCode = item.get("marketCode");
                String regDay = item.get("regDay");
                String state = item.get("state");

                String upName = item.get("upName");

                Long id = null;
                if (upName != null && stockCodeMap.containsKey(upName)) {
                    id = stockCodeMap.get(upName);
                }
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                LocalDate localDate = LocalDate.parse(regDay, formatter);

                if (code == null || name == null || code.trim().isEmpty() || name.trim().isEmpty()) {
                    System.err.println(name + " 파싱 실패");
                    continue;
                }

                result.add(new StockDTO(code, name, marketCode, id, Date.valueOf(localDate), state));
            } catch (Exception e) {
                System.err.println("종목 파싱 중 오류 발생: " + e.getMessage());
            }
        }
        return result;
    }
}
