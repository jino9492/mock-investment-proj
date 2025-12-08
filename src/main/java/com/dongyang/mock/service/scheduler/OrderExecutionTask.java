package com.dongyang.mock.service.scheduler;

import com.dongyang.mock.model.DAO.OrderDAO;
import com.dongyang.mock.model.DTO.OrderDTO;
import com.dongyang.mock.service.OrderService;
import com.dongyang.mock.util.db.DBManager;

import java.util.List;

/**
 * 주문 체결 태스크
 * - PENDING 상태의 주문을 주기적으로 조회하여 체결 가능한 주문을 실행
 */
public class OrderExecutionTask implements Runnable {

    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderService orderService = new OrderService();

    @Override
    public synchronized void run() {
        try {
            System.out.println("[OrderExecutionTask] 주문 체결 처리 시작");
            
            // 모든 PENDING 주문 조회
            List<OrderDTO> pendingOrders = DBManager.withConnection(conn -> 
                orderDAO.findAllPendingOrders(conn)
            );
            
            if (pendingOrders.isEmpty()) {
                System.out.println("[OrderExecutionTask] 처리할 PENDING 주문이 없습니다.");
                return;
            }
            
            System.out.println("[OrderExecutionTask] " + pendingOrders.size() + "개의 PENDING 주문 발견");
            
            int executedCount = 0;
            int failedCount = 0;
            
            for (OrderDTO order : pendingOrders) {
                try {
                    System.out.println("[OrderExecutionTask] 주문 체결 시도:");
                    System.out.println("  - 주문 ID: " + order.getOrderId());
                    System.out.println("  - 종목 ID: " + order.getStockId());
                    System.out.println("  - 거래 방향: " + order.getSide());
                    System.out.println("  - 주문 유형: " + order.getOrderType());
                    System.out.println("  - 주문가격: " + order.getPrice());
                    System.out.println("  - 수량: " + order.getQuantity());
                    
                    // 주문 체결 시도
                    boolean success = orderService.executeOrder(order);
                    
                    if (success) {
                        executedCount++;
                        System.out.println("[OrderExecutionTask] 주문 체결 성공. 주문 ID: " + order.getOrderId());
                    } else {
                        // 체결 조건 미달성 (정상적인 경우)
                        System.out.println("[OrderExecutionTask] 주문 체결 조건 미달성. 주문 ID: " + order.getOrderId());
                    }
                } catch (Exception e) {
                    failedCount++;
                    System.err.println("[OrderExecutionTask] 주문 ID " + order.getOrderId() + " 체결 중 오류: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("[OrderExecutionTask] 처리 완료. 체결 성공: " + executedCount + 
                             ", 체결 실패: " + failedCount + 
                             ", 총 처리: " + pendingOrders.size());
            
        } catch (Exception e) {
            System.err.println("[OrderExecutionTask] 주문 체결 처리 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

