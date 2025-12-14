package com.dongyang.mock.util;

import com.dongyang.mock.model.DTO.StockDTO;
import com.dongyang.mock.service.api.StockDetailService;

/**
 * 종목 코드를 다양한 형식으로 조회하는 유틸리티 클래스
 * - stock_symbol 우선 조회 (005930 같은 코드는 문자열로 저장되어 있을 수 있음)
 * - 실패 시 stock_code_id로 조회
 */
public class StockCodeResolver {
    private final StockDetailService stockDetailService;

    public StockCodeResolver(StockDetailService stockDetailService) {
        this.stockDetailService = stockDetailService;
    }

    /**
     * 종목 코드로 StockDTO를 조회합니다.
     * 먼저 stock_symbol로 조회하고, 실패 시 stock_code_id로 조회합니다.
     * 
     * @param codeParam 종목 코드 (문자열 또는 숫자)
     * @return StockDTO 또는 null (찾을 수 없을 경우)
     * @throws Exception 조회 중 오류 발생 시
     */
    public StockDTO resolveStock(String codeParam) throws Exception {
        if (codeParam == null || codeParam.trim().isEmpty()) {
            return null;
        }

        codeParam = codeParam.trim();
        
        // 먼저 stock_symbol로 조회 시도
        StockDTO stockDTO = stockDetailService.getStockBySymbol(codeParam);
        
        if (stockDTO != null) {
            return stockDTO;
        }
        
        // stock_symbol로 조회 실패 시, 숫자로 파싱하여 stock_code_id로 조회 시도
        try {
            Long numericCode = Long.parseLong(codeParam);
            return stockDetailService.getStockByCode(numericCode);
        } catch (NumberFormatException e) {
            // 숫자가 아니면 null 반환
            return null;
        }
    }
}
