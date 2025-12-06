package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.StockCodeDTO;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StockCodeDAO {
    public int[] insertStockCodeBatch(List<StockCodeDTO> dataList, Connection conn) throws SQLException {
        if (dataList == null || dataList.isEmpty()) {
            return new int[0];
        }

        final String sql = "INSERT INTO stock_code (code, code_name, description) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE" +
                "    code_name = VALUES(code_name)," +
                "    description = VALUES(description);";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (StockCodeDTO data : dataList) {
                pstmt.setString(1, data.getCode());
                pstmt.setString(2, data.getCodeName());
                pstmt.setString(3, data.getDescription());

                pstmt.addBatch();
            }

            // 배치 실행 (커밋/롤백은 DBManager가 처리)
            return pstmt.executeBatch();

        } catch (SQLException e) {
            System.err.println("업종코드 데이터 배치 저장/갱신 실패: " + e.getMessage());
            throw e; // DBManager가 트랜잭션 롤백을 처리하도록 예외를 던지기
        }
    }

    public Long getStockIdByStockCodeName(String codeName, Connection conn) throws SQLException {
        final String sql = "SELECT stock_code_id FROM stock_code WHERE code_name = ?";
        Long stockCodeId = null;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (codeName == null || codeName.trim().isEmpty()) {
                return null;
            }

            pstmt.setString(1, codeName);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // 결과가 있으면 stock_code_id (BIGINT)를 Long으로 추출
                    stockCodeId = rs.getLong("stock_code_id");
                }
            }
        } catch (SQLException e) {
            System.err.println("업종 코드 ID 조회 실패 (Code Name: " + codeName + "): " + e.getMessage());
            throw e;
        }

        // 조회된 ID가 없으면 null을 반환합니다.
        return stockCodeId;
    }

    public Map<String, Long> getAllStockCodeIdMap(Connection conn) throws SQLException {
        final String sql = "SELECT stock_code_id, code_name FROM stock_code";
        Map<String, Long> codeIdMap = new HashMap<>();

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                // key: code_name, value: stock_code_id
                codeIdMap.put(rs.getString("code_name"), rs.getLong("stock_code_id"));
            }
        }
        return codeIdMap;
    }
}
