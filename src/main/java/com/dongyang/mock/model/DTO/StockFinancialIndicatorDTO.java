package com.dongyang.mock.model.DTO;

import java.sql.Date;
import java.sql.Timestamp;
import java.math.BigDecimal;

public class StockFinancialIndicatorDTO {

    // 필드 (테이블 컬럼에 대응)
    private Long stockId;             // BIGINT NOT NULL (복합 PK & FK)
    private Date indicatorDate;       // DATE NOT NULL (복합 PK)

    private BigDecimal per;           // DECIMAL(10,2) NULL (주가수익비율)
    private BigDecimal pbr;           // DECIMAL(10,2) NULL (주가순자산비율)
    private BigDecimal roe;           // DECIMAL(10,2) NULL (자기자본이익률)
    private BigDecimal eps;           // DECIMAL(18,2) NULL (주당순이익)
    private BigDecimal bps;           // DECIMAL(18,2) NULL (주당순자산)

    private Timestamp createdAt;      // DATETIME NOT NULL
    private Timestamp updatedAt;      // DATETIME NOT NULL

    // 1. 기본 생성자
    public StockFinancialIndicatorDTO() {
    }

    // 2. 주요 필드를 포함하는 생성자 (선택 사항)
    public StockFinancialIndicatorDTO(Long stockId, Date indicatorDate, BigDecimal per, BigDecimal pbr,
                                      BigDecimal roe, BigDecimal eps, BigDecimal bps) {
        this.stockId = stockId;
        this.indicatorDate = indicatorDate;
        this.per = per;
        this.pbr = pbr;
        this.roe = roe;
        this.eps = eps;
        this.bps = bps;
    }

    // 3. Getter 및 Setter 메서드

    // stock_id
    public Long getStockId() {
        return stockId;
    }

    public void setStockId(Long stockId) {
        this.stockId = stockId;
    }

    // indicator_date
    public Date getIndicatorDate() {
        return indicatorDate;
    }

    public void setIndicatorDate(Date indicatorDate) {
        this.indicatorDate = indicatorDate;
    }

    // per
    public BigDecimal getPer() {
        return per;
    }

    public void setPer(BigDecimal per) {
        this.per = per;
    }

    // pbr
    public BigDecimal getPbr() {
        return pbr;
    }

    public void setPbr(BigDecimal pbr) {
        this.pbr = pbr;
    }

    // roe
    public BigDecimal getRoe() {
        return roe;
    }

    public void setRoe(BigDecimal roe) {
        this.roe = roe;
    }

    // eps
    public BigDecimal getEps() {
        return eps;
    }

    public void setEps(BigDecimal eps) {
        this.eps = eps;
    }

    // bps
    public BigDecimal getBps() {
        return bps;
    }

    public void setBps(BigDecimal bps) {
        this.bps = bps;
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