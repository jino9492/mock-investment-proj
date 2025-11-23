package com.dongyang.mock.model.DAO;


import java.sql.*;
import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.util.PasswordHasher;

import static com.dongyang.mock.util.db.DBManager.*;
import java.util.*;

public class MemberDAO {

    // 멤버 추가
    public int insertMember(MemberDTO member, Connection conn) throws SQLException {
        String sql = "INSERT INTO member(mem_email, mem_password, mem_nickname, mem_last_login_IP," +
                "agree_privacy_at, agree_third_at, agree_notification_at)" +
                "values (?, ?, ?, ?, ?, ?, ?)";
        int result = 0;
        PreparedStatement pstmt = null;

        try {
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, member.getMemEmail());
            pstmt.setString(2, PasswordHasher.hashPassword(member.getMemPassword()));
            pstmt.setString(3, member.getMemNickname());
            pstmt.setString(4, member.getMemLastLoginIP());
            pstmt.setTimestamp(5, member.getAgreePrivacyAt());
            pstmt.setTimestamp(6, member.getAgreeThirdAt());
            pstmt.setTimestamp(7, member.getAgreeNotificationAt());

            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            close(null, pstmt);
        }
        return result;
    }

    // 닉네임으로 검색
    public MemberDTO selectMemberByEmail(String email, Connection conn) throws SQLException {
        String sql = "SELECT * FROM member WHERE mem_email=?";
        MemberDTO member = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                member = new MemberDTO();

                member.setMemId(rs.getInt("mem_id"));
                member.setMemEmail(rs.getString("mem_email"));
                member.setMemPassword(rs.getString("mem_password"));
                member.setMemNickname(rs.getString("mem_nickname"));
                member.setMemJoinAt(rs.getTimestamp("mem_join_at"));
                member.setMemLastLoginIP(rs.getString("mem_last_login_ip"));
                member.setMemLastLoginAt(rs.getTimestamp("mem_last_login_at"));
                member.setAgreePrivacyAt(rs.getTimestamp("agree_privacy_at"));
                member.setAgreeThirdAt(rs.getTimestamp("agree_third_at"));
                member.setAgreeNotificationAt(rs.getTimestamp("agree_notification_at"));
                member.setVirtualCash(rs.getInt("virtual_cash"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(null, pstmt, rs);
        }
        return member;
    }

    // 닉네임 중복 체크
    public boolean isNicknameDuplicate(String nickname, Connection conn) throws SQLException {
        String sql = "SELECT * FROM member WHERE mem_nickname=?";
        return isUnique(sql, nickname, conn);
    }

    // 이메일 중복 체크
    public boolean isEmailDuplicate(String email, Connection conn) throws SQLException {
        String sql = "SELECT * FROM member WHERE mem_email=?";
        return isUnique(sql, email, conn);
    }

    public boolean isUnique(String sql, String col, Connection conn){
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, col);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return true;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(null, pstmt, rs);
        }

        return false;
    }

    // 최근 로그인 기록 갱신
    public int renewLastLogin(String email, String loginIP, Connection conn) throws SQLException {
        String sql = "UPDATE member SET mem_last_login_at = NOW(), mem_last_login_ip = ? WHERE mem_email = ?";
        int result = 0;
        PreparedStatement pstmt = null;

        try {
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, loginIP);
            pstmt.setString(2, email);

            result = pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            close(null, pstmt);
        }
        return result;
    }
}