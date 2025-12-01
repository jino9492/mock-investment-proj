package com.dongyang.mock.model.DTO;

/**
 * 리그 페이지 관련 모든 DTO를 포함하는 통합 클래스
 */
public class LeagueDTO {

    /**
     * 리그 페이지 계좌 현황 정보
     */
    public static class AccountSummary {
        private long totalAsset;        // 계좌 총 금액
        private long stockValue;        // 투자중인 금액 (보유주식 평가액)
        private long cashBalance;       // 보유 현금
        private double totalReturn;     // 총 수익률(%)
        private double percentile;      // 상위 백분율(%)

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

        public long getCashBalance() {
            return cashBalance;
        }

        public void setCashBalance(long cashBalance) {
            this.cashBalance = cashBalance;
        }

        public double getTotalReturn() {
            return totalReturn;
        }

        public void setTotalReturn(double totalReturn) {
            this.totalReturn = totalReturn;
        }

        public double getPercentile() {
            return percentile;
        }

        public void setPercentile(double percentile) {
            this.percentile = percentile;
        }
    }

    /**
     * 수익률 상위 종목 정보
     */
    public static class TopReturnStock {
        private String stockName;       // 종목명
        private long currentPrice;      // 현재가
        private long avgPurchasePrice;  // 매수 평균가
        private double changeRate;      // 당일 등락률(%)
        private double returnRate;      // 개별 수익률(%)

        public String getStockName() {
            return stockName;
        }

        public void setStockName(String stockName) {
            this.stockName = stockName;
        }

        public long getCurrentPrice() {
            return currentPrice;
        }

        public void setCurrentPrice(long currentPrice) {
            this.currentPrice = currentPrice;
        }

        public long getAvgPurchasePrice() {
            return avgPurchasePrice;
        }

        public void setAvgPurchasePrice(long avgPurchasePrice) {
            this.avgPurchasePrice = avgPurchasePrice;
        }

        public double getChangeRate() {
            return changeRate;
        }

        public void setChangeRate(double changeRate) {
            this.changeRate = changeRate;
        }

        public double getReturnRate() {
            return returnRate;
        }

        public void setReturnRate(double returnRate) {
            this.returnRate = returnRate;
        }
    }

    /**
     * 랭킹 정보
     */
    public static class Ranking {
        private int rank;               // 순위
        private String nickname;        // 닉네임
        private double returnRate;      // 수익률(%)
        private long totalAsset;        // 총 자산
        private boolean isCurrentUser;  // 현재 사용자 여부

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public double getReturnRate() {
            return returnRate;
        }

        public void setReturnRate(double returnRate) {
            this.returnRate = returnRate;
        }

        public long getTotalAsset() {
            return totalAsset;
        }

        public void setTotalAsset(long totalAsset) {
            this.totalAsset = totalAsset;
        }

        public boolean isCurrentUser() {
            return isCurrentUser;
        }

        public void setCurrentUser(boolean isCurrentUser) {
            this.isCurrentUser = isCurrentUser;
        }
    }

    /**
     * 포트폴리오 종목 상세 정보
     */
    public static class PortfolioStock {
        private int stockId;            // 주식 ID
        private String stockName;       // 종목명
        private int quantity;           // 보유 수량
        private long avgPurchasePrice;  // 평균 매입가
        private long currentPrice;      // 현재가
        private double changeRate;      // 당일 등락률(%)
        private long profitLoss;        // 총 수익(손익금액)
        private double returnRate;      // 수익률(%)

        public int getStockId() {
            return stockId;
        }

        public void setStockId(int stockId) {
            this.stockId = stockId;
        }

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

        public long getAvgPurchasePrice() {
            return avgPurchasePrice;
        }

        public void setAvgPurchasePrice(long avgPurchasePrice) {
            this.avgPurchasePrice = avgPurchasePrice;
        }

        public long getCurrentPrice() {
            return currentPrice;
        }

        public void setCurrentPrice(long currentPrice) {
            this.currentPrice = currentPrice;
        }

        public double getChangeRate() {
            return changeRate;
        }

        public void setChangeRate(double changeRate) {
            this.changeRate = changeRate;
        }

        public long getProfitLoss() {
            return profitLoss;
        }

        public void setProfitLoss(long profitLoss) {
            this.profitLoss = profitLoss;
        }

        public double getReturnRate() {
            return returnRate;
        }

        public void setReturnRate(double returnRate) {
            this.returnRate = returnRate;
        }
    }
}

