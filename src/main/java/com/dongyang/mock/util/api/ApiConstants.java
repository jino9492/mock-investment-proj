package com.dongyang.mock.util.api;

public final class ApiConstants {

    private ApiConstants() { }

    // 국내 지수 (KOSPI/KOSDAQ) 기간별 시세
    public static final String TR_DOMESTIC_INDEX_DAILY = "FHKST03010100";
    public static final String PATH_DOMESTIC_INDEX_DAILY = "/uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice";

    // 해외 지수 (NASDAQ/S&P) 기간별 시세
    public static final String TR_OVERSEAS_INDEX_DAILY = "HHDFS76240000";
    public static final String PATH_OVERSEAS_INDEX_DAILY = "/uapi/overseas-price/v1/quotations/inquire-daily-index-chartprice";
}