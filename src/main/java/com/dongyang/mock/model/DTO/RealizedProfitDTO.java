package com.dongyang.mock.model.DTO;

import java.sql.Timestamp;

/**
 * 실현수익 정보를 담는 DTO
 */
public class RealizedProfitDTO {
    private String soldAt; // 판매일 (YYYY.MM.DD 형식)
    private String stockName; // 종목명
    private int totalProfit; // 총 판매수익 (판매 금액 - 구매 금액)
    private double profitRate; // 수익률 (%)
    private int totalSalesAmount; // 총 판매금액 (판매 가격 * 수량)
    private int totalPurchaseAmount; // 총 구매금액 (구매 가격 * 수량)
    private int quantity; // 판매수량

    public String getSoldAt() {
        return soldAt;
    }

    public void setSoldAt(String soldAt) {
        this.soldAt = soldAt;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public int getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(int totalProfit) {
        this.totalProfit = totalProfit;
    }

    public double getProfitRate() {
        return profitRate;
    }

    public void setProfitRate(double profitRate) {
        this.profitRate = profitRate;
    }

    public int getTotalSalesAmount() {
        return totalSalesAmount;
    }

    public void setTotalSalesAmount(int totalSalesAmount) {
        this.totalSalesAmount = totalSalesAmount;
    }

    public int getTotalPurchaseAmount() {
        return totalPurchaseAmount;
    }

    public void setTotalPurchaseAmount(int totalPurchaseAmount) {
        this.totalPurchaseAmount = totalPurchaseAmount;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

