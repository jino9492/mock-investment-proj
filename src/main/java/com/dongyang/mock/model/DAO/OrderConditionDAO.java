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
 * - 조건 주문 저장 및 조회
 * - 조건 주문 활성화/비활성화
 */
public class OrderConditionDAO {

    /**
     * 조건 주문 저장
     * @param condition 조건 주문 정보
     * @param conn DB 연결
     * @throws SQLException
     */
    public void insertOrderCondition(OrderConditionDTO condition, Connection conn) throws SQLException {
        String sql = "INSERT INTO order_condition (order_id, condition_type, trigger_price, valid_until, is_active, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, NOW())";
        
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
     * 조건 주문 비활성화
     * @param orderId 주문 ID
     * @param conn DB 연결
     * @throws SQLException
     */
    public void deactivateCondition(long orderId, Connection conn) throws SQLException {
        String sql = "UPDATE order_condition SET is_active = false WHERE order_id = ?";

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
     * 조건 주문 활성화/비활성화 토글
     * @param orderId 주문 ID
     * @param isActive 활성화 여부
     * @param conn DB 연결
     * @throws SQLException
     */
    public void toggleCondition(long orderId, boolean isActive, Connection conn) throws SQLException {
        String sql = "UPDATE order_condition SET is_active = ? WHERE order_id = ?";

        PreparedStatement pstmt = null;

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setBoolean(1, isActive);
            pstmt.setLong(2, orderId);
            pstmt.executeUpdate();
        } finally {
            close(null, pstmt);
        }
    }

    /**
     * 조건 주문 정보 업데이트
     * @param orderId 주문 ID
     * @param triggerPrice 감시 가격 (null이면 업데이트하지 않음)
     * @param validUntil 유효 기간 (null이면 업데이트하지 않음)
     * @param conn DB 연결
     * @throws SQLException
     */
    public void updateCondition(long orderId, BigDecimal triggerPrice, Timestamp validUntil, Connection conn) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE order_condition SET ");
        List<Object> params = new ArrayList<>();

        if (triggerPrice != null) {
            sql.append("trigger_price = ?");
            params.add(triggerPrice);
        }

        if (validUntil != null) {
            if (params.size() > 0) {
                sql.append(", ");
            }
            sql.append("valid_until = ?");
            params.add(validUntil);
        }

        if (params.isEmpty()) {
            return; // 업데이트할 것이 없음
        }

        sql.append(" WHERE order_id = ?");

        PreparedStatement pstmt = null;

        try {
            pstmt = conn.prepareStatement(sql.toString());
            int paramIndex = 1;
            for (Object param : params) {
                if (param instanceof BigDecimal) {
                    pstmt.setBigDecimal(paramIndex++, (BigDecimal) param);
                } else if (param instanceof Timestamp) {
                    pstmt.setTimestamp(paramIndex++, (Timestamp) param);
                }
            }
            pstmt.setLong(paramIndex, orderId);
            pstmt.executeUpdate();
        } finally {
            close(null, pstmt);
        }
    }

    /**
     * 만료된 조건 주문 ID 목록 조회 (유효 기간이 지난 조건 주문)
     * @param conn DB 연결
     * @return 만료된 조건 주문 ID 목록
     * @throws SQLException
     */
    public List<Long> findExpiredConditionalOrderIds(Connection conn) throws SQLException {
        String sql = "SELECT order_id FROM order_condition " +
                     "WHERE is_active = true " +
                     "AND valid_until IS NOT NULL " +
                     "AND valid_until < NOW()";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Long> orderIds = new ArrayList<>();

        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                orderIds.add(rs.getLong("order_id"));
            }
        } finally {
            close(null, pstmt, rs);
        }

        return orderIds;
    }

    /**
     * 주문 ID로 조건 주문 조회
     * @param orderId 주문 ID
     * @param conn DB 연결
     * @return 조건 주문 정보, 없으면 null
     * @throws SQLException
     */
    public OrderConditionDTO findOrderConditionByOrderId(long orderId, Connection conn) throws SQLException {
        String sql = "SELECT order_id, condition_type, trigger_price, valid_until, is_active, created_at " +
                     "FROM order_condition WHERE order_id = ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, orderId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                OrderConditionDTO condition = new OrderConditionDTO();
                condition.setOrderId(rs.getLong("order_id"));
                condition.setConditionType(rs.getString("condition_type"));

                BigDecimal triggerPrice = rs.getBigDecimal("trigger_price");
                condition.setTriggerPrice(rs.wasNull() ? null : triggerPrice);

                Timestamp validUntil = rs.getTimestamp("valid_until");
                condition.setValidUntil(rs.wasNull() ? null : validUntil);

                condition.setActive(rs.getBoolean("is_active"));
                condition.setCreatedAt(rs.getTimestamp("created_at"));

                return condition;
            }
        } finally {
            close(null, pstmt, rs);
        }

        return null;
    }

    /**
     * 활성화된 조건 주문 조회
     * @param conn DB 연결
     * @return 활성화된 조건 주문 목록 (OrderDTO에 watchPrice 포함)
     * @throws SQLException
     */
    public List<OrderDTO> findActiveConditionalOrders(Connection conn) throws SQLException {
        String sql = "SELECT o.order_id, o.mem_id, o.stock_id, o.side, o.order_type, o.price, " +
                     "o.quantity, o.status, o.placed_at, o.cancelled_at, o.has_condition, " +
                     "s.stock_name, oc.trigger_price as watch_price " +
                     "FROM trade_order o " +
                     "INNER JOIN stock s ON o.stock_id = s.stock_id " +
                     "INNER JOIN order_condition oc ON o.order_id = oc.order_id " +
                     "WHERE oc.is_active = true " +
                     "AND o.status = 'PENDING' " +
                     "AND (oc.valid_until IS NULL OR oc.valid_until > NOW()) " +
                     "ORDER BY o.placed_at ASC";
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<OrderDTO> orders = new ArrayList<>();
        
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                OrderDTO order = mapToOrder(rs);
                orders.add(order);
            }
        } finally {
            close(null, pstmt, rs);
        }
        
        return orders;
    }

    /**
     * ResultSet을 OrderDTO로 변환 (watchPrice 포함)
     */
    private OrderDTO mapToOrder(ResultSet rs) throws SQLException {
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
        order.setStockName(rs.getString("stock_name"));

        // watchPrice 설정 (order_condition 테이블의 trigger_price)
        BigDecimal watchPrice = rs.getBigDecimal("watch_price");
        if (!rs.wasNull() && watchPrice != null) {
            order.setWatchPrice(watchPrice);
        }

        // JSP용 tradeType 변환
        if ("BUY".equals(order.getSide())) {
            order.setTradeType("buy");
        } else {
            order.setTradeType("sell");
        }
        
        return order;
    }
}


