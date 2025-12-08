package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.OrderConditionDTO;
import com.dongyang.mock.model.DTO.OrderDTO;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.dongyang.mock.util.db.DBManager.close;

/**
 * 주문 조건 데이터 접근 객체 (DAO)
 * - 주문 조건 저장 및 조회
 */
public class OrderConditionDAO {

    /**
     * 주문 조건 저장
     * @param condition 주문 조건 정보
     * @param conn DB 연결
     * @throws SQLException
     */
    public void insertOrderCondition(OrderConditionDTO condition, Connection conn) throws SQLException {
        String sql = "INSERT INTO order_condition (order_id, condition_type, trigger_price, valid_until, is_active) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        PreparedStatement pstmt = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, condition.getOrderId());
            pstmt.setString(2, condition.getConditionType());
            pstmt.setBigDecimal(3, condition.getTriggerPrice());
            
            if (condition.getValidUntil() != null) {
                pstmt.setTimestamp(4, condition.getValidUntil());
            } else {
                pstmt.setNull(4, Types.TIMESTAMP);
            }
            
            pstmt.setBoolean(5, condition.isActive());
            
            pstmt.executeUpdate();
        } finally {
            close(null, pstmt);
        }
    }

    /**
     * 활성화된 조건 주문 목록 조회 (조건 체크용)
     * @param conn DB 연결
     * @return 조건 주문 목록 (주문 정보 포함)
     * @throws SQLException
     */
    public List<OrderDTO> findActiveConditionalOrders(Connection conn) throws SQLException {
        String sql = "SELECT o.order_id, o.mem_id, o.stock_id, o.side, o.order_type, o.price, " +
                     "o.quantity, o.status, o.placed_at, o.cancelled_at, o.has_condition, " +
                     "oc.condition_type, oc.trigger_price, oc.valid_until, oc.is_active " +
                     "FROM trade_order o " +
                     "INNER JOIN order_condition oc ON o.order_id = oc.order_id " +
                     "WHERE o.has_condition = 1 AND oc.is_active = 1 AND o.status = 'PENDING' " +
                     "AND (oc.valid_until IS NULL OR oc.valid_until >= NOW())";
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<OrderDTO> orders = new ArrayList<>();
        
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                OrderDTO order = mapToOrderWithCondition(rs);
                orders.add(order);
            }
        } finally {
            close(null, pstmt, rs);
        }
        
        return orders;
    }

    /**
     * 조건 주문 비활성화 (조건 달성 후 처리)
     * @param orderId 주문 ID
     * @param conn DB 연결
     * @throws SQLException
     */
    public void deactivateCondition(long orderId, Connection conn) throws SQLException {
        String sql = "UPDATE order_condition SET is_active = 0 WHERE order_id = ?";
        
        PreparedStatement pstmt = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, orderId);
            pstmt.executeUpdate();
        } finally {
            close(null, pstmt);
        }
    }

    /**
     * ResultSet을 OrderDTO로 변환 (조건 정보 포함)
     */
    private OrderDTO mapToOrderWithCondition(ResultSet rs) throws SQLException {
        OrderDTO order = new OrderDTO();
        order.setOrderId(rs.getLong("order_id"));
        order.setMemId(rs.getInt("mem_id"));
        order.setStockId(rs.getLong("stock_id"));
        order.setSide(rs.getString("side"));
        order.setOrderType(rs.getString("order_type"));
        
        BigDecimal price = rs.getBigDecimal("price");
        order.setPrice(rs.wasNull() ? null : price);
        
        order.setQuantity(rs.getLong("quantity"));
        order.setStatus(rs.getString("status"));
        order.setPlacedAt(rs.getTimestamp("placed_at"));
        
        Timestamp cancelledAt = rs.getTimestamp("cancelled_at");
        order.setCancelledAt(rs.wasNull() ? null : cancelledAt);
        
        order.setHasCondition(rs.getBoolean("has_condition"));
        
        // 조건 정보를 watchPrice 필드에 임시 저장 (DTO에 watchPrice 필드 추가 필요)
        BigDecimal triggerPrice = rs.getBigDecimal("trigger_price");
        order.setWatchPrice(triggerPrice);
        
        return order;
    }
}


