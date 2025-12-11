package com.dongyang.mock.model.DTO;

/**
 * 회원 자산 정보 DTO
 */
public class MemberAssetDTO {
    private long virtualCash;        // 예치금
    private long totalAsset;         // 총 자산 (예치금 + 보유주식 평가액)
    private long stockValue;         // 보유주식 평가액
    private double profitRate;       // 수익률 (%)
    private long profitLoss;         // 평가 손익

    public long getVirtualCash() {
        return virtualCash;
    }

    public void setVirtualCash(long virtualCash) {
        this.virtualCash = virtualCash;
    }

    public long getTotalAsset() {
        return totalAsset;
    }

    public void setTotalAsset(long totalAsset) {
        this.totalAsset = totalAsset;
    }

    public long getStockValue() {
        return stockValue;
    }

    public void setStockValue(long stockValue) {
        this.stockValue = stockValue;
    }

    public double getProfitRate() {
        return profitRate;
    }

    public void setProfitRate(double profitRate) {
        this.profitRate = profitRate;
    }

    public long getProfitLoss() {
        return profitLoss;
    }

    public void setProfitLoss(long profitLoss) {
        this.profitLoss = profitLoss;
    }
}

