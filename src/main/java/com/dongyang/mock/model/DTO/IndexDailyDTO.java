package com.dongyang.mock.model.DTO;

import java.sql.Date;
import java.math.BigDecimal;

public class IndexDailyDTO {
    private String indexCode;
    private Date tradingDate;
    private BigDecimal closePrice;
    private Long volume;

    public IndexDailyDTO() {}

    public IndexDailyDTO(String indexCode, Date tradingDate, BigDecimal closePrice, Long volume) {
        this.indexCode = indexCode;
        this.tradingDate = tradingDate;
        this.closePrice = closePrice;
        this.volume = volume;
    }

    public String getIndexCode() { return indexCode; }
    public void setIndexCode(String indexCode) { this.indexCode = indexCode; }

    public Date getTradingDate() { return tradingDate; }
    public void setTradingDate(Date tradingDate) { this.tradingDate = tradingDate; }

    public BigDecimal getClosePrice() { return closePrice; }
    public void setClosePrice(BigDecimal closePrice) { this.closePrice = closePrice; }

    public Long getVolume() { return volume; }
    public void setVolume(Long volume) { this.volume = volume; }
}