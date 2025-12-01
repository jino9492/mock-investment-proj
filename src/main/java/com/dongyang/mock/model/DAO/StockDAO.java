package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.StockDTO;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Map<Long, String> getAllStockSymbolMap(Connection conn) throws SQLException {
        final String sql = "SELECT stock_id, stock_symbol FROM stock";
        Map<Long, String> symbolMap = new HashMap<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // key: stock_id, value: stock_symbol
                symbolMap.put(rs.getLong("stock_id"), rs.getString("stock_symbol"));
            }
        }
        return symbolMap;
    }

    public Date getListingDateById(Long id, Connection conn) throws SQLException {
        final String sql = "SELECT listing_date FROM stock WHERE stock_id = ?";
        ResultSet rs = null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Date(rs.getTimestamp("listing_date").getTime());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int getStockCount(Connection conn) throws SQLException {
        final String sql =  "SELECT COUNT(*) FROM stock";
        ResultSet rs = null;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}