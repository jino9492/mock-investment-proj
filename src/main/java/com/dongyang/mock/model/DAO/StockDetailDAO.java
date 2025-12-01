package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.StockDetailDTO;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

public class StockDetailDAO {

    public int[] insertStockDetailBatch(List<StockDetailDTO> dataList, Connection conn) throws SQLException {
        if (dataList == null || dataList.isEmpty()) {
            return new int[0];
        }

        // 컬럼 목록: stock_id, company_name, face_value, settlement_month, market_cap, listing_date, description
        final String sql = "INSERT INTO stock_detail (" +
                "stock_id, company_name, face_value, settlement_month, market_cap, " +
                "listing_date, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE" +
                "    company_name = VALUES(company_name)," +
                "    face_value = VALUES(face_value)," +
                "    settlement_month = VALUES(settlement_month)," +
                "    market_cap = VALUES(market_cap)," +
                "    listing_date = VALUES(listing_date)," +
                "    description = VALUES(description)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (StockDetailDTO data : dataList) {

                // 1. stock_id (BIGINT NOT NULL / PK)
                pstmt.setLong(1, data.getStockId());

                // 2. company_name (VARCHAR NOT NULL)
                pstmt.setString(2, data.getCompanyName());

                // 3. face_value (DECIMAL NULL)
                if (data.getFaceValue() != null) {
                    pstmt.setBigDecimal(3, data.getFaceValue());
                } else {
                    pstmt.setNull(3, Types.DECIMAL);
                }

                // 4. settlement_month (VARCHAR NULL)
                pstmt.setString(4, data.getSettlementMonth());

                // 5. market_cap (VARCHAR NULL)
                pstmt.setString(5, data.getMarketCap());

                // 6. listing_date (DATE NULL)
                pstmt.setDate(6, data.getListingDate());

                // 7. description (TEXT NULL)
                pstmt.setString(7, data.getDescription());

                pstmt.addBatch();
            }

            return pstmt.executeBatch();

        } catch (SQLException e) {
            System.err.println("종목 상세 데이터 배치 저장/갱신 실패: " + e.getMessage());
            throw e;
        }
    }

    public StockDetailDTO selectStockDetailById(Long stockId, Connection conn) throws SQLException {
        final String sql = "SELECT stock_id, company_name, face_value, settlement_month, market_cap, " +
                "listing_date, description, created_at, updated_at " +
                "FROM stock_detail WHERE stock_id = ?";

        StockDetailDTO detail = null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, stockId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    detail = mapResultSetToStockDetailDTO(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Stock Detail ID " + stockId + " 조회 실패: " + e.getMessage());
            throw e;
        }

        return detail;
    }

    private StockDetailDTO mapResultSetToStockDetailDTO(ResultSet rs) throws SQLException {
        StockDetailDTO dto = new StockDetailDTO();
        dto.setStockId(rs.getLong("stock_id"));
        dto.setCompanyName(rs.getString("company_name"));

        // NULL 가능성이 있는 DECIMAL 필드 처리
        BigDecimal faceValue = rs.getBigDecimal("face_value");
        if (!rs.wasNull()) {
            dto.setFaceValue(faceValue);
        }

        dto.setSettlementMonth(rs.getString("settlement_month"));
        dto.setMarketCap(rs.getString("market_cap"));
        dto.setListingDate(rs.getDate("listing_date"));
        dto.setDescription(rs.getString("description"));
        dto.setCreatedAt(rs.getTimestamp("created_at"));
        dto.setUpdatedAt(rs.getTimestamp("updated_at"));

        return dto;
    }

    public Boolean existsByStockCode(Long stkId, Connection conn) throws SQLException {
        final String sql = "SELECT COUNT(*) FROM stock_detail WHERE stock_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, stkId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}