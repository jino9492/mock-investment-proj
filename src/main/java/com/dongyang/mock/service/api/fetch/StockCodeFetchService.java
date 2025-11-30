package com.dongyang.mock.service.api.fetch;

import com.dongyang.mock.model.DAO.StockCodeDAO;
import com.dongyang.mock.model.DTO.IndexDTO;
import com.dongyang.mock.model.DTO.StockCodeDTO;
import com.dongyang.mock.service.auth.AuthService;
import com.dongyang.mock.util.api.ApiConstants;
import com.dongyang.mock.util.db.DBManager;
import com.google.gson.Gson;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebListener
public class StockCodeFetchService extends  FetchService implements ServletContextListener {
    AuthService authService = new AuthService();
    StockCodeDAO stockCodeDAO = new StockCodeDAO();

    public void contextInitialized(ServletContextEvent sce) {
        try {
            System.out.println("====================== StockCode 로드 중 ======================");
            runUpdate();
            System.out.println("StockCode 데이터가 서버 메모리에 로드되었습니다.");
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
            runFetch(token, "1");
            Thread.sleep(DELAY_TIME);
        } catch(Exception e){
            System.err.println("배치 업데이트 중 오류 발생: " + e.getMessage());
        }
    }

    public String buildRequestBody(String market) {
        return "{\"mrkt_tp\" : \"%s\"}".formatted(market);
    }

    public void runFetch(String token, String market) throws Exception {
        String tr = ApiConstants.TR_STOCK_CODE;
        String url = ApiConstants.PATH_STOCK_CODE;
        String requestBody = buildRequestBody(market);
        Map<String, Object> responseMap = callApi(token, url, tr, requestBody, "POST");

        DBManager.executeInTransaction(conn -> {
            List<StockCodeDTO> dataList = parseResponse(responseMap);

            if (!dataList.isEmpty()) {
                int[] results = stockCodeDAO.insertStockCodeBatch(dataList, conn);
                System.out.println(market + " 업종: " + results.length + "개 데이터 Batch 처리 완료.");
            }

            return DBManager.TransactionResult.commit(null);
        });
    }

    private List<StockCodeDTO> parseResponse(Map<String, Object> response) {
        List<StockCodeDTO> result = new ArrayList<>();

        List<Map<String, String>> outputList = (List<Map<String, String>>) response.get("list");

        if (outputList == null) {
            System.err.println("업종 파싱 실패: 응답 상세 리스트(list)가 없습니다.");
            return result;
        }

        for (Map<String, String> item : outputList) {
            try {
                String code = item.get("code");
                String name = item.get("name");

                if (code == null || name == null || code.trim().isEmpty() || name.trim().isEmpty()) {
                    System.err.println(name + " 파싱 실패");
                    continue;
                }

                result.add(new StockCodeDTO(code, name, ""));
            } catch (Exception e) {
                System.err.println("업종 파싱 중 오류 발생: " + e.getMessage());
            }
        }
        return result;
    }
}
