package com.dongyang.mock.model.DTO;

import java.sql.Date;
import java.sql.Timestamp;
import java.math.BigDecimal;

public class StockPriceDailyDTO {

    // 필드 (테이블 컬럼에 대응)
    private Long stockId;             // BIGINT NOT NULL (복합 PK & FK)
    private Date priceDate;           // DATE NOT NULL (복합 PK)

    private BigDecimal openPrice;     // DECIMAL(18, 2) NOT NULL (시가)
    private BigDecimal highPrice;     // DECIMAL(18, 2) NOT NULL (고가)
    private BigDecimal lowPrice;      // DECIMAL(18, 2) NOT NULL (저가)
    private BigDecimal closePrice;    // DECIMAL(18, 2) NOT NULL (종가)
    private Long volume;              // BIGINT NOT NULL (거래량)
    private Long amount;              // BIGINT NULL (거래대금)

    private BigDecimal prevClose;     // DECIMAL(18, 2) NULL (전일 종가)
    private BigDecimal changePrice;   // DECIMAL(18, 2) NULL (전일 대비 가격)
    private BigDecimal changeRate;    // DECIMAL(6, 2) NULL (등락률)

    private Timestamp createdAt;      // DATETIME NOT NULL

    // 1. 기본 생성자
    public StockPriceDailyDTO() {
    }

    // 2. 필수 필드를 포함하는 생성자 (선택 사항)
    public StockPriceDailyDTO(Long stockId, Date priceDate, BigDecimal openPrice, BigDecimal highPrice,
                              BigDecimal lowPrice, BigDecimal closePrice, Long volume, Long amount,
                              BigDecimal prevClose, BigDecimal changePrice, BigDecimal changeRate) {
        this.stockId = stockId;
        this.priceDate = priceDate;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
        this.amount = amount;
        this.prevClose = prevClose;
        this.changePrice = changePrice;
        this.changeRate = changeRate;
    }

    // 3. Getter 및 Setter 메서드

    // stock_id
    public Long getStockId() {
        return stockId;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    // price_date
    public Date getPriceDate() {
        return priceDate;
    }

    public void setPriceDate(Date priceDate) {
        this.priceDate = priceDate;
    }

    // open_price
    public BigDecimal getOpenPrice() {
        return openPrice;
    }
    
    /**
     * 화면 표시용: 시가를 음수로 반환
     * DB에는 양수로 저장되어 있지만, 화면에는 음수로 표시하기 위해 사용
     */
    public BigDecimal getOpenPriceForDisplay() {
        return openPrice != null ? openPrice.negate() : null;
    }

    public void setOpenPrice(BigDecimal openPrice) {
        this.openPrice = openPrice;
    }

    // high_price
    public BigDecimal getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(BigDecimal highPrice) {
        this.highPrice = highPrice;
    }

    // low_price
    public BigDecimal getLowPrice() {
        return lowPrice;
    }
    
    /**
     * 화면 표시용: 저가를 음수로 반환
     * DB에는 양수로 저장되어 있지만, 화면에는 음수로 표시하기 위해 사용
     */
    public BigDecimal getLowPriceForDisplay() {
        return lowPrice != null ? lowPrice.negate() : null;
    }

    public void setLowPrice(BigDecimal lowPrice) {
        this.lowPrice = lowPrice;
    }

    // close_price
    public BigDecimal getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(BigDecimal closePrice) {
        this.closePrice = closePrice;
    }

    // volume
    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    // amount
    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    // prev_close
    public BigDecimal getPrevClose() {
        return prevClose;
    }

    public void setPrevClose(BigDecimal prevClose) {
        this.prevClose = prevClose;
    }

    // change_price
    public BigDecimal getChangePrice() {
        return changePrice;
    }

    public void setChangePrice(BigDecimal changePrice) {
        this.changePrice = changePrice;
    }

    // change_rate
    public BigDecimal getChangeRate() {
        return changeRate;
    }

    public void setChangeRate(BigDecimal changeRate) {
        this.changeRate = changeRate;
    }

    // created_at
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}