package com.dongyang.mock.service.api.fetch;

import com.dongyang.mock.model.DAO.StockDAO;
import com.dongyang.mock.model.DTO.RankingItemDTO;
import com.dongyang.mock.service.auth.AuthService;
import com.dongyang.mock.util.api.ApiConstants;
import com.dongyang.mock.util.api.InmemoryCacheManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RankingFetchService extends FetchService {
    public static final String RANKING_CACHE_KEY = "stock:top100:ranking";
    AuthService authService = new AuthService();
    private final InmemoryCacheManager cacheManager = InmemoryCacheManager.getInstance();
    private final StockDAO stockDAO = new StockDAO();

    @Override
    public void runUpdate() {
        String token = authService.issueAccessToken();
        if (token == null) {
            System.err.println("Access Token 발급 실패. 업데이트 중단.");
            return;
        }

        try{
            runFetch(token, "000");
        } catch (Exception e) {
            System.err.println("배치 업데이트 중 오류 발생: " + e.getMessage());
        }
    }

    private void runFetch(String token, String market) throws Exception {
        String tr = ApiConstants.TR_RANKING;
        String url = ApiConstants.PATH_RANKING;
        String requestBody = buildRequestBody(market, "1", "3");
        Map<String, Object> responseMap = callApi(token, url, tr, requestBody, "POST");

        List<RankingItemDTO> dataList = parseResponse(responseMap);
        System.out.println("랭킹 API 획득 후 데이터 " + dataList.size() + "개.");

        if (!dataList.isEmpty()) {
            cacheManager.put(RANKING_CACHE_KEY, dataList);
            System.out.println("랭킹 데이터 " + dataList.size() + "개 인메모리 캐시 업데이트 완료.");
        }
    }

    private String buildRequestBody(String market, String mangStockIncls, String stexTp){
        return "{\"mrkt_tp\" : \"%s\",\"mang_stk_incls\" : \"%s\", \"stex_tp\" : \"%s\"}".formatted(market, mangStockIncls, stexTp);
    }

    private List<RankingItemDTO> parseResponse(Map<String, Object> response) {
        List<RankingItemDTO> result = new ArrayList<>();

        List<Map<String, String>> outputList = (List<Map<String, String>>) response.get("trde_prica_upper");

        if (outputList == null) {
            System.err.println("랭킹 파싱 실패: 응답 상세 리스트(ranking_list)가 없습니다.");
            return result;
        }

        for (Map<String, String> item : outputList) {
            try{
                String rankStr = item.get("now_rank");
                String stkCodeStr = item.get("stk_cd");
                String stkNameStr = item.get("stk_nm");

                if(stkCodeStr == null || stkNameStr == null){
                    System.err.println("랭킹" + rankStr + "누락");
                    continue;
                }

                result.add(new RankingItemDTO(rankStr, stkCodeStr, stkNameStr));
            } catch (Exception e) {
                System.err.println("랭킹 아이템 파싱 중 오류 발생");
            }
        }

        return result;
    }
}
