package com.dongyang.mock.util.api;

import java.io.InputStream;
import java.util.Properties;

public class TokenManager {
    private static final Properties props = new Properties();

    // api 설정 파일 열기
    static {
        try (InputStream is = TokenManager.class.getClassLoader().getResourceAsStream("api.properties")) {
            if (is == null) {
                throw new RuntimeException("api.properties 파일을 찾을 수 없습니다.");
            }
            props.load(is);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }

    public static final String APP_KEY = props.getProperty("koreainvest.app.key");
    public static final String APP_SECRET = props.getProperty("koreainvest.app.secret");
    public static final String API_TOKEN_URL = props.getProperty("koreainvest.api.token.url");

    private String accessToken; // 발급받은 토큰 정보
    private long tokenExpiryTime; // 토큰 만료 시간 (밀리초)

    private TokenManager() { }

    private static final TokenManager instance = new TokenManager();
    public static TokenManager getInstance() {
        return instance;
    }

    /**
     * 현재 유효한 Access Token을 반환합니다. 만료되었으면 null을 반환합니다.
     */
    public String getAccessToken() {
        if (accessToken != null && System.currentTimeMillis() < tokenExpiryTime) {
            return accessToken;
        }
        // 토큰이 만료되었거나 없음
        return null;
    }

    /**
     * 새로운 Access Token과 만료 시간을 설정합니다.
     * @param token 새로 발급받은 Access Token
     * @param expiresIn API가 알려준 유효 기간 (초 단위)
     */
    public void setToken(String token, long expiresIn) {
        this.accessToken = token;
        // API가 알려준 만료 시간(초)에서 약 60초를 미리 빼서 갱신하도록 설정
        this.tokenExpiryTime = System.currentTimeMillis() + (expiresIn * 1000) - 60000;
        System.out.println("Access Token이 성공적으로 저장되었습니다. 만료 시각: " + new java.util.Date(tokenExpiryTime));
    }
}