package com.dongyang.mock.model.DTO;

public class MemberStatsDTO {
    private int totalCount;
    private int todayLoginCount;
    private int activeCount;
    private int dormantCount;

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
}

