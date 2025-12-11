package com.dongyang.mock.service.api.fetch;

import com.dongyang.mock.model.DAO.IndexDAO;
import com.dongyang.mock.model.DTO.IndexDTO;
import com.dongyang.mock.service.auth.AuthService;
import com.dongyang.mock.util.api.ApiConstants;
import com.dongyang.mock.util.db.DBManager;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IndexFetchService extends FetchService {
    private final AuthService authService = new AuthService();
    private final IndexDAO indexDao = new IndexDAO();

    @Override
    public void runUpdate() {
        String token = authService.issueAccessToken();
        if (token == null) {
            System.err.println("Access Token 발급 실패. 업데이트 중단.");
            return;
        }

        try {
            runFetch(token, 5, "001", "KOSPI");
            Thread.sleep(DELAY_TIME);
            runFetch(token, 5, "101", "KOSDAQ");
            Thread.sleep(DELAY_TIME);
            runFetch(token, 5, "701", "KRX100");
            Thread.sleep(DELAY_TIME);
            runFetch(token, 5, "002", "대형주");
            Thread.sleep(DELAY_TIME);
            runFetch(token, 5, "003", "중형주");
            Thread.sleep(DELAY_TIME);
            runFetch(token, 5, "004", "소형주");

        } catch (Exception e) {
            System.err.println("배치 업데이트 중 오류 발생: " + e.getMessage());
        }
    }

    private void runFetch(String token, int tick, String code, String name) throws Exception {
        String tr = ApiConstants.TR_DOMESTIC_INDEX;
        String url = ApiConstants.PATH_DOMESTIC_INDEX;
        String requestBody = buildRequestBody(code, tick);
        Map<String, Object> responseMap = callApi(token, url, tr, requestBody, "GET");

        DBManager.executeInTransaction(conn -> {
            String latestTimeStr = indexDao.findLatestContractTime(name, conn);
            List<IndexDTO> dataList = parseResponse(responseMap, name, latestTimeStr);

            System.out.println(name + " 지수: API 획득 후 신규 데이터 " + dataList.size() + "개 저장");

            if (!dataList.isEmpty()) {
                int[] results = indexDao.insertIndexBatch(dataList, conn);
                System.out.println(name + " 지수: " + results.length + "개 데이터 Batch 처리 완료.");
            }

            return DBManager.TransactionResult.commit(null);
        });
    }

    private String buildRequestBody(String indexCode, int tick) {
        return "{\"inds_cd\" : \"%s\",\"tic_scope\" : \"%d\"}".formatted(indexCode, tick);
    }

    @SuppressWarnings("unchecked")
    private List<IndexDTO> parseResponse(Map<String, Object> response, String indexName, String latestTimeStr) {
        List<IndexDTO> result = new ArrayList<>();
        Timestamp latestTime = null;
        if (latestTimeStr != null) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                latestTime = Timestamp.valueOf(LocalDateTime.parse(latestTimeStr, formatter));
            } catch (Exception e) {
                System.err.println("DB 최신 시간 파싱 오류: " + latestTimeStr);
            }
        }

        List<Map<String, String>> outputList = (List<Map<String, String>>) response.get("inds_min_pole_qry");

        if (outputList == null) {
            System.err.println(indexName + " 지수 파싱 실패: 응답 상세 리스트(inds_min_pole_qry)가 없습니다.");
            return result;
        }

        for (Map<String, String> item : outputList) {
            try {
                String timeStr = item.get("cntr_tm");
                String priceStr = item.get("cur_prc");
                String overnightStr = item.get("pred_pre");

                if (timeStr == null || priceStr == null || timeStr.trim().isEmpty() || priceStr.trim().isEmpty()) {
                    System.err.println(indexName + " 파싱 실패: 날짜/종가 필드 누락. Date: " + timeStr + ", Price: " + priceStr);
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

                // 전일대비 수치 format 정규화 및 부호 선택
                BigDecimal overnight = null;
                if (overnightStr != null && !overnightStr.trim().isEmpty()) {
                    boolean isNegative = overnightStr.contains("-");
                    String cleanOvernightStr = overnightStr.replaceAll("-", "");

                    overnight = new BigDecimal(cleanOvernightStr);

                    if (isNegative) {
                        overnight = overnight.negate(); // 값을 음수로
                    }
                }

                result.add(new IndexDTO(indexName, time, price, overnight));
            } catch (Exception e) {
                System.err.println(indexName + " 지수 파싱 중 오류 발생: " + e.getMessage());
            }
        }
        return result;
    }
}