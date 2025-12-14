package com.dongyang.mock.controller.api;

import com.dongyang.mock.model.DTO.RealtimeDTO;
import com.dongyang.mock.service.api.WebSocketService;
import com.dongyang.mock.util.api.InmemoryCacheManager;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/price")
public class PriceLookupController extends HttpServlet {
    private final InmemoryCacheManager cacheManager = InmemoryCacheManager.getInstance();
    private final Gson gson = new Gson();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = req.getParameter("code");

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> responseData = new HashMap<>();

        if (code == null || code.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseData.put("status", "error");
            responseData.put("message", "종목 코드(code) 파라미터가 누락되었습니다.");
            resp.getWriter().write(gson.toJson(responseData));
            return;
        }

        Class<RealtimeDTO> type = RealtimeDTO.class;
        RealtimeDTO cachedRealtimeData =
                cacheManager.get(WebSocketService.REALTIME_CACHE_KEY + code, type);

        if (cachedRealtimeData != null) {
            resp.setStatus(HttpServletResponse.SC_OK);
            responseData.put("status", "success");
            responseData.put("code", code);

            try {
                double price = Double.parseDouble(cachedRealtimeData.getCurPrice().replaceAll(",", ""));
                responseData.put("price", price);

                double fluRt = Double.parseDouble(cachedRealtimeData.getFluRt());
                responseData.put("fluRt", fluRt);

                double changePrice = Double.parseDouble(cachedRealtimeData.getChangePrice());
                responseData.put("changePrice", changePrice);

            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                responseData.put("status", "error");
                responseData.put("message", "캐시된 데이터의 숫자 변환 중 오류 발생: " + e.getMessage());
                responseData.put("price", 0.0);
                responseData.put("fluRt", 0.0);
                responseData.put("changePrice", 0.0);
                resp.getWriter().write(gson.toJson(responseData));
                return;
            }

        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            responseData.put("status", "error");
            responseData.put("message", "종목 코드(" + code + ")의 실시간 가격 정보가 캐시에 없습니다.");
        }

        resp.getWriter().write(gson.toJson(responseData));
    }
}