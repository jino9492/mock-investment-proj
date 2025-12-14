package com.dongyang.mock.controller.mypage;

import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.model.DTO.OrderDTO;
import com.dongyang.mock.model.DTO.OrderConditionDTO;
import com.dongyang.mock.service.OrderService;
import com.dongyang.mock.util.JsonUtil;
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

@WebServlet("/mypage/orders")
public class OrderDetailsController extends HttpServlet {

    private final String viewPage = "/WEB-INF/views/mypage/order_details.jsp";
    private final OrderService orderService = new OrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");

        // 세션에서 회원 정보 확인
        HttpSession session = req.getSession(false);
        MemberDTO sessionMember = null;
        if (session != null) {
            sessionMember = (MemberDTO) session.getAttribute("member");
        }

        if (sessionMember == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int memberId = sessionMember.getMemId();

        try {
            // 대기 주문 조회
            List<OrderDTO> pendingOrders = orderService.getPendingOrders(memberId);
            
            // 완료된 주문 조회
            List<OrderDTO> completedOrders = orderService.getCompletedOrders(memberId);
            
            // 조건 주문 정보 조회 (대기 주문 중 조건 주문인 경우)
            Map<Long, OrderConditionDTO> conditionMap = new HashMap<>();
            for (OrderDTO order : pendingOrders) {
                if (order.isHasCondition()) {
                    OrderConditionDTO condition = orderService.getOrderCondition(order.getOrderId());
                    if (condition != null) {
                        conditionMap.put(order.getOrderId(), condition);
                    }
                }
            }
            
            // JSON 문자열 생성 (이스케이프 없이 순수 JSON)
            List<String> pendingOrdersJson = new ArrayList<>();
            for (OrderDTO order : pendingOrders) {
                OrderConditionDTO condition = conditionMap.get(order.getOrderId());
                String conditionJson = null;
                if (order.isHasCondition() && condition != null) {
                    conditionJson = JsonUtil.conditionToJson(condition.getTriggerPrice(), condition.getValidUntil());
                }
                String orderJson = JsonUtil.orderToJson(
                    order.getOrderId(),
                    order.getStockName(),
                    order.getSide(),
                    order.getOrderType(),
                    order.getPrice(),
                    order.getQuantity(),
                    order.isHasCondition(),
                    order.getPlacedAt(),
                    conditionJson
                );
                pendingOrdersJson.add(orderJson);
            }
            
            List<String> completedOrdersJson = new ArrayList<>();
            for (OrderDTO order : completedOrders) {
                String orderJson = JsonUtil.completedOrderToJson(
                    order.getOrderId(),
                    order.getStockName(),
                    order.getSide(),
                    order.getStatus(),
                    order.getOrderType(),
                    order.getPrice(),
                    order.getQuantity(),
                    order.getPlacedAt(),
                    order.getCancelledAt(),
                    order.getTradedAt()
                );
                completedOrdersJson.add(orderJson);
            }
            
            // JSP에 데이터 전달
            req.setAttribute("pendingOrders", pendingOrders);
            req.setAttribute("completedOrders", completedOrders);
            req.setAttribute("conditionMap", conditionMap);
            req.setAttribute("member", sessionMember);
            req.setAttribute("pendingOrdersJson", pendingOrdersJson);
            req.setAttribute("completedOrdersJson", completedOrdersJson);
            
        } catch (SQLException e) {
            e.printStackTrace();
            // 에러 발생 시 빈 리스트 전달
            req.setAttribute("pendingOrders", new ArrayList<>());
            req.setAttribute("completedOrders", new ArrayList<>());
            req.setAttribute("conditionMap", new HashMap<>());
            req.setAttribute("pendingOrdersJson", new ArrayList<>());
            req.setAttribute("completedOrdersJson", new ArrayList<>());
        }

        req.getRequestDispatcher(viewPage).forward(req, resp);
    }
}
