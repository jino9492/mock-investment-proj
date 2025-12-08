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
     * @param balance 거래 후 잔액 (사용하지 않음, 호환성을 위해 유지)
     * @param conn DB 연결
     * @return 생성된 cash_history ID
     * @throws SQLException
     */
    public long insertCashHistory(int memId, String txType, BigDecimal amount, 
                                  BigDecimal balance, Connection conn) throws SQLException {
        System.out.println("[CashHistoryDAO] cash_history 저장 시작 - memId: " + memId + ", txType: " + txType + ", amount: " + amount + ", balance: " + balance);
        
        String sql = "INSERT INTO cash_history (mem_id, tx_type, amount, balance_after, created_at) " +
                     "VALUES (?, ?, ?, ?, NOW())";
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            System.out.println("[CashHistoryDAO] PreparedStatement 생성 중...");
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, memId);
            pstmt.setString(2, txType);
            pstmt.setBigDecimal(3, amount);
            pstmt.setBigDecimal(4, balance);
            
            System.out.println("[CashHistoryDAO] SQL 실행 중... (파라미터: memId=" + memId + ", txType=" + txType + ", amount=" + amount + ")");
            long startTime = System.currentTimeMillis();
            int affectedRows = pstmt.executeUpdate();
            long endTime = System.currentTimeMillis();
            System.out.println("[CashHistoryDAO] SQL 실행 완료. 영향받은 행: " + affectedRows + ", 소요 시간: " + (endTime - startTime) + "ms");
            
            if (affectedRows == 0) {
                throw new SQLException("현금 거래 내역 저장에 실패했습니다.");
            }
            
            System.out.println("[CashHistoryDAO] Generated Keys 조회 중...");
            rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                long id = rs.getLong(1);
                System.out.println("[CashHistoryDAO] cash_history 저장 완료. ID: " + id);
                return id;
            } else {
                throw new SQLException("현금 거래 내역 ID를 가져올 수 없습니다.");
            }
        } catch (SQLException e) {
            System.err.println("[CashHistoryDAO] cash_history 저장 중 SQL 오류 발생: " + e.getMessage());
            System.err.println("[CashHistoryDAO] SQL: " + sql);
            System.err.println("[CashHistoryDAO] 파라미터 - memId: " + memId + ", txType: " + txType + ", amount: " + amount);
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("[CashHistoryDAO] cash_history 저장 중 예상치 못한 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("현금 거래 내역 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
        } finally {
            close(null, pstmt, rs);
        }
    }
}

