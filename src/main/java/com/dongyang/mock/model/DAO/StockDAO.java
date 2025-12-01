package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.StockDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public class StockDAO {
    public int[] insertStockBatch(List<StockDTO> dataList, Connection conn) throws SQLException {
        if (dataList == null || dataList.isEmpty()) {
            return new int[0];
        }

        final String sql = "INSERT INTO stock (stock_symbol, stock_name, market_type, stock_code_id, listing_date, status) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "    stock_name = VALUES(stock_name)," +
                "    market_type = VALUES(market_type)," +
                "    stock_code_id = VALUES(stock_code_id)," +
                "    listing_date = VALUES(listing_date)," +
                "    status = VALUES(status)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (StockDTO data : dataList) {

                pstmt.setString(1, data.getStockSymbol());
                pstmt.setString(2, data.getStockName());
                pstmt.setString(3, data.getMarketType());

                // stock_code_id (BIGINT NULL) 처리
                if (data.getStockCodeId() != null) {
                    pstmt.setLong(4, data.getStockCodeId());
                } else {
                    pstmt.setNull(4, Types.BIGINT);
                }

                // listing_date (DATE NULL) 처리
                pstmt.setDate(5, data.getListingDate());

                pstmt.setString(6, data.getStatus());

                pstmt.addBatch();
            }

            return pstmt.executeBatch();

        } catch (SQLException e) {
            System.err.println("주식 데이터 배치 저장/갱신 실패: " + e.getMessage());
            throw e;
        }
    }
}