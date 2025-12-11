package com.dongyang.mock.model.DTO;

import java.sql.Date;
import java.sql.Timestamp;

public class StockDTO {

    // 필드 (테이블 컬럼에 대응)
    private Long stockId;          // BIGINT AUTO_INCREMENT PRIMARY KEY
    private String stockSymbol;    // VARCHAR(20) NOT NULL UNIQUE
    private String stockName;      // VARCHAR(100) NOT NULL
    private String marketType;     // VARCHAR(20) NOT NULL
    private Long stockCodeId;      // BIGINT NULL (FK)
    private Date listingDate;      // DATE NULL
    private String status;         // VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
    private Timestamp createdAt;   // DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    private Timestamp updatedAt;   // DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

    public StockDTO() {
    }

    public StockDTO(Long stockId, String stockSymbol, String stockName, String marketType, Long stockCodeId,
                    Date listingDate, String status, Timestamp createdAt, Timestamp updatedAt) {
        this.stockId = stockId;
        this.stockSymbol = stockSymbol;
        this.stockName = stockName;
        this.marketType = marketType;
        this.stockCodeId = stockCodeId;
        this.listingDate = listingDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public StockDTO(String stockSymbol, String stockName, String marketType, Long stockCodeId, Date listingDate, String status){
        this.stockSymbol = stockSymbol;
        this.stockName = stockName;
        this.marketType = marketType;
        this.stockCodeId = stockCodeId;
        this.listingDate = listingDate;
        this.status = status;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    // stock_id
    public Long getStockId() {
        return stockId;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    // stock_symbol
    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    // stock_name
    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    // market_type
    public String getMarketType() {
        return marketType;
    }

    public void setMarketType(String marketType) {
        this.marketType = marketType;
    }

    // stock_code_id
    public Long getStockCodeId() {
        return stockCodeId;
    }

    public void setStockCodeId(Long stockCodeId) {
        this.stockCodeId = stockCodeId;
    }

    // listing_date
    public Date getListingDate() {
        return listingDate;
    }

    public void setListingDate(Date listingDate) {
        this.listingDate = listingDate;
    }

    // status
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // created_at
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    // updated_at
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // (선택 사항) 객체 내용 확인을 위한 toString() 메서드
    @Override
    public String toString() {
        return "StockDTO{" +
                "stockId=" + stockId +
                ", stockSymbol='" + stockSymbol + '\'' +
                ", stockName='" + stockName + '\'' +
                ", marketType='" + marketType + '\'' +
                ", stockCodeId=" + stockCodeId +
                ", listingDate=" + listingDate +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}