package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.StockDetailDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public class StockDetailDAO {

    public int[] insertStockDetailBatch(List<StockDetailDTO> dataList, Connection conn) throws SQLException {
        if (dataList == null || dataList.isEmpty()) {
            return new int[0];
        }

        final String sql = "INSERT INTO stock_detail (" +
                "stock_id, company_name, face_value, settlement_month, market_cap, " +
                "listing_date, founded_year, total_shares, shares_as_of, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE" +
                "    company_name = VALUES(company_name)," +
                "    face_value = VALUES(face_value)," +
                "    settlement_month = VALUES(settlement_month)," +
                "    market_cap = VALUES(market_cap)," +
                "    listing_date = VALUES(listing_date)," +
                "    founded_year = VALUES(founded_year)," +
                "    total_shares = VALUES(total_shares)," +
                "    shares_as_of = VALUES(shares_as_of)," +
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

                // 7. founded_year (VARCHAR NULL)
                pstmt.setString(7, data.getFoundedYear());

                // 8. total_shares (BIGINT NULL)
                if (data.getTotalShares() != null) {
                    pstmt.setLong(8, data.getTotalShares());
                } else {
                    pstmt.setNull(8, Types.BIGINT);
                }

                // 9. shares_as_of (DATE NULL)
                pstmt.setDate(9, data.getSharesAsOf());

                // 10. description (TEXT NULL)
                pstmt.setString(10, data.getDescription());

                pstmt.addBatch();
            }

            return pstmt.executeBatch();

        } catch (SQLException e) {
            System.err.println("종목 상세 데이터 배치 저장/갱신 실패: " + e.getMessage());
            throw e;
        }
    }
}