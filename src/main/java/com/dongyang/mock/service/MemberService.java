package com.dongyang.mock.service;

import com.dongyang.mock.model.DAO.MemberDAO;
import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.model.DTO.MemberStatsDTO;
import com.dongyang.mock.model.status.MemberStatus;
import com.dongyang.mock.util.*;
import com.dongyang.mock.util.db.DBManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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

    public MemberDTO getMemberById(int memberId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBManager.getConnection();
            return memberDAO.selectMemberById(memberId, conn);
        } finally {
            DBManager.close(conn, null);
        }
    }

    public MemberStatus.UpdateResult updateMemberAccount(int memberId, String email, String nickname, String rawPassword) throws SQLException {
        Connection conn = null;
        try {
            conn = DBManager.getConnection();
            conn.setAutoCommit(false);

            if (memberDAO.isEmailDuplicate(email, conn)) {
                MemberDTO existing = memberDAO.selectMemberByEmail(email, conn);
                if (existing != null && existing.getMemId() != memberId) {
                    return MemberStatus.UpdateResult.DUPLICATE_EMAIL;
                }
            }

            if (memberDAO.isNicknameDuplicate(nickname, conn)) {
                // Need to ensure it's not same member
                String sql = "SELECT mem_id FROM member WHERE mem_nickname = ?";
                PreparedStatement pstmt = null;
                ResultSet rs = null;
                try {
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, nickname);
                    rs = pstmt.executeQuery();
                    if (rs.next() && rs.getInt("mem_id") != memberId) {
                        return MemberStatus.UpdateResult.DUPLICATE_NICKNAME;
                    }
                } finally {
                    DBManager.close(null, pstmt, rs);
                }
            }

            int updated = memberDAO.updateMemberAccount(memberId, email, nickname, rawPassword, conn);
            if (updated > 0) {
                conn.commit();
                return MemberStatus.UpdateResult.SUCCESS;
            } else {
                conn.rollback();
                return MemberStatus.UpdateResult.FAIL;
            }
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
            DBManager.close(conn, null);
        }
    }

    public boolean deleteMember(int memberId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBManager.getConnection();
            conn.setAutoCommit(false);
            int deleted = memberDAO.deleteMember(memberId, conn);
            if (deleted > 0) {
                conn.commit();
                return true;
            }
            conn.rollback();
            return false;
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
            }
            DBManager.close(conn, null);
        }
    }

    public List<MemberDTO> getMembers(String keyword, String statusFilter, String profitFilter, String joinFilter) throws SQLException {
        Connection conn = null;
        try {
            conn = DBManager.getConnection();
            return memberDAO.findMembers(keyword, statusFilter, profitFilter, joinFilter, conn);
        } finally {
            DBManager.close(conn, null);
        }
    }

    public MemberStatsDTO getMemberStats() throws SQLException {
        Connection conn = null;
        try {
            conn = DBManager.getConnection();
            return memberDAO.fetchMemberStats(conn);
        } finally {
            DBManager.close(conn, null);
        }
    }
}
