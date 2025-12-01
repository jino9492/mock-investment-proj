package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.LeagueDTO;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static com.dongyang.mock.util.db.DBManager.close;

/**
 * 포트폴리오 데이터 접근 객체 (DAO)
 * - 수익률 상위 종목 조회
 * - 전체 포트폴리오 상세 조회
 */
public class PortfolioDAO {

    /**
     * 기능 4: 수익률 상위 3개 종목 조회
     */
    public List<LeagueDTO.TopReturnStock> selectTopReturnStocks(int memId, int limit, Connection conn) throws SQLException {
        String sql = "SELECT " +
                "s.stock_name, " +
                "spd.close_price as current_price, " +
                "h.avg_buy_price as avg_purchase_price, " +
                "CASE " +
                "  WHEN prev_spd.close_price > 0 " +
                "  THEN ((spd.close_price - prev_spd.close_price) / prev_spd.close_price) * 100.0 " +
                "  ELSE 0.0 " +
                "END as change_rate, " +
                "CASE " +
                "  WHEN h.avg_buy_price > 0 " +
                "  THEN ((spd.close_price - h.avg_buy_price) / h.avg_buy_price) * 100 " +
                "  ELSE 0 " +
                "END as return_rate " +
                "FROM holding h " +
                "INNER JOIN stock s ON h.stock_id = s.stock_id " +
                "INNER JOIN stock_price_daily spd ON h.stock_id = spd.stock_id " +
                "AND spd.price_date = (SELECT MAX(price_date) FROM stock_price_daily WHERE stock_id = h.stock_id) " +
                "LEFT JOIN stock_price_daily prev_spd ON h.stock_id = prev_spd.stock_id " +
                "AND prev_spd.price_date = (SELECT MAX(price_date) FROM stock_price_daily WHERE stock_id = h.stock_id AND price_date < spd.price_date) " +
                "WHERE h.mem_id = ? AND h.quantity > 0 AND h.avg_buy_price > 0 " +
                "ORDER BY return_rate DESC " +
                "LIMIT ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<LeagueDTO.TopReturnStock> stocks = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memId);
            pstmt.setInt(2, limit);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                LeagueDTO.TopReturnStock stock = new LeagueDTO.TopReturnStock();
                stock.setStockName(rs.getString("stock_name"));
                // decimal(18,2)를 정확하게 처리
                BigDecimal price = rs.getBigDecimal("current_price");
                stock.setCurrentPrice(price != null ? price.longValue() : 0L);
                BigDecimal avgPrice = rs.getBigDecimal("avg_purchase_price");
                stock.setAvgPurchasePrice(avgPrice != null ? avgPrice.longValue() : 0L);
                stock.setChangeRate(rs.getDouble("change_rate"));
                double returnRate = rs.getDouble("return_rate");
                stock.setReturnRate(Math.round(returnRate * 100.0) / 100.0);
                stocks.add(stock);
            }
        } finally {
            close(null, pstmt, rs);
        }
        return stocks;
    }

    /**
     * 기능 7: 전체 포트폴리오 상세 조회
     */
    public List<LeagueDTO.PortfolioStock> selectPortfolioDetails(int memId, Connection conn) throws SQLException {
        String sql = "SELECT " +
                "h.stock_id, " +
                "s.stock_name, " +
                "h.quantity, " +
                "h.avg_buy_price as avg_purchase_price, " +
                "spd.close_price as current_price, " +
                "CASE " +
                "  WHEN prev_spd.close_price > 0 " +
                "  THEN ((spd.close_price - prev_spd.close_price) / prev_spd.close_price) * 100.0 " +
                "  ELSE 0.0 " +
                "END as change_rate, " +
                "(spd.close_price - h.avg_buy_price) * h.quantity as profit_loss, " +
                "CASE " +
                "  WHEN h.avg_buy_price > 0 " +
                "  THEN ((spd.close_price - h.avg_buy_price) / h.avg_buy_price) * 100 " +
                "  ELSE 0 " +
                "END as return_rate " +
                "FROM holding h " +
                "INNER JOIN stock s ON h.stock_id = s.stock_id " +
                "INNER JOIN stock_price_daily spd ON h.stock_id = spd.stock_id " +
                "AND spd.price_date = (SELECT MAX(price_date) FROM stock_price_daily WHERE stock_id = h.stock_id) " +
                "LEFT JOIN stock_price_daily prev_spd ON h.stock_id = prev_spd.stock_id " +
                "AND prev_spd.price_date = (SELECT MAX(price_date) FROM stock_price_daily WHERE stock_id = h.stock_id AND price_date < spd.price_date) " +
                "WHERE h.mem_id = ? AND h.quantity > 0 " +
                "ORDER BY return_rate DESC";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<LeagueDTO.PortfolioStock> portfolio = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                LeagueDTO.PortfolioStock stock = new LeagueDTO.PortfolioStock();
                stock.setStockId(rs.getInt("stock_id"));
                stock.setStockName(rs.getString("stock_name"));
                stock.setQuantity(rs.getInt("quantity"));
                // decimal(18,2)를 정확하게 처리
                BigDecimal avgPrice = rs.getBigDecimal("avg_purchase_price");
                stock.setAvgPurchasePrice(avgPrice != null ? avgPrice.longValue() : 0L);
                BigDecimal currentPrice = rs.getBigDecimal("current_price");
                stock.setCurrentPrice(currentPrice != null ? currentPrice.longValue() : 0L);
                stock.setChangeRate(rs.getDouble("change_rate"));
                BigDecimal profitLoss = rs.getBigDecimal("profit_loss");
                stock.setProfitLoss(profitLoss != null ? profitLoss.longValue() : 0L);
                double returnRate = rs.getDouble("return_rate");
                stock.setReturnRate(Math.round(returnRate * 100.0) / 100.0);
                portfolio.add(stock);
            }
        } finally {
            close(null, pstmt, rs);
        }
        return portfolio;
    }
}

