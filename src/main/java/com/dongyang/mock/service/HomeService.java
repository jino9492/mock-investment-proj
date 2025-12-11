package com.dongyang.mock.service;

import com.dongyang.mock.model.DAO.LeagueDAO;
import com.dongyang.mock.model.DTO.LeagueDTO;
import com.dongyang.mock.util.db.DBManager;

import java.sql.SQLException;
import java.util.List;

/**
 * 홈 페이지 관련 비즈니스 로직 서비스
 * - 계좌 요약 정보 조회
 * - 수익률 상위 종목 조회
 */
public class HomeService {
    private final LeagueDAO leagueDAO = new LeagueDAO();

    /**
     * 계좌 요약 정보 조회
     * @param memId 회원 ID
     * @return 계좌 현황 정보
     */
    public LeagueDTO.AccountSummary getAccountSummary(int memId) throws SQLException {
        return DBManager.withConnection(conn -> leagueDAO.selectAccountSummary(memId, conn));
    }

    /**
     * 수익률 상위 종목 조회
     * @param memId 회원 ID
     * @param limit 조회할 개수 (기본 3개)
     * @return 수익률 상위 종목 목록
     */
    public List<LeagueDTO.TopReturnStock> getTopReturnStocks(int memId, int limit) throws SQLException {
        return DBManager.withConnection(conn -> leagueDAO.selectTopReturnStocks(memId, limit, conn));
    }
}






