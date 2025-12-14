package com.dongyang.mock.controller.api;

import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.model.DTO.RealizedProfitDTO;
import com.dongyang.mock.service.TradeService;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 수익 분석 페이지 API 컨트롤러
 * 실현수익 데이터를 JSON으로 제공
 */
@WebServlet("/api/mypage/analysis")
public class AnalysisApiController extends HttpServlet {
    private final TradeService tradeService = new TradeService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        resp.setContentType("application/json;charset=UTF-8");
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 로그인 확인
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("member") == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                resp.getWriter().write(gson.toJson(response));
                return;
            }
            
            MemberDTO member = (MemberDTO) session.getAttribute("member");
            int memId = member.getMemId();
            
            // 파라미터 가져오기
            String viewType = req.getParameter("viewType"); // "day" | "week" | "month" | "year" | "all"
            String date = req.getParameter("date"); // 날짜 파라미터
            
            // 기본값 설정
            if (viewType == null || viewType.trim().isEmpty()) {
                viewType = "month";
            }
            
            // 날짜 범위 계산
            String startDate;
            String endDate;
            
            if ("day".equals(viewType)) {
                // 일별: YYYY-MM-DD 형식
                if (date == null || date.trim().isEmpty()) {
                    date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
                LocalDate localDate = LocalDate.parse(date);
                startDate = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                endDate = startDate;
            } else if ("week".equals(viewType)) {
                // 주별: YYYY-MM-DD 형식 (해당 주의 시작일)
                if (date == null || date.trim().isEmpty()) {
                    date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                }
                LocalDate localDate = LocalDate.parse(date);
                // 주의 시작일 계산 (월요일 기준)
                int dayOfWeek = localDate.getDayOfWeek().getValue(); // 1=월요일, 7=일요일
                LocalDate weekStart = localDate.minusDays(dayOfWeek - 1);
                LocalDate weekEnd = weekStart.plusDays(6);
                startDate = weekStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                endDate = weekEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else if ("month".equals(viewType)) {
                // 월별: YYYY-MM 형식
                if (date == null || date.trim().isEmpty()) {
                    date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                }
                YearMonth yearMonth = YearMonth.parse(date);
                startDate = yearMonth.atDay(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                endDate = yearMonth.atEndOfMonth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else if ("year".equals(viewType)) {
                // 년별: YYYY 형식
                if (date == null || date.trim().isEmpty()) {
                    date = String.valueOf(LocalDate.now().getYear());
                }
                int year = Integer.parseInt(date);
                startDate = LocalDate.of(year, 1, 1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                endDate = LocalDate.of(year, 12, 31).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else if ("all".equals(viewType)) {
                // 전체: 날짜 제한 없음
                startDate = null;
                endDate = null;
            } else {
                // 기본값: 월별
                if (date == null || date.trim().isEmpty()) {
                    date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                }
                YearMonth yearMonth = YearMonth.parse(date);
                startDate = yearMonth.atDay(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                endDate = yearMonth.atEndOfMonth().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            
            // 실현수익 데이터 조회
            List<RealizedProfitDTO> profits = tradeService.getRealizedProfits(memId, startDate, endDate);
            int totalProfit = tradeService.getTotalRealizedProfit(memId, startDate, endDate);
            
            // 판매수익은 총 실현수익과 동일 (현재는 단순 판매 금액만)
            int sellProfit = totalProfit;
            
            response.put("success", true);
            response.put("totalProfit", totalProfit);
            response.put("sellProfit", sellProfit);
            response.put("profits", profits);
            
            resp.getWriter().write(gson.toJson(response));
            
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.put("success", false);
            response.put("message", "실현수익 조회 중 오류가 발생했습니다.");
            resp.getWriter().write(gson.toJson(response));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.put("success", false);
            response.put("message", "실현수익 조회 중 오류가 발생했습니다.");
            resp.getWriter().write(gson.toJson(response));
        }
    }
}

