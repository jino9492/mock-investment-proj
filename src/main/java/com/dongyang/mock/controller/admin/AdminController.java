package com.dongyang.mock.controller.admin;

import com.dongyang.mock.model.DAO.AdminDAO;
import com.dongyang.mock.model.DTO.AdminDTO;
import com.dongyang.mock.util.db.DBManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;

@WebServlet("/admin/*")
public class AdminController extends HttpServlet {

    /**
     * 루트 페이지 및 로그아웃만 담당: 나머지 세부 경로는 별도 컨트롤러에서 처리.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            if (session != null && session.getAttribute("adminUser") != null) {
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                return;
            }
            req.getRequestDispatcher("/WEB-INF/views/admin/login_content.jsp").forward(req, resp);
            return;
        }

        if ("/logout".equals(pathInfo)) {
            if (session != null) {
                session.invalidate();
            }
            resp.sendRedirect(req.getContextPath() + "/admin");
            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * 관리자 로그인 처리
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            String adminId = req.getParameter("adminId");
            String password = req.getParameter("password");

            // 입력값 검증 - 빈 값이면 그냥 로그인 페이지로 forward (에러 메시지 없음)
            if (adminId == null || adminId.trim().isEmpty() || 
                password == null || password.trim().isEmpty()) {
                req.getRequestDispatcher("/WEB-INF/views/admin/login_content.jsp").forward(req, resp);
                return;
            }

            try {
                // 클라이언트 IP 주소 가져오기
                // 프록시나 로드밸런서를 통한 경우를 대비하여 X-Forwarded-For 헤더 확인
                String forwardedFor = req.getHeader("X-Forwarded-For");
                final String clientIp;
                if (forwardedFor != null && !forwardedFor.isEmpty()) {
                    // X-Forwarded-For는 쉼표로 구분된 여러 IP를 가질 수 있음 (최초 클라이언트가 첫 번째)
                    String ip = forwardedFor.split(",")[0].trim();
                    // IPv6 localhost를 IPv4 localhost로 변환
                    if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
                        clientIp = "127.0.0.1";
                    } else {
                        clientIp = ip;
                    }
                } else {
                    String ip = req.getRemoteAddr();
                    // IPv6 localhost를 IPv4 localhost로 변환
                    if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
                        clientIp = "127.0.0.1";
                    } else {
                        clientIp = ip;
                    }
                }

                // 입력된 아이디로 관리자 로그인 처리
                AdminDAO adminDAO = new AdminDAO();
                AdminDTO admin = DBManager.withConnection(conn -> 
                    adminDAO.login(adminId, password, clientIp, conn));

                if (admin == null) {
                    req.setAttribute("errorMsg", "아이디 또는 비밀번호가 일치하지 않습니다.");
                    req.getRequestDispatcher("/WEB-INF/views/admin/login_content.jsp").forward(req, resp);
                    return;
                }

                // 로그인 성공 - 세션에 관리자 정보 저장 (비밀번호는 이미 login 메서드에서 제거됨)
                HttpSession session = req.getSession();
                session.setAttribute("adminUser", admin);

                // 대시보드로 리다이렉트
                resp.sendRedirect(req.getContextPath() + "/admin/dashboard");

            } catch (SQLException e) {
                e.printStackTrace();
                
                // 상세한 에러 정보 구성
                StringBuilder errorDetails = new StringBuilder();
                errorDetails.append("SQL 오류 발생\n");
                errorDetails.append("에러 타입: SQLException\n");
                errorDetails.append("SQLState: ").append(e.getSQLState()).append("\n");
                errorDetails.append("Error Code: ").append(e.getErrorCode()).append("\n");
                errorDetails.append("에러 메시지: ").append(e.getMessage()).append("\n\n");
                
                // 스택 트레이스 추가
                errorDetails.append("상세 스택 트레이스:\n");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                errorDetails.append(sw.toString());
                
                // 로그에도 출력
                System.err.println(errorDetails.toString());
                
                // 사용자에게 상세한 에러 정보 표시
                req.setAttribute("errorMsg", errorDetails.toString());
                req.getRequestDispatcher("/WEB-INF/views/admin/login_content.jsp").forward(req, resp);
            } catch (Exception e) {
                e.printStackTrace();
                
                // 상세한 에러 정보 구성
                StringBuilder errorDetails = new StringBuilder();
                errorDetails.append("예기치 않은 오류 발생\n");
                errorDetails.append("에러 타입: ").append(e.getClass().getName()).append("\n");
                errorDetails.append("에러 메시지: ").append(e.getMessage()).append("\n\n");
                
                // 스택 트레이스 추가
                errorDetails.append("상세 스택 트레이스:\n");
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                errorDetails.append(sw.toString());
                
                // 로그에도 출력
                System.err.println(errorDetails.toString());
                
                // 사용자에게 상세한 에러 정보 표시
                req.setAttribute("errorMsg", errorDetails.toString());
                req.getRequestDispatcher("/WEB-INF/views/admin/login_content.jsp").forward(req, resp);
            }

            return;
        }

        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}