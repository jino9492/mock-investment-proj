package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.OrderDTO;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.dongyang.mock.util.db.DBManager.close;

/**
 * 주문 데이터 접근 객체 (DAO)
 * - 주문 저장 및 조회
 */
public class OrderDAO {

    /**
     * 주문 저장
     * @param order 주문 정보
     * @param conn DB 연결
     * @return 생성된 주문 ID (Generated Key)
     * @throws SQLException
     */
    public long insertOrder(OrderDTO order, Connection conn) throws SQLException {
        String sql = "INSERT INTO trade_order (mem_id, stock_id, side, order_type, price, quantity, status, has_condition) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, order.getMemId());
            pstmt.setLong(2, order.getStockId());
            pstmt.setString(3, order.getSide());
            pstmt.setString(4, order.getOrderType());
            
            if (order.getPrice() != null) {
                pstmt.setBigDecimal(5, order.getPrice());
            } else {
                pstmt.setNull(5, Types.DECIMAL);
            }
            
            pstmt.setLong(6, order.getQuantity());
            pstmt.setString(7, order.getStatus());
            pstmt.setBoolean(8, order.isHasCondition());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("주문 저장에 실패했습니다.");
            }
            
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                throw new SQLException("주문 ID를 가져올 수 없습니다.");
            }
        } finally {
            close(null, pstmt, rs);
        }
    }

    /**
     * 회원의 대기 주문 목록 조회
     * @param memId 회원 ID
     * @param conn DB 연결
     * @return 대기 주문 목록
     * @throws SQLException
     */
    public List<OrderDTO> findPendingOrdersByMember(int memId, Connection conn) throws SQLException {
        String sql = "SELECT o.order_id, o.mem_id, o.stock_id, o.side, o.order_type, o.price, " +
                     "o.quantity, o.status, o.placed_at, o.cancelled_at, o.has_condition, " +
                     "s.stock_name " +
                     "FROM trade_order o " +
                     "INNER JOIN stock s ON o.stock_id = s.stock_id " +
                     "WHERE o.mem_id = ? AND o.status = 'PENDING' " +
                     "ORDER BY o.placed_at DESC";
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<OrderDTO> orders = new ArrayList<>();
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memId);
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
     * 특정 종목에 대한 대기 중인 판매 주문 수량 합계 조회
     * @param memId 회원 ID
     * @param stockId 종목 ID
     * @param conn DB 연결
     * @return 대기 중인 판매 주문 수량 합계
     * @throws SQLException
     */
    public long getPendingSellOrderQuantity(int memId, long stockId, Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity), 0) as total_quantity " +
                     "FROM trade_order " +
                     "WHERE mem_id = ? AND stock_id = ? AND side = 'SELL' AND status = 'PENDING'";
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memId);
            pstmt.setLong(2, stockId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getLong("total_quantity");
            }
            return 0;
        } finally {
            close(null, pstmt, rs);
        }
    }

    /**
     * 주문 ID로 주문 조회
     * @param orderId 주문 ID
     * @param conn DB 연결
     * @return 주문 정보, 없으면 null
     * @throws SQLException
     */
    public OrderDTO findOrderById(long orderId, Connection conn) throws SQLException {
        String sql = "SELECT o.order_id, o.mem_id, o.stock_id, o.side, o.order_type, o.price, " +
                     "o.quantity, o.status, o.placed_at, o.cancelled_at, o.has_condition, " +
                     "s.stock_name " +
                     "FROM trade_order o " +
                     "INNER JOIN stock s ON o.stock_id = s.stock_id " +
                     "WHERE o.order_id = ?";
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, orderId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapToOrder(rs);
            }
        } finally {
            close(null, pstmt, rs);
        }
        
        return null;
    }

    /**
     * 주문 상태 업데이트
     * @param orderId 주문 ID
     * @param status 새로운 상태
     * @param conn DB 연결
     * @return 영향받은 행 수
     * @throws SQLException
     */
    public int updateOrderStatus(long orderId, String status, Connection conn) throws SQLException {
        String sql = "UPDATE trade_order SET status = ? WHERE order_id = ?";
        
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setLong(2, orderId);
            
            return pstmt.executeUpdate();
        } finally {
            close(null, pstmt);
        }
    }

    /**
     * 주문 정보 업데이트 (가격, 수량)
     * @param orderId 주문 ID
     * @param price 주문 가격 (null이면 업데이트하지 않음)
     * @param quantity 주문 수량 (null이면 업데이트하지 않음)
     * @param conn DB 연결
     * @return 영향받은 행 수
     * @throws SQLException
     */
    public int updateOrder(long orderId, BigDecimal price, Long quantity, Connection conn) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE trade_order SET ");
        List<Object> params = new ArrayList<>();
        
        if (price != null) {
            sql.append("price = ?");
            params.add(price);
        }
        
        if (quantity != null) {
            if (params.size() > 0) {
                sql.append(", ");
            }
            sql.append("quantity = ?");
            params.add(quantity);
        }
        
        if (params.isEmpty()) {
            return 0; // 업데이트할 것이 없음
        }
        
        sql.append(" WHERE order_id = ?");
        
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql.toString());
            int paramIndex = 1;
            for (Object param : params) {
                if (param instanceof BigDecimal) {
                    pstmt.setBigDecimal(paramIndex++, (BigDecimal) param);
                } else if (param instanceof Long) {
                    pstmt.setLong(paramIndex++, (Long) param);
                }
            }
            pstmt.setLong(paramIndex, orderId);
            
            return pstmt.executeUpdate();
        } finally {
            close(null, pstmt);
        }
    }

    /**
     * 일반 주문 정보 업데이트 (주문 유형, 가격, 수량)
     * @param orderId 주문 ID
     * @param orderType 주문 유형 (LIMIT/MARKET, null이면 업데이트하지 않음)
     * @param price 주문 가격 (null이면 업데이트하지 않음, MARKET 주문인 경우 null로 설정)
     * @param quantity 주문 수량 (null이면 업데이트하지 않음)
     * @param conn DB 연결
     * @return 영향받은 행 수
     * @throws SQLException
     */
    public int updateGeneralOrder(long orderId, String orderType, BigDecimal price, Long quantity, Connection conn) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE trade_order SET ");
        List<Object> params = new ArrayList<>();
        List<Integer> paramTypes = new ArrayList<>();
        
        if (orderType != null) {
            sql.append("order_type = ?");
            params.add(orderType);
            paramTypes.add(Types.VARCHAR);
        }
        
        if (price != null || (orderType != null && "MARKET".equals(orderType))) {
            if (params.size() > 0) {
                sql.append(", ");
            }
            if ("MARKET".equals(orderType)) {
                // MARKET 주문인 경우 price를 null로 설정
                sql.append("price = NULL");
            } else {
                sql.append("price = ?");
                params.add(price);
                paramTypes.add(Types.DECIMAL);
            }
        }
        
        if (quantity != null) {
            if (params.size() > 0) {
                sql.append(", ");
            }
            sql.append("quantity = ?");
            params.add(quantity);
            paramTypes.add(Types.BIGINT);
        }
        
        if (params.isEmpty() && (orderType == null || !"MARKET".equals(orderType))) {
            return 0; // 업데이트할 것이 없음
        }
        
        sql.append(" WHERE order_id = ?");
        
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql.toString());
            int paramIndex = 1;
            
            // orderType 파라미터 설정
            if (orderType != null) {
                pstmt.setString(paramIndex++, orderType);
            }
            
            // price 파라미터 설정 (MARKET이 아닌 경우만)
            if (price != null && (orderType == null || !"MARKET".equals(orderType))) {
                pstmt.setBigDecimal(paramIndex++, price);
            }
            
            // quantity 파라미터 설정
            if (quantity != null) {
                pstmt.setLong(paramIndex++, quantity);
            }
            
            pstmt.setLong(paramIndex, orderId);
            
            return pstmt.executeUpdate();
        } finally {
            close(null, pstmt);
        }
    }

    /**
     * 모든 PENDING 주문 조회
     * @param conn DB 연결
     * @return PENDING 주문 목록
     * @throws SQLException
     */
    public List<OrderDTO> findAllPendingOrders(Connection conn) throws SQLException {
        String sql = "SELECT o.order_id, o.mem_id, o.stock_id, o.side, o.order_type, o.price, " +
                     "o.quantity, o.status, o.placed_at, o.cancelled_at, o.has_condition, " +
                     "s.stock_name " +
                     "FROM trade_order o " +
                     "INNER JOIN stock s ON o.stock_id = s.stock_id " +
                     "WHERE o.status = 'PENDING' " +
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
     * ResultSet을 OrderDTO로 변환
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
        
        // JSP용 tradeType 변환
        if ("BUY".equals(order.getSide())) {
            order.setTradeType("buy");
        } else {
            order.setTradeType("sell");
        }
        
        return order;
    }
}

