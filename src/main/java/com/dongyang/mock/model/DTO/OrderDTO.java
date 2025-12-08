package com.dongyang.mock.model.DTO;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 주문 정보를 담는 DTO
 */
public class OrderDTO {
    private long orderId;
    private int memId;
    private long stockId;
    private String side; // 'BUY' or 'SELL'
    private String orderType; // 'MARKET' or 'LIMIT'
    private BigDecimal price; // 지정가일 경우만, 시장가일 경우 null
    private long quantity;
    private String status; // 'PENDING', 'PARTIAL', 'FILLED', 'CANCELLED'
    private Timestamp placedAt;
    private Timestamp cancelledAt;
    private boolean hasCondition;
    
    // 조건 주문용 필드
    private BigDecimal watchPrice; // 감시 가격 (조건 주문용)
    
    // JSP에서 사용할 필드
    private String tradeType; // 'buy' or 'sell' (JSP용)
    private String stockName; // 종목명 (JSP용)

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public int getMemId() {
        return memId;
    }

    public void setMemId(int memId) {
        this.memId = memId;
    }

    public long getStockId() {
        return stockId;
    }

    public void setStockId(long stockId) {
        this.stockId = stockId;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getPlacedAt() {
        return placedAt;
    }

    public void setPlacedAt(Timestamp placedAt) {
        this.placedAt = placedAt;
    }

    public Timestamp getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(Timestamp cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public boolean isHasCondition() {
        return hasCondition;
    }

    public void setHasCondition(boolean hasCondition) {
        this.hasCondition = hasCondition;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }

    public BigDecimal getWatchPrice() {
        return watchPrice;
    }

    public void setWatchPrice(BigDecimal watchPrice) {
        this.watchPrice = watchPrice;
    }
}

