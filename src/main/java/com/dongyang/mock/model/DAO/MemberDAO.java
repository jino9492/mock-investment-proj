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
            pstmt = conn.prepareStatement(sql);
            System.out.println("--- 회원가입 필드 값 디버깅 시작 ---");
            System.out.println("1. 닉네임: " + member.getMemNickName());
            System.out.println("2. 비밀번호(해시 전): " + member.getMemPassword());
            System.out.println("3. 이름: " + member.getMemName());
            System.out.println("4. 생년월일: " + member.getMemBirth());
            System.out.println("5. 전화번호: " + member.getMemPhone());
            System.out.println("6. 이메일: " + member.getMemEmail());
            System.out.println("7. 마지막 로그인 IP: " + member.getMemLastLoginIP());
            System.out.println("8. 마지막 로그인 시각: " + member.getMemLastLoginAt());
            System.out.println("9. 공개 여부: " + member.getMemOpen()); // DTO에서 isMemOpen()을 사용한다고 가정
            System.out.println("10. 약관1 동의: " + member.getAgreeTermsAt());
            System.out.println("11. 약관2 동의: " + member.getAgreePrivacyAt());
            System.out.println("12. 약관3 동의: " + member.getAgreeThirdAt());
            System.out.println("---------------------------------");
            pstmt.setString(1, member.getMemNickName());
            pstmt.setString(2, PasswordHasher.hashPassword(member.getMemPassword()));
            pstmt.setString(3, member.getMemName());
            pstmt.setDate(4, member.getMemBirth());
            pstmt.setString(5, member.getMemPhone());
            pstmt.setString(6, member.getMemEmail());
            pstmt.setString(7, member.getMemLastLoginIP());
            pstmt.setTimestamp(8, member.getMemLastLoginAt());
            pstmt.setBoolean(9, member.getMemOpen());
            pstmt.setTimestamp(10, member.getAgreeTermsAt());
            pstmt.setTimestamp(11, member.getAgreePrivacyAt());
            pstmt.setTimestamp(12, member.getAgreeThirdAt());

            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            close(null, pstmt);
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
                member.setMemLastLoginAt(rs.getTimestamp("mem_last_login_at"));
                member.setMemOpen(rs.getBoolean("mem_open"));
                member.setAgreeTermsAt(rs.getTimestamp("agree_terms_at"));
                member.setAgreePrivacyAt(rs.getTimestamp("agree_privacy_at"));
                member.setAgreeThirdAt(rs.getTimestamp("agree_third_at"));
                member.setVirtualCash(rs.getInt("virtual_cash"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(null, pstmt, rs);
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
        } finally {
            close(null, pstmt, rs);
        }

        return true;
    }
}