package com.dongyang.mock.model.DTO;

import java.sql.Date;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class IndexDTO {
    private String indexCode;
    private Timestamp tradingTime;
    private BigDecimal currentPrice;
    private BigDecimal overnight;

    public IndexDTO() {}

    public IndexDTO(String indexCode, Timestamp tradingTime, BigDecimal currentPrice, BigDecimal overnight) {
        this.indexCode = indexCode;
        this.tradingTime = tradingTime;
        this.currentPrice = currentPrice;
        this.overnight = overnight;
    }

    public String getIndexCode() {
        return indexCode;
    }

    public void setIndexCode(String indexCode) {
        this.indexCode = indexCode;
    }

    public Timestamp getTradingTime() {
        return tradingTime;
    }

    public void setTradingTime(Timestamp tradingTime) {
        this.tradingTime = tradingTime;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getOvernight() {
        return overnight;
    }

    public void setOvernight(BigDecimal overnight) {
        this.overnight = overnight;
    }
}