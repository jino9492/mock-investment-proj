package com.dongyang.mock.controller.stock;

import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.model.DTO.OrderDTO;
import com.dongyang.mock.service.OrderService;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 주문 API 컨트롤러
 */
@WebServlet("/api/order")
public class OrderController extends HttpServlet {
    private final OrderService orderService = new OrderService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
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
            
            // 요청 파라미터 파싱
            String stockIdStr = req.getParameter("stockId");
            String side = req.getParameter("side"); // BUY or SELL
            String orderType = req.getParameter("orderType"); // LIMIT or MARKET
            String priceStr = req.getParameter("price");
            String quantityStr = req.getParameter("quantity");
            String watchPriceStr = req.getParameter("watchPrice"); // 조건 주문 감시 가격
            String orderDurationStr = req.getParameter("orderDuration"); // 조건 주문 유지 기간
            
            // 디버깅: 받은 파라미터 로그
            System.out.println("[OrderController] 받은 파라미터:");
            System.out.println("  - stockId: " + stockIdStr);
            System.out.println("  - side: " + side);
            System.out.println("  - orderType: " + orderType);
            System.out.println("  - price: " + priceStr);
            System.out.println("  - quantity: " + quantityStr);
            System.out.println("  - watchPrice: " + watchPriceStr);
            System.out.println("  - orderDuration: " + orderDurationStr);
            System.out.println("  - Content-Type: " + req.getContentType());
            
            // 유효성 검증
            if (stockIdStr == null || stockIdStr.trim().isEmpty()) {
                System.err.println("[OrderController] 종목 ID가 없습니다. stockIdStr: " + stockIdStr);
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.put("success", false);
                response.put("message", "종목 ID가 필요합니다.");
                resp.getWriter().write(gson.toJson(response));
                return;
            }
            
            if (side == null || (!side.equals("BUY") && !side.equals("SELL"))) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.put("success", false);
                response.put("message", "올바른 거래 유형이 필요합니다 (BUY/SELL).");
                resp.getWriter().write(gson.toJson(response));
                return;
            }
            
            if (orderType == null || (!orderType.equals("LIMIT") && !orderType.equals("MARKET"))) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.put("success", false);
                response.put("message", "올바른 주문 유형이 필요합니다 (LIMIT/MARKET).");
                resp.getWriter().write(gson.toJson(response));
                return;
            }
            
            if (quantityStr == null || quantityStr.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.put("success", false);
                response.put("message", "수량이 필요합니다.");
                resp.getWriter().write(gson.toJson(response));
                return;
            }
            
            long stockId = Long.parseLong(stockIdStr);
            long quantity = Long.parseLong(quantityStr);
            
            if (quantity <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.put("success", false);
                response.put("message", "수량은 1 이상이어야 합니다.");
                resp.getWriter().write(gson.toJson(response));
                return;
            }
            
            BigDecimal price = null;
            if ("LIMIT".equals(orderType)) {
                if (priceStr == null || priceStr.trim().isEmpty()) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.put("success", false);
                    response.put("message", "지정가 주문은 가격이 필요합니다.");
                    resp.getWriter().write(gson.toJson(response));
                    return;
                }
                
                try {
                    // 콤마 제거 후 파싱
                    String cleanPrice = priceStr.replace(",", "");
                    price = new BigDecimal(cleanPrice);
                    
                    if (price.compareTo(BigDecimal.ZERO) <= 0) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.put("success", false);
                        response.put("message", "가격은 0보다 커야 합니다.");
                        resp.getWriter().write(gson.toJson(response));
                        return;
                    }
                } catch (NumberFormatException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.put("success", false);
                    response.put("message", "올바른 가격 형식이 아닙니다.");
                    resp.getWriter().write(gson.toJson(response));
                    return;
                }
            }
            
            // 조건 주문 여부 확인
            boolean hasCondition = watchPriceStr != null && !watchPriceStr.trim().isEmpty();
            BigDecimal watchPrice = null;
            
            if (hasCondition) {
                try {
                    String cleanWatchPrice = watchPriceStr.replace(",", "");
                    watchPrice = new BigDecimal(cleanWatchPrice);
                    
                    if (watchPrice.compareTo(BigDecimal.ZERO) <= 0) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.put("success", false);
                        response.put("message", "감시 가격은 0보다 커야 합니다.");
                        resp.getWriter().write(gson.toJson(response));
                        return;
                    }
                } catch (NumberFormatException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.put("success", false);
                    response.put("message", "올바른 감시 가격 형식이 아닙니다.");
                    resp.getWriter().write(gson.toJson(response));
                    return;
                }
            }
            
            // OrderDTO 생성
            OrderDTO order = new OrderDTO();
            order.setMemId(member.getMemId());
            order.setStockId(stockId);
            order.setSide(side);
            order.setOrderType(orderType);
            order.setPrice(price);
            order.setQuantity(quantity);
            order.setStatus("PENDING");
            order.setHasCondition(hasCondition);
            order.setWatchPrice(watchPrice);
            
            // 주문 생성 (조건 주문인 경우 orderDuration도 전달)
            long orderId = orderService.createOrder(order, orderDurationStr);
            
            response.put("success", true);
            response.put("message", "주문이 성공적으로 생성되었습니다.");
            response.put("orderId", orderId);
            resp.getWriter().write(gson.toJson(response));
            
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.put("success", false);
            response.put("message", e.getMessage());
            resp.getWriter().write(gson.toJson(response));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.put("success", false);
            response.put("message", "주문 생성 중 오류가 발생했습니다.");
            resp.getWriter().write(gson.toJson(response));
        }
    }
}

