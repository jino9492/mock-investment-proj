package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.StockFinancialSummaryDTO;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

public class StockFinancialSummaryDAO {

    public int[] insertSummaryBatch(List<StockFinancialSummaryDTO> dataList, Connection conn) throws SQLException {
        if (dataList == null || dataList.isEmpty()) {
            return new int[0];
        }

        // 컬럼 목록: stock_id, revenue, operating_profit, credit_ratio
        final String sql = "INSERT INTO stock_financial_summary (" +
                "stock_id, revenue, operating_profit, credit_ratio) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE" +
                "    revenue = VALUES(revenue)," +
                "    operating_profit = VALUES(operating_profit)," +
                "    credit_ratio = VALUES(credit_ratio)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (StockFinancialSummaryDTO data : dataList) {

                // 1. stock_id (BIGINT NOT NULL / PK)
                pstmt.setLong(1, data.getStockId());

                // 2. revenue (BIGINT NULL)
                if (data.getRevenue() != null) {
                    pstmt.setLong(2, data.getRevenue());
                } else {
                    pstmt.setNull(2, Types.BIGINT);
                }

                // 3. operating_profit (BIGINT NULL)
                if (data.getOperatingProfit() != null) {
                    pstmt.setLong(3, data.getOperatingProfit());
                } else {
                    pstmt.setNull(3, Types.BIGINT);
                }

                // 4. credit_ratio (DECIMAL NULL)
                if (data.getCreditRatio() != null) {
                    pstmt.setBigDecimal(4, data.getCreditRatio());
                } else {
                    pstmt.setNull(4, Types.DECIMAL);
                }

                pstmt.addBatch();
            }

            return pstmt.executeBatch();

        } catch (SQLException e) {
            System.err.println("재무 요약 데이터 배치 저장/갱신 실패: " + e.getMessage());
            throw e;
        }
    }

    private StockFinancialSummaryDTO mapResultSetToDTO(ResultSet rs) throws SQLException {
        StockFinancialSummaryDTO dto = new StockFinancialSummaryDTO();
        dto.setStockId(rs.getLong("stock_id"));

        // NULL 가능성이 있는 BIGINT 필드 처리
        Long revenue = rs.getLong("revenue");
        if (!rs.wasNull()) dto.setRevenue(revenue);

        Long operatingProfit = rs.getLong("operating_profit");
        if (!rs.wasNull()) dto.setOperatingProfit(operatingProfit);

        // NULL 가능성이 있는 DECIMAL 필드 처리
        BigDecimal creditRatio = rs.getBigDecimal("credit_ratio");
        if (!rs.wasNull()) dto.setCreditRatio(creditRatio);

        dto.setCreatedAt(rs.getTimestamp("created_at"));
        dto.setUpdatedAt(rs.getTimestamp("updated_at"));

        return dto;
    }

    public StockFinancialSummaryDTO selectSummaryById(Long stockId, Connection conn) throws SQLException {
        final String sql = "SELECT stock_id, revenue, operating_profit, credit_ratio, created_at, updated_at " +
                "FROM stock_financial_summary WHERE stock_id = ?";

        StockFinancialSummaryDTO summary = null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, stockId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    summary = mapResultSetToDTO(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("재무 요약 조회 실패 (Stock ID: " + stockId + "): " + e.getMessage());
            throw e;
        }

        return summary;
    }
}