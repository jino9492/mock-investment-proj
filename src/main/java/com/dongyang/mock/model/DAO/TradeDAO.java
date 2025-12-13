package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.RealizedProfitDTO;
import com.dongyang.mock.model.DTO.TradeDTO;
import com.dongyang.mock.model.DTO.TradeStatsDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.dongyang.mock.util.db.DBManager.close;

/**
 * 거래 데이터 접근 객체 (DAO)
 * - 거래 정보 조회 및 필터링
 * - 거래 통계 조회
 */
public class TradeDAO {

    /**
     * 필터 조건에 맞는 거래 목록 조회
     * - 날짜 범위 필터
     * - 거래 유형 필터 (매수/매도)
     * - 상태 필터 (체결/취소)
     * - 종목 키워드 검색
     * - 회원 키워드 검색 (이름, ID, 이메일)
     * @return 필터링된 거래 목록
     */
    public List<TradeDTO> findTrades(String startDate, String endDate, String typeFilter, 
                                     String statusFilter, String stockKeyword, String userKeyword, 
                                     Connection conn) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT t.trade_id, t.mem_id, m.mem_nickname, s.stock_name, " +
                "CASE WHEN o.side = 'BUY' THEN 'buy' ELSE 'sell' END AS trade_type, " +
                "t.trade_quantity AS quantity, " +
                "t.trade_price AS price, " +
                "t.trade_price * t.trade_quantity AS total_amount, " +
                "CASE WHEN o.status = 'FILLED' THEN 'settled' WHEN o.status = 'CANCELLED' THEN 'cancelled' ELSE o.status END AS status, " +
                "t.traded_at AS trade_date " +
                "FROM trade t " +
                "INNER JOIN trade_order o ON t.order_id = o.order_id " +
                "INNER JOIN stock s ON t.stock_id = s.stock_id " +
                "LEFT JOIN member m ON t.mem_id = m.mem_id " +
                "WHERE 1=1");

        List<Object> params = new ArrayList<>();

        // 날짜 범위 필터
        if (startDate != null && !startDate.trim().isEmpty()) {
            sql.append(" AND DATE(t.traded_at) >= ?");
            params.add(startDate);
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            sql.append(" AND DATE(t.traded_at) <= ?");
            params.add(endDate);
        }

        // 거래 유형 필터 (buy/sell -> BUY/SELL 변환)
        if (typeFilter != null && !typeFilter.trim().isEmpty()) {
            if ("buy".equalsIgnoreCase(typeFilter)) {
                sql.append(" AND o.side = 'BUY'");
            } else if ("sell".equalsIgnoreCase(typeFilter)) {
                sql.append(" AND o.side = 'SELL'");
            }
        }

        // 상태 필터 (settled/cancelled -> FILLED/CANCELLED 변환)
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            if ("settled".equalsIgnoreCase(statusFilter)) {
                sql.append(" AND o.status = 'FILLED'");
            } else if ("cancelled".equalsIgnoreCase(statusFilter)) {
                sql.append(" AND o.status = 'CANCELLED'");
            }
        }

        // 종목 키워드 검색
        if (stockKeyword != null && !stockKeyword.trim().isEmpty()) {
            String pattern = "%" + stockKeyword + "%";
            sql.append(" AND (CAST(t.trade_id AS CHAR) LIKE ? OR s.stock_name LIKE ? OR s.stock_symbol LIKE ?)");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        // 회원 키워드 검색
        if (userKeyword != null && !userKeyword.trim().isEmpty()) {
            String pattern = "%" + userKeyword + "%";
            sql.append(" AND (CAST(m.mem_id AS CHAR) LIKE ? OR m.mem_nickname LIKE ? OR m.mem_email LIKE ?)");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        sql.append(" ORDER BY t.traded_at DESC, t.trade_id DESC");

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<TradeDTO> trades = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    pstmt.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) param);
                } else {
                    pstmt.setObject(i + 1, param);
                }
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                trades.add(mapToTrade(rs));
            }
        } finally {
            close(null, pstmt, rs);
        }
        return trades;
    }

    /**
     * 거래 통계 정보 조회
     * - 오늘 거래 건수 및 금액
     * - 전일 거래 건수 및 금액 (비교용)
     * - 매수/매도/취소 건수
     * @return 거래 통계 정보
     */
    public TradeStatsDTO fetchTradeStats(Connection conn) throws SQLException {
        // trade 테이블에는 체결된 거래만 있으므로, 취소 건수는 trade_order 테이블에서 별도로 조회
        String sql = "SELECT " +
                "SUM(CASE WHEN DATE(t.traded_at) = CURDATE() THEN 1 ELSE 0 END) AS today_trade_count, " +
                "SUM(CASE WHEN DATE(t.traded_at) = CURDATE() THEN t.trade_price * t.trade_quantity ELSE 0 END) AS today_trade_amount, " +
                "SUM(CASE WHEN DATE(t.traded_at) = CURDATE() AND o.side = 'BUY' THEN 1 ELSE 0 END) AS today_buy_count, " +
                "SUM(CASE WHEN DATE(t.traded_at) = CURDATE() AND o.side = 'SELL' THEN 1 ELSE 0 END) AS today_sell_count, " +
                "(SELECT COUNT(*) FROM trade_order WHERE status = 'CANCELLED' AND DATE(placed_at) = CURDATE()) AS today_cancelled_count, " +
                "SUM(CASE WHEN DATE(t.traded_at) = DATE_SUB(CURDATE(), INTERVAL 1 DAY) THEN 1 ELSE 0 END) AS yesterday_trade_count, " +
                "SUM(CASE WHEN DATE(t.traded_at) = DATE_SUB(CURDATE(), INTERVAL 1 DAY) THEN t.trade_price * t.trade_quantity ELSE 0 END) AS yesterday_trade_amount, " +
                "SUM(CASE WHEN DATE(t.traded_at) = DATE_SUB(CURDATE(), INTERVAL 1 DAY) AND o.side = 'BUY' THEN 1 ELSE 0 END) AS yesterday_buy_count, " +
                "SUM(CASE WHEN DATE(t.traded_at) = DATE_SUB(CURDATE(), INTERVAL 1 DAY) AND o.side = 'SELL' THEN 1 ELSE 0 END) AS yesterday_sell_count, " +
                "(SELECT COUNT(*) FROM trade_order WHERE status = 'CANCELLED' AND DATE(placed_at) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)) AS yesterday_cancelled_count " +
                "FROM trade t " +
                "INNER JOIN trade_order o ON t.order_id = o.order_id";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            TradeStatsDTO stats = new TradeStatsDTO();
            if (rs.next()) {
                stats.setTodayTradeCount(rs.getInt("today_trade_count"));
                // decimal 타입을 long으로 변환
                java.math.BigDecimal todayAmount = rs.getBigDecimal("today_trade_amount");
                stats.setTodayTradeAmount(todayAmount != null ? todayAmount.longValue() : 0L);
                stats.setBuyCount(rs.getInt("today_buy_count"));
                stats.setSellCount(rs.getInt("today_sell_count"));
                stats.setCancelledCount(rs.getInt("today_cancelled_count"));
                stats.setYesterdayTradeCount(rs.getInt("yesterday_trade_count"));
                java.math.BigDecimal yesterdayAmount = rs.getBigDecimal("yesterday_trade_amount");
                stats.setYesterdayTradeAmount(yesterdayAmount != null ? yesterdayAmount.longValue() : 0L);
                stats.setYesterdayBuyCount(rs.getInt("yesterday_buy_count"));
                stats.setYesterdaySellCount(rs.getInt("yesterday_sell_count"));
                stats.setYesterdayCancelledCount(rs.getInt("yesterday_cancelled_count"));
            }
            return stats;
        } finally {
            close(null, pstmt, rs);
        }
    }

    /**
     * ResultSet을 TradeDTO로 변환
     */
    private TradeDTO mapToTrade(ResultSet rs) throws SQLException {
        TradeDTO trade = new TradeDTO();
        trade.setTradeId((int) rs.getLong("trade_id"));
        trade.setMemId(rs.getInt("mem_id"));
        trade.setMemberNickname(rs.getString("mem_nickname"));
        trade.setStockName(rs.getString("stock_name"));
        trade.setTradeType(rs.getString("trade_type"));
        trade.setQuantity((int) rs.getLong("quantity"));
        // price와 total_amount는 decimal이므로 BigDecimal로 받아서 int로 변환
        trade.setPrice(rs.getBigDecimal("price").intValue());
        trade.setTotalAmount(rs.getBigDecimal("total_amount").intValue());
        trade.setStatus(rs.getString("status"));
        trade.setTradeDate(rs.getTimestamp("trade_date"));
        return trade;
    }

    /**
     * 특정 회원의 최근 거래 내역 조회
     * @param memberId 회원 ID
     * @param limit 조회할 최대 건수
     * @return 최근 거래 내역 목록
     */
    public List<TradeDTO> findMemberRecentTrades(int memberId, int limit, Connection conn) throws SQLException {
        String sql = "SELECT t.trade_id, t.mem_id, m.mem_nickname, s.stock_name, " +
                "CASE WHEN o.side = 'BUY' THEN 'buy' ELSE 'sell' END AS trade_type, " +
                "t.trade_quantity AS quantity, " +
                "t.trade_price AS price, " +
                "t.trade_price * t.trade_quantity AS total_amount, " +
                "CASE WHEN o.status = 'FILLED' THEN 'settled' WHEN o.status = 'CANCELLED' THEN 'cancelled' ELSE o.status END AS status, " +
                "t.traded_at AS trade_date " +
                "FROM trade t " +
                "INNER JOIN trade_order o ON t.order_id = o.order_id " +
                "INNER JOIN stock s ON t.stock_id = s.stock_id " +
                "LEFT JOIN member m ON t.mem_id = m.mem_id " +
                "WHERE t.mem_id = ? " +
                "ORDER BY t.traded_at DESC, t.trade_id DESC " +
                "LIMIT ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<TradeDTO> trades = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setInt(2, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                trades.add(mapToTrade(rs));
            }
        } finally {
            close(null, pstmt, rs);
        }
        return trades;
    }

    /**
     * 특정 회원의 실현수익 조회 (매도 거래만)
     * @param memId 회원 ID
     * @param startDate 시작일 (YYYY-MM-DD 형식)
     * @param endDate 종료일 (YYYY-MM-DD 형식)
     * @param conn DB 연결
     * @return 실현수익 목록
     */
    public List<RealizedProfitDTO> findRealizedProfits(int memId, String startDate, String endDate, Connection conn) throws SQLException {
        // 서브쿼리를 변수로 추출하여 한 번만 실행
        String sql = "SELECT " +
                "DATE_FORMAT(t.traded_at, '%Y.%m.%d') AS sold_at, " +
                "s.stock_name, " +
                "s.stock_symbol, " +
                "t.trade_price * t.trade_quantity AS total_sales_amount, " +
                "COALESCE((" +
                "    SELECT AVG(t2.trade_price) " +
                "    FROM trade t2 " +
                "    INNER JOIN trade_order o2 ON t2.order_id = o2.order_id " +
                "    WHERE t2.mem_id = t.mem_id " +
                "    AND t2.stock_id = t.stock_id " +
                "    AND o2.side = 'BUY' " +
                "    AND o2.status = 'FILLED' " +
                "    AND DATE(t2.traded_at) < DATE(t.traded_at) " +
                "    LIMIT 1" +
                "), t.trade_price) * t.trade_quantity AS total_purchase_amount, " +
                "(t.trade_price * t.trade_quantity) - (COALESCE((" +
                "    SELECT AVG(t2.trade_price) " +
                "    FROM trade t2 " +
                "    INNER JOIN trade_order o2 ON t2.order_id = o2.order_id " +
                "    WHERE t2.mem_id = t.mem_id " +
                "    AND t2.stock_id = t.stock_id " +
                "    AND o2.side = 'BUY' " +
                "    AND o2.status = 'FILLED' " +
                "    AND DATE(t2.traded_at) < DATE(t.traded_at) " +
                "    LIMIT 1" +
                "), t.trade_price) * t.trade_quantity) AS total_profit, " +
                "CASE " +
                "    WHEN COALESCE((" +
                "        SELECT AVG(t2.trade_price) " +
                "        FROM trade t2 " +
                "        INNER JOIN trade_order o2 ON t2.order_id = o2.order_id " +
                "        WHERE t2.mem_id = t.mem_id " +
                "        AND t2.stock_id = t.stock_id " +
                "        AND o2.side = 'BUY' " +
                "        AND o2.status = 'FILLED' " +
                "        AND DATE(t2.traded_at) < DATE(t.traded_at) " +
                "        LIMIT 1" +
                "    ), t.trade_price) * t.trade_quantity > 0 " +
                "    THEN (((t.trade_price * t.trade_quantity) - (COALESCE((" +
                "        SELECT AVG(t2.trade_price) " +
                "        FROM trade t2 " +
                "        INNER JOIN trade_order o2 ON t2.order_id = o2.order_id " +
                "        WHERE t2.mem_id = t.mem_id " +
                "        AND t2.stock_id = t.stock_id " +
                "        AND o2.side = 'BUY' " +
                "        AND o2.status = 'FILLED' " +
                "        AND DATE(t2.traded_at) < DATE(t.traded_at) " +
                "        LIMIT 1" +
                "    ), t.trade_price) * t.trade_quantity)) / " +
                "          (COALESCE((" +
                "        SELECT AVG(t2.trade_price) " +
                "        FROM trade t2 " +
                "        INNER JOIN trade_order o2 ON t2.order_id = o2.order_id " +
                "        WHERE t2.mem_id = t.mem_id " +
                "        AND t2.stock_id = t.stock_id " +
                "        AND o2.side = 'BUY' " +
                "        AND o2.status = 'FILLED' " +
                "        AND DATE(t2.traded_at) < DATE(t.traded_at) " +
                "        LIMIT 1" +
                "    ), t.trade_price) * t.trade_quantity)) * 100 " +
                "    ELSE 0.0 " +
                "END AS profit_rate, " +
                "t.trade_quantity AS quantity " +
                "FROM trade t " +
                "INNER JOIN trade_order o ON t.order_id = o.order_id " +
                "INNER JOIN stock s ON t.stock_id = s.stock_id " +
                "WHERE t.mem_id = ? " +
                "AND o.side = 'SELL' " +
                "AND o.status = 'FILLED' ";

        List<Object> params = new ArrayList<>();
        params.add(memId);

        // 날짜 범위 필터
        if (startDate != null && !startDate.trim().isEmpty()) {
            sql += "AND DATE(t.traded_at) >= ? ";
            params.add(startDate);
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            sql += "AND DATE(t.traded_at) <= ? ";
            params.add(endDate);
        }

        sql += "ORDER BY t.traded_at DESC, t.trade_id DESC";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<RealizedProfitDTO> profits = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement(sql);
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    pstmt.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) param);
                } else {
                    pstmt.setObject(i + 1, param);
                }
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                RealizedProfitDTO profit = new RealizedProfitDTO();
                profit.setSoldAt(rs.getString("sold_at"));
                profit.setStockName(rs.getString("stock_name"));
                profit.setStockSymbol(rs.getString("stock_symbol"));
                
                java.math.BigDecimal totalSalesAmount = rs.getBigDecimal("total_sales_amount");
                profit.setTotalSalesAmount(totalSalesAmount != null ? totalSalesAmount.intValue() : 0);
                
                java.math.BigDecimal totalPurchaseAmount = rs.getBigDecimal("total_purchase_amount");
                profit.setTotalPurchaseAmount(totalPurchaseAmount != null ? totalPurchaseAmount.intValue() : 0);
                
                java.math.BigDecimal totalProfit = rs.getBigDecimal("total_profit");
                profit.setTotalProfit(totalProfit != null ? totalProfit.intValue() : 0);
                
                profit.setProfitRate(rs.getDouble("profit_rate"));
                profit.setQuantity(rs.getInt("quantity"));
                
                // 디버깅: 값 확인
                System.out.println("DEBUG - Stock: " + profit.getStockName() + 
                                 ", totalSalesAmount: " + profit.getTotalSalesAmount() + 
                                 ", totalPurchaseAmount: " + profit.getTotalPurchaseAmount() + 
                                 ", totalProfit: " + profit.getTotalProfit() + 
                                 ", profitRate: " + profit.getProfitRate());
                
                profits.add(profit);
            }
        } finally {
            close(null, pstmt, rs);
        }
        return profits;
    }

    /**
     * 체결 내역 저장
     * @param memId 회원 ID
     * @param stockId 종목 ID
     * @param orderId 주문 ID
     * @param tradePrice 체결 가격
     * @param tradeQuantity 체결 수량
     * @param conn DB 연결
     * @return 생성된 trade ID
     * @throws SQLException
     */
    public long insertTrade(int memId, long stockId, long orderId, 
                           java.math.BigDecimal tradePrice, long tradeQuantity, 
                           Connection conn) throws SQLException {
        String sql = "INSERT INTO trade (mem_id, stock_id, order_id, trade_price, trade_quantity, traded_at) " +
                     "VALUES (?, ?, ?, ?, ?, NOW())";
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, memId);
            pstmt.setLong(2, stockId);
            pstmt.setLong(3, orderId);
            pstmt.setBigDecimal(4, tradePrice);
            pstmt.setLong(5, tradeQuantity);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("체결 내역 저장에 실패했습니다.");
            }
            
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                throw new SQLException("체결 ID를 가져올 수 없습니다.");
            }
        } finally {
            close(null, pstmt, rs);
        }
    }
}

