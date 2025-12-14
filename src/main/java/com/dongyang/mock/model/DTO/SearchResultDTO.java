package com.dongyang.mock.model.DTO;

public class SearchResultDTO {
    private String stockName;      // 종목명
    private String stockSymbol;    // 종목코드
    private String currentPrice;   // 주식의 현재가 (구매 가능 금액이 아닌 주식의 실제 현재가)
    private String changeRate;     // 전날 대비 변동 퍼센트 (등락률, 예: "+2.70%", "-3.26%", "0.00%")
    private String marketType;     // 시장 구분

    public SearchResultDTO() {
    }

    public SearchResultDTO(String stockName, String stockSymbol, String currentPrice, String changeRate, String marketType) {
        this.stockName = stockName;
        this.stockSymbol = stockSymbol;
        this.currentPrice = currentPrice;
        this.changeRate = changeRate;
        this.marketType = marketType;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public String getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(String currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getChangeRate() {
        return changeRate;
    }

    public void setChangeRate(String changeRate) {
        this.changeRate = changeRate;
    }

    public String getMarketType() {
        return marketType;
    }

    public void setMarketType(String marketType) {
        this.marketType = marketType;
    }
}
