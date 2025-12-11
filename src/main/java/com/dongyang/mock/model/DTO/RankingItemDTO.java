package com.dongyang.mock.model.DTO;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class RankingItemDTO {
    private String nowRank;
    private String stkCode;
    private String stkName;
    private BigDecimal curPrice; // 현재가
    private BigDecimal fluRt; // 등락률
    private Timestamp lastUpdate;

    public RankingItemDTO() {}

    public RankingItemDTO(String nowRank, String stkCode, String stkName,  BigDecimal curPrice, BigDecimal fluRt) {
        this.nowRank = nowRank;
        this.stkCode = stkCode;
        this.stkName = stkName;
        this.curPrice = curPrice;
        this.fluRt = fluRt;
        this.lastUpdate = new Timestamp(System.currentTimeMillis());
    }

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

    public BigDecimal getCurPrice() {
        return curPrice;
    }

    public void setCurPrice(BigDecimal curPrice) {
        this.curPrice = curPrice;
    }

    public BigDecimal getFluRt() {
        return fluRt;
    }

    public void setFluRt(BigDecimal fluRt) {
        this.fluRt = fluRt;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
