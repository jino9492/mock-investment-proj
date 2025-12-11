package com.dongyang.mock.util.api;

import java.io.InputStream;
import java.util.Properties;

public final class ApiConstants {
    private static final Properties props = new Properties();

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

    private ApiConstants() { }

    private static String getPropertyOrEmpty(String key) {
        String value = props.getProperty(key);
        if (value == null) {
            System.err.println("경고: api.properties에서 '" + key + "' 속성을 찾을 수 없습니다.");
            return "";
        }
        return value;
    }

    public static final String SOCKET_URL = getPropertyOrEmpty("kw.socket.url");

    // 국내 지수 (KOSPI/KOSDAQ ...) 분봉 시세
    public static final String TR_DOMESTIC_INDEX = getPropertyOrEmpty("kw.index.tr");
    public static final String PATH_DOMESTIC_INDEX = getPropertyOrEmpty("kw.api.domain") + getPropertyOrEmpty("kw.index.endpoint");

    public static final String TR_STOCK_CODE = getPropertyOrEmpty("kw.stockcode.tr");
    public static final String PATH_STOCK_CODE = getPropertyOrEmpty("kw.api.domain") + getPropertyOrEmpty("kw.stockcode.endpoint");

    public static final String TR_STOCK = getPropertyOrEmpty("kw.stock.tr");
    public static final String PATH_STOCK = getPropertyOrEmpty("kw.api.domain") + getPropertyOrEmpty("kw.stock.endpoint");

    public static final String TR_STOCK_DETAIL = getPropertyOrEmpty("kw.stockdetail.tr");
    public static final String PATH_STOCK_DETAIL = getPropertyOrEmpty("kw.api.domain") + getPropertyOrEmpty("kw.stockdetail.endpoint");

    public static final String TR_RANKING = getPropertyOrEmpty("kw.ranking.tr");
    public static final String PATH_RANKING = getPropertyOrEmpty("kw.api.domain") + getPropertyOrEmpty("kw.ranking.endpoint");
}