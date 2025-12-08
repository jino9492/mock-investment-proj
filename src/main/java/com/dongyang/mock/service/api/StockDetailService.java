package com.dongyang.mock.service.api;


import com.dongyang.mock.model.DAO.*;
import com.dongyang.mock.model.DTO.*;
import com.dongyang.mock.util.db.DBManager;

import java.sql.Date;
import java.time.LocalDate;

public class StockDetailService {
    private final StockDAO stockDAO = new StockDAO();
    private final StockDetailDAO stockDetailDAO = new StockDetailDAO();
    private final StockFinancialIndicatorDAO indicatorDAO = new StockFinancialIndicatorDAO();
    private final StockFinancialSummaryDAO summaryDAO = new StockFinancialSummaryDAO();
    private final StockPriceDailyDAO priceDAO = new StockPriceDailyDAO();
    
    /**
     * 공통으로 사용하는 오늘 날짜 계산 (한 번만 계산)
     * @return 오늘 날짜의 java.sql.Date 객체
     */
    private Date getTodayDate() {
        return Date.valueOf(LocalDate.now());
    }

    public StockDTO getStockById(long stockId) throws Exception {
        StockDTO stock = DBManager.withConnection(conn->stockDAO.selectStockById(stockId, conn));
        return stock;
    }

    public StockDetailDTO getStockDetailById(long stockId) throws Exception {
        return DBManager.withConnection(conn -> stockDetailDAO.selectStockDetailById(stockId, conn));
    }

    public StockFinancialIndicatorDTO getFinancialIndicatorById(long stockId) throws Exception {
        Date todaySqlDate = getTodayDate();
        return DBManager.withConnection(conn -> indicatorDAO.selectIndicatorByIdAndDate(stockId, todaySqlDate, conn));
    }

    public StockFinancialSummaryDTO getFinancialSummaryById(long stockId) throws Exception {
        return DBManager.withConnection(conn -> summaryDAO.selectSummaryById(stockId, conn));
    }

    /**
     * 주식 일일 가격 정보 조회
     * 오늘 날짜의 데이터가 없을 경우 최신 데이터를 조회합니다.
     * @param stockId 주식 ID
     * @return 일일 가격 정보 (없으면 null)
     * @throws Exception
     */
    public StockPriceDailyDTO getStockPriceDailyById(long stockId) throws Exception {
        Date todaySqlDate = getTodayDate();
        
        return DBManager.withConnection(conn -> {
            // 먼저 오늘 날짜의 데이터 조회
            StockPriceDailyDTO todayData = priceDAO.selectPriceDailyByIdAndDate(stockId, todaySqlDate, conn);
            
            // 오늘 날짜의 데이터가 없으면 최신 데이터 조회
            if (todayData == null) {
                return priceDAO.selectLatestPriceDailyById(stockId, conn);
            }
            
            return todayData;
        });
    }
}
