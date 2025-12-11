package com.dongyang.mock.util;

import com.dongyang.mock.model.DTO.ApiStatusDTO;
import com.dongyang.mock.util.api.TokenManager;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * API 연동 상태를 체크하는 유틸리티 클래스
 */
public class ApiStatusUtil {

    /**
     * API 연동 상태 조회
     * - 토큰 유효성 확인
     * - 실제 API 엔드포인트에 요청하여 응답 시간 및 상태 확인
     * @return API 연동 상태 정보
     */
    public static ApiStatusDTO getApiStatus() {
        ApiStatusDTO status = new ApiStatusDTO();
        
        try {
            // 1. 토큰 유효성 확인
            TokenManager tokenManager = TokenManager.getInstance();
            String accessToken = tokenManager.getAccessToken();
            
            if (accessToken == null) {
                // 토큰이 없거나 만료된 경우
                status.setConnected(false);
                status.setStatus("오류");
                status.setErrorMessage("토큰이 없거나 만료되었습니다.");
                status.setResponseTime(0);
                return status;
            }

            // 2. 실제 API 엔드포인트에 간단한 요청 전송하여 연결 상태 확인
            // API 도메인에 HEAD 요청을 보내서 응답 시간 측정
            long startTime = System.currentTimeMillis();
            boolean isApiReachable = checkApiReachability();
            long responseTime = System.currentTimeMillis() - startTime;

            if (isApiReachable) {
                status.setConnected(true);
                status.setStatus("정상");
                status.setResponseTime(responseTime);
                status.setErrorMessage(null);
            } else {
                status.setConnected(false);
                status.setStatus("연결 실패");
                status.setResponseTime(responseTime);
                status.setErrorMessage("API 서버에 연결할 수 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            status.setConnected(false);
            status.setStatus("오류");
            status.setErrorMessage("API 상태 확인 중 오류가 발생했습니다: " + e.getMessage());
            status.setResponseTime(0);
        }

        return status;
    }

    /**
     * API 서버 연결 가능 여부 확인
     * @return 연결 가능하면 true, 아니면 false
     */
    private static boolean checkApiReachability() {
        HttpURLConnection conn = null;
        try {
            // API 도메인에 HEAD 요청 전송 (가벼운 요청)
            URL url = new URL(TokenManager.API_DOMAIN);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(5000); // 5초 타임아웃
            conn.setReadTimeout(5000);
            
            int responseCode = conn.getResponseCode();
            // 200-299 또는 401(인증 필요)는 서버가 응답한다는 의미
            return responseCode >= 200 && responseCode < 500;
        } catch (Exception e) {
            // 연결 실패
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}

