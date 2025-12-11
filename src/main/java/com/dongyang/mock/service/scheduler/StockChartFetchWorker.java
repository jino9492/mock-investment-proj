package com.dongyang.mock.service.scheduler;

import com.dongyang.mock.service.api.StockChartService;
import com.dongyang.mock.service.api.fetch.StockChartFetchService;

import java.sql.SQLException;

public class StockChartFetchWorker implements Runnable {

    @Override
    public synchronized void run() {
        StockChartService stockChartService = new StockChartService();
        StockChartFetchService stockChartFetchService = new StockChartFetchService();

        System.out.println("--- 주가 업데이트 워커 시작 ---");
        stockChartFetchService.runUpdate();
        try {
            stockChartService.reCalculateAndUpateZeroChangeRates();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("--- 주가 업데이트 워커 종료 ---");
    }
}
