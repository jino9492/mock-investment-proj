package com.dongyang.mock.controller.util;

import com.dongyang.mock.model.DAO.MemberDAO;
import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.util.db.DBManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/**
 * 임시 테스트용: 예치금 추가 컨트롤러
 * 개발/테스트 목적으로만 사용
 */
@WebServlet("/api/add-cash")
public class AddCashController extends HttpServlet {
    private final MemberDAO memberDAO = new MemberDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // GET 요청도 POST와 동일하게 처리
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        resp.setContentType("application/json;charset=UTF-8");
        
        try {
            // 로그인 확인
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("member") == null) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write("{\"success\": false, \"message\": \"로그인이 필요합니다.\"}");
                return;
            }
            
            MemberDTO member = (MemberDTO) session.getAttribute("member");
            final int memberId = member.getMemId();
            
            // 추가할 금액 파라미터 (기본값: 10,000,000원)
            String amountStr = req.getParameter("amount");
            final int amount;
            if (amountStr != null && !amountStr.trim().isEmpty()) {
                try {
                    amount = Integer.parseInt(amountStr);
                } catch (NumberFormatException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"success\": false, \"message\": \"올바른 금액을 입력해주세요.\"}");
                    return;
                }
            } else {
                amount = 10_000_000; // 기본값: 1천만원
            }
            
            // 예치금 추가
            int newBalance = DBManager.withConnection(conn -> {
                int currentCash = memberDAO.getVirtualCash(memberId, conn);
                int newCash = currentCash + amount;
                
                // DB 업데이트
                String sql = "UPDATE member SET virtual_cash = ? WHERE mem_id = ?";
                try (var pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, newCash);
                    pstmt.setInt(2, memberId);
                    pstmt.executeUpdate();
                }
                
                return newCash;
            });
            
            // 세션의 member 객체 업데이트 (람다 외부에서)
            member.setVirtualCash(newBalance);
            session.setAttribute("member", member);
            
            resp.getWriter().write(String.format(
                "{\"success\": true, \"message\": \"예치금이 추가되었습니다.\", \"newBalance\": %d}",
                newBalance
            ));
            
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\": false, \"message\": \"예치금 추가 중 오류가 발생했습니다: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\": false, \"message\": \"예치금 추가 중 오류가 발생했습니다.\"}");
        }
    }
}

