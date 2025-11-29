package com.dongyang.mock.model.DAO;
import com.dongyang.mock.model.DTO.IndexDailyDTO;
import com.dongyang.mock.util.db.DBManager; // DBManager 사용 가정
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IndexHistoryDAO {

    /**
     * 일별 지수 데이터를 DB에 저장/갱신합니다 (UPSERT).
     */
    public int saveOrUpdateIndex(IndexDailyDTO data) {
        String sql = "INSERT INTO STOCK_INDEX_HISTORY " +
                "(INDEX_CODE, TRADING_DATE, CLOSE_PRICE, VOLUME) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "CLOSE_PRICE = VALUES(CLOSE_PRICE), VOLUME = VALUES(VOLUME)";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, data.getIndexCode());
            pstmt.setDate(2, data.getTradingDate());
            pstmt.setBigDecimal(3, data.getClosePrice());

            if (data.getVolume() != null) {
                pstmt.setLong(4, data.getVolume());
            } else {
                pstmt.setNull(4, java.sql.Types.BIGINT);
            }
            return pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("지수 데이터 저장/갱신 실패: " + e.getMessage());
            return 0;
        }
    }

    /**
     * 스파크라인을 그리기 위해 최근 7일치 종가 리스트를 조회합니다.
     */
    public List<Double> findRecentClosePrices(String indexCode, int limit) {
        List<Double> prices = new ArrayList<>();
        // 최신 날짜 순으로 정렬 후 제한된 개수만큼 가져옵니다.
        String sql = "SELECT CLOSE_PRICE FROM STOCK_INDEX_HISTORY " +
                "WHERE INDEX_CODE = ? " +
                "ORDER BY TRADING_DATE DESC LIMIT ?";

        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, indexCode);
            pstmt.setInt(2, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    prices.add(rs.getDouble("CLOSE_PRICE"));
                }
            }
        } catch (SQLException e) {
            System.err.println("스파크라인 데이터 조회 실패: " + e.getMessage());
        }
        // 스파크라인은 시간 순서대로 그려지므로, 리스트를 역순으로 반환
        java.util.Collections.reverse(prices);
        return prices;
    }
}