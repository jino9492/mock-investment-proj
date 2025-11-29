package com.dongyang.mock.util.api;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

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

    public static final String KIS_APP_KEY = props.getProperty("koreainvest.app.key");
    public static final String KIS_APP_SECRET = props.getProperty("koreainvest.app.secret");
    public static final String KIS_API_TOKEN_URL = props.getProperty("koreainvest.api.token.url");

    // 토큰 종류를 키(String)로, ApiToken 객체를 값으로 저장
    private final Map<String, ApiToken> tokenStore = new ConcurrentHashMap<>();

    public static final String TOKEN_KIS = "KIS";

    private TokenManager() { }

    private static final TokenManager instance = new TokenManager();
    public static TokenManager getInstance() {
        return instance;
    }

    public String getAccessToken(String tokenKey) {
        ApiToken token = tokenStore.get(tokenKey);
        if (token != null && !token.isExpired()) {
            return token.token;
        }
        // 토큰이 만료되었거나 없음
        return null;
    }

    public void setToken(String tokenKey, String token, long expiresIn) {
        ApiToken apiToken = new ApiToken(token, expiresIn);
        tokenStore.put(tokenKey, apiToken);
        System.out.println(tokenKey + " Access Token is successfully saved.");
    }
}