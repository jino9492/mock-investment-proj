package com.dongyang.mock.model.DTO;

import java.sql.Date;
import java.sql.Timestamp;
import java.math.BigDecimal;

public class StockDetailDTO {

    // 필드 (제공된 SQL 정의에 기반)
    private Long stockId;             // BIGINT NOT NULL (Primary Key & Foreign Key)
    private String companyName;       // VARCHAR(100) NOT NULL
    private BigDecimal faceValue;     // DECIMAL(18,2) NULL
    private String settlementMonth;   // VARCHAR(10) NULL
    private String marketCap;         // VARCHAR(50) NULL
    private Date listingDate;         // DATE NULL
    private String description;       // TEXT NULL
    private Timestamp createdAt;      // DATETIME NOT NULL
    private Timestamp updatedAt;      // DATETIME NOT NULL

    // 1. 기본 생성자
    public StockDetailDTO() {
    }

    // 2. 주요 필드를 포함하는 생성자 (선택 사항)
    public StockDetailDTO(Long stockId, String companyName, BigDecimal faceValue, String settlementMonth,
                          String marketCap, Date listingDate, String description) {
        this.stockId = stockId;
        this.companyName = companyName;
        this.faceValue = faceValue;
        this.settlementMonth = settlementMonth;
        this.marketCap = marketCap;
        this.listingDate = listingDate;
        this.description = description;
    }

    // 3. Getter 및 Setter 메서드

    // stock_id
    public Long getStockId() {
        return stockId;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    // company_name
    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    // face_value (권면액)
    public BigDecimal getFaceValue() {
        return faceValue;
    }

    public void setFaceValue(BigDecimal faceValue) {
        this.faceValue = faceValue;
    }

    // settlement_month (결산월)
    public String getSettlementMonth() {
        return settlementMonth;
    }

    public void setSettlementMonth(String settlementMonth) {
        this.settlementMonth = settlementMonth;
    }

    // market_cap (시가총액)
    public String getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(String marketCap) {
        this.marketCap = marketCap;
    }

    // listing_date (상장일)
    public Date getListingDate() {
        return listingDate;
    }

    public void setListingDate(Date listingDate) {
        this.listingDate = listingDate;
    }

    // description (주요 사업)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}