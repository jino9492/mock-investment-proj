package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.LeagueDTO;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.dongyang.mock.util.db.DBManager.close;

/**
 * 랭킹 데이터 접근 객체 (DAO)
 * - 주변 랭킹 조회
 * - 수익률 기준 순위 계산
 */
public class RankingDAO {

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
                "COALESCE(initial_cap.initial_capital, 10000000) as initial_capital " +
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
            while (rs.next()) {
                int userId = rs.getInt("mem_id");
                // decimal(18,2)를 정확하게 처리
                BigDecimal totalAssetBD = rs.getBigDecimal("total_asset");
                BigDecimal initialCapitalBD = rs.getBigDecimal("initial_capital");
                long totalAsset = totalAssetBD != null ? totalAssetBD.longValue() : 0L;
                long initialCapital = initialCapitalBD != null ? initialCapitalBD.longValue() : 10000000L;
                double returnRate = initialCapital > 0 ? ((double) (totalAsset - initialCapital) / initialCapital) * 100.0 : 0.0;
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

