package com.dongyang.mock.model.DTO;

import java.sql.Date;
import java.sql.Timestamp;
import java.math.BigDecimal; // DECIMAL(18,2) 처리를 위해 필요

public class StockDetailDTO {
    private Long stockId;             // BIGINT NOT NULL (Primary Key & Foreign Key)
    private String companyName;       // VARCHAR(100) NOT NULL
    private BigDecimal faceValue;     // DECIMAL(18,2) NULL
    private String settlementMonth;   // VARCHAR(10) NULL
    private String marketCap;         // VARCHAR(50) NULL (시가총액 문자 형태)
    private Date listingDate;         // DATE NULL
    private String foundedYear;       // VARCHAR(20) NULL (텍스트)
    private Long totalShares;         // BIGINT NULL
    private Date sharesAsOf;          // DATE NULL (기준일)
    private String description;       // TEXT NULL (TEXT는 String으로 처리)
    private Timestamp createdAt;      // DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
    private Timestamp updatedAt;      // DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

    public StockDetailDTO() {
    }

    public StockDetailDTO(Long stockId, String companyName, BigDecimal faceValue, String settlementMonth,
                          String marketCap, Date listingDate, String foundedYear, Long totalShares,
                          Date sharesAsOf, String description) {
        this.stockId = stockId;
        this.companyName = companyName;
        this.faceValue = faceValue;
        this.settlementMonth = settlementMonth;
        this.marketCap = marketCap;
        this.listingDate = listingDate;
        this.foundedYear = foundedYear;
        this.totalShares = totalShares;
        this.sharesAsOf = sharesAsOf;
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

    // founded_year (설립년도)
    public String getFoundedYear() {
        return foundedYear;
    }

    public void setFoundedYear(String foundedYear) {
        this.foundedYear = foundedYear;
    }

    // total_shares (발행주식수)
    public Long getTotalShares() {
        return totalShares;
    }

    public void setTotalShares(Long totalShares) {
        this.totalShares = totalShares;
    }

    // shares_as_of (기준일)
    public Date getSharesAsOf() {
        return sharesAsOf;
    }

    public void setSharesAsOf(Date sharesAsOf) {
        this.sharesAsOf = sharesAsOf;
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