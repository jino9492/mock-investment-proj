package com.dongyang.mock.model.DTO;

public class MemberStatsDTO {
    private int totalCount;
    private int todayLoginCount;
    private int activeCount;
    private int dormantCount;
    private int deletedCount; // 탈퇴 사용자 수
    
    // 전일 대비 데이터
    private int yesterdayTotalCount;
    private int yesterdayLoginCount;
    private int yesterdayActiveCount;
    private int yesterdayDormantCount;
    private int yesterdayDeletedCount;

    // 전체 회원 수
    public int getTotalCount() {
        return totalCount;
    }

    // 전체 회원 수 설정
    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    // 당일 접속자 수
    public int getTodayLoginCount() {
        return todayLoginCount;
    }

    // 당일 접속자 수 설정
    public void setTodayLoginCount(int todayLoginCount) {
        this.todayLoginCount = todayLoginCount;
    }

    // 활동 회원 수
    public int getActiveCount() {
        return activeCount;
    }

    // 활동 회원 수 설정
    public void setActiveCount(int activeCount) {
        this.activeCount = activeCount;
    }

    // 휴면 회원 수
    public int getDormantCount() {
        return dormantCount;
    }

    // 휴면 회원 수 설정
    public void setDormantCount(int dormantCount) {
        this.dormantCount = dormantCount;
    }

    // 탈퇴 사용자 수
    public int getDeletedCount() {
        return deletedCount;
    }

    // 탈퇴 사용자 수 설정
    public void setDeletedCount(int deletedCount) {
        this.deletedCount = deletedCount;
    }

    // 전일 전체 회원 수
    public int getYesterdayTotalCount() {
        return yesterdayTotalCount;
    }

    public void setYesterdayTotalCount(int yesterdayTotalCount) {
        this.yesterdayTotalCount = yesterdayTotalCount;
    }

    // 전일 접속자 수
    public int getYesterdayLoginCount() {
        return yesterdayLoginCount;
    }

    public void setYesterdayLoginCount(int yesterdayLoginCount) {
        this.yesterdayLoginCount = yesterdayLoginCount;
    }

    // 전일 활동 회원 수
    public int getYesterdayActiveCount() {
        return yesterdayActiveCount;
    }

    public void setYesterdayActiveCount(int yesterdayActiveCount) {
        this.yesterdayActiveCount = yesterdayActiveCount;
    }

    // 전일 휴면 회원 수
    public int getYesterdayDormantCount() {
        return yesterdayDormantCount;
    }

    public void setYesterdayDormantCount(int yesterdayDormantCount) {
        this.yesterdayDormantCount = yesterdayDormantCount;
    }

    // 전일 탈퇴 사용자 수
    public int getYesterdayDeletedCount() {
        return yesterdayDeletedCount;
    }

    public void setYesterdayDeletedCount(int yesterdayDeletedCount) {
        this.yesterdayDeletedCount = yesterdayDeletedCount;
    }

    /**
     * 전일 대비 전체 회원 수 변화량 계산 (명 단위)
     */
    public int getTotalCountChange() {
        return totalCount - yesterdayTotalCount;
    }

    /**
     * 전일 대비 당일 접속자 수 변화량 계산 (명 단위)
     */
    public int getTodayLoginCountChange() {
        return todayLoginCount - yesterdayLoginCount;
    }

    /**
     * 전일 대비 활동 사용자 수 변화량 계산 (명 단위)
     */
    public int getActiveCountChange() {
        return activeCount - yesterdayActiveCount;
    }

    /**
     * 전일 대비 휴면 사용자 수 변화량 계산 (명 단위)
     */
    public int getDormantCountChange() {
        return dormantCount - yesterdayDormantCount;
    }

    /**
     * 전일 대비 탈퇴 사용자 수 변화량 계산 (명 단위)
     */
    public int getDeletedCountChange() {
        return deletedCount - yesterdayDeletedCount;
    }
}

