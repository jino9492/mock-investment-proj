package com.dongyang.mock.service;

import com.dongyang.mock.model.DAO.AdminDAO;
import com.dongyang.mock.model.DTO.AdminDTO;
import com.dongyang.mock.util.db.DBManager;

import java.sql.SQLException;

/**
 * 관리자 관련 비즈니스 로직 서비스
 * - 관리자 로그인
 * - 관리자 정보 조회
 */
public class AdminService {
    private final AdminDAO adminDAO = new AdminDAO();

    /**
     * 관리자 로그인 처리
     * 
     * @param username 관리자 아이디
     * @param plainPassword 평문 비밀번호
     * @param loginIp 로그인 IP 주소
     * @return 로그인 성공 시 AdminDTO, 실패 시 null
     */
    public AdminDTO login(String username, String plainPassword, String loginIp) throws SQLException {
        return DBManager.withConnection(conn -> adminDAO.login(username, plainPassword, loginIp, conn));
    }
}

