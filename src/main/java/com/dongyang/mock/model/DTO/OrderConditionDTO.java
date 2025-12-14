package com.dongyang.mock.model.DTO;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 주문 조건 정보를 담는 DTO
 */
public class OrderConditionDTO {
    private long orderId;
    private String conditionType; // 'PRICE_UP', 'PRICE_DOWN', 'STOP_LOSS', 'TAKE_PROFIT'
    private BigDecimal triggerPrice; // 감시 가격
    private Timestamp validUntil; // 유효 기간
    private boolean isActive; // 활성 상태
    private Timestamp createdAt;

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public BigDecimal getTriggerPrice() {
        return triggerPrice;
    }

    public void setTriggerPrice(BigDecimal triggerPrice) {
        this.triggerPrice = triggerPrice;
    }

    public Timestamp getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Timestamp validUntil) {
        this.validUntil = validUntil;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}





