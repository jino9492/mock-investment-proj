package com.dongyang.mock.service;

import com.dongyang.mock.model.DAO.*;
import com.dongyang.mock.model.DTO.OrderDTO;
import com.dongyang.mock.model.DTO.OrderConditionDTO;
import com.dongyang.mock.model.DTO.RealtimeDTO;
import com.dongyang.mock.model.DTO.StockPriceDailyDTO;
import com.dongyang.mock.util.api.InmemoryCacheManager;
import com.dongyang.mock.util.db.DBManager;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.dongyang.mock.service.api.WebSocketService.REALTIME_CACHE_KEY;

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
                        InmemoryCacheManager inmemoryCacheManager = InmemoryCacheManager.getInstance();
                        // 시장가 주문 - 현재가로 계산
                        StockDAO stockDAO = new StockDAO();

                        BigDecimal currentPrice = new BigDecimal(inmemoryCacheManager.get(REALTIME_CACHE_KEY + stockDAO.getStockCodeById(order.getStockId(), conn), RealtimeDTO.class).getCurPrice());
                        // 현재가가 음수인 경우 절댓값 사용
                        if (currentPrice.compareTo(BigDecimal.ZERO) < 0) {
                            currentPrice = currentPrice.abs();
                        }
                        order.setPrice(currentPrice);
                        totalAmount = currentPrice.longValue() * order.getQuantity();
                        System.out.println("[OrderService] 시장가 주문 총 금액 계산: 현재가 " + currentPrice + " × 수량 " + order.getQuantity() + " = " + totalAmount);
                    }
                    
                    if (virtualCash < totalAmount) {
                        throw new RuntimeException(new IllegalArgumentException("예치금이 부족합니다. 필요: " + totalAmount + "원, 보유: " + virtualCash + "원"));
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
                    
                    // 대기 중인 판매 주문 수량 조회
                    long pendingSellQuantity = orderDAO.getPendingSellOrderQuantity(order.getMemId(), order.getStockId(), conn);
                    
                    // 거래 가능한 수량 = 보유 수량 - 대기 중인 판매 주문 수량
                    long availableQuantity = holding.getQuantity() - pendingSellQuantity;
                    
                    // 판매 수량이 거래 가능한 수량을 초과하는지 확인
                    if (order.getQuantity() > availableQuantity) {
                        throw new RuntimeException(new IllegalArgumentException(
                            "거래 가능한 수량(" + availableQuantity + "주)을 초과할 수 없습니다. " +
                            "(보유: " + holding.getQuantity() + "주, 대기 중인 판매 주문: " + pendingSellQuantity + "주)"));
                    }
                }
                
                // 주문 저장
                long orderId = orderDAO.insertOrder(order, conn);
                order.setOrderId(orderId);
                
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
                
                // 시장가 주문인 경우 즉시 체결 처리
                if ("MARKET".equals(order.getOrderType())) {
                    System.out.println("[OrderService] 시장가 주문 감지. 즉시 체결 처리 시작. 주문 ID: " + orderId);
                    try {
                        executeOrderInTransaction(order, conn);
                        System.out.println("[OrderService] 시장가 주문 즉시 체결 완료. 주문 ID: " + orderId);
                    } catch (Exception e) {
                        System.err.println("[OrderService] 시장가 주문 즉시 체결 실패. 주문 ID: " + orderId + ", 오류: " + e.getMessage());
                        e.printStackTrace();
                        // 체결 실패 시 주문은 PENDING 상태로 유지됨
                    }
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
     * 회원의 완료된 주문 목록 조회 (FILLED, CANCELLED)
     * @param memId 회원 ID
     * @return 완료된 주문 목록
     * @throws SQLException
     */
    public List<OrderDTO> getCompletedOrders(int memId) throws SQLException {
        return DBManager.withConnection(conn -> orderDAO.findCompletedOrdersByMember(memId, conn));
    }

    /**
     * 주문 ID로 조건 정보 조회
     * @param orderId 주문 ID
     * @return 조건 정보, 없으면 null
     * @throws SQLException
     */
    public OrderConditionDTO getOrderCondition(long orderId) throws SQLException {
        return DBManager.withConnection(conn -> orderConditionDAO.findOrderConditionByOrderId(orderId, conn));
    }

    /**
     * 주문 ID로 주문 조회
     * @param orderId 주문 ID
     * @return 주문 정보, 없으면 null
     * @throws SQLException
     */
    public OrderDTO findOrderById(long orderId) throws SQLException {
        return DBManager.withConnection(conn -> orderDAO.findOrderById(orderId, conn));
    }

    /**
     * 특정 종목에 대한 대기 중인 판매 주문 수량 조회
     * @param memId 회원 ID
     * @param stockId 종목 ID
     * @return 대기 중인 판매 주문 수량
     * @throws SQLException
     */
    public long getPendingSellOrderQuantity(int memId, long stockId) throws SQLException {
        return DBManager.withConnection(conn -> orderDAO.getPendingSellOrderQuantity(memId, stockId, conn));
    }

    /**
     * 주문 체결 실행 (트랜잭션 내에서 직접 호출)
     * @param order 체결할 주문
     * @param conn 데이터베이스 연결
     * @return 체결 성공 여부
     * @throws SQLException
     */
    private boolean executeOrderInTransaction(OrderDTO order, java.sql.Connection conn) throws SQLException {
        // 현재가 조회
        InmemoryCacheManager inmemoryCacheManager = InmemoryCacheManager.getInstance();
        // 시장가 주문 - 현재가로 계산
        StockDAO stockDAO = new StockDAO();

        try{

            BigDecimal currentPrice = new BigDecimal(inmemoryCacheManager.get(REALTIME_CACHE_KEY + stockDAO.getStockCodeById(order.getStockId(), conn), RealtimeDTO.class).getCurPrice());

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
                    return false;
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
                return false;
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
                    return false;
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
                    return false;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return true;
    }

    /**
     * 주문 체결 실행 (공개 메서드 - 트랜잭션 포함)
     * @param order 체결할 주문
     * @return 체결 성공 여부
     * @throws SQLException
     */
    public boolean executeOrder(OrderDTO order) throws SQLException {
        try {
            return DBManager.executeInTransaction(conn -> {
                boolean result = executeOrderInTransaction(order, conn);
                return result ? DBManager.TransactionResult.commit(true) : DBManager.TransactionResult.rollback(false);
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

    /**
     * 주문 취소
     * @param orderId 취소할 주문 ID
     * @param memId 주문을 소유한 회원 ID (권한 확인용)
     * @return 취소 성공 여부
     * @throws SQLException
     * @throws IllegalArgumentException 주문을 찾을 수 없거나 권한이 없을 때
     */
    public boolean cancelOrder(long orderId, int memId) throws SQLException, IllegalArgumentException {
        return DBManager.executeInTransaction(conn -> {
            // 주문 조회 및 권한 확인
            OrderDTO order = orderDAO.findOrderById(orderId, conn);
            if (order == null) {
                throw new RuntimeException(new IllegalArgumentException("주문을 찾을 수 없습니다."));
            }
            
            // 주문 소유자 확인
            if (order.getMemId() != memId) {
                throw new RuntimeException(new IllegalArgumentException("주문을 취소할 권한이 없습니다."));
            }
            
            // 이미 체결되었거나 취소된 주문은 취소할 수 없음
            if (!"PENDING".equals(order.getStatus())) {
                throw new RuntimeException(new IllegalArgumentException("대기 중인 주문만 취소할 수 있습니다."));
            }
            
            // 주문 상태를 CANCELLED로 변경
            int updated = orderDAO.updateOrderStatus(orderId, "CANCELLED", conn);
            
            if (updated > 0) {
                // 조건 주문인 경우 order_condition 테이블의 active를 false로 변경
                if (order.isHasCondition()) {
                    orderConditionDAO.deactivateCondition(orderId, conn);
                }
                
                return DBManager.TransactionResult.commit(true);
            } else {
                return DBManager.TransactionResult.rollback(false);
            }
        });
    }

    /**
     * 조건 주문 활성화/비활성화 토글
     * @param orderId 조건 주문 ID
     * @param memId 주문을 소유한 회원 ID (권한 확인용)
     * @param isActive 활성화 여부
     * @return 토글 성공 여부
     * @throws SQLException
     * @throws IllegalArgumentException 주문을 찾을 수 없거나 권한이 없을 때
     */
    public boolean toggleConditionalOrder(long orderId, int memId, boolean isActive) throws SQLException, IllegalArgumentException {
        return DBManager.executeInTransaction(conn -> {
            // 주문 조회 및 권한 확인
            OrderDTO order = orderDAO.findOrderById(orderId, conn);
            if (order == null) {
                throw new RuntimeException(new IllegalArgumentException("주문을 찾을 수 없습니다."));
            }

            // 주문 소유자 확인
            if (order.getMemId() != memId) {
                throw new RuntimeException(new IllegalArgumentException("주문을 수정할 권한이 없습니다."));
            }

            // 조건 주문인지 확인
            if (!order.isHasCondition()) {
                throw new RuntimeException(new IllegalArgumentException("조건 주문이 아닙니다."));
            }

            // 대기 중인 주문만 토글 가능
            if (!"PENDING".equals(order.getStatus())) {
                throw new RuntimeException(new IllegalArgumentException("대기 중인 주문만 활성화/비활성화할 수 있습니다."));
            }

            // 조건 주문 활성화 상태 변경
            orderConditionDAO.toggleCondition(orderId, isActive, conn);

            return DBManager.TransactionResult.commit(true);
        });
    }

    /**
     * 조건 주문 수정
     * @param orderId 조건 주문 ID
     * @param memId 주문을 소유한 회원 ID (권한 확인용)
     * @param watchPrice 감시 가격 (null이면 업데이트하지 않음)
     * @param price 주문 가격 (null이면 업데이트하지 않음)
     * @param quantity 주문 수량 (null이면 업데이트하지 않음)
     * @param isActive 활성화 여부 (null이면 업데이트하지 않음)
     * @param orderDuration 주문 유지 기간 (null이면 업데이트하지 않음)
     * @return 수정 성공 여부
     * @throws SQLException
     * @throws IllegalArgumentException 주문을 찾을 수 없거나 권한이 없을 때, 유효성 검증 실패 시
     */
    public boolean updateConditionalOrder(long orderId, int memId, BigDecimal watchPrice, BigDecimal price,
                                         Long quantity, Boolean isActive, String orderDuration)
                                         throws SQLException, IllegalArgumentException {
        return DBManager.executeInTransaction(conn -> {
            // 주문 조회 및 권한 확인
            OrderDTO order = orderDAO.findOrderById(orderId, conn);
            if (order == null) {
                throw new RuntimeException(new IllegalArgumentException("주문을 찾을 수 없습니다."));
            }

            // 주문 소유자 확인
            if (order.getMemId() != memId) {
                throw new RuntimeException(new IllegalArgumentException("주문을 수정할 권한이 없습니다."));
            }

            // 조건 주문인지 확인
            if (!order.isHasCondition()) {
                throw new RuntimeException(new IllegalArgumentException("조건 주문이 아닙니다."));
            }

            // 대기 중인 주문만 수정 가능
            if (!"PENDING".equals(order.getStatus())) {
                throw new RuntimeException(new IllegalArgumentException("대기 중인 주문만 수정할 수 있습니다."));
            }

            // 주문 정보 업데이트 (가격, 수량)
            if (price != null || quantity != null) {
                // 수량 검증 (판매 주문인 경우 보유 수량 확인)
                if (quantity != null && "SELL".equals(order.getSide())) {
                    com.dongyang.mock.model.DTO.MemberHoldingDTO holding =
                        memberDAO.findHoldingByStock(memId, order.getStockId(), conn);

                    if (holding == null) {
                        throw new RuntimeException(new IllegalArgumentException("보유하지 않은 종목입니다."));
                    }

                    long pendingSellQuantity = orderDAO.getPendingSellOrderQuantity(memId, order.getStockId(), conn);
                    long currentOrderQuantity = order.getQuantity();
                    long availableQuantity = holding.getQuantity() - (pendingSellQuantity - currentOrderQuantity);

                    if (quantity > availableQuantity) {
                        throw new RuntimeException(new IllegalArgumentException(
                            "거래 가능한 수량(" + availableQuantity + "주)을 초과할 수 없습니다."));
                    }
                }

                // 가격 검증
                if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException(new IllegalArgumentException("가격은 0보다 커야 합니다."));
                }

                // 수량 검증
                if (quantity != null && quantity <= 0) {
                    throw new RuntimeException(new IllegalArgumentException("수량은 1 이상이어야 합니다."));
                }

                orderDAO.updateOrder(orderId, price, quantity, conn);
            }

            // 조건 주문 정보 업데이트 (감시 가격, 유효 기간, 활성화 상태)
            Timestamp validUntil = null;
            if (orderDuration != null) {
                validUntil = calculateValidUntil(orderDuration);
            }

            if (watchPrice != null || validUntil != null || isActive != null) {
                if (watchPrice != null && watchPrice.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new RuntimeException(new IllegalArgumentException("감시 가격은 0보다 커야 합니다."));
                }

                orderConditionDAO.updateCondition(orderId, watchPrice, validUntil, conn);

                // 활성화 상태 업데이트
                if (isActive != null) {
                    OrderConditionDTO condition = orderConditionDAO.findOrderConditionByOrderId(orderId, conn);
                    if (condition != null && condition.isActive() != isActive) {
                        orderConditionDAO.toggleCondition(orderId, isActive, conn);
                    }
                }
            }

            return DBManager.TransactionResult.commit(true);
        });
    }

    /**
     * 일반 주문 수정
     * @param orderId 주문 ID
     * @param memId 주문을 소유한 회원 ID (권한 확인용)
     * @param orderType 주문 유형 (LIMIT/MARKET, null이면 업데이트하지 않음)
     * @param price 주문 가격 (null이면 업데이트하지 않음, MARKET 주문인 경우 무시됨)
     * @param quantity 주문 수량 (null이면 업데이트하지 않음)
     * @return 수정 성공 여부
     * @throws SQLException
     * @throws IllegalArgumentException 주문을 찾을 수 없거나 권한이 없을 때, 유효성 검증 실패 시
     */
    public boolean updateGeneralOrder(long orderId, int memId, String orderType, BigDecimal price, Long quantity)
            throws SQLException, IllegalArgumentException {
        return DBManager.executeInTransaction(conn -> {
            // 주문 조회 및 권한 확인
            OrderDTO order = orderDAO.findOrderById(orderId, conn);
            if (order == null) {
                throw new RuntimeException(new IllegalArgumentException("주문을 찾을 수 없습니다."));
            }

            // 주문 소유자 확인
            if (order.getMemId() != memId) {
                throw new RuntimeException(new IllegalArgumentException("주문을 수정할 권한이 없습니다."));
            }

            // 조건 주문이 아닌지 확인
            if (order.isHasCondition()) {
                throw new RuntimeException(new IllegalArgumentException("일반 주문만 수정할 수 있습니다."));
            }

            // 대기 중인 주문만 수정 가능
            if (!"PENDING".equals(order.getStatus())) {
                throw new RuntimeException(new IllegalArgumentException("대기 중인 주문만 수정할 수 있습니다."));
            }

            // 최종 주문 유형 결정
            String finalOrderType = orderType != null ? orderType : order.getOrderType();

            // 최종 가격 결정
            BigDecimal finalPrice = null;
            if ("LIMIT".equals(finalOrderType)) {
                // LIMIT 주문인 경우 가격 필요
                if (price != null) {
                    finalPrice = price;
                } else if (order.getPrice() == null) {
                    throw new RuntimeException(new IllegalArgumentException("지정가 주문은 가격이 필요합니다."));
                } else {
                    finalPrice = order.getPrice();
                }
            } else {
                // MARKET 주문인 경우 가격은 null
                finalPrice = null;
            }

            // 최종 수량 결정
            Long finalQuantity = quantity != null ? quantity : order.getQuantity();

            // 유효성 검증
            if (finalPrice != null && finalPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException(new IllegalArgumentException("가격은 0보다 커야 합니다."));
            }

            if (finalQuantity <= 0) {
                throw new RuntimeException(new IllegalArgumentException("수량은 1 이상이어야 합니다."));
            }

            // 구매 주문 검증
            if ("BUY".equals(order.getSide())) {
                int virtualCash = memberDAO.getVirtualCash(memId, conn);

                // 총 주문 금액 계산
                long totalAmount;
                if (finalPrice != null) {
                    // 지정가 주문
                    totalAmount = finalPrice.longValue() * finalQuantity;
                } else {
                    // 시장가 주문 - 현재가로 계산
                    StockPriceDailyDAO stockPriceDailyDAO = new StockPriceDailyDAO();
                    StockPriceDailyDTO priceDaily = stockPriceDailyDAO.selectLatestPriceDailyById(order.getStockId(), conn);
                    if (priceDaily == null || priceDaily.getClosePrice() == null) {
                        throw new RuntimeException(new IllegalArgumentException("시장가 주문을 위해 현재가를 조회할 수 없습니다."));
                    }
                    BigDecimal currentPrice = priceDaily.getClosePrice();
                    if (currentPrice.compareTo(BigDecimal.ZERO) < 0) {
                        currentPrice = currentPrice.abs();
                    }
                    totalAmount = currentPrice.longValue() * finalQuantity;
                }

                if (virtualCash < totalAmount) {
                    throw new RuntimeException(new IllegalArgumentException("예치금이 부족합니다. 필요: " + totalAmount + "원, 보유: " + virtualCash + "원"));
                }
            }

            // 판매 주문 검증
            if ("SELL".equals(order.getSide())) {
                // 보유 주식 수량 확인
                com.dongyang.mock.model.DTO.MemberHoldingDTO holding =
                    memberDAO.findHoldingByStock(memId, order.getStockId(), conn);

                if (holding == null) {
                    throw new RuntimeException(new IllegalArgumentException("보유하지 않은 종목입니다."));
                }

                // 대기 중인 판매 주문 수량 조회
                long pendingSellQuantity = orderDAO.getPendingSellOrderQuantity(memId, order.getStockId(), conn);

                // 현재 주문의 수량을 제외한 대기 중인 판매 주문 수량
                long otherPendingSellQuantity = pendingSellQuantity - order.getQuantity();

                // 거래 가능한 수량 = 보유 수량 - (다른 대기 중인 판매 주문 수량)
                long availableQuantity = holding.getQuantity() - otherPendingSellQuantity;

                // 수정된 수량이 거래 가능한 수량을 초과하는지 확인
                if (finalQuantity > availableQuantity) {
                    throw new RuntimeException(new IllegalArgumentException(
                        "거래 가능한 수량(" + availableQuantity + "주)을 초과할 수 없습니다. " +
                        "(보유: " + holding.getQuantity() + "주, 다른 대기 중인 판매 주문: " + otherPendingSellQuantity + "주)"));
                }
            }

            // 주문 정보 업데이트
            int updated = orderDAO.updateGeneralOrder(orderId, orderType, finalPrice, finalQuantity, conn);

            if (updated > 0) {
                return DBManager.TransactionResult.commit(true);
            } else {
                return DBManager.TransactionResult.rollback(false);
            }
        });
    }

    /**
     * 만료된 조건 주문 자동 삭제
     * 유효 기간이 지난 조건 주문을 찾아서 취소 처리
     * @return 삭제된 조건 주문 수
     * @throws SQLException
     */
    public int cleanupExpiredConditionalOrders() throws SQLException {
        return DBManager.executeInTransaction(conn -> {
            // 만료된 조건 주문 ID 목록 조회
            List<Long> expiredOrderIds = orderConditionDAO.findExpiredConditionalOrderIds(conn);

            if (expiredOrderIds.isEmpty()) {
                return DBManager.TransactionResult.commit(0);
            }

            System.out.println("[OrderService] 만료된 조건 주문 " + expiredOrderIds.size() + "개 발견");

            int deletedCount = 0;

            for (Long orderId : expiredOrderIds) {
                try {
                    // 주문 조회
                    OrderDTO order = orderDAO.findOrderById(orderId, conn);
                    if (order == null) {
                        System.out.println("[OrderService] 만료된 조건 주문 ID " + orderId + "를 찾을 수 없습니다.");
                        continue;
                    }

                    // 대기 중인 주문만 취소 가능
                    if (!"PENDING".equals(order.getStatus())) {
                        System.out.println("[OrderService] 만료된 조건 주문 ID " + orderId + "는 이미 처리되었습니다. 상태: " + order.getStatus());
                        // 상태와 관계없이 조건 주문은 비활성화
                        orderConditionDAO.deactivateCondition(orderId, conn);
                        continue;
                    }

                    // 주문 상태를 CANCELLED로 변경
                    int updated = orderDAO.updateOrderStatus(orderId, "CANCELLED", conn);

                    if (updated > 0) {
                        // 조건 주문 비활성화
                        orderConditionDAO.deactivateCondition(orderId, conn);
                        deletedCount++;
                        System.out.println("[OrderService] 만료된 조건 주문 ID " + orderId + " 삭제 완료");
                    }
                } catch (Exception e) {
                    System.err.println("[OrderService] 만료된 조건 주문 ID " + orderId + " 삭제 중 오류: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("[OrderService] 만료된 조건 주문 정리 완료. 삭제된 주문: " + deletedCount + "개");
            return DBManager.TransactionResult.commit(deletedCount);
        });
    }
}

