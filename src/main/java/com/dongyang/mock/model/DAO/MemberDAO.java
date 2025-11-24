package com.dongyang.mock.model.DAO;


import java.sql.*;
import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.model.DTO.MemberStatsDTO;
import com.dongyang.mock.util.PasswordHasher;

import static com.dongyang.mock.util.db.DBManager.*;
import java.util.ArrayList;
import java.util.List;

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

    public MemberDTO selectMemberById(int memberId, Connection conn) throws SQLException {
        String sql = "SELECT * FROM member WHERE mem_id=?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapToMember(rs);
            }
        } finally {
            close(null, pstmt, rs);
        }
        return null;
    }

    public int updateMemberAccount(int memberId, String email, String nickname, String rawPassword, Connection conn) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE member SET mem_email = ?, mem_nickname = ?");
        boolean hasPassword = rawPassword != null && !rawPassword.trim().isEmpty();
        if (hasPassword) {
            sql.append(", mem_password = ?");
        }
        sql.append(" WHERE mem_id = ?");

        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql.toString());
            int idx = 1;
            pstmt.setString(idx++, email);
            pstmt.setString(idx++, nickname);
            if (hasPassword) {
                pstmt.setString(idx++, PasswordHasher.hashPassword(rawPassword));
            }
            pstmt.setInt(idx, memberId);
            return pstmt.executeUpdate();
        } finally {
            close(null, pstmt);
        }
    }

    public int deleteMember(int memberId, Connection conn) throws SQLException {
        String sql = "DELETE FROM member WHERE mem_id = ?";
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            return pstmt.executeUpdate();
        } finally {
            close(null, pstmt);
        }
    }

    // 닉네임으로 검색
    public MemberDTO selectMemberByEmail(String email, Connection conn) throws SQLException {
        String sql = "SELECT * FROM member WHERE mem_email=?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapToMember(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(null, pstmt, rs);
        }
        return null;
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

    private MemberDTO mapToMember(ResultSet rs) throws SQLException {
        MemberDTO member = new MemberDTO();
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
        try {
            member.setProfitRate(rs.getDouble("profit_rate"));
        } catch (SQLException ignored) {
            member.setProfitRate(null);
        }
        try {
            member.setStatusLabel(rs.getString("status_label"));
        } catch (SQLException ignored) {
            member.setStatusLabel(null);
        }
        return member;
    }

    public List<MemberDTO> findMembers(String keyword, String statusFilter, String profitFilter, String joinFilter, Connection conn) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT mem_id, mem_email, mem_nickname, mem_join_at, mem_last_login_at, mem_last_login_ip, mem_password, " +
                        "agree_privacy_at, agree_third_at, agree_notification_at, virtual_cash, " +
                        "((virtual_cash - 10000000) / 10000000.0) * 100 AS profit_rate, " +
                        "CASE " +
                        " WHEN mem_last_login_at IS NULL THEN '신규' " +
                        " WHEN mem_last_login_at >= DATE_SUB(NOW(), INTERVAL 6 MONTH) THEN '활동' " +
                        " ELSE '휴면' " +
                        "END AS status_label " +
                        "FROM member WHERE 1=1");

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword + "%";
            sql.append(" AND (CAST(mem_id AS CHAR) LIKE ? OR mem_email LIKE ? OR mem_nickname LIKE ?)");
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        if ("active".equals(statusFilter)) {
            sql.append(" AND mem_last_login_at IS NOT NULL AND mem_last_login_at >= DATE_SUB(NOW(), INTERVAL 6 MONTH)");
        } else if ("dormant".equals(statusFilter)) {
            sql.append(" AND (mem_last_login_at IS NULL OR mem_last_login_at < DATE_SUB(NOW(), INTERVAL 6 MONTH))");
        }

        if ("10".equals(profitFilter)) {
            sql.append(" AND ((virtual_cash - 10000000) / 10000000.0) * 100 >= 10");
        } else if ("30".equals(profitFilter)) {
            sql.append(" AND ((virtual_cash - 10000000) / 10000000.0) * 100 >= 30");
        } else if ("50".equals(profitFilter)) {
            sql.append(" AND ((virtual_cash - 10000000) / 10000000.0) * 100 >= 50");
        }

        if ("1m".equals(joinFilter)) {
            sql.append(" AND mem_join_at >= DATE_SUB(NOW(), INTERVAL 1 MONTH)");
        } else if ("3m".equals(joinFilter)) {
            sql.append(" AND mem_join_at >= DATE_SUB(NOW(), INTERVAL 3 MONTH)");
        } else if ("6m".equals(joinFilter)) {
            sql.append(" AND mem_join_at >= DATE_SUB(NOW(), INTERVAL 6 MONTH)");
        } else if ("1y".equals(joinFilter)) {
            sql.append(" AND mem_join_at >= DATE_SUB(NOW(), INTERVAL 1 YEAR)");
        }

        sql.append(" ORDER BY mem_id DESC");

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<MemberDTO> members = new ArrayList<>();
        try {
            pstmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            rs = pstmt.executeQuery();
            while (rs.next()) {
                members.add(mapToMember(rs));
            }
        } finally {
            close(null, pstmt, rs);
        }
        return members;
    }

    public MemberStatsDTO fetchMemberStats(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) AS total_count, " +
                "SUM(CASE WHEN DATE(mem_last_login_at) = CURDATE() THEN 1 ELSE 0 END) AS today_login_count, " +
                "SUM(CASE WHEN mem_last_login_at IS NOT NULL AND mem_last_login_at >= DATE_SUB(NOW(), INTERVAL 6 MONTH) THEN 1 ELSE 0 END) AS active_count, " +
                "SUM(CASE WHEN mem_last_login_at IS NULL OR mem_last_login_at < DATE_SUB(NOW(), INTERVAL 6 MONTH) THEN 1 ELSE 0 END) AS dormant_count " +
                "FROM member";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                MemberStatsDTO stats = new MemberStatsDTO();
                stats.setTotalCount(rs.getInt("total_count"));
                stats.setTodayLoginCount(rs.getInt("today_login_count"));
                stats.setActiveCount(rs.getInt("active_count"));
                stats.setDormantCount(rs.getInt("dormant_count"));
                return stats;
            }
        } finally {
            close(null, pstmt, rs);
        }
        return null;
    }
}