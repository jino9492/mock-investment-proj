package com.dongyang.mock.controller.api;

import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.model.DTO.MemberHoldingDTO;
import com.dongyang.mock.model.DAO.MemberDAO;
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
import java.util.HashMap;
import java.util.Map;

/**
 * 보유 종목 정보 API 컨트롤러
 */
@WebServlet("/api/holding")
public class HoldingController extends HttpServlet {
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
            int memId = member.getMemId();
            
            // 종목 ID 파라미터 확인 (선택적)
            String stockIdStr = req.getParameter("stockId");
            
            if (stockIdStr != null && !stockIdStr.trim().isEmpty()) {
                // 특정 종목 보유 정보 조회
                long stockId;
                try {
                    stockId = Long.parseLong(stockIdStr);
                } catch (NumberFormatException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.put("success", false);
                    response.put("message", "올바른 종목 ID 형식이 아닙니다.");
                    resp.getWriter().write(gson.toJson(response));
                    return;
                }
                
                MemberHoldingDTO holding = DBManager.withConnection(conn -> 
                    memberDAO.findHoldingByStock(memId, stockId, conn));
                
                response.put("success", true);
                response.put("holding", holding);
            } else {
                // 전체 보유 종목 목록 조회
                java.util.List<MemberHoldingDTO> holdings = DBManager.withConnection(conn -> 
                    memberDAO.fetchMemberHoldings(memId, conn));
                
                response.put("success", true);
                response.put("holdings", holdings);
            }
            
            resp.getWriter().write(gson.toJson(response));
            
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.put("success", false);
            response.put("message", "보유 정보 조회 중 오류가 발생했습니다.");
            resp.getWriter().write(gson.toJson(response));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.put("success", false);
            response.put("message", "보유 정보 조회 중 오류가 발생했습니다.");
            resp.getWriter().write(gson.toJson(response));
        }
    }
}

