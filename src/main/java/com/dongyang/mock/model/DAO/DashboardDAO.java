package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.DashboardDTO;
import com.dongyang.mock.model.DTO.LeagueDTO;
import com.dongyang.mock.model.DTO.TopStockDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.dongyang.mock.util.db.DBManager.close;

/**
 * 대시보드 데이터 접근 객체 (DAO)
 * - 대시보드 통계 정보 조회
 * - 인기종목 조회
 * - 수익률 상위 종목 조회
 */
public class DashboardDAO {

    /**
     * 대시보드 통계 정보 조회
     * - 전체 회원 수, 당일 접속자 수
     * - 당일 거래량, 거래 금액
     * - 전일 대비 증가율 계산
     */
    public DashboardDTO fetchDashboardStats(Connection conn) throws SQLException {
        String sql = "SELECT " +
                // 회원 통계
                "(SELECT COUNT(*) FROM member) AS total_users, " +
                "(SELECT COUNT(*) FROM member WHERE DATE(mem_last_login_at) = CURDATE()) AS today_visitors, " +
                "(SELECT COUNT(*) FROM member WHERE DATE(mem_last_login_at) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)) AS yesterday_visitors, " +
                "(SELECT COUNT(*) FROM member WHERE DATE(mem_join_at) = CURDATE()) AS today_new_users, " +
                "(SELECT COUNT(*) FROM member WHERE DATE(mem_join_at) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)) AS yesterday_new_users, " +
                "(SELECT COUNT(*) FROM member WHERE DATE(mem_join_at) <= DATE_SUB(CURDATE(), INTERVAL 1 DAY)) AS yesterday_total_users, " +
                // 거래 통계
                "SUM(CASE WHEN DATE(t.traded_at) = CURDATE() THEN 1 ELSE 0 END) AS today_trades, " +
                "SUM(CASE WHEN DATE(t.traded_at) = CURDATE() THEN t.trade_price * t.trade_quantity ELSE 0 END) AS today_amount, " +
                "SUM(CASE WHEN DATE(t.traded_at) = DATE_SUB(CURDATE(), INTERVAL 1 DAY) THEN 1 ELSE 0 END) AS yesterday_trades, " +
                "SUM(CASE WHEN DATE(t.traded_at) = DATE_SUB(CURDATE(), INTERVAL 1 DAY) THEN t.trade_price * t.trade_quantity ELSE 0 END) AS yesterday_amount " +
                "FROM trade t";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            DashboardDTO stats = new DashboardDTO();
            if (rs.next()) {
                // 회원 통계
                stats.setTotalUsers(rs.getInt("total_users"));
                stats.setTodayVisitors(rs.getInt("today_visitors"));
                int yesterdayVisitors = rs.getInt("yesterday_visitors");
                int yesterdayTotalUsers = rs.getInt("yesterday_total_users");

                // 전일 대비 접속자 증감 수 계산 (명 단위)
                int visitorGrowthCount = stats.getTodayVisitors() - yesterdayVisitors;
                stats.setVisitorGrowthCount(visitorGrowthCount);

                // 전일 대비 회원 증감 수 계산 (명 단위)
                int userGrowthCount = stats.getTotalUsers() - yesterdayTotalUsers;
                stats.setUserGrowthCount(userGrowthCount);

                // 거래 통계
                stats.setTodayTrades(rs.getInt("today_trades"));
                java.math.BigDecimal todayAmount = rs.getBigDecimal("today_amount");
                stats.setTodayAmount(todayAmount != null ? todayAmount.longValue() : 0L);
                
                int yesterdayTrades = rs.getInt("yesterday_trades");

                // 전일 대비 거래 증감 수 계산 (건 단위)
                int tradeGrowthCount = stats.getTodayTrades() - yesterdayTrades;
                stats.setTradeGrowthCount(tradeGrowthCount);
            }
            return stats;
        } finally {
            close(null, pstmt, rs);
        }
    }

    /**
     * 인기종목 TOP N 조회 (거래건수 기준)
     * @param limit 조회할 개수
     */
    public List<TopStockDTO> fetchTopStocks(int limit, Connection conn) throws SQLException {
        String sql = "SELECT s.stock_name, " +
                "COUNT(t.trade_id) AS trade_count, " +
                "SUM(t.trade_price * t.trade_quantity) AS trade_amount " +
                "FROM trade t " +
                "INNER JOIN stock s ON t.stock_id = s.stock_id " +
                "WHERE DATE(t.traded_at) = CURDATE() " +
                "GROUP BY s.stock_id, s.stock_name " +
                "ORDER BY trade_count DESC, trade_amount DESC " +
                "LIMIT ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<TopStockDTO> stocks = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                TopStockDTO stock = new TopStockDTO();
                stock.setStockName(rs.getString("stock_name"));
                stock.setTradeCount(rs.getInt("trade_count"));
                java.math.BigDecimal amount = rs.getBigDecimal("trade_amount");
                stock.setTradeAmount(amount != null ? amount.longValue() : 0L);
                stocks.add(stock);
            }
            return stocks;
        } finally {
            close(null, pstmt, rs);
        }
    }

    /**
     * 수익률 상위 종목 TOP N 조회
     * @param limit 조회할 개수
     */
    public List<LeagueDTO.TopReturnStock> fetchTopReturnStocks(int limit, Connection conn) throws SQLException {
        String sql = "SELECT s.stock_name, " +
                "COALESCE(spd.close_price, 0) AS current_price, " +
                "COALESCE(h.avg_buy_price, 0) AS avg_purchase_price, " +
                "CASE " +
                "  WHEN COALESCE(h.avg_buy_price, 0) > 0 " +
                "  THEN ((COALESCE(spd.close_price, 0) - COALESCE(h.avg_buy_price, 0)) / COALESCE(h.avg_buy_price, 1)) * 100 " +
                "  ELSE 0 " +
                "END AS return_rate, " +
                "CASE " +
                "  WHEN prev_spd.close_price > 0 " +
                "  THEN ((spd.close_price - prev_spd.close_price) / prev_spd.close_price) * 100.0 " +
                "  ELSE 0.0 " +
                "END AS change_rate " +
                "FROM holding h " +
                "INNER JOIN stock s ON h.stock_id = s.stock_id " +
                "INNER JOIN stock_price_daily spd ON h.stock_id = spd.stock_id " +
                "  AND spd.price_date = (SELECT MAX(price_date) FROM stock_price_daily WHERE stock_id = h.stock_id) " +
                "LEFT JOIN stock_price_daily prev_spd ON h.stock_id = prev_spd.stock_id " +
                "  AND prev_spd.price_date = (SELECT MAX(price_date) FROM stock_price_daily WHERE stock_id = h.stock_id AND price_date < spd.price_date) " +
                "WHERE h.quantity > 0 " +
                "GROUP BY s.stock_id, s.stock_name, spd.close_price, h.avg_buy_price, prev_spd.close_price " +
                "HAVING return_rate != 0 " +
                "ORDER BY return_rate DESC " +
                "LIMIT ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<LeagueDTO.TopReturnStock> stocks = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                LeagueDTO.TopReturnStock stock = new LeagueDTO.TopReturnStock();
                stock.setStockName(rs.getString("stock_name"));
                java.math.BigDecimal currentPrice = rs.getBigDecimal("current_price");
                stock.setCurrentPrice(currentPrice != null ? currentPrice.longValue() : 0L);
                java.math.BigDecimal avgPrice = rs.getBigDecimal("avg_purchase_price");
                stock.setAvgPurchasePrice(avgPrice != null ? avgPrice.longValue() : 0L);
                stock.setReturnRate(rs.getDouble("return_rate"));
                stock.setChangeRate(rs.getDouble("change_rate"));
                stocks.add(stock);
            }
            return stocks;
        } finally {
            close(null, pstmt, rs);
        }
    }
}

