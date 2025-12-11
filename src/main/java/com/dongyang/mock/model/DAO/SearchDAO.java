package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.StockDTO;
import com.dongyang.mock.util.db.DBManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SearchDAO {

    public List<StockDTO> search(String keyword) {
        List<StockDTO> list = new ArrayList<>();

        String sql = """
            SELECT stock_symbol, stock_name, market_type
            FROM stock
            WHERE stock_name LIKE ? OR stock_symbol LIKE ?
            LIMIT 20
        """;


        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String like = "%" + keyword + "%";

            pstmt.setString(1, like);
            pstmt.setString(2, like);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                StockDTO dto = new StockDTO();
                dto.setStockSymbol(rs.getString("stock_symbol"));
                dto.setStockName(rs.getString("stock_name"));
                dto.setMarketType(rs.getString("market_type"));
                list.add(dto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
