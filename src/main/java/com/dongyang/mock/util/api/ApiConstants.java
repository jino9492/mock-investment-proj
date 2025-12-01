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

    // 국내 지수 (KOSPI/KOSDAQ ...) 분봉 시세
    public static final String TR_DOMESTIC_INDEX = props.getProperty("kw.index.tr");
    public static final String PATH_DOMESTIC_INDEX = props.getProperty("kw.api.domain") + props.getProperty("kw.index.endpoint");

    public static final String TR_STOCK_CODE = props.getProperty("kw.stockcode.tr");
    public static final String PATH_STOCK_CODE = props.getProperty("kw.api.domain") + props.getProperty("kw.stockcode.endpoint");

    public static final String TR_STOCK = props.getProperty("kw.stock.tr");
    public static final String PATH_STOCK = props.getProperty("kw.api.domain") + props.getProperty("kw.stock.endpoint");

    public static final String TR_STOCK_DETAIL = props.getProperty("kw.stockdetail.tr");
    public static final String PATH_STOCK_DETAIL = props.getProperty("kw.api.domain") + props.getProperty("kw.stockdetail.endpoint");
}