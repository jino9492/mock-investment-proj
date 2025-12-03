package com.dongyang.mock.controller.admin;

import com.dongyang.mock.model.DAO.AdminDAO;
import com.dongyang.mock.model.DTO.AdminDTO;
import com.dongyang.mock.util.PasswordHasher;
import com.dongyang.mock.util.db.DBManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * 관리자 관리 컨트롤러
 * - 관리자 목록 조회
 * - 신규 관리자 생성
 * - 관리자 정보 수정
 * - 관리자 삭제
 */
@WebServlet(urlPatterns = {"/admin/admins", "/admin/admins/form"})
public class AdminAdminsController extends BaseAdminController {

    private final AdminDAO adminDAO = new AdminDAO();

    /**
     * GET 요청 처리
     * - /admin/admins: 관리자 목록 페이지
     * - /admin/admins/form: 관리자 등록/수정 폼 페이지
     */
    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String servletPath = req.getServletPath();
        
        if ("/admin/admins/form".equals(servletPath)) {
            setCurrentPath(req, "/admin/admins");
            handleAdminForm(req, resp);
        } else {
            setCurrentPath(req, "/admin/admins");
            handleAdminsPage(req, resp);
        }
    }

    /**
     * POST 요청 처리
     * - /admin/admins: 신규 관리자 생성
     * - /admin/admins/form: 관리자 정보 수정/삭제
     */
    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String servletPath = req.getServletPath();
        String action = req.getParameter("action");
        
        if ("/admin/admins/form".equals(servletPath) && "update".equals(action)) {
            handleAdminUpdate(req, resp);
        } else if ("/admin/admins/form".equals(servletPath) && "delete".equals(action)) {
            handleAdminDelete(req, resp);
        } else {
            handleCreateAdmin(req, resp);
        }
    }

    /**
     * 관리자 목록 페이지 처리
     */
    private void handleAdminsPage(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        try {
            List<AdminDTO> admins = DBManager.withConnection(adminDAO::getAllAdmins);
            req.setAttribute("adminList", admins);
        } catch (SQLException e) {
            handleSQLException(e, "Failed to load admin list");
        }
        
        req.getRequestDispatcher("/WEB-INF/views/admin/admins.jsp").forward(req, resp);
    }

    /**
     * 관리자 등록/수정 폼 페이지 처리
     */
    private void handleAdminForm(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        Integer adminId = parseIntSafely(req.getParameter("adminId"));
        
        if (adminId != null) {
            // 수정 모드
            try {
                AdminDTO admin = DBManager.withConnection(conn -> adminDAO.getAdminById(adminId, conn));
                if (admin == null) {
                    redirectWithStatus(req, resp, "/admin/admins", "not_found");
                    return;
                }
                req.setAttribute("admin", admin);
                req.setAttribute("isEdit", true);
            } catch (SQLException e) {
                handleSQLException(e, "Failed to load admin");
            }
        } else {
            // 등록 모드
            req.setAttribute("isEdit", false);
        }
        
        req.getRequestDispatcher("/WEB-INF/views/admin/admin_form.jsp").forward(req, resp);
    }

    /**
     * 신규 관리자 생성 처리
     */
    private void handleCreateAdmin(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        String loginId = trim(req.getParameter("adminLoginId"));
        String password = req.getParameter("adminPassword");
        String name = trim(req.getParameter("adminName"));

        if (isBlank(loginId) || isBlank(password) || isBlank(name)) {
            redirectWithStatus(req, resp, "/admin/admins", "invalid");
            return;
        }

        try {
            // 비밀번호 해시화
            String hashedPassword = PasswordHasher.hashPassword(password);

            AdminDTO newAdmin = new AdminDTO();
            newAdmin.setAdminUsername(loginId);
            newAdmin.setAdminPassword(hashedPassword);
            newAdmin.setAdminName(name);

            // 중복 체크와 INSERT를 하나의 트랜잭션으로 통합
            boolean success = DBManager.executeInTransaction(conn -> {
                // 로그인 ID 중복 체크
                if (adminDAO.isLoginIdDuplicate(loginId, null, conn)) {
                    return DBManager.TransactionResult.rollback(Boolean.FALSE);
                }
                
                int inserted = adminDAO.insertAdmin(newAdmin, conn);
                if (inserted > 0) {
                    return DBManager.TransactionResult.commit(Boolean.TRUE);
                }
                return DBManager.TransactionResult.rollback(Boolean.FALSE);
            });
            
            if (!success) {
                redirectWithStatus(req, resp, "/admin/admins", "duplicate_login_id");
                return;
            }

            if (success) {
                redirectWithStatus(req, resp, "/admin/admins", "create_success");
            } else {
                redirectWithStatus(req, resp, "/admin/admins", "create_fail");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            redirectWithStatus(req, resp, "/admin/admins", "create_fail");
        }
    }

    /**
     * 관리자 정보 수정 처리
     */
    private void handleAdminUpdate(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        Integer adminId = parseIntSafely(req.getParameter("adminId"));
        String loginId = trim(req.getParameter("adminLoginId"));
        String password = req.getParameter("adminPassword");
        String name = trim(req.getParameter("adminName"));

        if (adminId == null || isBlank(loginId) || isBlank(name)) {
            redirectWithQuery(req, resp, "/admin/admins/form", 
                "adminId=" + adminId + "&status=" + encode("update_fail"));
            return;
        }

        try {
            AdminDTO admin = new AdminDTO();
            admin.setAdminId(adminId);
            admin.setAdminUsername(loginId);
            admin.setAdminName(name);
            
            // 비밀번호가 입력되었으면 변경, 아니면 변경하지 않음
            if (!isBlank(password)) {
                admin.setAdminPassword(PasswordHasher.hashPassword(password));
            }

            // 중복 체크와 UPDATE를 하나의 트랜잭션으로 통합
            boolean success = DBManager.executeInTransaction(conn -> {
                // 로그인 ID 중복 체크 (자기 자신 제외)
                if (adminDAO.isLoginIdDuplicate(loginId, adminId, conn)) {
                    return DBManager.TransactionResult.rollback(Boolean.FALSE);
                }
                
                int updated = adminDAO.updateAdmin(admin, conn);
                if (updated > 0) {
                    return DBManager.TransactionResult.commit(Boolean.TRUE);
                }
                return DBManager.TransactionResult.rollback(Boolean.FALSE);
            });
            
            if (!success) {
                redirectWithQuery(req, resp, "/admin/admins/form", 
                    "adminId=" + adminId + "&status=" + encode("duplicate_login_id"));
                return;
            }

            if (success) {
                redirectWithStatus(req, resp, "/admin/admins", "update_success");
            } else {
                redirectWithQuery(req, resp, "/admin/admins/form", 
                    "adminId=" + adminId + "&status=" + encode("update_fail"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            redirectWithQuery(req, resp, "/admin/admins/form", 
                "adminId=" + adminId + "&status=" + encode("update_fail"));
        }
    }

    /**
     * 관리자 삭제 처리
     */
    private void handleAdminDelete(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        Integer adminId = parseIntSafely(req.getParameter("adminId"));
        
        if (adminId == null) {
            redirectWithStatus(req, resp, "/admin/admins", "delete_fail");
            return;
        }

        try {
            boolean success = DBManager.executeInTransaction(conn -> {
                int deleted = adminDAO.deleteAdmin(adminId, conn);
                if (deleted > 0) {
                    return DBManager.TransactionResult.commit(Boolean.TRUE);
                }
                return DBManager.TransactionResult.rollback(Boolean.FALSE);
            });

            if (success) {
                redirectWithStatus(req, resp, "/admin/admins", "delete_success");
            } else {
                redirectWithStatus(req, resp, "/admin/admins", "delete_fail");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            redirectWithStatus(req, resp, "/admin/admins", "delete_fail");
        }
    }
}

