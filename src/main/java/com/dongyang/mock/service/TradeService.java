package com.dongyang.mock.service;

import com.dongyang.mock.model.DAO.TradeDAO;
import com.dongyang.mock.model.DTO.RealizedProfitDTO;
import com.dongyang.mock.model.DTO.TradeDTO;
import com.dongyang.mock.model.DTO.TradeStatsDTO;
import com.dongyang.mock.util.db.DBManager;

import java.sql.SQLException;
import java.util.List;

/**
 * 거래 관련 비즈니스 로직 서비스
 * - 거래 목록 조회 및 필터링
 * - 거래 통계 조회
 */
public class TradeService {
    private final TradeDAO tradeDAO = new TradeDAO();

    /**
     * 필터 조건에 맞는 거래 목록 조회
     * @param startDate 시작일 (YYYY-MM-DD 형식)
     * @param endDate 종료일 (YYYY-MM-DD 형식)
     * @param typeFilter 거래 유형 필터 (buy, sell)
     * @param statusFilter 상태 필터 (settled, cancelled)
     * @param stockKeyword 종목 키워드 검색
     * @param userKeyword 회원 키워드 검색
     * @return 필터링된 거래 목록
     */
    public List<TradeDTO> getTrades(String startDate, String endDate, String typeFilter,
                                    String statusFilter, String stockKeyword, String userKeyword) throws SQLException {
        return DBManager.withConnection(conn -> 
            tradeDAO.findTrades(startDate, endDate, typeFilter, statusFilter, stockKeyword, userKeyword, conn));
    }

    /**
     * 거래 통계 정보 조회
     * - 오늘 거래 건수 및 금액
     * - 전일 거래 건수 및 금액 (비교용)
     * - 매수/매도/취소 건수
     * @return 거래 통계 정보
     */
    public TradeStatsDTO getTradeStats() throws SQLException {
        return DBManager.withConnection(tradeDAO::fetchTradeStats);
    }

    /**
     * 특정 회원의 최근 거래 내역 조회
     * @param memberId 회원 ID
     * @param limit 조회할 최대 건수
     * @return 최근 거래 내역 목록
     */
    public List<TradeDTO> getMemberRecentTrades(int memberId, int limit) throws SQLException {
        return DBManager.withConnection(conn -> tradeDAO.findMemberRecentTrades(memberId, limit, conn));
    }

    /**
     * 특정 회원의 실현수익 조회
     * @param memberId 회원 ID
     * @param startDate 시작일 (YYYY-MM-DD 형식)
     * @param endDate 종료일 (YYYY-MM-DD 형식)
     * @return 실현수익 목록
     */
    public List<RealizedProfitDTO> getRealizedProfits(int memberId, String startDate, String endDate) throws SQLException {
        return DBManager.withConnection(conn -> tradeDAO.findRealizedProfits(memberId, startDate, endDate, conn));
    }

    /**
     * 특정 회원의 총 실현수익 합계 조회
     * @param memberId 회원 ID
     * @param startDate 시작일 (YYYY-MM-DD 형식)
     * @param endDate 종료일 (YYYY-MM-DD 형식)
     * @return 총 실현수익 합계
     */
    public int getTotalRealizedProfit(int memberId, String startDate, String endDate) throws SQLException {
        List<RealizedProfitDTO> profits = getRealizedProfits(memberId, startDate, endDate);
        return profits.stream()
                .mapToInt(RealizedProfitDTO::getTotalProfit)
                .sum();
    }
}

