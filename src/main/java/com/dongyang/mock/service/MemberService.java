package com.dongyang.mock.service;

import com.dongyang.mock.model.DAO.MemberDAO;
import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.util.*;
import com.dongyang.mock.util.db.DBManager;

import java.sql.Connection;
import java.sql.SQLException;

public class MemberService {
    private MemberDAO memberDAO = new MemberDAO();

    public boolean join(MemberDTO member) throws SQLException {
        Connection conn = null;
        conn = DBManager.getConnection();
        boolean success = false;
        try {
            conn.setAutoCommit(false);

            // 닉네임 중복 체크
            if (memberDAO.isNicknameDuplicate(member.getMemNickName(), conn)) return false;

            int result = memberDAO.insertMember(member, conn);

            if (result > 0) {
                conn.commit();
                success = true;
            } else {
                conn.rollback();
            }
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException rollbackEx) { rollbackEx.printStackTrace(); }
            e.printStackTrace();
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            DBManager.close(conn, null);
        }

        return success;
    }

    public MemberDTO login(String nickname, String plainPassword) throws SQLException {
        Connection conn = DBManager.getConnection();
        MemberDTO member = memberDAO.selectMemberByNickname(nickname, conn);

        if (member != null) {
            String hashedPassword = member.getMemPassword();

            // 해싱된 비밀번호 비교
            if (PasswordHasher.checkPassword(plainPassword, hashedPassword)) {
                memberDAO.renewLastLogin(nickname, member.getMemLastLoginIP(), conn);
                member.setMemPassword(null); // 보안 이슈
                return member;
            }
        }

        return null;
    }
}
