package com.dongyang.mock.service;

import com.dongyang.mock.model.DAO.LeagueDAO;
import com.dongyang.mock.model.DTO.LeagueDTO;
import com.dongyang.mock.util.db.DBManager;

import java.sql.SQLException;
import java.util.List;

/**
 * 리그 페이지 관련 비즈니스 로직 서비스
 */
public class LeagueService {
    private final LeagueDAO leagueDAO = new LeagueDAO();

    /**
     * 기능 1-3: 계좌 요약 정보 조회
     * @param memId 회원 ID
     * @return 계좌 현황 정보
     */
    public LeagueDTO.AccountSummary getAccountSummary(int memId) throws SQLException {
        return DBManager.withConnection(conn -> leagueDAO.selectAccountSummary(memId, conn));
    }

    /**
     * 기능 4: 수익률 상위 종목 조회
     * @param memId 회원 ID
     * @param limit 조회할 개수 (기본 3개)
     * @return 수익률 상위 종목 목록
     */
    public List<LeagueDTO.TopReturnStock> getTopReturnStocks(int memId, int limit) throws SQLException {
        return DBManager.withConnection(conn -> leagueDAO.selectTopReturnStocks(memId, limit, conn));
    }

    /**
     * 기능 7: 전체 포트폴리오 상세 조회
     * @param memId 회원 ID
     * @return 포트폴리오 종목 목록
     */
    public List<LeagueDTO.PortfolioStock> getPortfolioDetails(int memId) throws SQLException {
        return DBManager.withConnection(conn -> leagueDAO.selectPortfolioDetails(memId, conn));
    }

    /**
     * 기능 5: 주변 랭킹 조회
     * @param memId 회원 ID
     * @return 랭킹 목록
     */
    public List<LeagueDTO.Ranking> getNearbyRankings(int memId) throws SQLException {
        return DBManager.withConnection(conn -> leagueDAO.selectNearbyRankings(memId, conn));
    }
}

