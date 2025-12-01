package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.StockFinancialIndicatorDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public class StockFinancialIndicatorDAO {

    public int[] insertIndicatorBatch(List<StockFinancialIndicatorDTO> dataList, Connection conn) throws SQLException {
        if (dataList == null || dataList.isEmpty()) {
            return new int[0];
        }

        // 복합 PK (stock_id, indicator_date)를 사용하여 ON DUPLICATE KEY UPDATE를 적용합니다.
        final String sql = "INSERT INTO stock_financial_indicator (" +
                "stock_id, indicator_date, per, pbr, roe, eps, bps) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE" +
                "    per = VALUES(per)," +
                "    pbr = VALUES(pbr)," +
                "    roe = VALUES(roe)," +
                "    eps = VALUES(eps)," +
                "    bps = VALUES(bps)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (StockFinancialIndicatorDTO data : dataList) {

                // 1. stock_id (BIGINT NOT NULL / 복합 PK & FK)
                pstmt.setLong(1, data.getStockId());

                // 2. indicator_date (DATE NOT NULL / 복합 PK)
                pstmt.setDate(2, data.getIndicatorDate());

                // 3. per (DECIMAL NULL)
                if (data.getPer() != null) {
                    pstmt.setBigDecimal(3, data.getPer());
                } else {
                    pstmt.setNull(3, Types.DECIMAL);
                }

                // 4. pbr (DECIMAL NULL)
                if (data.getPbr() != null) {
                    pstmt.setBigDecimal(4, data.getPbr());
                } else {
                    pstmt.setNull(4, Types.DECIMAL);
                }

                // 5. roe (DECIMAL NULL)
                if (data.getRoe() != null) {
                    pstmt.setBigDecimal(5, data.getRoe());
                } else {
                    pstmt.setNull(5, Types.DECIMAL);
                }

                // 6. eps (DECIMAL NULL)
                if (data.getEps() != null) {
                    pstmt.setBigDecimal(6, data.getEps());
                } else {
                    pstmt.setNull(6, Types.DECIMAL);
                }

                // 7. bps (DECIMAL NULL)
                if (data.getBps() != null) {
                    pstmt.setBigDecimal(7, data.getBps());
                } else {
                    pstmt.setNull(7, Types.DECIMAL);
                }

                pstmt.addBatch();
            }

            return pstmt.executeBatch();

        } catch (SQLException e) {
            System.err.println("재무 지표 배치 저장/갱신 실패: " + e.getMessage());
            throw e;
        }
    }
}
