package com.dongyang.mock.controller.api;

import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.model.DTO.RankingItemDTO;
import com.dongyang.mock.model.DTO.RealtimeDTO;
import com.dongyang.mock.model.DAO.MemberDAO;
import com.dongyang.mock.service.api.RealtimeStockService;
import com.dongyang.mock.service.api.WebSocketService;
import com.dongyang.mock.service.api.fetch.RankingFetchService;
import com.dongyang.mock.util.api.InmemoryCacheManager;
import com.dongyang.mock.util.db.DBManager;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/socket")
public class SocketController extends HttpServlet {
    InmemoryCacheManager cacheManager = InmemoryCacheManager.getInstance();
    RealtimeStockService realtimeStockService = new RealtimeStockService();
    MemberDAO memberDAO = new MemberDAO();
    Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        super.init();
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = req.getParameter("code");

        Class<RealtimeDTO> type = RealtimeDTO.class;
        RealtimeDTO cachedReal = cacheManager.get(WebSocketService.REALTIME_CACHE_KEY + code + "_AL", type );
        
        // 응답 데이터 구성
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("realtime", cachedReal);

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("member") != null) {
            MemberDTO member = (MemberDTO) session.getAttribute("member");
            int memId = member.getMemId();

            try {
                // DB에서 최신 예치금 조회
                int virtualCash = DBManager.withConnection(conn ->
                        memberDAO.getVirtualCash(memId, conn));

                responseData.put("availableCash", virtualCash);
                responseData.put("memberId", memId);
            } catch (SQLException e) {
                System.err.println("[SocketController] 예치금 조회 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
                // 오류가 발생해도 기본값 사용
                responseData.put("availableCash", 0);
            }
        } else {
            // 로그인하지 않은 경우
            responseData.put("availableCash", 0);
        }
        String jsonResponse = gson.toJson(responseData);

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(jsonResponse);
    }
}
