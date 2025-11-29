package com.dongyang.mock.service.auth;

import com.dongyang.mock.model.DAO.IndexHistoryDAO;
import java.util.List;

/**
 * DB에 저장된 주가지수 데이터를 조회하는 서비스 클래스입니다.
 * Controller/Servlet의 AJAX 요청에 응답하기 위해 사용됩니다.
 */
public class IndexDataService {

    // 데이터 접근 객체 (DAO) 인스턴스
    private final IndexHistoryDAO indexDao;

    // 스파크라인을 위해 필요한 일수
    private static final int SPARKLINE_DAYS = 7;

    public IndexDataService() {
        // DAO 인스턴스화 (싱글톤 패턴을 사용하는 DB 환경이라면 여기서 인스턴스 관리 필요)
        this.indexDao = new IndexHistoryDAO();
    }

    /**
     * 특정 지수 코드에 대한 최근 7일치 종가 데이터를 조회합니다.
     * 이 데이터는 AJAX 응답으로 전송되어 클라이언트 측에서 스파크라인을 그리는 데 사용됩니다.
     * * @param indexCode 조회할 지수 코드 (예: KOSPI, NASDAQ)
     * @return Double 타입의 종가 리스트 (오래된 순서대로 정렬됨)
     */
    public List<Double> getRecentSparklineData(String indexCode) {
        if (indexCode == null || indexCode.trim().isEmpty()) {
            throw new IllegalArgumentException("지수 코드는 null이거나 비어있을 수 없습니다.");
        }

        System.out.println("Service: " + indexCode + "에 대한 최근 " + SPARKLINE_DAYS + "일치 종가 조회 시작.");

        // DAO를 통해 DB에서 데이터를 조회합니다.
        List<Double> priceList = indexDao.findRecentClosePrices(indexCode, SPARKLINE_DAYS);

        if (priceList.isEmpty()) {
            System.out.println("Service: 조회된 데이터가 없습니다.");
        }

        return priceList;
    }

    // 필요하다면, 특정 날짜 범위의 데이터를 조회하는 메서드나
    // 모든 지수 목록을 조회하는 메서드 등을 추가할 수 있습니다.

    /*
    public Map<String, List<Double>> getAllIndexSparklineData() {
        // ... (모든 지수 코드를 조회하여 각각 getRecentSparklineData를 호출)
    }
    */
}