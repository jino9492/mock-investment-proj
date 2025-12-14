package com.dongyang.mock.model.DAO;
import com.dongyang.mock.model.DTO.StockChartDTO;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StockChartDAO {
    public List<StockChartDTO> findRecentStockChartData(Long stkId, int limit, Connection conn) {
        List<StockChartDTO> stockChartDTOS = new ArrayList<>();

        String sql = "SELECT CHART_TIME, OPEN_PRICE, HIGH_PRICE, LOW_PRICE, CLOSE_PRICE, VOLUME, CHANGE_RATE FROM stock_chart_data " +
                "WHERE STK_ID = ? " +
                "ORDER BY CHART_TIME DESC LIMIT ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, stkId);
            pstmt.setInt(2, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    stockChartDTOS.add(new StockChartDTO(
                            stkId,
                            rs.getTimestamp("CHART_TIME"),
                            rs.getBigDecimal("OPEN_PRICE"),
                            rs.getBigDecimal("HIGH_PRICE"),
                            rs.getBigDecimal("LOW_PRICE"),
                            rs.getBigDecimal("CLOSE_PRICE"),
                            rs.getLong("VOLUME"),
                            rs.getBigDecimal("CHANGE_RATE")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("차트 데이터 조회 실패: " + e.getMessage());
            return null;
        }

        Collections.sort(stockChartDTOS, Comparator.comparing(StockChartDTO::getChartTime).reversed());

        return stockChartDTOS;
    }

    public String findLatestChartTime(Long stkId, Connection conn) throws SQLException {
        String sql = "SELECT CHART_TIME FROM stock_chart_data " +
                "WHERE STK_ID = ? " +
                "ORDER BY CHART_TIME DESC LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, stkId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp latestTime = rs.getTimestamp("CHART_TIME");
                    if (latestTime != null) {
                        LocalDateTime localDateTime = latestTime.toLocalDateTime();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                        return localDateTime.format(formatter);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("최신 차트 시간 조회 실패: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public int[] insertStockChartBatch(List<StockChartDTO> dataList, Connection conn) throws SQLException {
        if (dataList == null || dataList.isEmpty()) {
            return new int[0];
        }

        // DTO 필드 (OHLCV + 등락률)에 맞게 SQL 수정
        final String sql = "INSERT INTO stock_chart_data " +
                "(STK_ID, CHART_TIME, OPEN_PRICE, HIGH_PRICE, LOW_PRICE, CLOSE_PRICE, VOLUME, CHANGE_RATE) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "OPEN_PRICE = VALUES(OPEN_PRICE), HIGH_PRICE = VALUES(HIGH_PRICE), LOW_PRICE = VALUES(LOW_PRICE), " +
                "CLOSE_PRICE = VALUES(CLOSE_PRICE), VOLUME = VALUES(VOLUME), CHANGE_RATE = VALUES(CHANGE_RATE)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (StockChartDTO data : dataList) {
                pstmt.setLong(1, data.getStkId());
                pstmt.setTimestamp(2, data.getChartTime());
                pstmt.setBigDecimal(3, data.getOpenPrice());
                pstmt.setBigDecimal(4, data.getHighPrice());
                pstmt.setBigDecimal(5, data.getLowPrice());
                pstmt.setBigDecimal(6, data.getClosePrice());
                pstmt.setLong(7, data.getVolume());
                if (data.getChangeRate() != null) {
                    pstmt.setBigDecimal(8, data.getChangeRate());
                } else {
                    pstmt.setNull(8, java.sql.Types.DECIMAL);
                }

                pstmt.addBatch();
            }

            return pstmt.executeBatch();

        } catch (SQLException e) {
            System.err.println("캔들 차트 데이터 배치 저장/갱신 실패: " + e.getMessage());
            throw e;
        }
    }

    public static class ChangeRateTarget {
        private final long stkId;
        private final String chartTimeStr; // CHAR_TIME 문자열 (YYYYMMDDHHMISS)
        private final BigDecimal closePrice; // 현재 종가

        public ChangeRateTarget(long stkId, String chartTimeStr, BigDecimal closePrice) {
            this.stkId = stkId;
            this.chartTimeStr = chartTimeStr;
            this.closePrice = closePrice;
        }

        public long getStkId() { return stkId; }
        public String getChartTimeStr() { return chartTimeStr; }
        public BigDecimal getClosePrice() { return closePrice; }
    }

    public List<ChangeRateTarget> findZeroChangeRateTargets(Connection conn) throws SQLException {
        List<ChangeRateTarget> targets = new ArrayList<>();

        // 등락률이 0인 레코드를 찾는 SQL
        String sql = "SELECT STK_ID, CHART_TIME, CLOSE_PRICE FROM stock_chart_data " +
                "WHERE CHANGE_RATE = 0.00";

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // CHART_TIME을 문자열로 가져와서 이후 LocalDateTime 변환에 사용
                targets.add(new ChangeRateTarget(
                        rs.getLong("STK_ID"),
                        rs.getString("CHART_TIME"), // DB에서 String 또는 Timestamp로 가져온 후 String으로 변환 필요
                        rs.getBigDecimal("CLOSE_PRICE")
                ));
            }
        }
        return targets;
    }

    public BigDecimal findPreviousClosePrice(long stkId, String chartTimeStr, Connection conn) throws SQLException {
        // CHART_TIME이 현재 시간보다 작으면서, 가장 최근인 데이터를 조회
        String sql = "SELECT CLOSE_PRICE FROM stock_chart_data " +
                "WHERE STK_ID = ? AND CHART_TIME < ? " +
                "ORDER BY CHART_TIME DESC LIMIT 1";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, stkId);
            pstmt.setString(2, chartTimeStr);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("CLOSE_PRICE");
                }
            }
        }
        return null;
    }

    public int updateChangeRate(long stkId, String chartTimeStr, BigDecimal newChangeRate, Connection conn) throws SQLException {
        String sql = "UPDATE stock_chart_data SET CHANGE_RATE = ? " +
                "WHERE STK_ID = ? AND CHART_TIME = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, newChangeRate.setScale(2, java.math.RoundingMode.HALF_UP));
            pstmt.setLong(2, stkId);
            pstmt.setString(3, chartTimeStr);

            return pstmt.executeUpdate();
        }
    }
}
