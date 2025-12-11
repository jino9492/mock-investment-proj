package com.dongyang.mock.controller.stock;

import com.dongyang.mock.model.DAO.MemberDAO;
import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.model.DTO.OrderDTO;
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
import java.util.Map;

/**
 * 주문 API 컨트롤러
 */
@WebServlet("/api/order")
public class OrderController extends HttpServlet {
    private final OrderService orderService = new OrderService();
    private final MemberDAO memberDAO = new MemberDAO();
    private final Gson gson = new Gson();
    
    /**
     * 세션에서 로그인한 회원 정보를 가져옵니다.
     * @param req HTTP 요청
     * @return 회원 정보, 로그인하지 않은 경우 null
     */
    private MemberDTO getMemberFromSession(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("member") == null) {
            return null;
        }
        return (MemberDTO) session.getAttribute("member");
    }
    
    /**
     * 로그인하지 않은 경우 401 응답을 보냅니다.
     * @param resp HTTP 응답
     * @return 로그인 여부 (true: 로그인됨, false: 로그인 안됨)
     * @throws IOException
     */
    private boolean requireLogin(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "로그인이 필요합니다.");
        resp.getWriter().write(gson.toJson(response));
        return false;
    }
    
    /**
     * 에러 응답을 보냅니다.
     * @param resp HTTP 응답
     * @param status HTTP 상태 코드
     * @param message 에러 메시지
     * @throws IOException
     */
    private void sendErrorResponse(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        resp.getWriter().write(gson.toJson(response));
    }
    
    /**
     * 성공 응답을 보냅니다.
     * @param resp HTTP 응답
     * @param data 응답 데이터
     * @throws IOException
     */
    private void sendSuccessResponse(HttpServletResponse resp, Map<String, Object> data) throws IOException {
        Map<String, Object> response = new HashMap<>(data);
        response.put("success", true);
        resp.getWriter().write(gson.toJson(response));
    }
    
    /**
     * 문자열을 Long으로 파싱합니다.
     * @param value 파싱할 문자열
     * @param paramName 파라미터 이름 (에러 메시지용)
     * @return 파싱된 Long 값
     * @throws IllegalArgumentException 파싱 실패 시
     */
    private Long parseLong(String value, String paramName) throws IllegalArgumentException {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(paramName + "가 필요합니다.");
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("올바른 " + paramName + " 형식이 아닙니다.");
        }
    }
    
    /**
     * 문자열을 BigDecimal로 파싱합니다 (콤마 제거).
     * @param value 파싱할 문자열
     * @param paramName 파라미터 이름 (에러 메시지용)
     * @return 파싱된 BigDecimal 값
     * @throws IllegalArgumentException 파싱 실패 시
     */
    private BigDecimal parseBigDecimal(String value, String paramName) throws IllegalArgumentException {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(paramName + "가 필요합니다.");
        }
        try {
            String cleanValue = value.replace(",", "");
            BigDecimal result = new BigDecimal(cleanValue);
            if (result.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(paramName + "은(는) 0보다 커야 합니다.");
            }
            return result;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("올바른 " + paramName + " 형식이 아닙니다.");
        }
    }
    
    /**
     * 선택적 BigDecimal 파라미터를 파싱합니다 (null 허용).
     * @param value 파싱할 문자열
     * @param paramName 파라미터 이름 (에러 메시지용)
     * @return 파싱된 BigDecimal 값, null이거나 빈 문자열이면 null
     * @throws IllegalArgumentException 파싱 실패 시
     */
    private BigDecimal parseBigDecimalOptional(String value, String paramName) throws IllegalArgumentException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            String cleanValue = value.replace(",", "");
            BigDecimal result = new BigDecimal(cleanValue);
            if (result.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(paramName + "은(는) 0보다 커야 합니다.");
            }
            return result;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("올바른 " + paramName + " 형식이 아닙니다.");
        }
    }
    
    /**
     * 선택적 Long 파라미터를 파싱합니다 (null 허용).
     * @param value 파싱할 문자열
     * @param paramName 파라미터 이름 (에러 메시지용)
     * @return 파싱된 Long 값, null이거나 빈 문자열이면 null
     * @throws IllegalArgumentException 파싱 실패 시
     */
    private Long parseLongOptional(String value, String paramName) throws IllegalArgumentException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("올바른 " + paramName + " 형식이 아닙니다.");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        resp.setContentType("application/json;charset=UTF-8");
        
        try {
            MemberDTO member = getMemberFromSession(req);
            if (member == null) {
                requireLogin(resp);
                return;
            }
            
            // 가용 현금 조회
            int availableCash = DBManager.withConnection(conn -> 
                memberDAO.getVirtualCash(member.getMemId(), conn)
            );
            
            Map<String, Object> data = new HashMap<>();
            data.put("availableCash", availableCash);
            sendSuccessResponse(resp, data);
            
        } catch (SQLException e) {
            e.printStackTrace();
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "가용 현금 조회 중 오류가 발생했습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "오류가 발생했습니다.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        resp.setContentType("application/json;charset=UTF-8");
        
        try {
            MemberDTO member = getMemberFromSession(req);
            if (member == null) {
                requireLogin(resp);
                return;
            }
            
            // 요청 파라미터 파싱 및 검증
            String stockIdStr = req.getParameter("stockId");
            String side = req.getParameter("side");
            String orderType = req.getParameter("orderType");
            String priceStr = req.getParameter("price");
            String quantityStr = req.getParameter("quantity");
            String watchPriceStr = req.getParameter("watchPrice");
            String orderDurationStr = req.getParameter("orderDuration");
            
            // 기본 파라미터 검증
            long stockId = parseLong(stockIdStr, "종목 ID");
            
            if (side == null || (!side.equals("BUY") && !side.equals("SELL"))) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, 
                    "올바른 거래 유형이 필요합니다 (BUY/SELL).");
                return;
            }
            
            if (orderType == null || (!orderType.equals("LIMIT") && !orderType.equals("MARKET"))) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, 
                    "올바른 주문 유형이 필요합니다 (LIMIT/MARKET).");
                return;
            }
            
            long quantity = parseLong(quantityStr, "수량");
            if (quantity <= 0) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, 
                    "수량은 1 이상이어야 합니다.");
                return;
            }
            
            // 가격 파싱 (지정가 주문인 경우)
            BigDecimal price = null;
            if ("LIMIT".equals(orderType)) {
                price = parseBigDecimal(priceStr, "가격");
            }
            
            // 조건 주문 파라미터 파싱
            boolean hasCondition = watchPriceStr != null && !watchPriceStr.trim().isEmpty();
            BigDecimal watchPrice = null;
            if (hasCondition) {
                watchPrice = parseBigDecimal(watchPriceStr, "감시 가격");
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
            
            // 주문 생성
            long orderId = orderService.createOrder(order, orderDurationStr);
            
            Map<String, Object> data = new HashMap<>();
            data.put("message", "주문이 성공적으로 생성되었습니다.");
            data.put("orderId", orderId);
            sendSuccessResponse(resp, data);
            
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "주문 생성 중 오류가 발생했습니다.");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        resp.setContentType("application/json;charset=UTF-8");
        
        try {
            MemberDTO member = getMemberFromSession(req);
            if (member == null) {
                requireLogin(resp);
                return;
            }
            
            long orderId = parseLong(req.getParameter("orderId"), "주문 ID");
            
            boolean success = orderService.cancelOrder(orderId, member.getMemId());
            
            Map<String, Object> data = new HashMap<>();
            if (success) {
                data.put("message", "주문이 성공적으로 취소되었습니다.");
            } else {
                data.put("message", "주문 취소에 실패했습니다.");
            }
            sendSuccessResponse(resp, data);
            
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "주문 취소 중 오류가 발생했습니다.");
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        resp.setContentType("application/json;charset=UTF-8");
        
        try {
            MemberDTO member = getMemberFromSession(req);
            if (member == null) {
                requireLogin(resp);
                return;
            }
            
            long orderId = parseLong(req.getParameter("orderId"), "주문 ID");
            String action = req.getParameter("action");
            
            if ("toggle".equals(action)) {
                // 토글 요청
                String isActiveStr = req.getParameter("isActive");
                if (isActiveStr == null || isActiveStr.trim().isEmpty()) {
                    sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, 
                        "isActive 파라미터가 필요합니다.");
                    return;
                }
                
                boolean isActive = Boolean.parseBoolean(isActiveStr);
                boolean success = orderService.toggleConditionalOrder(orderId, member.getMemId(), isActive);
                
                Map<String, Object> data = new HashMap<>();
                if (success) {
                    data.put("message", "조건 주문 활성화 상태가 변경되었습니다.");
                } else {
                    data.put("message", "조건 주문 활성화 상태 변경에 실패했습니다.");
                }
                sendSuccessResponse(resp, data);
                return;
            }
            
            // 주문 조회하여 조건 주문인지 일반 주문인지 확인
            OrderDTO order = orderService.findOrderById(orderId);
            if (order == null) {
                sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, 
                    "주문을 찾을 수 없습니다.");
                return;
            }
            
            if (order.isHasCondition()) {
                // 조건 주문 수정
                BigDecimal watchPrice = parseBigDecimalOptional(req.getParameter("watchPrice"), "감시 가격");
                BigDecimal price = parseBigDecimalOptional(req.getParameter("price"), "가격");
                Long quantity = parseLongOptional(req.getParameter("quantity"), "수량");
                Boolean isActive = null;
                String isActiveStr = req.getParameter("isActive");
                if (isActiveStr != null && !isActiveStr.trim().isEmpty()) {
                    isActive = Boolean.parseBoolean(isActiveStr);
                }
                String orderDurationStr = req.getParameter("orderDuration");
                
                boolean success = orderService.updateConditionalOrder(orderId, member.getMemId(), 
                    watchPrice, price, quantity, isActive, orderDurationStr);
                
                Map<String, Object> data = new HashMap<>();
                if (success) {
                    data.put("message", "조건 주문이 성공적으로 수정되었습니다.");
                } else {
                    data.put("message", "조건 주문 수정에 실패했습니다.");
                }
                sendSuccessResponse(resp, data);
            } else {
                // 일반 주문 수정
                String orderTypeStr = req.getParameter("orderType");
                String orderType = null;
                if (orderTypeStr != null && !orderTypeStr.trim().isEmpty()) {
                    if (!"LIMIT".equals(orderTypeStr) && !"MARKET".equals(orderTypeStr)) {
                        sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, 
                            "올바른 주문 유형이 아닙니다 (LIMIT/MARKET).");
                        return;
                    }
                    orderType = orderTypeStr;
                }
                
                BigDecimal price = parseBigDecimalOptional(req.getParameter("price"), "가격");
                Long quantity = parseLongOptional(req.getParameter("quantity"), "수량");
                
                boolean success = orderService.updateGeneralOrder(orderId, member.getMemId(), 
                    orderType, price, quantity);
                
                Map<String, Object> data = new HashMap<>();
                if (success) {
                    data.put("message", "주문이 성공적으로 수정되었습니다.");
                } else {
                    data.put("message", "주문 수정에 실패했습니다.");
                }
                sendSuccessResponse(resp, data);
            }
            
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorResponse(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "주문 수정 중 오류가 발생했습니다.");
        }
    }
}

