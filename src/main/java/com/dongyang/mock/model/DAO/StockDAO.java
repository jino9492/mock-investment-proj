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

    public Map<Long, String> selectAllStockSymbolMap(Connection conn) throws SQLException {
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

    public Date selectListingDateById(Long id, Connection conn) throws SQLException {
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

    public StockDTO selectStockById(Long stockId, Connection conn) throws SQLException {
        final String sql = "SELECT stock_id, stock_symbol, stock_name, market_type, stock_code_id, " +
                "listing_date, status, created_at, updated_at " +
                "FROM stock WHERE stock_id = ?";

        StockDTO stock = null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, stockId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stock = mapResultSetToStockDTO(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Stock ID " + stockId + " 조회 실패: " + e.getMessage());
            throw e;
        }

        return stock;
    }

    public StockDTO selectStockByCode(Long stockCode, Connection conn) throws SQLException {
        final String sql = "SELECT stock_id, stock_symbol, stock_name, market_type, stock_code_id, " +
                "listing_date, status, created_at, updated_at " +
                "FROM stock WHERE stock_code_id = ?";

        StockDTO stock = null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, stockCode);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stock = mapResultSetToStockDTO(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Stock Code " + stockCode + " 조회 실패: " + e.getMessage());
            throw e;
        }

        return stock;
    }

    public StockDTO selectStockBySymbol(String stockSymbol, Connection conn) throws SQLException {
        final String sql = "SELECT stock_id, stock_symbol, stock_name, market_type, stock_code_id, " +
                "listing_date, status, created_at, updated_at " +
                "FROM stock WHERE stock_symbol = ?";

        StockDTO stock = null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, stockSymbol);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stock = mapResultSetToStockDTO(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Stock Symbol " + stockSymbol + " 조회 실패: " + e.getMessage());
            throw e;
        }

        return stock;
    }

    public Long getStockIdByCode(String stockCode, Connection conn) throws SQLException {
        final String sql = "SELECT stock_id FROM stock WHERE stock_symbol = ?";
        Long stockId = null;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, stockCode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    stockId = rs.getLong("stock_id");
                    return stockId;
                }
            }
            catch (SQLException e) {
                return null;
            }
        }
        return null;
    }

    private StockDTO mapResultSetToStockDTO(ResultSet rs) throws SQLException {
        StockDTO dto = new StockDTO();
        dto.setStockId(rs.getLong("stock_id"));
        dto.setStockSymbol(rs.getString("stock_symbol"));
        dto.setStockName(rs.getString("stock_name"));
        dto.setMarketType(rs.getString("market_type"));

        // stock_code_id (BIGINT NULL) 처리
        Long stockCodeId = rs.getLong("stock_code_id");
        if (rs.wasNull()) {
            dto.setStockCodeId(null);
        } else {
            dto.setStockCodeId(stockCodeId);
        }

        dto.setListingDate(rs.getDate("listing_date"));
        dto.setStatus(rs.getString("status"));
        dto.setCreatedAt(rs.getTimestamp("created_at"));
        dto.setUpdatedAt(rs.getTimestamp("updated_at"));

        return dto;
    }

    public int getCount(Connection conn) throws  SQLException {
        final String sql = "SELECT count(*) FROM stock";
        int count = 0;

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            count = rs.getInt(1);

            return  count;
        } catch (SQLException e) {
            return 0;
        }
    }
}