package com.dongyang.mock.service.api;

import com.dongyang.mock.model.DAO.IndexHistoryDAO;
import com.dongyang.mock.util.db.DBManager;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class IndexDataService {
    private final IndexHistoryDAO indexDao;

    // 스파크라인을 위해 필요한 데이터 수
    private static final int SPARKLINE_POINT_COUNT = 12*5;

    public IndexDataService() {
        this.indexDao = new IndexHistoryDAO();
    }

    public Map<String, List<Double>> getRecentSparklineData(String indexCode) throws SQLException {
        if (indexCode == null || indexCode.trim().isEmpty()) {
            throw new IllegalArgumentException("지수 코드는 null이거나 비어있을 수 없습니다.");
        }

        System.out.println("Service: " + indexCode + "에 대한 최근 " + SPARKLINE_POINT_COUNT + "일치 종가 조회 시작.");

        return DBManager.withConnection(conn -> {
            Map<String, List<Double>> priceList = indexDao.findRecentIndexData(indexCode, SPARKLINE_POINT_COUNT, conn);

            if (priceList.isEmpty()) {
                System.out.println("Service: 조회된 데이터가 없습니다.");
            }

            return priceList;
        });
    }
}