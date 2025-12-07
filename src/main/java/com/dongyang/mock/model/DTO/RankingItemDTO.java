package com.dongyang.mock.model.DTO;

public class RankingItemDTO {
    private String nowRank;
    private String stkCode;
    private String stkName;

    public RankingItemDTO(String nowRank, String stkCode, String stkName) {}

    public String getNowRank() {
        return nowRank;
    }

    public void setNowRank(String nowRank) {
        this.nowRank = nowRank;
    }

    public String getStkCode() {
        return stkCode;
    }

    public void setStkCode(String stkCode) {
        this.stkCode = stkCode;
    }

    public String getStkName() {
        return stkName;
    }

    public void setStkName(String stkName) {
        this.stkName = stkName;
    }
}
