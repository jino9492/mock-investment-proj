package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.LeagueDTO;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.dongyang.mock.util.db.DBManager.close;

/**
 * 리그 페이지 데이터 접근 객체 (DAO)
 * - 계좌 현황 조회
 * - 포트폴리오 정보 조회
 * - 랭킹 정보 조회
 */
public class LeagueDAO {

    /**
     * 각 주식의 수익률 평균 계산
     * 각 주식의 수익률 = ((현재가 - 매수평균가) / 매수평균가) × 100
     * 전체 계좌 수익률 = 모든 주식 수익률의 평균
     */
    private double calculateAverageStockReturnRate(int memId, Connection conn) throws SQLException {
        String sql = "SELECT " +
                "CASE " +
                "  WHEN h.avg_buy_price > 0 " +
                "  THEN ((spd.close_price - h.avg_buy_price) / h.avg_buy_price) * 100 " +
                "  ELSE 0 " +
                "END as return_rate " +
                "FROM holding h " +
                "INNER JOIN stock_price_daily spd ON h.stock_id = spd.stock_id " +
                "AND spd.price_date = (SELECT MAX(price_date) FROM stock_price_daily WHERE stock_id = h.stock_id) " +
                "WHERE h.mem_id = ? AND h.quantity > 0 AND h.avg_buy_price > 0";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        double sumReturnRate = 0.0;
        int count = 0;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                double returnRate = rs.getDouble("return_rate");
                sumReturnRate += returnRate;
                count++;
            }

            // 평균 계산
            if (count > 0) {
                return sumReturnRate / count;
            }
            return 0.0;
        } finally {
            close(null, pstmt, rs);
        }
    }

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
                "COALESCE(initial_cap.amount, 1000000) as initial_capital " +
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
        long initialCapital = 1000000L;
        
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

        // 계좌 총 잔액
        long totalAsset = cashBalance + stockValue;

        // 전체 계좌 수익률 계산: 각 주식의 수익률을 모두 더해서 평균을 낸 값
        double totalReturn = calculateAverageStockReturnRate(memId, conn);
        totalReturn = Math.round(totalReturn * 100.0) / 100.0; // 소수점 2자리 반올림

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

    /**
     * 기능 4: 수익률 상위 3개 종목 조회
     */
    public List<LeagueDTO.TopReturnStock> selectTopReturnStocks(int memId, int limit, Connection conn) throws SQLException {
        String sql = "SELECT " +
                "s.stock_name, " +
                "spd.close_price as current_price, " +
                "h.avg_buy_price as avg_purchase_price, " +
                "CASE " +
                "  WHEN prev_spd.close_price > 0 " +
                "  THEN ((spd.close_price - prev_spd.close_price) / prev_spd.close_price) * 100.0 " +
                "  ELSE 0.0 " +
                "END as change_rate, " +
                "CASE " +
                "  WHEN h.avg_buy_price > 0 " +
                "  THEN ((spd.close_price - h.avg_buy_price) / h.avg_buy_price) * 100 " +
                "  ELSE 0 " +
                "END as return_rate " +
                "FROM holding h " +
                "INNER JOIN stock s ON h.stock_id = s.stock_id " +
                "INNER JOIN stock_price_daily spd ON h.stock_id = spd.stock_id " +
                "AND spd.price_date = (SELECT MAX(price_date) FROM stock_price_daily WHERE stock_id = h.stock_id) " +
                "LEFT JOIN stock_price_daily prev_spd ON h.stock_id = prev_spd.stock_id " +
                "AND prev_spd.price_date = (SELECT MAX(price_date) FROM stock_price_daily WHERE stock_id = h.stock_id AND price_date < spd.price_date) " +
                "WHERE h.mem_id = ? AND h.quantity > 0 AND h.avg_buy_price > 0 " +
                "ORDER BY return_rate DESC " +
                "LIMIT ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<LeagueDTO.TopReturnStock> stocks = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memId);
            pstmt.setInt(2, limit);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                LeagueDTO.TopReturnStock stock = new LeagueDTO.TopReturnStock();
                stock.setStockName(rs.getString("stock_name"));
                // decimal(18,2)를 정확하게 처리
                BigDecimal price = rs.getBigDecimal("current_price");
                stock.setCurrentPrice(price != null ? price.longValue() : 0L);
                BigDecimal avgPrice = rs.getBigDecimal("avg_purchase_price");
                stock.setAvgPurchasePrice(avgPrice != null ? avgPrice.longValue() : 0L);
                stock.setChangeRate(rs.getDouble("change_rate"));
                double returnRate = rs.getDouble("return_rate");
                stock.setReturnRate(Math.round(returnRate * 100.0) / 100.0);
                stocks.add(stock);
            }
        } finally {
            close(null, pstmt, rs);
        }
        return stocks;
    }

    /**
     * 기능 7: 전체 포트폴리오 상세 조회
     */
    public List<LeagueDTO.PortfolioStock> selectPortfolioDetails(int memId, Connection conn) throws SQLException {
        String sql = "SELECT " +
                "h.stock_id, " +
                "s.stock_name, " +
                "h.quantity, " +
                "h.avg_buy_price as avg_purchase_price, " +
                "spd.close_price as current_price, " +
                "CASE " +
                "  WHEN prev_spd.close_price > 0 " +
                "  THEN ((spd.close_price - prev_spd.close_price) / prev_spd.close_price) * 100.0 " +
                "  ELSE 0.0 " +
                "END as change_rate, " +
                "(spd.close_price - h.avg_buy_price) * h.quantity as profit_loss, " +
                "CASE " +
                "  WHEN h.avg_buy_price > 0 " +
                "  THEN ((spd.close_price - h.avg_buy_price) / h.avg_buy_price) * 100 " +
                "  ELSE 0 " +
                "END as return_rate " +
                "FROM holding h " +
                "INNER JOIN stock s ON h.stock_id = s.stock_id " +
                "INNER JOIN stock_price_daily spd ON h.stock_id = spd.stock_id " +
                "AND spd.price_date = (SELECT MAX(price_date) FROM stock_price_daily WHERE stock_id = h.stock_id) " +
                "LEFT JOIN stock_price_daily prev_spd ON h.stock_id = prev_spd.stock_id " +
                "AND prev_spd.price_date = (SELECT MAX(price_date) FROM stock_price_daily WHERE stock_id = h.stock_id AND price_date < spd.price_date) " +
                "WHERE h.mem_id = ? AND h.quantity > 0 " +
                "ORDER BY return_rate DESC";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<LeagueDTO.PortfolioStock> portfolio = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                LeagueDTO.PortfolioStock stock = new LeagueDTO.PortfolioStock();
                stock.setStockId(rs.getInt("stock_id"));
                stock.setStockName(rs.getString("stock_name"));
                stock.setQuantity(rs.getInt("quantity"));
                // decimal(18,2)를 정확하게 처리
                BigDecimal avgPrice = rs.getBigDecimal("avg_purchase_price");
                stock.setAvgPurchasePrice(avgPrice != null ? avgPrice.longValue() : 0L);
                BigDecimal currentPrice = rs.getBigDecimal("current_price");
                stock.setCurrentPrice(currentPrice != null ? currentPrice.longValue() : 0L);
                stock.setChangeRate(rs.getDouble("change_rate"));
                BigDecimal profitLoss = rs.getBigDecimal("profit_loss");
                stock.setProfitLoss(profitLoss != null ? profitLoss.longValue() : 0L);
                double returnRate = rs.getDouble("return_rate");
                stock.setReturnRate(Math.round(returnRate * 100.0) / 100.0);
                portfolio.add(stock);
            }
        } finally {
            close(null, pstmt, rs);
        }
        return portfolio;
    }

    /**
     * 기능 5: 주변 랭킹 조회 (나를 중심으로 위 2명, 본인, 아래 3명)
     * 순위 기준: 수익률 (총자산 대비 초기자본 대비 수익률)
     */
    public List<LeagueDTO.Ranking> selectNearbyRankings(int memId, Connection conn) throws SQLException {
        // 모든 회원의 총자산 계산 및 순위 매기기
        String sql = "SELECT " +
                "m.mem_id, " +
                "m.mem_nickname, " +
                "COALESCE(m.virtual_cash, 0) + COALESCE(SUM(h.quantity * spd.close_price), 0) as total_asset, " +
                "COALESCE(initial_cap.initial_capital, 1000000) as initial_capital " +
                "FROM member m " +
                "LEFT JOIN holding h ON m.mem_id = h.mem_id " +
                "LEFT JOIN stock_price_daily spd ON h.stock_id = spd.stock_id " +
                "AND spd.price_date = (SELECT MAX(price_date) FROM stock_price_daily WHERE stock_id = h.stock_id) " +
                "LEFT JOIN ( " +
                "  SELECT mem_id, amount as initial_capital " +
                "  FROM cash_history " +
                "  WHERE tx_type = 'DEPOSIT' " +
                "  AND created_at = (SELECT MIN(created_at) FROM cash_history ch2 WHERE ch2.mem_id = cash_history.mem_id AND tx_type = 'DEPOSIT') " +
                ") initial_cap ON m.mem_id = initial_cap.mem_id " +
                "GROUP BY m.mem_id, m.mem_nickname, m.virtual_cash, initial_cap.initial_capital " +
                "ORDER BY total_asset DESC";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<LeagueDTO.Ranking> allRankings = new ArrayList<>();
        int currentUserRank = 0;

        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            // 모든 회원 데이터를 먼저 수집하고 수익률 계산
            // 전체 계좌 수익률 = 각 주식의 수익률을 모두 더해서 평균을 낸 값
            while (rs.next()) {
                int userId = rs.getInt("mem_id");
                // decimal(18,2)를 정확하게 처리
                BigDecimal totalAssetBD = rs.getBigDecimal("total_asset");
                long totalAsset = totalAssetBD != null ? totalAssetBD.longValue() : 0L;
                
                // 각 주식의 수익률 평균 계산
                double returnRate = calculateAverageStockReturnRate(userId, conn);
                returnRate = Math.round(returnRate * 100.0) / 100.0;

                LeagueDTO.Ranking ranking = new LeagueDTO.Ranking();
                ranking.setNickname(rs.getString("mem_nickname"));
                ranking.setReturnRate(returnRate);
                ranking.setTotalAsset(totalAsset);
                ranking.setCurrentUser(userId == memId);

                allRankings.add(ranking);
            }

            // 수익률 기준으로 정렬 (내림차순)
            allRankings.sort((a, b) -> Double.compare(b.getReturnRate(), a.getReturnRate()));

            // 정렬 후 순위 부여 및 현재 사용자 순위 찾기
            for (int i = 0; i < allRankings.size(); i++) {
                LeagueDTO.Ranking ranking = allRankings.get(i);
                ranking.setRank(i + 1);
                
                if (ranking.isCurrentUser()) {
                    currentUserRank = i + 1;
                }
            }

        } finally {
            close(null, pstmt, rs);
        }

        // 현재 사용자 기준 위 2명, 본인, 아래 3명 추출 (총 6명)
        List<LeagueDTO.Ranking> nearbyRankings = new ArrayList<>();
        if (currentUserRank > 0) {
            // 배열 인덱스는 0부터 시작하므로 rank - 1
            int currentIdx = currentUserRank - 1;
            int startIdx = Math.max(0, currentIdx - 2); // 위 2명
            int endIdx = Math.min(allRankings.size(), currentIdx + 4); // 본인 + 아래 3명

            for (int i = startIdx; i < endIdx; i++) {
                nearbyRankings.add(allRankings.get(i));
            }
        }

        return nearbyRankings;
    }
}

