package com.dongyang.mock.model.DTO;

/**
 * 회원 보유 종목 정보 DTO
 */
public class MemberHoldingDTO {
    private String stockName;        // 종목명
    private int quantity;           // 보유 수량
    private int averagePrice;       // 평균 단가
    private int currentPrice;       // 현재가
    private long evaluationAmount;  // 평가액
    private long profitLoss;        // 손익
    private double profitRate;      // 손익률 (%)

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getAveragePrice() {
        return averagePrice;
    }

    public void setAveragePrice(int averagePrice) {
        this.averagePrice = averagePrice;
    }

    public int getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(int currentPrice) {
        this.currentPrice = currentPrice;
    }

    public long getEvaluationAmount() {
        return evaluationAmount;
    }

    public void setEvaluationAmount(long evaluationAmount) {
        this.evaluationAmount = evaluationAmount;
    }

    public long getProfitLoss() {
        return profitLoss;
    }

    public void setProfitLoss(long profitLoss) {
        this.profitLoss = profitLoss;
    }

    public double getProfitRate() {
        return profitRate;
    }

    public void setProfitRate(double profitRate) {
        this.profitRate = profitRate;
    }
}

