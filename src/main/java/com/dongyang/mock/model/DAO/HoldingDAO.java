package com.dongyang.mock.model.DAO;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.dongyang.mock.util.db.DBManager.close;

/**
 * 보유 주식 데이터 접근 객체 (DAO)
 * - holding 테이블 CRUD 작업
 */
public class HoldingDAO {

    /**
     * 보유 주식 추가 또는 업데이트
     * - 기존 보유 주식이 있으면 수량과 평균 매수 가격 업데이트
     * - 없으면 새로 추가
     * 
     * @param memId 회원 ID
     * @param stockId 주식 ID
     * @param quantity 추가할 수량
     * @param buyPrice 매수 가격
     * @param conn 데이터베이스 연결
     * @return 영향받은 행 수
     */
    public int insertOrUpdateHolding(int memId, int stockId, int quantity, long buyPrice, Connection conn) throws SQLException {
        // 기존 보유 주식 확인
        String checkSql = "SELECT quantity, avg_buy_price FROM holding WHERE mem_id = ? AND stock_id = ?";
        PreparedStatement checkPstmt = null;
        ResultSet rs = null;
        
        int existingQuantity = 0;
        BigDecimal existingAvgPrice = BigDecimal.ZERO;
        
        try {
            checkPstmt = conn.prepareStatement(checkSql);
            checkPstmt.setInt(1, memId);
            checkPstmt.setInt(2, stockId);
            rs = checkPstmt.executeQuery();
            
            if (rs.next()) {
                existingQuantity = rs.getInt("quantity");
                existingAvgPrice = rs.getBigDecimal("avg_buy_price");
            }
        } finally {
            close(null, checkPstmt, rs);
        }
        
        // 평균 매수 가격 계산: (기존금액 + 신규금액) / (기존수량 + 신규수량)
        int newQuantity = existingQuantity + quantity;
        BigDecimal newAvgPrice;
        
        if (existingQuantity > 0) {
            BigDecimal existingTotal = existingAvgPrice.multiply(BigDecimal.valueOf(existingQuantity));
            BigDecimal newTotal = BigDecimal.valueOf(buyPrice).multiply(BigDecimal.valueOf(quantity));
            BigDecimal totalAmount = existingTotal.add(newTotal);
            newAvgPrice = totalAmount.divide(BigDecimal.valueOf(newQuantity), 2, BigDecimal.ROUND_HALF_UP);
        } else {
            newAvgPrice = BigDecimal.valueOf(buyPrice);
        }
        
        // INSERT 또는 UPDATE
        PreparedStatement pstmt = null;
        try {
            if (existingQuantity > 0) {
                // UPDATE
                String updateSql = "UPDATE holding SET quantity = ?, avg_buy_price = ? " +
                                   "WHERE mem_id = ? AND stock_id = ?";
                pstmt = conn.prepareStatement(updateSql);
                pstmt.setInt(1, newQuantity);
                pstmt.setBigDecimal(2, newAvgPrice);
                pstmt.setInt(3, memId);
                pstmt.setInt(4, stockId);
            } else {
                // INSERT
                String insertSql = "INSERT INTO holding (mem_id, stock_id, quantity, avg_buy_price) " +
                                   "VALUES (?, ?, ?, ?)";
                pstmt = conn.prepareStatement(insertSql);
                pstmt.setInt(1, memId);
                pstmt.setInt(2, stockId);
                pstmt.setInt(3, newQuantity);
                pstmt.setBigDecimal(4, newAvgPrice);
            }
            
            return pstmt.executeUpdate();
        } finally {
            close(null, pstmt, null);
        }
    }

    /**
     * 보유 주식 조회
     * 
     * @param memId 회원 ID
     * @param conn 데이터베이스 연결
     * @return 보유 주식 목록
     */
    public List<HoldingInfo> selectHoldings(int memId, Connection conn) throws SQLException {
        String sql = "SELECT h.mem_id, h.stock_id, s.stock_name, h.quantity, h.avg_buy_price " +
                     "FROM holding h " +
                     "INNER JOIN stock s ON h.stock_id = s.stock_id " +
                     "WHERE h.mem_id = ? AND h.quantity > 0 " +
                     "ORDER BY h.stock_id";
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<HoldingInfo> holdings = new ArrayList<>();
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                HoldingInfo holding = new HoldingInfo();
                holding.setMemId(rs.getInt("mem_id"));
                holding.setStockId(rs.getInt("stock_id"));
                holding.setStockName(rs.getString("stock_name"));
                holding.setQuantity(rs.getInt("quantity"));
                BigDecimal avgPrice = rs.getBigDecimal("avg_buy_price");
                holding.setAvgBuyPrice(avgPrice != null ? avgPrice.longValue() : 0L);
                holdings.add(holding);
            }
        } finally {
            close(null, pstmt, rs);
        }
        
        return holdings;
    }

    /**
     * 보유 주식 정보 DTO
     */
    public static class HoldingInfo {
        private int memId;
        private int stockId;
        private String stockName;
        private int quantity;
        private long avgBuyPrice;

        public int getMemId() { return memId; }
        public void setMemId(int memId) { this.memId = memId; }

        public int getStockId() { return stockId; }
        public void setStockId(int stockId) { this.stockId = stockId; }

        public String getStockName() { return stockName; }
        public void setStockName(String stockName) { this.stockName = stockName; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public long getAvgBuyPrice() { return avgBuyPrice; }
        public void setAvgBuyPrice(long avgBuyPrice) { this.avgBuyPrice = avgBuyPrice; }
    }
}

