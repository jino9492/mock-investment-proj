package com.dongyang.mock.service.api;

import com.dongyang.mock.model.DAO.IndexDAO;
import com.dongyang.mock.model.DAO.StockChartDAO;
import com.dongyang.mock.model.DTO.StockChartDTO;
import com.dongyang.mock.util.db.DBManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class StockChartService {
    private final StockChartDAO stockChartDAO;

    // 차트를 위해 필요한 데이터 수
    private static final int CHART_POINT_COUNT = 24*5;

    public StockChartService() {
        this.stockChartDAO = new StockChartDAO();
    }

    public List<StockChartDTO> getRecentChartData(Long stkId) throws SQLException {
        if (stkId == null) {
            throw new IllegalArgumentException("주식 코드는 null이거나 비어있을 수 없습니다.");
        }

        System.out.println("Service: " + stkId + "에 대한 " + CHART_POINT_COUNT + "회분 5분봉 조회 시작.");

        return DBManager.withConnection(conn -> {
            List<StockChartDTO> chartDataList = stockChartDAO.findRecentStockChartData(stkId, CHART_POINT_COUNT, conn);

            if (chartDataList.isEmpty()) {
                System.out.println("Service: 조회된 데이터가 없습니다.");
            }

            return chartDataList;
        });
    }

    public int reCalculateAndUpateZeroChangeRates() throws SQLException {
        System.out.println("--- 0% 등락률 재계산 및 업데이트 시작 ---");

        return DBManager.executeInTransaction(conn -> {
            int totalUpdated = 0;

            // 1. 등락률이 0인 모든 대상 레코드 조회
            List<StockChartDAO.ChangeRateTarget> targets = stockChartDAO.findZeroChangeRateTargets(conn);

            if (targets.isEmpty()) {
                System.out.println("재계산할 대상 데이터가 없습니다.");
                return DBManager.TransactionResult.commit(0);
            }

            System.out.println(targets.size() + "개의 0% 등락률 대상 레코드를 발견했습니다. 재계산을 시작합니다.");

            // 2. 각 대상에 대해 직전 종가를 조회하고 등락률을 계산
            for (StockChartDAO.ChangeRateTarget currentTarget : targets) {
                long stkId = currentTarget.getStkId();
                String chartTimeStr = currentTarget.getChartTimeStr();
                BigDecimal currentClosePrice = currentTarget.getClosePrice();

                try {
                    // 3. 직전 봉의 종가를 조회 (5분 전 데이터 역할)
                    BigDecimal previousClosePrice = stockChartDAO.findPreviousClosePrice(stkId, chartTimeStr, conn);

                    if (previousClosePrice != null && previousClosePrice.compareTo(BigDecimal.ZERO) != 0) {
                        // 4. 등락률 계산
                        BigDecimal newChangeRate = calculateChangeRate(currentClosePrice, previousClosePrice);

                        // 5. 등락률 업데이트
                        totalUpdated += stockChartDAO.updateChangeRate(stkId, chartTimeStr, newChangeRate, conn);

                    } else if (previousClosePrice == null) {
                        // 직전 봉이 없는 경우 (해당 종목의 첫 데이터일 가능성)
                        System.err.println("Warning: " + stkId + " - " + chartTimeStr + ": 직전 봉 데이터가 없어 계산을 건너뜁니다.");
                    } else {
                        // 직전 종가가 0인 경우 (나눗셈 방지)
                        System.err.println("Warning: " + stkId + " - " + chartTimeStr + ": 직전 종가가 0이어서 계산을 건너뜁니다.");
                    }
                } catch (SQLException e) {
                    System.err.println("등락률 재계산 및 업데이트 중 DB 오류 발생: " + e.getMessage());
                    // 트랜잭션 롤백 유도를 위해 예외를 다시 던짐
                    throw e;
                }
            }

            System.out.println("--- 0% 등락률 재계산 및 업데이트 완료. 총 " + totalUpdated + "개 업데이트됨. ---");
            return DBManager.TransactionResult.commit(totalUpdated);
        });
    }

    private BigDecimal calculateChangeRate(BigDecimal currentClosePrice, BigDecimal previousClosePrice) {
        // 등락률 = ((현재 종가 - 이전 종가) / 이전 종가) * 100
        BigDecimal difference = currentClosePrice.subtract(previousClosePrice);
        BigDecimal rate = difference.divide(
                previousClosePrice,
                6, // 계산 정밀도
                RoundingMode.HALF_UP
        );
        return rate.multiply(new BigDecimal("100"));
    }
}