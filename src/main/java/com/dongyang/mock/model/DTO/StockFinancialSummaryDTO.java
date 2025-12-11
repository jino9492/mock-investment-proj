package com.dongyang.mock.model.DTO;

import java.sql.Timestamp;
import java.math.BigDecimal;

public class StockFinancialSummaryDTO {

    // 필드 (테이블 컬럼에 대응)
    private Long stockId;             // BIGINT NOT NULL (Primary Key & Foreign Key)
    private Long revenue;             // BIGINT NULL (매출액)
    private Long operatingProfit;     // BIGINT NULL (영업이익)
    private BigDecimal creditRatio;   // DECIMAL(5,2) NULL (신용 비율)

    private Timestamp createdAt;      // DATETIME NOT NULL
    private Timestamp updatedAt;      // DATETIME NOT NULL

    // 1. 기본 생성자
    public StockFinancialSummaryDTO() {
    }

    // 2. 주요 필드를 포함하는 생성자 (선택 사항)
    public StockFinancialSummaryDTO(Long stockId, Long revenue, Long operatingProfit, BigDecimal creditRatio) {
        this.stockId = stockId;
        this.revenue = revenue;
        this.operatingProfit = operatingProfit;
        this.creditRatio = creditRatio;
    }

    // 3. Getter 및 Setter 메서드

    // stock_id
    public Long getStockId() {
        return stockId;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    // revenue
    public Long getRevenue() {
        return revenue;
    }

    public void setRevenue(Long revenue) {
        this.revenue = revenue;
    }

    // operating_profit
    public Long getOperatingProfit() {
        return operatingProfit;
    }

    public void setOperatingProfit(Long operatingProfit) {
        this.operatingProfit = operatingProfit;
    }

    // credit_ratio
    public BigDecimal getCreditRatio() {
        return creditRatio;
    }

    public void setCreditRatio(BigDecimal creditRatio) {
        this.creditRatio = creditRatio;
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