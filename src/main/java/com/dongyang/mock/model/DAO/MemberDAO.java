package com.dongyang.mock.model.DAO;


import java.sql.*;
import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.util.PasswordHasher;

import static com.dongyang.mock.util.db.DBManager.*;
import java.util.*;

public class MemberDAO {

    // 1. 회원 가입 (Insert)
    public int insertMember(MemberDTO member, Connection conn) throws SQLException {
        String sql = "INSERT INTO member(mem_nickname, mem_password, mem_name," +
                "mem_birth, mem_phone, mem_email, mem_last_login_ip," +
                "mem_last_login_at, mem_open, agree_terms_at, agree_privacy_at," +
                " agree_third_at)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int result = 0;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, member.getMemNickName());
            pstmt.setString(2, PasswordHasher.hashPassword(member.getMemPassword()));
            pstmt.setString(3, member.getMemName());
            pstmt.setDate(4, member.getMemBirth());
            pstmt.setString(5, member.getMemPhone());
            pstmt.setString(6, member.getMemEmail());
            pstmt.setString(7, member.getMemLastLoginIP());
            pstmt.setDate(8, member.getMemLastLoginAt());
            pstmt.setBoolean(9, member.getMemOpen());
            pstmt.setDate(10, member.getAgreeTermsAt());
            pstmt.setDate(11, member.getAgreePrivacyAt());
            pstmt.setDate(12, member.getAgreeThirdAt());

            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(conn, pstmt);
        }
        return result;
    }

    public MemberDTO selectMemberByNickname(String nickname, Connection conn) throws SQLException {
        String sql = "SELECT * FROM member WHERE mem_nickname=?";
        MemberDTO member = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nickname);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                member = new MemberDTO();

                member.setMemId(rs.getInt("mem_id"));
                member.setMemNickName(rs.getString("mem_nickname"));
                member.setMemPassword(rs.getString("mem_password"));
                member.setMemName(rs.getString("mem_name"));
                member.setMemBirth(rs.getDate("mem_birth"));
                member.setMemPhone(rs.getString("mem_phone"));
                member.setMemEmail(rs.getString("mem_email"));
                member.setMemJoinAt(rs.getTimestamp("mem_join_at"));
                member.setMemLastLoginIP(rs.getString("mem_last_login_ip"));
                member.setMemLastLoginAt(rs.getDate("mem_last_login_at"));
                member.setMemOpen(rs.getBoolean("mem_open"));
                member.setAgreeTermsAt(rs.getDate("agree_terms_at"));
                member.setAgreePrivacyAt(rs.getDate("agree_privacy_at"));
                member.setAgreeThirdAt(rs.getDate("agree_third_at"));
                member.setVirtualCash(rs.getInt("virtual_cash"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(conn, pstmt, rs); // DBManager의 close 유틸리티 사용
        }
        return member;
    }

    public boolean isNicknameDuplicate(String nickname, Connection conn) throws SQLException {
        String sql = "SELECT * FROM member WHERE mem_nickname=?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nickname);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return false;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            close(conn, pstmt, rs);
        }
        return true;
    }
}