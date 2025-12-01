package com.dongyang.mock.model.DTO;

/**
 * 인기종목 정보를 담는 DTO
 */
public class TopStockDTO {
    private String stockName;     // 종목명
    private int tradeCount;       // 거래건수
    private long tradeAmount;     // 거래금액

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public int getTradeCount() {
        return tradeCount;
    }

    public void setTradeCount(int tradeCount) {
        this.tradeCount = tradeCount;
    }

    public long getTradeAmount() {
        return tradeAmount;
    }

    public void setTradeAmount(long tradeAmount) {
        this.tradeAmount = tradeAmount;
    }
}

