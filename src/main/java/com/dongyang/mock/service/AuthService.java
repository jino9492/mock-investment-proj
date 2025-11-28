package com.dongyang.mock.service;

import com.dongyang.mock.util.api.TokenManager;
import com.google.gson.Gson;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class AuthService {

    private final TokenManager tokenManager;
    private final Gson gson = new Gson();

    public AuthService() {
        this.tokenManager = TokenManager.getInstance();
    }

    public String issueAccessToken() {

        String cachedToken = tokenManager.getAccessToken();
        if (cachedToken != null) {
            System.out.println("캐시된 토큰 재사용: " + cachedToken);
            return cachedToken;
        }

        HttpURLConnection conn = null;
        try {
            URL url = new URL(TokenManager.API_TOKEN_URL + "/oauth2/tokenP");
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            // 요청 본문 생성
            String jsonInputString = String.format(
                    "{\"grant_type\":\"client_credentials\", \"appkey\":\"%s\", \"appsecret\":\"%s\"}",
                    TokenManager.APP_KEY,
                    TokenManager.APP_SECRET
            );

            // 본문 전송
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonInputString.getBytes("utf-8"));
            }

            // 응답 처리
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String responseJson = readResponse(conn.getInputStream());

                Map<String, Object> responseMap = gson.fromJson(responseJson, Map.class);

                String accessToken = (String) responseMap.get("access_token");
                Object expiresInObj = responseMap.get("expires_in");

                long expiresIn = 0;
                if (expiresInObj instanceof Number) {
                    expiresIn = ((Number) expiresInObj).longValue();
                } else if (expiresInObj instanceof String) {
                    expiresIn = Long.parseLong((String) expiresInObj);
                }

                // TokenManager에 저장
                if (accessToken != null && expiresIn > 0) {
                    tokenManager.setToken(accessToken, expiresIn);
                    return accessToken;
                } else {
                    System.err.println("Required information (access_token, expires_in) is missing in the token issuance response.");
                }
            } else {
                InputStream errorStream = conn.getErrorStream();
                if (errorStream == null) {
                    errorStream = conn.getInputStream();
                }

                String errorResponse = readResponse(errorStream);
                System.err.println("Token Issuing Failed - Response Code : " + conn.getResponseCode());
                System.err.println("receipted origin data:\n" + errorResponse);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }

        return null;
    }

    public String getValidAccessToken() {
        TokenManager tokenManager = TokenManager.getInstance();

        // 만료 여부 확인
        if (tokenManager.getAccessToken() == null) {
            String newToken = issueAccessToken();

            if (newToken == null) {
                throw new RuntimeException("Access Token refresh failed.");
            }
            return newToken;
        }

        return tokenManager.getAccessToken();
    }

    private String readResponse(InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"))) {
            StringBuilder responseBody = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                responseBody.append(responseLine.trim());
            }
            return responseBody.toString();
        }
    }

}