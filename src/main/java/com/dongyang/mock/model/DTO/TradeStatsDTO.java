package com.dongyang.mock.model.DTO;

/**
 * 거래 통계 정보를 담는 DTO
 */
public class TradeStatsDTO {
    private int todayTradeCount;      // 오늘 거래 건수
    private long todayTradeAmount;    // 오늘 거래 금액
    private int buyCount;             // 매수 건수
    private int sellCount;            // 매도 건수
    private int cancelledCount;       // 거래 취소 건수
    private int yesterdayTradeCount;  // 전일 거래 건수 (비교용)
    private long yesterdayTradeAmount; // 전일 거래 금액 (비교용)
    private int yesterdayBuyCount;    // 전일 매수 건수 (비교용)
    private int yesterdaySellCount;   // 전일 매도 건수 (비교용)
    private int yesterdayCancelledCount; // 전일 취소 건수 (비교용)

    public int getTodayTradeCount() {
        return todayTradeCount;
    }

    public void setTodayTradeCount(int todayTradeCount) {
        this.todayTradeCount = todayTradeCount;
    }

    public long getTodayTradeAmount() {
        return todayTradeAmount;
    }

    public void setTodayTradeAmount(long todayTradeAmount) {
        this.todayTradeAmount = todayTradeAmount;
    }

    public int getBuyCount() {
        return buyCount;
    }

    public void setBuyCount(int buyCount) {
        this.buyCount = buyCount;
    }

    public int getSellCount() {
        return sellCount;
    }

    public void setSellCount(int sellCount) {
        this.sellCount = sellCount;
    }

    public int getCancelledCount() {
        return cancelledCount;
    }

    public void setCancelledCount(int cancelledCount) {
        this.cancelledCount = cancelledCount;
    }

    public int getYesterdayTradeCount() {
        return yesterdayTradeCount;
    }

    public void setYesterdayTradeCount(int yesterdayTradeCount) {
        this.yesterdayTradeCount = yesterdayTradeCount;
    }

    public long getYesterdayTradeAmount() {
        return yesterdayTradeAmount;
    }

    public void setYesterdayTradeAmount(long yesterdayTradeAmount) {
        this.yesterdayTradeAmount = yesterdayTradeAmount;
    }

    public int getYesterdayBuyCount() {
        return yesterdayBuyCount;
    }

    public void setYesterdayBuyCount(int yesterdayBuyCount) {
        this.yesterdayBuyCount = yesterdayBuyCount;
    }

    public int getYesterdaySellCount() {
        return yesterdaySellCount;
    }

    public void setYesterdaySellCount(int yesterdaySellCount) {
        this.yesterdaySellCount = yesterdaySellCount;
    }

    public int getYesterdayCancelledCount() {
        return yesterdayCancelledCount;
    }

    public void setYesterdayCancelledCount(int yesterdayCancelledCount) {
        this.yesterdayCancelledCount = yesterdayCancelledCount;
    }

    /**
     * 전일 대비 거래 건수 변화율 계산
     */
    public double getTradeCountChangeRate() {
        if (yesterdayTradeCount == 0) {
            return todayTradeCount > 0 ? 100.0 : 0.0;
        }
        return ((double)(todayTradeCount - yesterdayTradeCount) / yesterdayTradeCount) * 100.0;
    }

    /**
     * 전일 대비 거래 금액 변화율 계산
     */
    public double getTradeAmountChangeRate() {
        if (yesterdayTradeAmount == 0) {
            return todayTradeAmount > 0 ? 100.0 : 0.0;
        }
        return ((double)(todayTradeAmount - yesterdayTradeAmount) / yesterdayTradeAmount) * 100.0;
    }

    /**
     * 전일 대비 매수 건수 변화율 계산
     */
    public double getBuyCountChangeRate() {
        if (yesterdayBuyCount == 0) {
            return buyCount > 0 ? 100.0 : 0.0;
        }
        return ((double)(buyCount - yesterdayBuyCount) / yesterdayBuyCount) * 100.0;
    }

    /**
     * 전일 대비 매도 건수 변화율 계산
     */
    public double getSellCountChangeRate() {
        if (yesterdaySellCount == 0) {
            return sellCount > 0 ? 100.0 : 0.0;
        }
        return ((double)(sellCount - yesterdaySellCount) / yesterdaySellCount) * 100.0;
    }

    /**
     * 전일 대비 취소 건수 변화율 계산
     */
    public double getCancelledCountChangeRate() {
        if (yesterdayCancelledCount == 0) {
            return cancelledCount > 0 ? 100.0 : 0.0;
        }
        return ((double)(cancelledCount - yesterdayCancelledCount) / yesterdayCancelledCount) * 100.0;
    }
}

