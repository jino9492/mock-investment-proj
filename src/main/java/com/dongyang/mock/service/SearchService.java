package com.dongyang.mock.service;

import com.dongyang.mock.model.DAO.SearchDAO;
import com.dongyang.mock.model.DAO.StockPriceDailyDAO;
import com.dongyang.mock.model.DTO.RealtimeDTO;
import com.dongyang.mock.model.DTO.SearchResultDTO;
import com.dongyang.mock.model.DTO.StockDTO;
import com.dongyang.mock.model.DTO.StockPriceDailyDTO;
import com.dongyang.mock.service.api.WebSocketService;
import com.dongyang.mock.util.api.InmemoryCacheManager;
import com.dongyang.mock.util.db.DBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    private final SearchDAO searchServiceDAO = new SearchDAO();
    private final StockPriceDailyDAO stockPriceDailyDAO = new StockPriceDailyDAO();
    private final InmemoryCacheManager cacheManager = InmemoryCacheManager.getInstance();
    private final DecimalFormat priceFormat = new DecimalFormat("#,###");
    private final DecimalFormat rateFormat = new DecimalFormat("0.00");
    
    /**
     * 실시간 시세 캐시 키 접미사
     * "_AL"은 WebSocket 서버에서 사용하는 종목 타입 식별자
     * TODO: 설정 파일로 이동 고려
     */
    private static final String CACHE_KEY_SUFFIX = "_AL";

    /**
     * 검색어를 받아 실시간 가격 정보를 포함한 검색 결과 반환
     * @param keyword 검색 키워드
     * @return 검색 결과 리스트
     */
    public List<SearchResultDTO> searchStock(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            logger.debug("빈 검색어로 인한 빈 결과 반환");
            return new ArrayList<>();
        }
        
        logger.debug("검색 시작 - 키워드: '{}'", keyword);
        List<StockDTO> stocks = searchServiceDAO.search(keyword);
        List<SearchResultDTO> results = new ArrayList<>();
        
        logger.debug("검색된 종목 수: {}", stocks.size());

        for (StockDTO stock : stocks) {
            SearchResultDTO result = new SearchResultDTO();
            result.setStockName(stock.getStockName());
            result.setStockSymbol(stock.getStockSymbol());
            result.setMarketType(stock.getMarketType());

            // 실시간 가격 정보 조회
            String cacheKey = WebSocketService.REALTIME_CACHE_KEY + stock.getStockSymbol() + CACHE_KEY_SUFFIX;
            RealtimeDTO realtime = cacheManager.get(cacheKey, RealtimeDTO.class);

            if (realtime != null) {
                logger.debug("실시간 데이터 사용 - 종목: {}, 캐시 키: {}", stock.getStockSymbol(), cacheKey);
                // 주식의 현재가 포맷팅 (구매 가능 금액이 아닌 주식의 실제 현재가)
                // RealtimeDTO.curPrice: WebSocket에서 받은 주식의 현재가
                try {
                    BigDecimal price = new BigDecimal(realtime.getCurPrice());
                    result.setCurrentPrice(priceFormat.format(price) + "원");
                } catch (Exception e) {
                    result.setCurrentPrice(realtime.getCurPrice());
                }

                // 전날 대비 변동 퍼센트 포맷팅
                // RealtimeDTO.fluRt: 전일 대비 등락률 (예: 2.70, -3.26)
                try {
                    BigDecimal rate = new BigDecimal(realtime.getFluRt());
                    String rateStr = rateFormat.format(rate);
                    if (rate.compareTo(BigDecimal.ZERO) > 0) {
                        result.setChangeRate("+" + rateStr + "%");
                    } else if (rate.compareTo(BigDecimal.ZERO) < 0) {
                        result.setChangeRate(rateStr + "%");
                    } else {
                        result.setChangeRate("0.00%");
                    }
                } catch (Exception e) {
                    // 예외 발생 시 원본 값에 % 추가 시도
                    String fluRt = realtime.getFluRt();
                    if (fluRt != null && !fluRt.contains("%")) {
                        result.setChangeRate(fluRt + "%");
                    } else {
                        result.setChangeRate(fluRt != null ? fluRt : "0.00%");
                    }
                }
            } else {
                // 실시간 정보가 없는 경우 DB에서 최신 가격 정보 조회 (fallback)
                try {
                    StockPriceDailyDTO priceDaily = DBManager.withConnection(conn -> 
                        stockPriceDailyDAO.selectLatestPriceDailyById(stock.getStockId(), conn)
                    );
                    
                    if (priceDaily != null && priceDaily.getClosePrice() != null) {
                        // DB에서 가져온 종가를 현재가로 사용
                        BigDecimal closePrice = priceDaily.getClosePrice();
                        result.setCurrentPrice(priceFormat.format(closePrice) + "원");
                        
                        // 등락률 처리
                        if (priceDaily.getChangeRate() != null) {
                            BigDecimal rate = priceDaily.getChangeRate();
                            String rateStr = rateFormat.format(rate);
                            if (rate.compareTo(BigDecimal.ZERO) > 0) {
                                result.setChangeRate("+" + rateStr + "%");
                            } else if (rate.compareTo(BigDecimal.ZERO) < 0) {
                                result.setChangeRate(rateStr + "%");
                            } else {
                                result.setChangeRate("0.00%");
                            }
                        } else {
                            result.setChangeRate("0.00%");
                        }
                    } else {
                        // DB에도 데이터가 없는 경우 기본값
                        result.setCurrentPrice("-");
                        result.setChangeRate("0.00%");
                    }
                } catch (SQLException e) {
                    // DB 조회 실패 시 기본값
                    logger.error("DB 조회 실패 - 종목: {}, 오류: {}", stock.getStockSymbol(), e.getMessage(), e);
                    result.setCurrentPrice("-");
                    result.setChangeRate("0.00%");
                }
            }

            results.add(result);
        }

        logger.debug("검색 완료 - 키워드: '{}', 결과 수: {}", keyword, results.size());
        return results;
    }
}
