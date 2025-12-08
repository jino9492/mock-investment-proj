package com.dongyang.mock.model.DAO;

import java.math.BigDecimal;
import java.sql.*;

import static com.dongyang.mock.util.db.DBManager.close;

/**
 * 현금 거래 내역 데이터 접근 객체 (DAO)
 * - 현금 거래 기록 저장
 */
public class CashHistoryDAO {

    /**
     * 현금 거래 내역 저장
     * @param memId 회원 ID
     * @param txType 거래 유형 ('BUY', 'SELL', 'DEPOSIT' 등)
     * @param amount 거래 금액 (매수는 음수, 매도는 양수)
     * @param balance 거래 후 잔액
     * @param conn DB 연결
     * @return 생성된 cash_history ID
     * @throws SQLException
     */
    public long insertCashHistory(int memId, String txType, BigDecimal amount, 
                                  BigDecimal balance, Connection conn) throws SQLException {
        String sql = "INSERT INTO cash_history (mem_id, tx_type, amount, balance, created_at) " +
                     "VALUES (?, ?, ?, ?, NOW())";
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, memId);
            pstmt.setString(2, txType);
            pstmt.setBigDecimal(3, amount);
            pstmt.setBigDecimal(4, balance);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("현금 거래 내역 저장에 실패했습니다.");
            }
            
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                throw new SQLException("현금 거래 내역 ID를 가져올 수 없습니다.");
            }
        } finally {
            close(null, pstmt, rs);
        }
    }
}

