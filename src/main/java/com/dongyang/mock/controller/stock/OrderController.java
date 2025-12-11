package com.dongyang.mock.controller.stock;

import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.model.DTO.OrderDTO;
import com.dongyang.mock.model.DAO.MemberDAO;
import com.dongyang.mock.service.OrderService;
import com.dongyang.mock.util.db.DBManager;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 주문 API 컨트롤러
 */
@WebServlet("/api/order")
public class OrderController extends HttpServlet {
    private final OrderService orderService = new OrderService();
    private final MemberDAO memberDAO = new MemberDAO();
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
            
            // 대기 주문 목록 조회
            List<OrderDTO> pendingOrders = orderService.getPendingOrders(member.getMemId());
            
            // 회원의 최신 구매 가능 금액 조회 및 세션 업데이트
            int availableCash = 0;
            try {
                availableCash = DBManager.withConnection(conn -> 
                    memberDAO.getVirtualCash(member.getMemId(), conn));
                
                // 세션의 member 정보 업데이트 (최신 예치금 반영)
                member.setVirtualCash(availableCash);
                session.setAttribute("member", member);
            } catch (SQLException e) {
                System.err.println("[OrderController] 구매 가능 금액 조회 중 오류 발생: " + e.getMessage());
                e.printStackTrace();
            }
            
            // stockId 파라미터가 있으면 해당 종목의 대기 중인 판매 주문 수량 조회
            String stockIdStr = req.getParameter("stockId");
            if (stockIdStr != null && !stockIdStr.trim().isEmpty()) {
                try {
                    long stockId = Long.parseLong(stockIdStr);
                    long pendingSellQuantity = orderService.getPendingSellOrderQuantity(member.getMemId(), stockId);
                    response.put("pendingSellQuantity", pendingSellQuantity);
                } catch (NumberFormatException | SQLException e) {
                    System.err.println("[OrderController] 대기 중인 판매 주문 수량 조회 중 오류 발생: " + e.getMessage());
                    e.printStackTrace();
                    response.put("pendingSellQuantity", 0);
                }
            }
            
            response.put("success", true);
            response.put("orders", pendingOrders);
            response.put("availableCash", availableCash);
            resp.getWriter().write(gson.toJson(response));
            
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.put("success", false);
            response.put("message", "주문 목록 조회 중 오류가 발생했습니다.");
            resp.getWriter().write(gson.toJson(response));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.put("success", false);
            response.put("message", "주문 목록 조회 중 오류가 발생했습니다.");
            resp.getWriter().write(gson.toJson(response));
        }
    }

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
            
            // 세션의 member 정보 업데이트 (최신 예치금 반영)
            try {
                int latestVirtualCash = DBManager.withConnection(conn -> 
                    memberDAO.getVirtualCash(member.getMemId(), conn));
                member.setVirtualCash(latestVirtualCash);
                session.setAttribute("member", member);
            } catch (SQLException e) {
                System.err.println("[OrderController] 세션 업데이트 중 오류 발생: " + e.getMessage());
                // 세션 업데이트 실패해도 주문 생성은 성공했으므로 계속 진행
            }
            
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

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
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
            
            // 주문 ID 파라미터 확인
            String orderIdStr = req.getParameter("orderId");
            if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.put("success", false);
                response.put("message", "주문 ID가 필요합니다.");
                resp.getWriter().write(gson.toJson(response));
                return;
            }
            
            long orderId;
            try {
                orderId = Long.parseLong(orderIdStr);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.put("success", false);
                response.put("message", "올바른 주문 ID 형식이 아닙니다.");
                resp.getWriter().write(gson.toJson(response));
                return;
            }
            
            // 주문 취소 실행
            boolean cancelled = orderService.cancelOrder(orderId, member.getMemId());
            
            if (cancelled) {
                // 세션의 member 정보 업데이트 (최신 예치금 반영)
                try {
                    int latestVirtualCash = DBManager.withConnection(conn -> 
                        memberDAO.getVirtualCash(member.getMemId(), conn));
                    member.setVirtualCash(latestVirtualCash);
                    session.setAttribute("member", member);
                } catch (SQLException e) {
                    System.err.println("[OrderController] 세션 업데이트 중 오류 발생: " + e.getMessage());
                    // 세션 업데이트 실패해도 주문 취소는 성공했으므로 계속 진행
                }
                
                response.put("success", true);
                response.put("message", "주문이 성공적으로 취소되었습니다.");
                resp.getWriter().write(gson.toJson(response));
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.put("success", false);
                response.put("message", "주문 취소에 실패했습니다.");
                resp.getWriter().write(gson.toJson(response));
            }
            
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.put("success", false);
            response.put("message", e.getMessage());
            resp.getWriter().write(gson.toJson(response));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.put("success", false);
            response.put("message", "주문 취소 중 오류가 발생했습니다.");
            resp.getWriter().write(gson.toJson(response));
        }
    }
}

