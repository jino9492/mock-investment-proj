package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.StockFinancialIndicatorDTO;

import java.math.BigDecimal;
import java.sql.*;
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

    public StockFinancialIndicatorDTO selectIndicatorByStockId(Long stockId, Connection conn) throws SQLException {
        final String sql = "SELECT stock_id, indicator_date, per, pbr, roe, eps, bps, created_at, updated_at " +
                "FROM stock_financial_indicator " +
                "WHERE stock_id = ? " +
                "ORDER BY indicator_date DESC " +
                "LIMIT 1";

        StockFinancialIndicatorDTO indicator = null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, stockId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    indicator = mapResultSetToDTO(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("최근 재무 지표 조회 실패 (Stock ID: " + stockId + "): " + e.getMessage());
            throw e;
        }

        return indicator;
    }

    private StockFinancialIndicatorDTO mapResultSetToDTO(ResultSet rs) throws SQLException {
        StockFinancialIndicatorDTO dto = new StockFinancialIndicatorDTO();
        dto.setStockId(rs.getLong("stock_id"));
        dto.setIndicatorDate(rs.getDate("indicator_date"));

        // NULL 가능성이 있는 DECIMAL 필드 처리
        BigDecimal per = rs.getBigDecimal("per");
        if (!rs.wasNull()) dto.setPer(per);

        BigDecimal pbr = rs.getBigDecimal("pbr");
        if (!rs.wasNull()) dto.setPbr(pbr);

        BigDecimal roe = rs.getBigDecimal("roe");
        if (!rs.wasNull()) dto.setRoe(roe);

        BigDecimal eps = rs.getBigDecimal("eps");
        if (!rs.wasNull()) dto.setEps(eps);

        BigDecimal bps = rs.getBigDecimal("bps");
        if (!rs.wasNull()) dto.setBps(bps);

        dto.setCreatedAt(rs.getTimestamp("created_at"));
        dto.setUpdatedAt(rs.getTimestamp("updated_at"));

        return dto;
    }
}
