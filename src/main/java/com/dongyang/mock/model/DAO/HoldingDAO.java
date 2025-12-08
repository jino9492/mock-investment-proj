package com.dongyang.mock.model.DAO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;

import static com.dongyang.mock.util.db.DBManager.close;

/**
 * 보유 종목 데이터 접근 객체 (DAO)
 * - 보유 종목 추가/업데이트
 * - 보유 종목 수량 감소
 * - 보유 종목 조회
 */
public class HoldingDAO {

    /**
     * 보유 종목 추가 또는 업데이트 (매수 시)
     * - 기존 보유가 있으면 수량 증가 및 평균단가 재계산
     * - 기존 보유가 없으면 신규 생성
     * @param memId 회원 ID
     * @param stockId 종목 ID
     * @param quantity 추가할 수량
     * @param buyPrice 매수 가격
     * @param conn DB 연결
     * @throws SQLException
     */
    public void insertOrUpdateHolding(int memId, long stockId, long quantity, 
                                      BigDecimal buyPrice, Connection conn) throws SQLException {
        // 기존 보유 정보 조회
        HoldingInfo existing = findHolding(memId, stockId, conn);
        
        if (existing != null) {
            // 기존 보유가 있는 경우: 수량 증가 및 평균단가 재계산
            long newQuantity = existing.quantity + quantity;
            // 가중평균 계산: (기존금액 + 신규금액) / (기존수량 + 신규수량)
            BigDecimal existingTotal = existing.avgBuyPrice.multiply(BigDecimal.valueOf(existing.quantity));
            BigDecimal newTotal = buyPrice.multiply(BigDecimal.valueOf(quantity));
            BigDecimal totalAmount = existingTotal.add(newTotal);
            BigDecimal newAvgPrice = totalAmount.divide(BigDecimal.valueOf(newQuantity), 2, RoundingMode.HALF_UP);
            // total_buy_amount = newQuantity * newAvgPrice
            BigDecimal newTotalBuyAmount = newAvgPrice.multiply(BigDecimal.valueOf(newQuantity));
            
            String sql = "UPDATE holding SET quantity = ?, avg_buy_price = ?, total_buy_amount = ? " +
                        "WHERE mem_id = ? AND stock_id = ?";
            
            PreparedStatement pstmt = null;
            try {
                pstmt = conn.prepareStatement(sql);
                pstmt.setLong(1, newQuantity);
                pstmt.setBigDecimal(2, newAvgPrice);
                pstmt.setBigDecimal(3, newTotalBuyAmount);
                pstmt.setInt(4, memId);
                pstmt.setLong(5, stockId);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("보유 종목 업데이트에 실패했습니다.");
                }
            } finally {
                close(null, pstmt);
            }
        } else {
            // 기존 보유가 없는 경우: 신규 생성
            // total_buy_amount = quantity * avg_buy_price
            BigDecimal totalBuyAmount = buyPrice.multiply(BigDecimal.valueOf(quantity));
            
            String sql = "INSERT INTO holding (mem_id, stock_id, quantity, avg_buy_price, total_buy_amount) " +
                         "VALUES (?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = null;
            try {
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, memId);
                pstmt.setLong(2, stockId);
                pstmt.setLong(3, quantity);
                pstmt.setBigDecimal(4, buyPrice);
                pstmt.setBigDecimal(5, totalBuyAmount);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("보유 종목 생성에 실패했습니다.");
                }
            } finally {
                close(null, pstmt);
            }
        }
    }

    /**
     * 보유 종목 수량 감소 (매도 시)
     * @param memId 회원 ID
     * @param stockId 종목 ID
     * @param quantity 감소할 수량
     * @param conn DB 연결
     * @throws SQLException
     */
    public void decreaseHolding(int memId, long stockId, long quantity, Connection conn) throws SQLException {
        // 기존 보유 정보 조회
        HoldingInfo existing = findHolding(memId, stockId, conn);
        
        if (existing == null) {
            throw new SQLException("보유하지 않은 종목입니다.");
        }
        
        if (existing.quantity < quantity) {
            throw new SQLException("보유 수량이 부족합니다.");
        }
        
        long newQuantity = existing.quantity - quantity;
        
        if (newQuantity == 0) {
            // 수량이 0이 되면 삭제 (또는 유지할 수도 있음 - 여기서는 삭제)
            String sql = "DELETE FROM holding WHERE mem_id = ? AND stock_id = ?";
            
            PreparedStatement pstmt = null;
            try {
                pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, memId);
                pstmt.setLong(2, stockId);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("보유 종목 삭제에 실패했습니다.");
                }
            } finally {
                close(null, pstmt);
            }
        } else {
            // 수량만 감소 (평균단가는 유지)
            String sql = "UPDATE holding SET quantity = ? WHERE mem_id = ? AND stock_id = ?";
            
            PreparedStatement pstmt = null;
            try {
                pstmt = conn.prepareStatement(sql);
                pstmt.setLong(1, newQuantity);
                pstmt.setInt(2, memId);
                pstmt.setLong(3, stockId);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("보유 종목 업데이트에 실패했습니다.");
                }
            } finally {
                close(null, pstmt);
            }
        }
    }

    /**
     * 특정 회원/종목의 보유 정보 조회
     * @param memId 회원 ID
     * @param stockId 종목 ID
     * @param conn DB 연결
     * @return 보유 정보 (없으면 null)
     * @throws SQLException
     */
    public HoldingInfo findHolding(int memId, long stockId, Connection conn) throws SQLException {
        String sql = "SELECT quantity, avg_buy_price FROM holding " +
                     "WHERE mem_id = ? AND stock_id = ?";
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memId);
            pstmt.setLong(2, stockId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new HoldingInfo(
                    rs.getLong("quantity"),
                    rs.getBigDecimal("avg_buy_price")
                );
            }
            return null;
        } finally {
            close(null, pstmt, rs);
        }
    }

    /**
     * 보유 정보를 담는 내부 클래스
     */
    public static class HoldingInfo {
        public final long quantity;
        public final BigDecimal avgBuyPrice;

        public HoldingInfo(long quantity, BigDecimal avgBuyPrice) {
            this.quantity = quantity;
            this.avgBuyPrice = avgBuyPrice;
        }
    }
}

