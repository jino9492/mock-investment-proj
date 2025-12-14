package com.dongyang.mock.service;

import com.dongyang.mock.model.DAO.WatchlistDAO;
import com.dongyang.mock.model.DTO.WatchlistDTO;
import com.dongyang.mock.model.DTO.WatchlistStockDTO;
import com.dongyang.mock.util.db.DBManager;

import java.sql.SQLException;
import java.util.List;

/**
 * 관심종목 관련 비즈니스 로직 서비스
 * - 관심종목 추가/삭제
 * - 관심종목 조회
 * - 트랜잭션 관리
 */
public class WatchlistService {
    private final WatchlistDAO watchlistDAO = new WatchlistDAO();

    /**
     * 관심종목 추가
     * @param memId 회원 ID
     * @param stockId 종목 ID
     * @return 추가 성공 시 true, 실패 시 false
     * @throws SQLException
     */
    public boolean addWatchlist(int memId, long stockId) throws SQLException {
        return DBManager.executeInTransaction(conn -> {
            // 중복 체크
            if (watchlistDAO.isWatchlistExists(memId, stockId, conn)) {
                return DBManager.TransactionResult.rollback(Boolean.FALSE);
            }
            
            int result = watchlistDAO.insertWatchlist(memId, stockId, conn);
            if (result > 0) {
                return DBManager.TransactionResult.commit(Boolean.TRUE);
            }
            return DBManager.TransactionResult.rollback(Boolean.FALSE);
        });
    }

    /**
     * 관심종목 삭제
     * @param memId 회원 ID
     * @param stockId 종목 ID
     * @return 삭제 성공 시 true, 실패 시 false
     * @throws SQLException
     */
    public boolean removeWatchlist(int memId, long stockId) throws SQLException {
        return DBManager.executeInTransaction(conn -> {
            int result = watchlistDAO.deleteWatchlist(memId, stockId, conn);
            if (result > 0) {
                return DBManager.TransactionResult.commit(Boolean.TRUE);
            }
            return DBManager.TransactionResult.rollback(Boolean.FALSE);
        });
    }

    /**
     * 관심종목 여부 확인
     * @param memId 회원 ID
     * @param stockId 종목 ID
     * @return 관심종목이면 true, 아니면 false
     * @throws SQLException
     */
    public boolean isWatchlist(int memId, long stockId) throws SQLException {
        return DBManager.withConnection(conn -> 
            watchlistDAO.isWatchlistExists(memId, stockId, conn));
    }

    /**
     * 특정 회원의 모든 관심종목 조회
     * @param memId 회원 ID
     * @return 관심종목 목록
     * @throws SQLException
     */
    public List<WatchlistDTO> getWatchlistByMember(int memId) throws SQLException {
        return DBManager.withConnection(conn -> 
            watchlistDAO.findWatchlistByMember(memId, conn));
    }

    /**
     * 특정 회원의 관심종목 목록 조회 (종목 정보 및 현재가 포함)
     * @param memId 회원 ID
     * @return 관심종목 목록 (종목 정보 포함)
     * @throws SQLException
     */
    public List<WatchlistStockDTO> getWatchlistStocksByMember(int memId) throws SQLException {
        return DBManager.withConnection(conn -> 
            watchlistDAO.findWatchlistStocksByMember(memId, conn));
    }
}
