package com.dongyang.mock.model.DTO;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class StockChartDTO {
    private Long stkId;
    private Timestamp chartTime;
    private BigDecimal openPrice;     // DECIMAL(18, 2) NOT NULL (시가)
    private BigDecimal highPrice;     // DECIMAL(18, 2) NOT NULL (고가)
    private BigDecimal lowPrice;      // DECIMAL(18, 2) NOT NULL (저가)
    private BigDecimal closePrice;    // DECIMAL(18, 2) NOT NULL (종가)
    private Long volume;              // BIGINT NOT NULL (거래량)
    private BigDecimal changeRate;    // DECIMAL(6, 2) NULL (등락률)

    public StockChartDTO(Long stkId, Timestamp chartTime, BigDecimal openPrice, BigDecimal highPrice, BigDecimal lowPrice, BigDecimal closePrice, Long volume, BigDecimal changeRate) {
        this.stkId = stkId;
        this.chartTime = chartTime;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
        this.changeRate = changeRate;
    }

    public Long getStkId() {
        return stkId;
    }

    public void setStkId(Long stkId) {
        this.stkId = stkId;
    }

    public Timestamp getChartTime() {
        return chartTime;
    }

    public void setChartTime(Timestamp chartTime) {
        this.chartTime = chartTime;
    }

    public BigDecimal getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(BigDecimal openPrice) {
        this.openPrice = openPrice;
    }

    public BigDecimal getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(BigDecimal highPrice) {
        this.highPrice = highPrice;
    }

    public BigDecimal getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(BigDecimal lowPrice) {
        this.lowPrice = lowPrice;
    }

    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(BigDecimal closePrice) {
        this.closePrice = closePrice;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public BigDecimal getChangeRate() {
        return changeRate;
    }

    public void setChangeRate(BigDecimal changeRate) {
        this.changeRate = changeRate;
    }
}
