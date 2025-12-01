package com.dongyang.mock.model.DTO;

/**
 * 대시보드 통계 정보를 담는 DTO
 */
public class DashboardDTO {
    private int totalUsers;           // 전체 회원 수
    private int todayVisitors;        // 당일 접속자 수
    private double userGrowthRate;    // 전일 대비 회원 증가율 (%) - 사용 안 함
    private int userGrowthCount;       // 전일 대비 회원 증감 수 (명)
    private int todayTrades;          // 당일 거래량
    private long todayAmount;         // 당일 거래 금액
    private double tradeGrowthRate;   // 전일 대비 거래 증가율 (%) - 사용 안 함
    private int tradeGrowthCount;     // 전일 대비 거래 증감 수 (건)
    private double visitorGrowthRate; // 전일 대비 접속자 증가율 (%) - 사용 안 함
    private int visitorGrowthCount;   // 전일 대비 접속자 증감 수 (명)

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTodayVisitors() {
        return todayVisitors;
    }

    public void setTodayVisitors(int todayVisitors) {
        this.todayVisitors = todayVisitors;
    }

    public double getUserGrowthRate() {
        return userGrowthRate;
    }

    public void setUserGrowthRate(double userGrowthRate) {
        this.userGrowthRate = userGrowthRate;
    }

    public int getUserGrowthCount() {
        return userGrowthCount;
    }

    public void setUserGrowthCount(int userGrowthCount) {
        this.userGrowthCount = userGrowthCount;
    }

    public int getTodayTrades() {
        return todayTrades;
    }

    public void setTodayTrades(int todayTrades) {
        this.todayTrades = todayTrades;
    }

    public long getTodayAmount() {
        return todayAmount;
    }

    public void setTodayAmount(long todayAmount) {
        this.todayAmount = todayAmount;
    }

    public double getTradeGrowthRate() {
        return tradeGrowthRate;
    }

    public void setTradeGrowthRate(double tradeGrowthRate) {
        this.tradeGrowthRate = tradeGrowthRate;
    }

    public double getVisitorGrowthRate() {
        return visitorGrowthRate;
    }

    public void setVisitorGrowthRate(double visitorGrowthRate) {
        this.visitorGrowthRate = visitorGrowthRate;
    }

    public int getTradeGrowthCount() {
        return tradeGrowthCount;
    }

    public void setTradeGrowthCount(int tradeGrowthCount) {
        this.tradeGrowthCount = tradeGrowthCount;
    }

    public int getVisitorGrowthCount() {
        return visitorGrowthCount;
    }

    public void setVisitorGrowthCount(int visitorGrowthCount) {
        this.visitorGrowthCount = visitorGrowthCount;
    }
}

