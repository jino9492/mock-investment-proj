package com.dongyang.mock.model.DAO;

import com.dongyang.mock.model.DTO.AdminDTO;
import com.dongyang.mock.util.PasswordHasher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
        String sql = "SELECT admin_id, admin_username, admin_password, admin_name, " +
                     "last_login_at, last_login_ip, created_at, updated_at " +
                     "FROM admin WHERE admin_username = ?";
        
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
                    int adminId = rs.getInt("admin_id");
                    
                    // 로그인 정보 업데이트
                    updateLastLogin(adminId, loginIp, conn);
                    
                    // AdminDTO 생성 (비밀번호 제외, 업데이트된 로그인 정보 포함)
                    AdminDTO admin = new AdminDTO();
                    admin.setAdminId(adminId);
                    admin.setAdminUsername(rs.getString("admin_username"));
                    admin.setAdminPassword(null); // 비밀번호는 반환하지 않음
                    admin.setAdminName(rs.getString("admin_name"));
                    // 업데이트된 로그인 정보 설정 (NOW()로 업데이트했으므로 현재 시간, 입력 IP)
                    admin.setLastLoginAt(new Timestamp(System.currentTimeMillis()));
                    admin.setLastLoginIp(loginIp);
                    admin.setCreatedAt(rs.getTimestamp("created_at"));
                    admin.setUpdatedAt(rs.getTimestamp("updated_at"));
                    
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
        
        // last_login_at은 NULL일 수 있음
        Timestamp lastLoginAt = rs.getTimestamp("last_login_at");
        admin.setLastLoginAt(lastLoginAt);
        
        // last_login_ip는 NULL일 수 있음
        String lastLoginIp = rs.getString("last_login_ip");
        if (rs.wasNull()) {
            lastLoginIp = null;
        }
        admin.setLastLoginIp(lastLoginIp);
        
        admin.setCreatedAt(rs.getTimestamp("created_at"));
        
        // updated_at은 NULL일 수 있음
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        admin.setUpdatedAt(updatedAt);
        
        return admin;
    }

    /**
     * 전체 관리자 목록 조회 (비밀번호 제외, 보안 및 성능 최적화)
     * 
     * @param conn 데이터베이스 연결
     * @return 관리자 목록
     * @throws SQLException DB 오류 발생 시
     */
    public List<AdminDTO> getAllAdmins(Connection conn) throws SQLException {
        String sql = "SELECT admin_id, admin_username, admin_name, " +
                     "last_login_at, last_login_ip, created_at, updated_at " +
                     "FROM admin ORDER BY admin_id ASC";
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<AdminDTO> admins = new ArrayList<>();
        
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                admins.add(mapToAdminWithoutPassword(rs));
            }
            return admins;
        } finally {
            close(null, pstmt, rs);
        }
    }
    
    /**
     * 비밀번호를 제외한 AdminDTO 매핑 (목록 조회용)
     */
    private AdminDTO mapToAdminWithoutPassword(ResultSet rs) throws SQLException {
        AdminDTO admin = new AdminDTO();
        admin.setAdminId(rs.getInt("admin_id"));
        admin.setAdminUsername(rs.getString("admin_username"));
        admin.setAdminPassword(null); // 비밀번호는 조회하지 않음
        admin.setAdminName(rs.getString("admin_name"));
        
        // last_login_at은 NULL일 수 있음
        Timestamp lastLoginAt = rs.getTimestamp("last_login_at");
        admin.setLastLoginAt(lastLoginAt);
        
        // last_login_ip는 NULL일 수 있음
        String lastLoginIp = rs.getString("last_login_ip");
        if (rs.wasNull()) {
            lastLoginIp = null;
        }
        admin.setLastLoginIp(lastLoginIp);
        
        admin.setCreatedAt(rs.getTimestamp("created_at"));
        
        // updated_at은 NULL일 수 있음
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        admin.setUpdatedAt(updatedAt);
        
        return admin;
    }

    /**
     * 관리자 ID로 관리자 정보 조회
     * 
     * @param adminId 관리자 ID
     * @param conn 데이터베이스 연결
     * @return 관리자 정보, 없으면 null
     * @throws SQLException DB 오류 발생 시
     */
    public AdminDTO getAdminById(int adminId, Connection conn) throws SQLException {
        String sql = "SELECT admin_id, admin_username, admin_password, admin_name, " +
                     "last_login_at, last_login_ip, created_at, updated_at " +
                     "FROM admin WHERE admin_id = ?";
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, adminId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return mapToAdmin(rs);
            }
            return null;
        } finally {
            close(null, pstmt, rs);
        }
    }

    /**
     * 관리자 로그인 ID 중복 체크
     * 
     * @param loginId 관리자 로그인 ID
     * @param excludeAdminId 제외할 관리자 ID (null이면 모든 관리자 체크)
     * @param conn 데이터베이스 연결
     * @return 중복이면 true, 없으면 false
     * @throws SQLException DB 오류 발생 시
     */
    public boolean isLoginIdDuplicate(String loginId, Integer excludeAdminId, Connection conn) throws SQLException {
        String sql;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            if (excludeAdminId != null) {
                sql = "SELECT 1 FROM admin WHERE admin_username = ? AND admin_id != ? LIMIT 1";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, loginId);
                pstmt.setInt(2, excludeAdminId);
            } else {
                sql = "SELECT 1 FROM admin WHERE admin_username = ? LIMIT 1";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, loginId);
            }
            
            rs = pstmt.executeQuery();
            return rs.next();
        } finally {
            close(null, pstmt, rs);
        }
    }

    /**
     * 신규 관리자 등록
     * 
     * @param admin 관리자 정보 (비밀번호는 이미 해시된 상태여야 함)
     * @param conn 데이터베이스 연결
     * @return 영향받은 행 수 (1: 성공, 0: 실패)
     * @throws SQLException DB 오류 발생 시
     */
    public int insertAdmin(AdminDTO admin, Connection conn) throws SQLException {
        String sql = "INSERT INTO admin (admin_username, admin_password, admin_name) " +
                     "VALUES (?, ?, ?)";
        
        PreparedStatement pstmt = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, admin.getAdminUsername());
            pstmt.setString(2, admin.getAdminPassword());
            pstmt.setString(3, admin.getAdminName());
            
            return pstmt.executeUpdate();
        } finally {
            close(null, pstmt);
        }
    }

    /**
     * 관리자 정보 수정
     * 
     * @param admin 수정할 관리자 정보 (admin_id 필수)
     * @param conn 데이터베이스 연결
     * @return 영향받은 행 수
     * @throws SQLException DB 오류 발생 시
     */
    public int updateAdmin(AdminDTO admin, Connection conn) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE admin SET ");
        List<Object> params = new ArrayList<>();
        boolean needsComma = false;
        
        if (admin.getAdminUsername() != null && !admin.getAdminUsername().trim().isEmpty()) {
            sql.append("admin_username = ?");
            params.add(admin.getAdminUsername());
            needsComma = true;
        }
        
        if (admin.getAdminPassword() != null && !admin.getAdminPassword().trim().isEmpty()) {
            if (needsComma) sql.append(", ");
            sql.append("admin_password = ?");
            params.add(admin.getAdminPassword());
            needsComma = true;
        }
        
        if (admin.getAdminName() != null && !admin.getAdminName().trim().isEmpty()) {
            if (needsComma) sql.append(", ");
            sql.append("admin_name = ?");
            params.add(admin.getAdminName());
            needsComma = true;
        }
        
        if (!needsComma) {
            return 0; // 수정할 항목이 없음
        }
        
        sql.append(" WHERE admin_id = ?");
        params.add(admin.getAdminId());
        
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql.toString());
            int index = 1;
            // 마지막 파라미터 전까지 SET 절 파라미터들
            for (int i = 0; i < params.size() - 1; i++) {
                Object param = params.get(i);
                if (param == null) {
                    pstmt.setNull(index++, Types.VARCHAR);
                } else {
                    pstmt.setObject(index++, param);
                }
            }
            // 마지막 파라미터는 admin_id (항상 null이 아님)
            pstmt.setInt(index, (Integer) params.get(params.size() - 1));
            return pstmt.executeUpdate();
        } finally {
            close(null, pstmt);
        }
    }

    /**
     * 관리자 삭제
     * 
     * @param adminId 관리자 ID
     * @param conn 데이터베이스 연결
     * @return 영향받은 행 수 (1: 성공, 0: 실패)
     * @throws SQLException DB 오류 발생 시
     */
    public int deleteAdmin(int adminId, Connection conn) throws SQLException {
        String sql = "DELETE FROM admin WHERE admin_id = ?";
        PreparedStatement pstmt = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, adminId);
            return pstmt.executeUpdate();
        } finally {
            close(null, pstmt);
        }
    }
}

