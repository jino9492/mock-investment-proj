package com.dongyang.mock.service;

import com.dongyang.mock.model.DAO.DashboardDAO;
import com.dongyang.mock.model.DTO.DashboardDTO;
import com.dongyang.mock.model.DTO.LeagueDTO;
import com.dongyang.mock.model.DTO.TopStockDTO;
import com.dongyang.mock.util.db.DBManager;

import java.sql.SQLException;
import java.util.List;

/**
 * 대시보드 관련 비즈니스 로직 서비스
 * - 대시보드 통계 정보 조회
 * - 인기종목 조회
 * - 수익률 상위 종목 조회
 */
public class DashboardService {
    private final DashboardDAO dashboardDAO = new DashboardDAO();

    /**
     * 대시보드 통계 정보 조회
     * @return 대시보드 통계 정보
     */
    public DashboardDTO getDashboardStats() throws SQLException {
        return DBManager.withConnection(dashboardDAO::fetchDashboardStats);
    }

    /**
     * 인기종목 TOP N 조회
     * @param limit 조회할 개수
     * @return 인기종목 목록
     */
    public List<TopStockDTO> getTopStocks(int limit) throws SQLException {
        return DBManager.withConnection(conn -> dashboardDAO.fetchTopStocks(limit, conn));
    }

    /**
     * 수익률 상위 종목 TOP N 조회
     * @param limit 조회할 개수
     * @return 수익률 상위 종목 목록
     */
    public List<LeagueDTO.TopReturnStock> getTopReturnStocks(int limit) throws SQLException {
        return DBManager.withConnection(conn -> dashboardDAO.fetchTopReturnStocks(limit, conn));
    }
}

