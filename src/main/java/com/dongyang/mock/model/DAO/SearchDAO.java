package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.StockDTO;
import com.dongyang.mock.util.db.DBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SearchDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(SearchDAO.class);
    
    /**
     * 검색 결과 최대 개수 제한
     * TODO: 페이지네이션 또는 "더보기" 기능 추가 시 제거 또는 동적 조정
     */
    private static final int MAX_SEARCH_RESULTS = 6;

    /**
     * 키워드로 주식 검색
     * @param keyword 검색 키워드
     * @return 검색된 주식 리스트
     */
    public List<StockDTO> search(String keyword) {
        List<StockDTO> list = new ArrayList<>();

        if (keyword == null || keyword.trim().isEmpty()) {
            logger.debug("빈 검색어로 인한 빈 결과 반환");
            return list;
        }

        // status 필드는 증거금 정보를 담고 있으므로 조건 제거
        // stock_detail과 JOIN하여 시가총액으로 정렬
        // 시가총액은 VARCHAR이므로 숫자로 변환 가능한 경우만 정렬
        String sql = String.format("""
            SELECT s.stock_id, s.stock_symbol, s.stock_name, s.market_type, sd.market_cap
            FROM stock s
            LEFT JOIN stock_detail sd ON s.stock_id = sd.stock_id
            WHERE (s.stock_name LIKE ? OR s.stock_symbol LIKE ?)
            ORDER BY 
                CASE 
                    WHEN sd.market_cap IS NULL OR sd.market_cap = '' THEN 0
                    WHEN sd.market_cap REGEXP '^[0-9]+(\\.[0-9]+)?$' THEN CAST(sd.market_cap AS DECIMAL(20,2))
                    WHEN sd.market_cap REGEXP '^[0-9]+(\\.[0-9]+)?[억]?$' THEN 
                        CAST(SUBSTRING_INDEX(REPLACE(REPLACE(sd.market_cap, '억', ''), ' ', ''), '.', 1) AS DECIMAL(20,2)) * 
                        CASE WHEN sd.market_cap LIKE '%%억%%' THEN 100000000 ELSE 1 END
                    ELSE 0
                END DESC,
                s.stock_name ASC
            LIMIT %d
        """, MAX_SEARCH_RESULTS);


        try (Connection conn = DBManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String trimmedKeyword = keyword.trim();
            String like = "%" + trimmedKeyword + "%";

            logger.debug("검색 실행 - 검색어: '{}', LIKE 패턴: '{}'", trimmedKeyword, like);
            
            pstmt.setString(1, like);
            pstmt.setString(2, like);
            
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                StockDTO dto = new StockDTO();
                dto.setStockId(rs.getLong("stock_id"));
                dto.setStockSymbol(rs.getString("stock_symbol"));
                dto.setStockName(rs.getString("stock_name"));
                dto.setMarketType(rs.getString("market_type"));
                list.add(dto);
            }

            logger.debug("검색 완료 - 검색어: '{}', 결과 수: {}", trimmedKeyword, list.size());

        } catch (SQLException e) {
            logger.error("검색 중 SQL 오류 발생 - 검색어: '{}', 오류: {}", keyword, e.getMessage(), e);
        } catch (Exception e) {
            logger.error("검색 중 예상치 못한 오류 발생 - 검색어: '{}', 오류: {}", keyword, e.getMessage(), e);
        }

        return list;
    }
}
