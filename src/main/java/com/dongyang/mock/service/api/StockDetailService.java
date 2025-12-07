package com.dongyang.mock.service.api;


import com.dongyang.mock.model.DAO.*;
import com.dongyang.mock.model.DTO.*;
import com.dongyang.mock.util.db.DBManager;

import java.sql.Date;
import java.time.LocalDate;

public class StockDetailService {
    StockDAO stockDAO = new StockDAO();
    StockDetailDAO stockDetailDAO = new StockDetailDAO();
    StockFinancialIndicatorDAO indicatorDAO = new StockFinancialIndicatorDAO();
    StockFinancialSummaryDAO summaryDAO = new StockFinancialSummaryDAO();
    StockPriceDailyDAO priceDAO = new StockPriceDailyDAO();

    public StockDTO getStockById(long stockId) throws Exception {
        StockDTO stock = DBManager.withConnection(conn->stockDAO.selectStockById(stockId, conn));
        return stock;
    }

    public StockDTO getStockByCode(long stockCode) throws Exception {
        StockDTO stock = DBManager.withConnection(conn->stockDAO.selectStockByCode(stockCode, conn));
        return stock;
    }

    public StockDetailDTO getStockDetailById(long stockId) throws Exception {
        return DBManager.withConnection(conn -> stockDetailDAO.selectStockDetailById(stockId, conn));
    }

    public StockFinancialIndicatorDTO getFinancialIndicatorById(long stockId) throws Exception {
        Date todaySqlDate = Date.valueOf(LocalDate.now());

        return DBManager.withConnection(conn -> indicatorDAO.selectIndicatorByIdAndDate(stockId, todaySqlDate, conn));
    }

    public StockFinancialSummaryDTO getFinancialSummaryById(long stockId) throws Exception {
        return DBManager.withConnection(conn -> summaryDAO.selectSummaryById(stockId, conn));
    }

    public StockPriceDailyDTO getStockPriceDailyById(long stockId) throws Exception {
        Date todaySqlDate = Date.valueOf(LocalDate.now());

        return DBManager.withConnection(conn -> priceDAO.selectPriceDailyByIdAndDate(stockId, todaySqlDate, conn));
    }
}
