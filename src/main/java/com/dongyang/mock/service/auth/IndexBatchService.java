package com.dongyang.mock.service.auth;

import com.dongyang.mock.model.DAO.IndexHistoryDAO;
import com.dongyang.mock.model.DTO.IndexDailyDTO;
import com.dongyang.mock.service.auth.AuthService;
import com.dongyang.mock.util.DateUtility;
import com.dongyang.mock.util.api.ApiConstants;
import com.dongyang.mock.util.api.TokenManager;
import com.google.gson.Gson;
import java.io.*;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IndexBatchService {

    private final AuthService authService = new AuthService();
    private final IndexHistoryDAO indexDao = new IndexHistoryDAO();
    private final Gson gson = new Gson();

    /**
     * 주간 데이터를 DB에 저장하는 기본 배치 실행 메서드 (7일치 요청)
     */
    public void runDailyUpdate() {
        System.out.println("--- [KIS Batch] 주간 지수 데이터 업데이트 시작 (7일치) ---");

        String token = authService.issueAccessToken();
        System.out.println("token: " + token);
        if (token == null) {
            System.err.println("KIS Access Token 발급 실패. 업데이트 중단.");
            return;
        }

        try {
            final long DELAY = 500;
            final int DAYS_TO_FETCH = 7; // <-- 7일치 요청으로 설정

            // 1. 국내 지수 (KOSPI) 업데이트 - 0001 (업종코드)
            fetchAndSaveIndexPeriod(token, DAYS_TO_FETCH, "069500", "KOSPI", "DOMESTIC");
            Thread.sleep(DELAY);

            // 2. 해외 지수 (NASDAQ) 업데이트 - IXIC
            fetchAndSaveIndexPeriod(token, DAYS_TO_FETCH, "IXIC", "NASDAQ", "OVERSEAS");
            Thread.sleep(DELAY);

            // 3. 해외 지수 (S&P 500) 업데이트 - SPI
            fetchAndSaveIndexPeriod(token, DAYS_TO_FETCH, "SPI", "S&P500", "OVERSEAS");

        } catch (Exception e) {
            System.err.println("배치 업데이트 중 오류 발생: " + e.getMessage());
        }
        System.out.println("--- [KIS Batch] 주간 지수 데이터 업데이트 완료 ---");
    }

    /**
     * N일치 데이터를 요청하여 저장하는 공통 로직
     */
    private void fetchAndSaveIndexPeriod(String token, int days, String code, String name, String type) throws Exception {
        String startDate = DateUtility.getTradingDateXDaysAgo(days);
        String endDate = DateUtility.getTodayDate();

        String urlString;
        String trId;

        if (type.equals("DOMESTIC")) {
            trId = ApiConstants.TR_DOMESTIC_INDEX_DAILY;
            urlString = buildDomesticUrl(code, startDate, endDate);
        } else {
            trId = ApiConstants.TR_OVERSEAS_INDEX_DAILY;
            // 해외 지수는 NAS(NASDAQ) 시장 코드를 예시로 사용
            urlString = buildOverseasUrl(code, startDate, endDate, "NAS");
        }

        Map<String, Object> responseMap = callKisApi(token, urlString, trId);

        List<IndexDailyDTO> dataList = parseResponse(responseMap, name, type);

        System.out.println(name + " 지수: " + dataList.size() + "일치 데이터 획득");

        for (IndexDailyDTO data : dataList) {
            indexDao.saveOrUpdateIndex(data); // DB에 7일치 데이터가 UPSERT됨
        }
    }

    // --- buildDomesticUrl 메서드: 기간 파라미터 적용 ---
    private String buildDomesticUrl(String indexCode, String startDate, String endDate) {
        return TokenManager.API_DOMAIN + ApiConstants.PATH_DOMESTIC_INDEX_DAILY +
                "?fid_cond_mrkt_div_code=J" +
                "&fid_input_iscd=" + indexCode +
                "&fid_period_div_code=D" +
                "&fid_input_date_1=" + startDate + // <-- 7 영업일 전
                "&fid_input_date_2=" + endDate +   // <-- 오늘
                "&fid_org_adj_prc=0";
    }

    // --- buildOverseasUrl 메서드: 기간 파라미터 적용 ---
    private String buildOverseasUrl(String symbol, String startDate, String endDate, String marketCode) {
        return TokenManager.API_DOMAIN + ApiConstants.PATH_OVERSEAS_INDEX_DAILY +
                "?AUTH=dummy&EXCD=" + marketCode +
                "&SYMB=" + symbol +
                "&GUBN=D" +
                "&TR_ID=" + ApiConstants.TR_OVERSEAS_INDEX_DAILY +
                "&FID_DATE_1=" + startDate +
                "&FID_DATE_2=" + endDate;
    }

    // --- KIS API 호출 공통 메서드 (HTTP 통신) ---
    private Map<String, Object> callKisApi(String token, String urlString, String trId) throws Exception {

        // 1. URL 객체 생성
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // 2. 요청 헤더 설정
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("authorization", "Bearer " + token);
        conn.setRequestProperty("appkey", TokenManager.APP_KEY);
        conn.setRequestProperty("appsecret", TokenManager.APP_SECRET);
        conn.setRequestProperty("custtype", "P");
        conn.setRequestProperty("tr_id", trId); // Transaction ID 설정

        // KIS API는 TR_CONT(연속 거래 여부) 헤더를 요구할 수 있습니다.
        // 일별 차트 조회는 보통 "F" (Final)로 설정합니다.
        conn.setRequestProperty("tr_cont", "F");

        // 3. 응답 코드 확인 및 데이터 읽기
        int responseCode = conn.getResponseCode();
        BufferedReader br;

        if (responseCode >= 200 && responseCode <= 300) {
            // 성공 응답 (2xx)
            br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        } else {
            // 실패 응답 (4xx, 5xx)
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
        }

        // 4. 응답 본문 읽기
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();

        String jsonResponse = response.toString();

        // 디버깅을 위해 응답 출력
        System.out.println("KIS API 응답 (" + trId + "): " + jsonResponse.substring(0, Math.min(jsonResponse.length(), 200)) + "...");

        // 5. JSON 파싱
        // Gson 라이브러리를 사용하여 JSON 문자열을 Map 객체로 변환
        // field: private final Gson gson = new Gson(); 사용
        return gson.fromJson(jsonResponse, Map.class);
    }

    // --- 응답 파싱 및 DTO 변환 (최종 필드 분기 적용) ---
    private List<IndexDailyDTO> parseResponse(Map<String, Object> response, String indexName, String type) {
        List<IndexDailyDTO> result = new ArrayList<>();

        List<Map<String, String>> outputList = (List<Map<String, String>>) response.get("output2");

        if (outputList == null) {
            outputList = (List<Map<String, String>>) response.get("output");
            if (outputList == null) {
                System.err.println(indexName + " 지수 파싱 실패: 응답 상세 리스트(output2/output)가 없습니다.");
                return result;
            }
        }

        for (Map<String, String> item : outputList) {
            try {
                String dateStr;
                String priceStr;
                String volumeStr;

                // --- Type에 따라 필드명 분기 적용 ---
                if (type.equals("DOMESTIC")) {
                    // 국내 지수 (KOSPI/KOSDAQ): 주신 국내 주식 필드 적용
                    dateStr = item.get("stck_bsop_date");
                    System.out.println(dateStr);
                    priceStr = item.get("stck_clpr");     // 주식 종가 (stck_clpr)
                    volumeStr = item.get("acml_vol");
                } else {
                    // 해외 지수 (NASDAQ/S&P500): 이전 해외 필드 가정
                    dateStr = item.get("stck_bsop_date"); // 날짜는 국내와 동일 가정
                    priceStr = item.get("ovrs_nmix_prpr"); // 현재가 (종가로 사용)
                    volumeStr = item.get("acml_vol");

                    // NOTE: 해외 지수 API 필드명은 KIS 문서를 통해 최종 확인 필요
                }

                // Null 체크 방어 코드
                if (dateStr == null || priceStr == null || dateStr.trim().isEmpty() || priceStr.trim().isEmpty()) {
                    System.err.println(indexName + " 파싱 실패: 날짜/종가 필드 누락. Date: " + dateStr + ", Price: " + priceStr);
                    continue;
                }

                Date date = Date.valueOf(dateStr.replaceAll("(\\d{4})(\\d{2})(\\d{2})", "$1-$2-$3"));
                BigDecimal price = new BigDecimal(priceStr);
                Long volume = volumeStr != null ? Long.parseLong(volumeStr) : null;

                result.add(new IndexDailyDTO(indexName, date, price, volume));
            } catch (Exception e) {
                System.err.println(indexName + " 지수 파싱 중 오류 발생: " + e.getMessage());
            }
        }
        return result;
    }
}