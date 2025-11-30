package com.dongyang.mock.model.DAO;
import com.dongyang.mock.model.DTO.IndexDTO;
import com.dongyang.mock.util.db.DBManager; // DBManager 사용 가정
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IndexHistoryDAO {
    public Map<String, List<Double>> findRecentIndexData(String indexCode, int limit, Connection conn) {
        Map<String, List<Double>> result = new HashMap<>();
        List<Double> prices = new ArrayList<>();
        List<Double> overnights = new ArrayList<>();

        String sql = "SELECT CURRENT_PRICE, OVERNIGHT FROM STOCK_INDEX_HISTORY " +
                "WHERE INDEX_CODE = ? " +
                "ORDER BY TRADING_TIME DESC LIMIT ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, indexCode);
            pstmt.setInt(2, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    prices.add(rs.getDouble("CURRENT_PRICE"));
                    overnights.add(rs.getDouble("OVERNIGHT"));
                }
            }
        } catch (SQLException e) {
            System.err.println("스파크라인 데이터 조회 실패: " + e.getMessage());
            // 오류 발생 시 빈 맵 반환
            return result;
        }

        // 스파크라인은 시간 순서대로(오래된 순) 그려지므로, 리스트를 역순으로 정렬
        java.util.Collections.reverse(prices);
        java.util.Collections.reverse(overnights);

        result.put("prices", prices);
        result.put("overnights", overnights);

        return result;
    }

    public String findLatestContractTime(String indexName, Connection conn) throws SQLException {
        String sql = "SELECT TRADING_TIME FROM STOCK_INDEX_HISTORY " +
                "WHERE INDEX_CODE = ? " +
                "ORDER BY TRADING_TIME DESC LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, indexName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp latestTime = rs.getTimestamp("TRADING_TIME");
                    if (latestTime != null) {
                        LocalDateTime localDateTime = latestTime.toLocalDateTime();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                        return localDateTime.format(formatter);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("최신 체결 시간 조회 실패: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public int[] saveOrUpdateIndexBatch(List<IndexDTO> dataList, Connection conn) throws SQLException {
        if (dataList == null || dataList.isEmpty()) {
            return new int[0];
        }

        final String sql = "INSERT INTO STOCK_INDEX_HISTORY " +
                "(INDEX_CODE, TRADING_TIME, CURRENT_PRICE, OVERNIGHT) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "CURRENT_PRICE = VALUES(CURRENT_PRICE), OVERNIGHT = VALUES(OVERNIGHT)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (IndexDTO data : dataList) {
                pstmt.setString(1, data.getIndexCode());
                pstmt.setTimestamp(2, data.getTradingTime());
                pstmt.setBigDecimal(3, data.getCurrentPrice());

                if (data.getOvernight() != null) {
                    pstmt.setBigDecimal(4, data.getOvernight());
                } else {
                    pstmt.setNull(4, java.sql.Types.DECIMAL);
                }

                pstmt.addBatch();
            }

            // 배치 실행 (커밋/롤백은 DBManager가 처리)
            return pstmt.executeBatch();

        } catch (SQLException e) {
            System.err.println("지수 데이터 배치 저장/갱신 실패: " + e.getMessage());
            throw e; // DBManager가 트랜잭션 롤백을 처리하도록 예외를 던지기
        }
    }
}