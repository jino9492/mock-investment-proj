package com.dongyang.mock.service;

import com.dongyang.mock.model.DAO.MemberDAO;
import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.model.status.MemberStatus;
import com.dongyang.mock.util.*;
import com.dongyang.mock.util.db.DBManager;

import java.sql.Connection;
import java.sql.SQLException;

public class MemberService {
    private final MemberDAO memberDAO = new MemberDAO();

    public MemberStatus.JoinResult join(MemberDTO member) throws SQLException {
        Connection conn = null;
        conn = DBManager.getConnection();
        MemberStatus.JoinResult status = MemberStatus.JoinResult.FAIL_INSERT;
        try {
            conn.setAutoCommit(false);

            // 이메일 중복 체크
            if (memberDAO.isEmailDuplicate(member.getMemEmail(), conn)) { return MemberStatus.JoinResult.DUPLICATE_EMAIL; }
            // 닉네임 중복 체크
            if (memberDAO.isNicknameDuplicate(member.getMemNickname(), conn)) { return MemberStatus.JoinResult.DUPLICATE_NICKNAME; }

            int result = memberDAO.insertMember(member, conn);

            if (result > 0) {
                conn.commit();
                status = MemberStatus.JoinResult.SUCCESS;
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

        return status;
    }

    public MemberDTO login(String email, String plainPassword) throws SQLException {
        Connection conn = DBManager.getConnection();
        MemberDTO member = memberDAO.selectMemberByEmail(email, conn);

        if (member != null) {
            String hashedPassword = member.getMemPassword();

            // 해싱된 비밀번호 비교
            if (PasswordHasher.checkPassword(plainPassword, hashedPassword)) {
                memberDAO.renewLastLogin(email, member.getMemLastLoginIP(), conn);
                member.setMemPassword(null); // 보안 이슈
                return member;
            }
        }

        return null;
    }

    public int modifyMemberInfo(MemberDTO member) throws SQLException {
        Connection conn = null;
        int updatedCount = 0;

        try {
            conn = DBManager.getConnection();
            conn.setAutoCommit(false);

            String email = member.getMemEmail();

            if (member.getMemNickname() != null && !member.getMemNickname().isEmpty()) {
                updatedCount += memberDAO.updateNickname(email, member.getMemNickname(), conn);
            }

            if (member.getMemPassword() != null && !member.getMemPassword().isEmpty()) {
                updatedCount += memberDAO.updatePassword(email, member.getMemPassword(), conn);
            }

            if (member.getAgreeNotificationAt() != null) {
                updatedCount += memberDAO.updateNotificationAgreement(email, member.getAgreeNotificationAt(), conn);
            } else {
                updatedCount += memberDAO.updateNotificationAgreement(email, null, conn);
            }

            conn.commit();

        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException rollbackEx) { rollbackEx.printStackTrace(); }
            e.printStackTrace();
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            DBManager.close(conn, null);
        }

        return updatedCount;
    }
}
