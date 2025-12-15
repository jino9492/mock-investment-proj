package com.dongyang.mock.model.DAO;

import java.sql.*;
import com.dongyang.mock.model.DTO.WatchlistDTO;
import com.dongyang.mock.model.DTO.WatchlistStockDTO;

import static com.dongyang.mock.util.db.DBManager.close;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

/**
 * 관심종목 데이터 접근 객체 (DAO)
 * - 관심종목 추가/삭제
 * - 관심종목 조회
 * - 중복 체크
 */
public class WatchlistDAO {

    /**
     * 관심종목 추가
     * @param memId 회원 ID
     * @param stockId 종목 ID
     * @param conn DB 연결
     * @return 영향받은 행 수 (1: 성공, 0: 실패)
     * @throws SQLException
     */
    public int insertWatchlist(int memId, long stockId, Connection conn) throws SQLException {
        String sql = "INSERT INTO watchlist (mem_id, stock_id) VALUES (?, ?)";
        PreparedStatement pstmt = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memId);
            pstmt.setLong(2, stockId);
            
            return pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            close(null, pstmt);
        }
    }

    /**
     * 관심종목 삭제
     * @param memId 회원 ID
     * @param stockId 종목 ID
     * @param conn DB 연결
     * @return 영향받은 행 수 (1: 성공, 0: 실패)
     * @throws SQLException
     */
    public int deleteWatchlist(int memId, long stockId, Connection conn) throws SQLException {
        String sql = "DELETE FROM watchlist WHERE mem_id = ? AND stock_id = ?";
        PreparedStatement pstmt = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memId);
            pstmt.setLong(2, stockId);
            
            return pstmt.executeUpdate();
        } finally {
            close(null, pstmt);
        }
    }

    /**
     * 특정 회원/종목의 관심종목 여부 확인
     * @param memId 회원 ID
     * @param stockId 종목 ID
     * @param conn DB 연결
     * @return 관심종목 정보 (없으면 null)
     * @throws SQLException
     */
    public WatchlistDTO findWatchlistByMemberAndStock(int memId, long stockId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM watchlist WHERE mem_id = ? AND stock_id = ?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memId);
            pstmt.setLong(2, stockId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapToWatchlist(rs);
            }
            return null;
        } finally {
            close(null, pstmt, rs);
        }
    }

    /**
     * 특정 회원의 모든 관심종목 조회
     * @param memId 회원 ID
     * @param conn DB 연결
     * @return 관심종목 목록
     * @throws SQLException
     */
    public List<WatchlistDTO> findWatchlistByMember(int memId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM watchlist WHERE mem_id = ? ORDER BY created_at DESC";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<WatchlistDTO> watchlist = new ArrayList<>();
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                watchlist.add(mapToWatchlist(rs));
            }
        } finally {
            close(null, pstmt, rs);
        }
        return watchlist;
    }

    /**
     * 관심종목 중복 체크
     * @param memId 회원 ID
     * @param stockId 종목 ID
     * @param conn DB 연결
     * @return 중복이면 true, 없으면 false
     * @throws SQLException
     */
    public boolean isWatchlistExists(int memId, long stockId, Connection conn) throws SQLException {
        String sql = "SELECT watchlist_id FROM watchlist WHERE mem_id = ? AND stock_id = ?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memId);
            pstmt.setLong(2, stockId);
            rs = pstmt.executeQuery();
            
            return rs.next();
        } finally {
            close(null, pstmt, rs);
        }
    }

    /**
     * 특정 회원의 관심종목 목록 조회 (종목 정보 및 현재가 포함)
     * @param memId 회원 ID
     * @param conn DB 연결
     * @return 관심종목 목록 (종목 정보 포함)
     * @throws SQLException
     */
    public List<WatchlistStockDTO> findWatchlistStocksByMember(int memId, Connection conn) throws SQLException {
        String sql = "SELECT " +
                "w.stock_id, " +
                "s.stock_name, " +
                "s.stock_symbol, " +
                "spd.close_price as current_price, " +
                "spd.change_price as change_price, " +
                "spd.change_rate as change_rate " +
                "FROM watchlist w " +
                "INNER JOIN stock s ON w.stock_id = s.stock_id " +
                "LEFT JOIN stock_price_daily spd ON w.stock_id = spd.stock_id " +
                "AND spd.price_date = (SELECT MAX(price_date) FROM stock_price_daily WHERE stock_id = w.stock_id) " +
                "WHERE w.mem_id = ? " +
                "ORDER BY w.created_at DESC";
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<WatchlistStockDTO> watchlist = new ArrayList<>();
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                WatchlistStockDTO item = new WatchlistStockDTO();
                item.setStockId(rs.getLong("stock_id"));
                item.setStockName(rs.getString("stock_name"));
                item.setStockSymbol(rs.getString("stock_symbol"));
                
                BigDecimal currentPrice = rs.getBigDecimal("current_price");
                item.setCurrentPrice(currentPrice != null ? currentPrice.intValue() : null);
                
                BigDecimal changePrice = rs.getBigDecimal("change_price");
                item.setChangePrice(changePrice != null ? changePrice.longValue() : null);
                
                item.setChangeRate(rs.getDouble("change_rate"));
                watchlist.add(item);
            }
        } finally {
            close(null, pstmt, rs);
        }
        return watchlist;
    }

    /**
     * ResultSet을 WatchlistDTO로 변환
     */
    private WatchlistDTO mapToWatchlist(ResultSet rs) throws SQLException {
        WatchlistDTO watchlist = new WatchlistDTO();
        watchlist.setWatchlistId(rs.getLong("watchlist_id"));
        watchlist.setMemId(rs.getInt("mem_id"));
        watchlist.setStockId(rs.getLong("stock_id"));
        watchlist.setCreatedAt(rs.getTimestamp("created_at"));
        return watchlist;
    }
}
