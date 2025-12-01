package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.StockFinancialSummaryDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
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
}