package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.AdminDTO;
import com.dongyang.mock.util.PasswordHasher;

import java.sql.*;
import static com.dongyang.mock.util.db.DBManager.close;

/**
 * 관리자 데이터 접근 객체 (DAO)
 * - 관리자 정보 CRUD 작업
 * - 관리자 로그인 처리
 */
public class AdminDAO {

    /**
     * 관리자 로그인 처리
     * - 아이디와 비밀번호로 관리자 조회
     * - 로그인 성공 시 마지막 로그인 시간 및 IP 업데이트
     * 
     * @param username 관리자 아이디
     * @param plainPassword 평문 비밀번호
     * @param loginIp 로그인 IP 주소
     * @param conn 데이터베이스 연결
     * @return 로그인 성공 시 AdminDTO, 실패 시 null
     */
    public AdminDTO login(String username, String plainPassword, String loginIp, Connection conn) throws SQLException {
        String sql = "SELECT admin_id, admin_username, admin_password, admin_name, admin_role, " +
                     "is_active, last_login_at, last_login_ip, created_at, updated_at " +
                     "FROM admin WHERE admin_username = ? AND is_active = TRUE";
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String hashedPassword = rs.getString("admin_password");
                
                // 비밀번호 검증
                if (PasswordHasher.checkPassword(plainPassword, hashedPassword)) {
                    AdminDTO admin = mapToAdmin(rs);
                    
                    // 마지막 로그인 정보 업데이트
                    updateLastLogin(admin.getAdminId(), loginIp, conn);
                    
                    // 비밀번호는 반환하지 않음
                    admin.setAdminPassword(null);
                    return admin;
                }
            }
            return null;
        } finally {
            close(null, pstmt, rs);
        }
    }

    /**
     * 마지막 로그인 정보 업데이트
     * 
     * @param adminId 관리자 ID
     * @param loginIp 로그인 IP 주소
     * @param conn 데이터베이스 연결
     */
    private void updateLastLogin(int adminId, String loginIp, Connection conn) throws SQLException {
        String sql = "UPDATE admin SET last_login_at = NOW(), last_login_ip = ? WHERE admin_id = ?";
        PreparedStatement pstmt = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, loginIp);
            pstmt.setInt(2, adminId);
            pstmt.executeUpdate();
        } finally {
            close(null, pstmt, null);
        }
    }

    /**
     * ResultSet을 AdminDTO로 변환
     */
    private AdminDTO mapToAdmin(ResultSet rs) throws SQLException {
        AdminDTO admin = new AdminDTO();
        admin.setAdminId(rs.getInt("admin_id"));
        admin.setAdminUsername(rs.getString("admin_username"));
        admin.setAdminPassword(rs.getString("admin_password"));
        admin.setAdminName(rs.getString("admin_name"));
        admin.setAdminRole(rs.getString("admin_role"));
        admin.setActive(rs.getBoolean("is_active"));
        admin.setLastLoginAt(rs.getTimestamp("last_login_at"));
        admin.setLastLoginIp(rs.getString("last_login_ip"));
        admin.setCreatedAt(rs.getTimestamp("created_at"));
        admin.setUpdatedAt(rs.getTimestamp("updated_at"));
        return admin;
    }
}

