package com.dongyang.mock.model.DTO;

import java.time.LocalDateTime;

public class StockCodeDTO {
    private Long stockCodeId;
    private String code;
    private String codeName;
    private String description;
    private LocalDateTime createdAt;

    // 기본 생성자 (필수)
    public StockCodeDTO() {
    }

    // 생성자 (ID 제외)
    public StockCodeDTO(String code, String codeName, String description) {
        this.code = code;
        this.codeName = codeName;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    // --- Getter 및 Setter ---

    public Long getStockCodeId() {
        return stockCodeId;
    }

    public void setStockCodeId(Long stockCodeId) {
        this.stockCodeId = stockCodeId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeName() {
        return codeName;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "StockCode{" +
                "stockCodeId=" + stockCodeId +
                ", code='" + code + '\'' +
                ", codeName='" + codeName + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}