package com.dongyang.mock.service;

import com.dongyang.mock.model.DAO.*;
import com.dongyang.mock.model.DTO.OrderDTO;
import com.dongyang.mock.model.DTO.OrderConditionDTO;
import com.dongyang.mock.model.DTO.StockPriceDailyDTO;
import com.dongyang.mock.service.api.StockDetailService;
import com.dongyang.mock.util.db.DBManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 주문 관련 비즈니스 로직 서비스
 * - 주문 생성 및 검증
 * - 대기 주문 조회
 */
public class OrderService {
    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderConditionDAO orderConditionDAO = new OrderConditionDAO();
    private final MemberDAO memberDAO = new MemberDAO();
    private final TradeDAO tradeDAO = new TradeDAO();
    private final HoldingDAO holdingDAO = new HoldingDAO();
    private final CashHistoryDAO cashHistoryDAO = new CashHistoryDAO();
    private final StockPriceDailyDAO stockPriceDailyDAO = new StockPriceDailyDAO();
    private final StockDetailService stockDetailService = new StockDetailService();

    /**
     * 주문 생성
     * @param order 주문 정보
     * @param orderDuration 조건 주문 유지 기간 (1week, 1month, 1year, indefinite) - 조건 주문일 경우만 사용
     * @return 생성된 주문 ID
     * @throws SQLException
     * @throws IllegalArgumentException 예치금 부족 등 유효성 검증 실패 시
     */
    public long createOrder(OrderDTO order, String orderDuration) throws SQLException, IllegalArgumentException {
        return createOrder(order, orderDuration, null);
    }

    /**
     * 주문 생성 (오버로드 - 호환성 유지)
     */
    public long createOrder(OrderDTO order) throws SQLException, IllegalArgumentException {
        return createOrder(order, null, null);
    }

    /**
     * 주문 생성 (내부 메서드)
     */
    private long createOrder(OrderDTO order, String orderDuration, Timestamp validUntil) throws SQLException, IllegalArgumentException {
        // 유효 기간 계산 (람다 표현식 외부에서 처리)
        final Timestamp finalValidUntil;
        if (validUntil == null && orderDuration != null) {
            finalValidUntil = calculateValidUntil(orderDuration);
        } else {
            finalValidUntil = validUntil;
        }
        
        try {
            return DBManager.executeInTransaction(conn -> {
                // 구매 주문 검증
                if ("BUY".equals(order.getSide())) {
                    int virtualCash = memberDAO.getVirtualCash(order.getMemId(), conn);
                    
                    // 총 주문 금액 계산
                    long totalAmount;
                    if (order.getPrice() != null) {
                        // 지정가 주문
                        totalAmount = order.getPrice().longValue() * order.getQuantity();
                    } else {
                        // 시장가 주문 - 현재가로 계산 (임시로 최대 예치금으로 제한)
                        // 실제로는 현재가를 가져와서 계산해야 함
                        throw new RuntimeException(new IllegalArgumentException("시장가 주문은 아직 지원하지 않습니다."));
                    }
                    
                    if (virtualCash < totalAmount) {
                        throw new RuntimeException(new IllegalArgumentException("예치금이 부족합니다."));
                    }
                }
                
                // 판매 주문 검증
                if ("SELL".equals(order.getSide())) {
                    // 보유 주식 수량 확인
                    com.dongyang.mock.model.DTO.MemberHoldingDTO holding = 
                        memberDAO.findHoldingByStock(order.getMemId(), order.getStockId(), conn);
                    
                    if (holding == null) {
                        throw new RuntimeException(new IllegalArgumentException("보유하지 않은 종목입니다."));
                    }
                    
                    // 판매 수량이 보유 수량을 초과하는지 확인
                    if (order.getQuantity() > holding.getQuantity()) {
                        throw new RuntimeException(new IllegalArgumentException(
                            "보유 수량(" + holding.getQuantity() + "주)을 초과할 수 없습니다."));
                    }
                }
                
                // 주문 저장
                long orderId = orderDAO.insertOrder(order, conn);
                
                // 조건 주문인 경우 order_condition 테이블에 저장
                if (order.isHasCondition() && order.getWatchPrice() != null) {
                    OrderConditionDTO condition = new OrderConditionDTO();
                    condition.setOrderId(orderId);
                    
                    // 조건 타입 결정: 구매는 PRICE_DOWN (현재가가 감시가격 이하로 내려가면), 판매는 PRICE_UP (현재가가 감시가격 이상으로 올라가면)
                    if ("BUY".equals(order.getSide())) {
                        condition.setConditionType("PRICE_DOWN");
                    } else {
                        condition.setConditionType("PRICE_UP");
                    }
                    
                    condition.setTriggerPrice(order.getWatchPrice());
                    condition.setActive(true);
                    condition.setValidUntil(finalValidUntil);
                    
                    orderConditionDAO.insertOrderCondition(condition, conn);
                }
                
                return DBManager.TransactionResult.commit(orderId);
            });
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e.getCause();
            }
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    /**
     * 회원의 대기 주문 목록 조회
     * @param memId 회원 ID
     * @return 대기 주문 목록
     * @throws SQLException
     */
    public List<OrderDTO> getPendingOrders(int memId) throws SQLException {
        return DBManager.withConnection(conn -> orderDAO.findPendingOrdersByMember(memId, conn));
    }

    /**
     * 주문 체결 실행
     * @param order 체결할 주문
     * @return 체결 성공 여부
     * @throws SQLException
     */
    public boolean executeOrder(OrderDTO order) throws SQLException {
        try {
            return DBManager.executeInTransaction(conn -> {
                // 현재가 조회
                StockPriceDailyDTO priceDaily = stockPriceDailyDAO.selectLatestPriceDailyById(order.getStockId(), conn);
                
                if (priceDaily == null || priceDaily.getClosePrice() == null) {
                    System.out.println("[OrderService] 종목 ID " + order.getStockId() + "의 현재가를 조회할 수 없습니다.");
                    return DBManager.TransactionResult.rollback(false);
                }
                
                BigDecimal currentPrice = priceDaily.getClosePrice();
                
                // 현재가가 음수인 경우 절댓값 사용 (데이터 오류 방지)
                if (currentPrice.compareTo(BigDecimal.ZERO) < 0) {
                    System.err.println("[OrderService] 경고: 종목 ID " + order.getStockId() + "의 현재가가 음수입니다: " + currentPrice + ". 절댓값을 사용합니다.");
                    currentPrice = currentPrice.abs();
                }
                BigDecimal orderPrice = order.getPrice();
                boolean canExecute = false;
                BigDecimal executionPrice = null;
                
                System.out.println("[OrderService] 주문 체결 조건 확인:");
                System.out.println("  - 주문 ID: " + order.getOrderId());
                System.out.println("  - 종목 ID: " + order.getStockId());
                System.out.println("  - 주문 유형: " + order.getOrderType() + ", 거래 방향: " + order.getSide());
                System.out.println("  - 현재가: " + currentPrice);
                System.out.println("  - 주문가격: " + orderPrice);
                
                // 체결 조건 확인
                if ("MARKET".equals(order.getOrderType())) {
                    // 시장가: 즉시 체결
                    canExecute = true;
                    executionPrice = currentPrice;
                    System.out.println("[OrderService] 시장가 주문: 즉시 체결 가능");
                } else if ("LIMIT".equals(order.getOrderType())) {
                    // 지정가: 가격 조건 확인
                    if (orderPrice == null) {
                        System.out.println("[OrderService] 지정가 주문인데 가격이 없습니다. 주문 ID: " + order.getOrderId());
                        return DBManager.TransactionResult.rollback(false);
                    }
                    
                    if ("BUY".equals(order.getSide())) {
                        // 매수 지정가: 현재가 <= 주문가격이면 체결
                        canExecute = currentPrice.compareTo(orderPrice) <= 0;
                        System.out.println("[OrderService] 매수 지정가 체결 조건: " + currentPrice + " <= " + orderPrice + " = " + canExecute);
                    } else if ("SELL".equals(order.getSide())) {
                        // 매도 지정가: 현재가 >= 주문가격이면 체결
                        canExecute = currentPrice.compareTo(orderPrice) >= 0;
                        System.out.println("[OrderService] 매도 지정가 체결 조건: " + currentPrice + " >= " + orderPrice + " = " + canExecute);
                    }
                    executionPrice = orderPrice; // 지정가는 주문가격으로 체결
                }
                
                if (!canExecute) {
                    // 체결 조건 미달성
                    System.out.println("[OrderService] 체결 조건 미달성으로 주문을 체결할 수 없습니다. 주문 ID: " + order.getOrderId());
                    return DBManager.TransactionResult.rollback(false);
                }
                
                System.out.println("[OrderService] 체결 조건 달성! 체결가: " + executionPrice);
                
                // 체결 금액 계산
                BigDecimal totalAmount = executionPrice.multiply(BigDecimal.valueOf(order.getQuantity()));
                System.out.println("[OrderService] 체결 금액 계산: " + executionPrice + " * " + order.getQuantity() + " = " + totalAmount);
                
                // 매수 주문 체결
                if ("BUY".equals(order.getSide())) {
                    // 예치금 확인
                    System.out.println("[OrderService] 예치금 확인 중...");
                    int virtualCash = memberDAO.getVirtualCash(order.getMemId(), conn);
                    System.out.println("[OrderService] 현재 예치금: " + virtualCash + ", 필요 금액: " + totalAmount.intValue());
                    
                    if (virtualCash < totalAmount.intValue()) {
                        System.out.println("[OrderService] 예치금이 부족합니다. 주문 ID: " + order.getOrderId());
                        return DBManager.TransactionResult.rollback(false);
                    }
                    
                    // trade 테이블에 체결 내역 저장
                    System.out.println("[OrderService] trade 테이블에 체결 내역 저장 중...");
                    tradeDAO.insertTrade(order.getMemId(), order.getStockId(), order.getOrderId(),
                                       executionPrice, order.getQuantity(), conn);
                    System.out.println("[OrderService] trade 테이블 저장 완료");
                    
                    // holding 테이블 업데이트 (매수)
                    System.out.println("[OrderService] holding 테이블 업데이트 중...");
                    holdingDAO.insertOrUpdateHolding(order.getMemId(), order.getStockId(),
                                                    order.getQuantity(), executionPrice, conn);
                    System.out.println("[OrderService] holding 테이블 업데이트 완료");
                    
                    // cash_history 테이블에 기록 저장 (매수는 음수)
                    System.out.println("[OrderService] 예치금 차감 중...");
                    int newBalance = memberDAO.updateVirtualCash(order.getMemId(), -totalAmount.intValue(), conn);
                    System.out.println("[OrderService] 예치금 차감 완료. 새 잔액: " + newBalance);
                    
                    System.out.println("[OrderService] cash_history 테이블에 기록 저장 중...");
                    try {
                        cashHistoryDAO.insertCashHistory(order.getMemId(), "BUY",
                                                        totalAmount.negate(),
                                                        BigDecimal.valueOf(newBalance), conn);
                        System.out.println("[OrderService] cash_history 테이블 저장 완료");
                    } catch (SQLException e) {
                        System.err.println("[OrderService] cash_history 저장 실패: " + e.getMessage());
                        throw e;
                    }
                    
                    // 주문 상태를 FILLED로 변경
                    System.out.println("[OrderService] 주문 상태를 FILLED로 변경 중...");
                    orderDAO.updateOrderStatus(order.getOrderId(), "FILLED", conn);
                    System.out.println("[OrderService] 주문 상태 변경 완료");
                    
                    System.out.println("[OrderService] 매수 주문 체결 완료. 주문 ID: " + order.getOrderId() + 
                                     ", 체결가: " + executionPrice + ", 수량: " + order.getQuantity());
                    
                } else if ("SELL".equals(order.getSide())) {
                    // 보유 수량 확인
                    HoldingDAO.HoldingInfo holding = holdingDAO.findHolding(order.getMemId(), order.getStockId(), conn);
                    if (holding == null || holding.quantity < order.getQuantity()) {
                        System.out.println("[OrderService] 보유 수량이 부족합니다. 주문 ID: " + order.getOrderId());
                        return DBManager.TransactionResult.rollback(false);
                    }
                    
                    // trade 테이블에 체결 내역 저장
                    tradeDAO.insertTrade(order.getMemId(), order.getStockId(), order.getOrderId(),
                                       executionPrice, order.getQuantity(), conn);
                    
                    // holding 테이블 업데이트 (매도)
                    holdingDAO.decreaseHolding(order.getMemId(), order.getStockId(),
                                              order.getQuantity(), conn);
                    
                    // cash_history 테이블에 기록 저장 (매도는 양수)
                    int newBalance = memberDAO.updateVirtualCash(order.getMemId(), totalAmount.intValue(), conn);
                    cashHistoryDAO.insertCashHistory(order.getMemId(), "SELL",
                                                    totalAmount,
                                                    BigDecimal.valueOf(newBalance), conn);
                    
                    // 주문 상태를 FILLED로 변경
                    orderDAO.updateOrderStatus(order.getOrderId(), "FILLED", conn);
                    
                    System.out.println("[OrderService] 매도 주문 체결 완료. 주문 ID: " + order.getOrderId() + 
                                     ", 체결가: " + executionPrice + ", 수량: " + order.getQuantity());
                }
                
                return DBManager.TransactionResult.commit(true);
            });
        } catch (Exception e) {
            System.err.println("[OrderService] 주문 체결 중 오류 발생. 주문 ID: " + order.getOrderId() + 
                             ", 오류: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("주문 체결 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 조건 주문 유지 기간에 따른 유효 기간 계산
     * @param orderDuration 주문 유지 기간 (1week, 1month, 1year, indefinite)
     * @return 유효 기간 종료 시간 (indefinite인 경우 null)
     */
    private Timestamp calculateValidUntil(String orderDuration) {
        if (orderDuration == null || "indefinite".equals(orderDuration)) {
            return null;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime validUntil;
        
        switch (orderDuration) {
            case "1week":
                validUntil = now.plus(1, ChronoUnit.WEEKS);
                break;
            case "1month":
                validUntil = now.plus(1, ChronoUnit.MONTHS);
                break;
            case "1year":
                validUntil = now.plus(1, ChronoUnit.YEARS);
                break;
            default:
                // 기본값: 1개월
                validUntil = now.plus(1, ChronoUnit.MONTHS);
                break;
        }
        
        return Timestamp.valueOf(validUntil);
    }
}

