package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.LeagueDTO;

import java.math.BigDecimal;
import java.sql.*;

import static com.dongyang.mock.util.db.DBManager.close;

/**
 * 계좌 요약 정보 데이터 접근 객체 (DAO)
 * - 계좌 현황 조회
 * - 총자산, 현금잔고, 보유주식 평가액 계산
 * - 총 수익률 및 상위 백분율 계산
 */
public class AccountSummaryDAO {

    /**
     * 투자중인 금액 계산 (보유주식 평가액)
     */
    private long calculateStockValue(int memId, Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(SUM(h.quantity * spd.close_price), 0) as stock_value " +
                "FROM holding h " +
                "INNER JOIN stock_price_daily spd ON h.stock_id = spd.stock_id " +
                "AND spd.price_date = (SELECT MAX(price_date) FROM stock_price_daily WHERE stock_id = h.stock_id) " +
                "WHERE h.mem_id = ? AND h.quantity > 0";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                // decimal(18,2)를 정확하게 처리
                BigDecimal value = rs.getBigDecimal("stock_value");
                return value != null ? value.longValue() : 0L;
            }
            return 0L;
        } finally {
            close(null, pstmt, rs);
        }
    }

    /**
     * 전체 회원 중 상위 백분율 계산
     */
    private double calculatePercentile(int memId, long totalAsset, Connection conn) throws SQLException {
        String sql = "SELECT " +
                "m.mem_id, " +
                "COALESCE(m.virtual_cash, 0) + COALESCE(SUM(h.quantity * spd.close_price), 0) as total_asset " +
                "FROM member m " +
                "LEFT JOIN holding h ON m.mem_id = h.mem_id " +
                "LEFT JOIN stock_price_daily spd ON h.stock_id = spd.stock_id " +
                "AND spd.price_date = (SELECT MAX(price_date) FROM stock_price_daily WHERE stock_id = h.stock_id) " +
                "GROUP BY m.mem_id, m.virtual_cash " +
                "HAVING total_asset > 0 " +
                "ORDER BY total_asset DESC";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            int rank = 0;
            int totalUsers = 0;

            while (rs.next()) {
                totalUsers++;
                if (rs.getInt("mem_id") == memId) {
                    rank = totalUsers;
                }
            }

            if (rank == 0 || totalUsers == 0) {
                return 0.0;
            }

            return ((double) rank / totalUsers) * 100.0;
        } finally {
            close(null, pstmt, rs);
        }
    }

    /**
     * 기능 1-3: 계좌 요약 정보 조회 (최적화: 단일 쿼리로 통합)
     */
    public LeagueDTO.AccountSummary selectAccountSummary(int memId, Connection conn) throws SQLException {
        // 현금 잔고와 초기 자본금을 하나의 쿼리로 조회 (LEFT JOIN으로 최적화)
        String sql = "SELECT " +
                "COALESCE(m.virtual_cash, 0) as cash_balance, " +
                "COALESCE(initial_cap.amount, 10000000) as initial_capital " +
                "FROM member m " +
                "LEFT JOIN ( " +
                "  SELECT ch1.mem_id, ch1.amount " +
                "  FROM cash_history ch1 " +
                "  INNER JOIN ( " +
                "    SELECT mem_id, MIN(created_at) as min_created_at " +
                "    FROM cash_history " +
                "    WHERE tx_type = 'DEPOSIT' " +
                "    GROUP BY mem_id " +
                "  ) ch2 ON ch1.mem_id = ch2.mem_id " +
                "  AND ch1.created_at = ch2.min_created_at " +
                "  AND ch1.tx_type = 'DEPOSIT' " +
                ") initial_cap ON m.mem_id = initial_cap.mem_id " +
                "WHERE m.mem_id = ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        long cashBalance = 0L;
        long initialCapital = 10000000L;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                // decimal(18,2)를 BigDecimal로 처리하거나 long으로 변환 (소수점 반올림)
                cashBalance = Math.round(rs.getBigDecimal("cash_balance").doubleValue());
                initialCapital = Math.round(rs.getBigDecimal("initial_capital").doubleValue());
            }
        } finally {
            close(null, pstmt, rs);
        }

        // 투자중인 금액 (보유주식 평가액)
        long stockValue = calculateStockValue(memId, conn);

        // 계좌 총 금액
        long totalAsset = cashBalance + stockValue;

        // 총 수익률 계산
        double totalReturn = 0.0;
        if (initialCapital > 0) {
            totalReturn = ((double) (totalAsset - initialCapital) / initialCapital) * 100.0;
            totalReturn = Math.round(totalReturn * 100.0) / 100.0; // 소수점 2자리 반올림
        }

        // 상위 백분율 계산
        double percentile = calculatePercentile(memId, totalAsset, conn);
        percentile = Math.round(percentile * 10.0) / 10.0; // 소수점 1자리 반올림

        LeagueDTO.AccountSummary summary = new LeagueDTO.AccountSummary();
        summary.setTotalAsset(totalAsset);
        summary.setStockValue(stockValue);
        summary.setCashBalance(cashBalance);
        summary.setTotalReturn(totalReturn);
        summary.setPercentile(percentile);

        return summary;
    }
}

