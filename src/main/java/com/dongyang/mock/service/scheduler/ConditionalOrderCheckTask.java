package com.dongyang.mock.service.scheduler;

import com.dongyang.mock.model.DAO.OrderConditionDAO;
import com.dongyang.mock.model.DAO.OrderDAO;
import com.dongyang.mock.model.DTO.OrderDTO;
import com.dongyang.mock.model.DTO.StockPriceDailyDTO;
import com.dongyang.mock.service.OrderService;
import com.dongyang.mock.service.api.StockDetailService;
import com.dongyang.mock.util.db.DBManager;

import java.math.BigDecimal;
import java.util.List;

/**
 * 조건 주문 체크 및 자동 주문 생성 태스크
 * - 활성화된 조건 주문을 주기적으로 체크
 * - 조건 달성 시 자동으로 일반 주문 생성
 */
public class ConditionalOrderCheckTask implements Runnable {

    private final OrderConditionDAO orderConditionDAO = new OrderConditionDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderService orderService = new OrderService();
    private final StockDetailService stockDetailService = new StockDetailService();

    @Override
    public synchronized void run() {
        try {
            System.out.println("[ConditionalOrderCheckTask] 조건 주문 체크 시작");
            
            // 만료된 조건 주문 정리
            try {
                int deletedCount = orderService.cleanupExpiredConditionalOrders();
                if (deletedCount > 0) {
                    System.out.println("[ConditionalOrderCheckTask] 만료된 조건 주문 " + deletedCount + "개 정리 완료");
                }
            } catch (Exception e) {
                System.err.println("[ConditionalOrderCheckTask] 만료된 조건 주문 정리 중 오류: " + e.getMessage());
                e.printStackTrace();
            }
            
            // 활성화된 조건 주문 조회
            List<OrderDTO> conditionalOrders = DBManager.withConnection(conn -> 
                orderConditionDAO.findActiveConditionalOrders(conn)
            );
            
            if (conditionalOrders.isEmpty()) {
                System.out.println("[ConditionalOrderCheckTask] 처리할 조건 주문이 없습니다.");
                return;
            }
            
            System.out.println("[ConditionalOrderCheckTask] " + conditionalOrders.size() + "개의 조건 주문 발견");
            
            int processedCount = 0;
            int createdCount = 0;
            
            for (OrderDTO conditionalOrder : conditionalOrders) {
                try {
                    // 종목의 현재가 조회
                    StockPriceDailyDTO priceDaily = stockDetailService.getStockPriceDailyById(conditionalOrder.getStockId());
                    
                    if (priceDaily == null || priceDaily.getClosePrice() == null) {
                        System.out.println("[ConditionalOrderCheckTask] 종목 ID " + conditionalOrder.getStockId() + "의 현재가를 조회할 수 없습니다.");
                        continue;
                    }
                    
                    BigDecimal currentPrice = priceDaily.getClosePrice();
                    BigDecimal watchPrice = conditionalOrder.getWatchPrice();
                    
                    if (watchPrice == null) {
                        System.out.println("[ConditionalOrderCheckTask] 주문 ID " + conditionalOrder.getOrderId() + "의 감시 가격이 없습니다.");
                        continue;
                    }
                    
                    // 조건 달성 여부 확인
                    boolean conditionMet = false;
                    
                    if ("BUY".equals(conditionalOrder.getSide())) {
                        // 구매 조건: 현재가가 감시가격 이하로 내려가면
                        conditionMet = currentPrice.compareTo(watchPrice) <= 0;
                    } else if ("SELL".equals(conditionalOrder.getSide())) {
                        // 판매 조건: 현재가가 감시가격 이상으로 올라가면
                        conditionMet = currentPrice.compareTo(watchPrice) >= 0;
                    }
                    
                    if (conditionMet) {
                        System.out.println("[ConditionalOrderCheckTask] 조건 달성! 주문 ID: " + conditionalOrder.getOrderId() + 
                                         ", 종목 ID: " + conditionalOrder.getStockId() + 
                                         ", 현재가: " + currentPrice + 
                                         ", 감시가격: " + watchPrice);
                        
                        try {
                            // 일반 주문 생성 (조건 없음)
                            OrderDTO regularOrder = new OrderDTO();
                            regularOrder.setMemId(conditionalOrder.getMemId());
                            regularOrder.setStockId(conditionalOrder.getStockId());
                            regularOrder.setSide(conditionalOrder.getSide());
                            regularOrder.setOrderType(conditionalOrder.getOrderType());
                            regularOrder.setPrice(conditionalOrder.getPrice());
                            regularOrder.setQuantity(conditionalOrder.getQuantity());
                            regularOrder.setStatus("PENDING");
                            regularOrder.setHasCondition(false); // 일반 주문으로 생성
                            
                            // 주문 생성
                            long newOrderId = orderService.createOrder(regularOrder);
                            createdCount++;
                            
                            System.out.println("[ConditionalOrderCheckTask] 일반 주문 생성 완료. 새 주문 ID: " + newOrderId);
                            
                            // 조건 주문 비활성화 및 취소 처리
                            DBManager.executeInTransaction(conn -> {
                                // 조건 주문 비활성화
                                orderConditionDAO.deactivateCondition(conditionalOrder.getOrderId(), conn);
                                
                                // 조건 주문 상태를 CANCELLED로 변경 (일반 주문이 생성되었으므로)
                                orderDAO.updateOrderStatus(conditionalOrder.getOrderId(), "CANCELLED", conn);
                                
                                return DBManager.TransactionResult.commit(null);
                            });
                            
                            System.out.println("[ConditionalOrderCheckTask] 조건 주문 비활성화 및 취소 완료. 주문 ID: " + conditionalOrder.getOrderId());
                        } catch (Exception e) {
                            System.err.println("[ConditionalOrderCheckTask] 조건 달성 후 주문 생성 중 오류: " + e.getMessage());
                            e.printStackTrace();
                            // 오류가 발생해도 다음 주문 처리 계속
                        }
                    }
                    
                    processedCount++;
                    
                } catch (Exception e) {
                    System.err.println("[ConditionalOrderCheckTask] 주문 ID " + conditionalOrder.getOrderId() + " 처리 중 오류: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("[ConditionalOrderCheckTask] 처리 완료. 처리된 주문: " + processedCount + ", 생성된 주문: " + createdCount);
            
        } catch (Exception e) {
            System.err.println("[ConditionalOrderCheckTask] 조건 주문 체크 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

