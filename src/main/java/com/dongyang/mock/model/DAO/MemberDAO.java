package com.dongyang.mock.model.DAO;


import java.sql.*;

import com.dongyang.mock.model.DTO.*;
import com.dongyang.mock.service.api.RealtimeStockService;
import com.dongyang.mock.service.api.WebSocketService;
import com.dongyang.mock.util.PasswordHasher;
import com.dongyang.mock.util.api.InmemoryCacheManager;
import com.dongyang.mock.util.db.DBManager;

import java.math.BigDecimal;

import static com.dongyang.mock.service.api.WebSocketService.REALTIME_CACHE_KEY;
import static com.dongyang.mock.util.db.DBManager.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 회원 데이터 접근 객체 (DAO)
 * - 회원 정보 CRUD 작업
 * - 회원 검색 및 필터링
 * - 회원 통계 조회
 * - 중복 체크
 */
public class MemberDAO {
    InmemoryCacheManager inmemoryCacheManager = InmemoryCacheManager.getInstance();

    /**
     * 신규 회원 추가
     * - 비밀번호는 해시화하여 저장
     * - 기본 예치금 포함
     * @return 영향받은 행 수 (1: 성공, 0: 실패)
     */
    public int insertMember(MemberDTO member, Connection conn) throws SQLException {
        String sql = "INSERT INTO member(mem_email, mem_password, mem_nickname, mem_last_login_IP," +
                "agree_privacy_at, agree_third_at, agree_notification_at, virtual_cash)" +
                "values (?, ?, ?, ?, ?, ?, ?, ?)";
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
            pstmt.setInt(8, member.getVirtualCash());

            result = pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            close(null, pstmt);
        }
        return result;
    }


    /**
     * 회원 ID로 회원 정보 조회
     * @return 회원 정보, 없으면 null
     */
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

    /**
     * 이메일로 회원 정보 조회
     * @return 회원 정보, 없으면 null
     */
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

    /**
     * 회원 계정 정보 수정
     * - 비밀번호는 null이 아니고 비어있지 않을 때만 업데이트
     * - 보유자금은 null이 아닐 때만 업데이트
     * - 비밀번호는 해시화하여 저장
     * @return 영향받은 행 수
     */
    // MemberDAO.java 내부에 위치한다고 가정

    /**
     * 회원 정보를 업데이트합니다. DTO의 필드가 null이 아닌 경우에만 해당 필드를 쿼리에 반영합니다.
     *
     * @param member 업데이트할 정보를 담은 DTO (memId는 WHERE 조건에 사용, 나머지는 SET 절에 사용)
     * @param conn DB 연결 객체
     * @return 업데이트된 행의 수
     * @throws SQLException DB 오류 발생 시
     */
    public int updateMemberAccount(MemberDTO member, Connection conn) throws SQLException {
        if (member == null || member.getMemId() <= 0) {
            throw new IllegalArgumentException("Member ID is required for update.");
        }

        final Integer memberId = member.getMemId();
        final String nickname = member.getMemNickname();
        final String rawPassword = member.getMemPassword();
        final Integer virtualCash = member.getVirtualCash();
        final Timestamp agreeNotificationAt = member.getAgreeNotificationAt();

        StringBuilder sql = new StringBuilder("UPDATE member SET ");
        List<Object> parameters = new ArrayList<>();

        boolean needsComma = false;

        // Nickname (null/empty 체크)
        if (nickname != null && !nickname.trim().isEmpty()) {
            if (needsComma) sql.append(", ");
            sql.append("mem_nickname = ?");
            parameters.add(nickname);
            needsComma = true;
        }

        // Password (rawPassword가 null/empty가 아니면 해싱하여 반영)
        if (rawPassword != null && !rawPassword.trim().isEmpty()) {
            if (needsComma) sql.append(", ");
            sql.append("mem_password = ?");
            parameters.add(PasswordHasher.hashPassword(rawPassword));
            needsComma = true;
        }

        // Virtual Cash (null 체크)
        if (virtualCash != null) {
            if (needsComma) sql.append(", ");
            sql.append("virtual_cash = ?");
            parameters.add(virtualCash);
            needsComma = true;
        }

        if (!needsComma) {
            return 0;
        }

        sql.append(", agree_notification_at = ?");
        parameters.add(agreeNotificationAt);
        sql.append(" WHERE mem_id = ?");
        parameters.add(memberId);
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql.toString());
            int idx = 1;
            for (Object param : parameters) {
                if (param instanceof String) {
                    pstmt.setString(idx++, (String) param);
                } else if (param instanceof Integer) {
                    pstmt.setInt(idx++, (Integer) param);
                } else {
                    pstmt.setObject(idx++, param);
                }
            }

            return pstmt.executeUpdate();
        } finally {
            close(null, pstmt);
        }
    }

    /**
     * 회원 삭제
     * @return 영향받은 행 수 (1: 성공, 0: 실패)
     */
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

    /**
     * 닉네임 중복 체크
     * @return 중복이면 true, 없으면 false
     */
    public boolean isNicknameDuplicate(String nickname, Connection conn) throws SQLException {
        String sql = "SELECT * FROM member WHERE mem_nickname=?";
        return hasRecord(sql, conn, nickname);
    }

    /**
     * 특정 ID를 가진 회원을 제외하고 닉네임의 중복 여부를 확인합니다.
     * @param nickname 확인할 닉네임
     * @param memberId 제외할 회원 ID (본인 ID)
     * @param conn DB 연결 객체
     * @return 다른 회원이 해당 닉네임을 사용 중이면 true, 아니면 false
     * @throws SQLException DB 오류 발생 시
     */
    public boolean isNicknameDuplicateByOtherId(String nickname, int memberId, Connection conn) throws SQLException {
        String sql = "SELECT mem_id FROM member WHERE mem_nickname = ? AND mem_id != ?";

        return hasRecord(sql, conn, nickname, memberId);
    }

    /**
     * 이메일 중복 체크
     * @return 중복이면 true, 없으면 false
     */
    public boolean isEmailDuplicate(String email, Connection conn) throws SQLException {
        String sql = "SELECT * FROM member WHERE mem_email=?";
        return hasRecord(sql, conn, email);
    }

    /**
     * 중복 체크 공통 메서드
     * @param sql 실행할 SQL 쿼리
     * @param conn DB 연결 객체
     * @param params SQL 쿼리에 바인딩할 파라미터들 (가변 인자)
     * @return 중복이면 true (결과 행이 존재), 없으면 false
     */
    public boolean hasRecord(String sql, Connection conn, Object... params) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = conn.prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {

                if (params[i] instanceof String) {
                    pstmt.setString(i + 1, (String) params[i]);
                } else if (params[i] instanceof Integer) {
                    pstmt.setInt(i + 1, (Integer) params[i]);
                } else {
                    pstmt.setObject(i + 1, params[i]);
                }
            }

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

    /**
     * 최근 로그인 정보 갱신
     * - 로그인 시간을 현재 시간으로 업데이트
     * - 로그인 IP 주소 업데이트
     * @return 영향받은 행 수
     */
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

    /**
     * ResultSet을 MemberDTO로 변환
     * - profit_rate와 status_label은 선택적 필드 (없을 수 있음)
     */
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

    /**
     * 필터 조건에 맞는 회원 목록 조회
     * - 키워드 검색: 회원 ID, 이메일, 닉네임
     * - 상태 필터: 활동/휴면
     * - 수익률 필터: 10%, 30%, 50% 이상
     * - 가입일 필터: 1개월, 3개월, 6개월, 1년
     * - 수익률과 상태 레이블을 계산하여 포함
     * @return 필터링된 회원 목록
     */
    public List<MemberDTO> findMembers(String keyword, String statusFilter, String profitFilter, String joinFilter, Connection conn) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT mem_id, mem_email, mem_nickname, mem_join_at, mem_last_login_at, mem_last_login_ip, mem_password, " +
                        "agree_privacy_at, agree_third_at, agree_notification_at, virtual_cash, " +
                        "((virtual_cash - 1000000) / 1000000.0) * 100 AS profit_rate, " +
                        "CASE " +
                        " WHEN mem_last_login_at IS NULL THEN '신규' " +
                        " WHEN mem_last_login_at >= DATE_SUB(NOW(), INTERVAL 6 MONTH) THEN '활동' " +
                        " ELSE '휴면' " +
                        "END AS status_label " +
                        "FROM member WHERE 1=1");

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
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
            sql.append(" AND ((virtual_cash - 1000000) / 1000000.0) * 100 >= 10");
        } else if ("30".equals(profitFilter)) {
            sql.append(" AND ((virtual_cash - 1000000) / 1000000.0) * 100 >= 30");
        } else if ("50".equals(profitFilter)) {
            sql.append(" AND ((virtual_cash - 1000000) / 1000000.0) * 100 >= 50");
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

    /**
     * 회원의 예치금 조회
     * @param memberId 회원 ID
     * @param conn DB 연결
     * @return 예치금
     * @throws SQLException
     */
    public int getVirtualCash(int memberId, Connection conn) throws SQLException {
        String sql = "SELECT virtual_cash FROM member WHERE mem_id = ?";
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("virtual_cash");
            } else {
                throw new SQLException("회원을 찾을 수 없습니다.");
            }
        } finally {
            close(null, pstmt, rs);
        }
    }

    /**
     * 회원 통계 정보 조회
     * - 전체 회원 수
     * - 당일 접속자 수
     * - 활동 사용자 수 (최근 6개월 내 로그인)
     * - 휴면 사용자 수 (6개월 이상 미접속)
     * @return 회원 통계 정보
     */
    public MemberStatsDTO fetchMemberStats(Connection conn) throws SQLException {
        String sql = "SELECT " +
                "COUNT(*) AS total_count, " +
                "SUM(CASE WHEN DATE(mem_last_login_at) = CURDATE() THEN 1 ELSE 0 END) AS today_login_count, " +
                "SUM(CASE WHEN mem_last_login_at IS NOT NULL AND mem_last_login_at >= DATE_SUB(NOW(), INTERVAL 6 MONTH) THEN 1 ELSE 0 END) AS active_count, " +
                "SUM(CASE WHEN mem_last_login_at IS NULL OR mem_last_login_at < DATE_SUB(NOW(), INTERVAL 6 MONTH) THEN 1 ELSE 0 END) AS dormant_count, " +
                "0 AS deleted_count, " + // 탈퇴 사용자 수는 현재 논리적 삭제가 없으므로 0
                "COUNT(*) - SUM(CASE WHEN DATE(mem_join_at) = CURDATE() THEN 1 ELSE 0 END) AS yesterday_total_count, " + // 전일 전체 회원 수 = 오늘 전체 - 오늘 가입
                "SUM(CASE WHEN DATE(mem_last_login_at) = DATE_SUB(CURDATE(), INTERVAL 1 DAY) THEN 1 ELSE 0 END) AS yesterday_login_count, " +
                "SUM(CASE WHEN mem_last_login_at IS NOT NULL AND mem_last_login_at >= DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 6 MONTH) AND mem_last_login_at < DATE_SUB(CURDATE(), INTERVAL 1 DAY) THEN 1 ELSE 0 END) AS yesterday_active_count, " +
                "SUM(CASE WHEN mem_last_login_at IS NULL OR mem_last_login_at < DATE_SUB(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 6 MONTH) THEN 1 ELSE 0 END) AS yesterday_dormant_count, " +
                "0 AS yesterday_deleted_count " + // 전일 탈퇴 사용자 수
                "FROM member";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            MemberStatsDTO stats = new MemberStatsDTO();
            if (rs.next()) {
                stats.setTotalCount(rs.getInt("total_count"));
                stats.setTodayLoginCount(rs.getInt("today_login_count"));
                stats.setActiveCount(rs.getInt("active_count"));
                stats.setDormantCount(rs.getInt("dormant_count"));
                stats.setDeletedCount(rs.getInt("deleted_count"));
                stats.setYesterdayTotalCount(rs.getInt("yesterday_total_count"));
                stats.setYesterdayLoginCount(rs.getInt("yesterday_login_count"));
                stats.setYesterdayActiveCount(rs.getInt("yesterday_active_count"));
                stats.setYesterdayDormantCount(rs.getInt("yesterday_dormant_count"));
                stats.setYesterdayDeletedCount(rs.getInt("yesterday_deleted_count"));
            }
            return stats;
        } finally {
            close(null, pstmt, rs);
        }
    }

    /**
     * 회원 자산 정보 조회
     * - 예치금
     * - 보유주식 평가액
     * - 총 자산
     * - 수익률
     * - 평가 손익
     */
    public MemberAssetDTO fetchMemberAsset(int memberId, Connection conn) throws SQLException {
        // 보유주식 평가액 계산
        String stockValueSql = "SELECT COALESCE(SUM(h.quantity * spd.close_price), 0) as stock_value " +
                "FROM holding h " +
                "INNER JOIN stock_price_daily spd ON h.stock_id = spd.stock_id " +
                "AND spd.price_date = (SELECT MAX(price_date) FROM stock_price_daily WHERE stock_id = h.stock_id) " +
                "WHERE h.mem_id = ? AND h.quantity > 0";

        long stockValue = 0L;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(stockValueSql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                BigDecimal value = rs.getBigDecimal("stock_value");
                stockValue = value != null ? value.longValue() : 0L;
            }
        } finally {
            close(null, pstmt, rs);
        }

        // 회원 정보 조회 (예치금, 초기 자본)
        String memberSql = "SELECT virtual_cash, 1000000 as initial_capital FROM member WHERE mem_id = ?";
        long virtualCash = 0L;
        long initialCapital = 1000000L;
        try {
            pstmt = conn.prepareStatement(memberSql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                virtualCash = rs.getLong("virtual_cash");
                initialCapital = rs.getLong("initial_capital");
            }
        } finally {
            close(null, pstmt, rs);
        }

        // 총 자산 = 예치금 + 보유주식 평가액
        long totalAsset = virtualCash + stockValue;
        
        // 평가 손익 = 총 자산 - 초기 자본
        long profitLoss = totalAsset - initialCapital;
        
        // 수익률 = (평가 손익 / 초기 자본) * 100
        double profitRate = initialCapital > 0 ? ((double) profitLoss / initialCapital) * 100.0 : 0.0;

        MemberAssetDTO asset = new MemberAssetDTO();
        asset.setVirtualCash(virtualCash);
        asset.setStockValue(stockValue);
        asset.setTotalAsset(totalAsset);
        asset.setProfitLoss(profitLoss);
        asset.setProfitRate(profitRate);
        return asset;
    }

    public MemberHoldingDTO findHoldingByStock(int memberId, long stockId, Connection conn) throws SQLException {
        String sql = "SELECT " +
                "s.stock_name, " +
                "h.quantity, " +
                "h.avg_buy_price as average_price " +
                "FROM holding h " +
                "INNER JOIN stock s ON h.stock_id = s.stock_id " +
                "WHERE h.mem_id = ? AND h.stock_id = ? AND h.quantity > 0";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            pstmt.setLong(2, stockId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                MemberHoldingDTO holding = new MemberHoldingDTO();

                // 1. DB에서 기본 정보 로드
                String stockName = rs.getString("stock_name");
                int quantity = rs.getInt("quantity");

                BigDecimal avgPriceBd = rs.getBigDecimal("average_price");
                int averagePrice = avgPriceBd != null ? avgPriceBd.intValue() : 0;

                holding.setStockName(stockName);
                holding.setQuantity(quantity);
                holding.setAveragePrice(averagePrice);

                // 2. 캐시에서 실시간 현재가(curPrice) 로드
                StockDAO stockDAO = new StockDAO();
                String code = stockDAO.getStockCodeById(stockId, conn);
                RealtimeDTO cached = inmemoryCacheManager.get(REALTIME_CACHE_KEY + code, RealtimeDTO.class);

                int currentPrice;
                if (cached != null) {
                    currentPrice = (int)Double.parseDouble(cached.getCurPrice());
                }
                else {
                    currentPrice = 0;
                }
                holding.setCurrentPrice(currentPrice);

                // 3. Java 코드에서 계산 수행
                long evaluationAmount = (long) quantity * currentPrice;
                long profitLoss = ((long) currentPrice - averagePrice) * quantity;
                double profitRate = 0.0;

                if (averagePrice > 0) {
                    profitRate = ((double) (currentPrice - averagePrice) / averagePrice) * 100.0;
                }

                holding.setEvaluationAmount(evaluationAmount);
                holding.setProfitLoss(profitLoss);
                holding.setProfitRate(profitRate);

                return holding;
            }
            return null;
        } finally {
            close(null, pstmt, rs);
        }
    }


    public List<MemberHoldingDTO> fetchMemberHoldings(int memberId, Connection conn) throws SQLException {
        String sql = "SELECT " +
                "s.stock_name, " +
                "h.stock_id, " +  // 💡 캐시 조회를 위해 추가
                "h.quantity, " +
                "h.avg_buy_price as average_price " +
                "FROM holding h " +
                "INNER JOIN stock s ON h.stock_id = s.stock_id " +
                "WHERE h.mem_id = ? AND h.quantity > 0 " +
                "ORDER BY s.stock_name";

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<MemberHoldingDTO> holdings = new ArrayList<>();

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, memberId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                MemberHoldingDTO holding = new MemberHoldingDTO();

                // 1. DB에서 기본 정보 로드
                long stockId = rs.getLong("stock_id"); // 💡 종목 ID 로드
                String stockName = rs.getString("stock_name");
                int quantity = rs.getInt("quantity");

                BigDecimal avgPriceBd = rs.getBigDecimal("average_price");
                int averagePrice = avgPriceBd != null ? avgPriceBd.intValue() : 0;

                holding.setStockName(stockName);
                holding.setQuantity(quantity);
                holding.setAveragePrice(averagePrice);

                StockDAO stockDAO = new StockDAO();
                String code = stockDAO.getStockCodeById(stockId, conn);
                RealtimeDTO cached = inmemoryCacheManager.get(REALTIME_CACHE_KEY + code, RealtimeDTO.class);

                int currentPrice;
                if (cached != null) {
                    currentPrice = (int)Double.parseDouble(cached.getCurPrice());
                }
                else {
                    currentPrice = 0;
                }
                holding.setCurrentPrice(currentPrice);

                long evaluationAmount = (long) quantity * currentPrice;
                long profitLoss = ((long) currentPrice - averagePrice) * quantity;
                double profitRate = 0.0;

                if (averagePrice > 0) {
                    profitRate = ((double) (currentPrice - averagePrice) / averagePrice) * 100.0;
                }

                holding.setEvaluationAmount(evaluationAmount);
                holding.setProfitLoss(profitLoss);
                holding.setProfitRate(profitRate);

                holdings.add(holding);
            }
        } finally {
            close(null, pstmt, rs);
        }
        return holdings;
    }

    /**
     * 회원의 가상 자금 업데이트
     * @param memberId 회원 ID
     * @param amount 변경 금액 (양수: 증가, 음수: 감소)
     * @param conn DB 연결
     * @return 업데이트 후 잔액
     * @throws SQLException
     */
    public int updateVirtualCash(int memberId, int amount, Connection conn) throws SQLException {
        // 현재 잔액 조회
        int currentCash = getVirtualCash(memberId, conn);
        int newCash = currentCash + amount;
        
        if (newCash < 0) {
            throw new SQLException("잔액이 부족합니다.");
        }
        
        String sql = "UPDATE member SET virtual_cash = ? WHERE mem_id = ?";
        
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, newCash);
            pstmt.setInt(2, memberId);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("가상 자금 업데이트에 실패했습니다.");
            }
            
            return newCash;
        } finally {
            close(null, pstmt);
        }
    }

    /**
     * 파산 신청 처리
     * - member 정보와 관심 테이블은 유지
     * - trade_order, order_condition, trade, holding, cash_history 데이터 삭제
     * - virtual_cash를 초기 자금(100만원)으로 리셋
     * @param memberId 회원 ID
     * @param conn DB 연결
     * @return 성공 시 true, 실패 시 false
     * @throws SQLException
     */
    public boolean declareBankruptcy(int memberId, Connection conn) throws SQLException {
        try {
            // 1. order_condition 삭제 (trade_order와 연결된 데이터)
            String deleteOrderConditionSql = "DELETE oc FROM order_condition oc " +
                    "INNER JOIN trade_order o ON oc.order_id = o.order_id " +
                    "WHERE o.mem_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteOrderConditionSql);
            pstmt.setInt(1, memberId);
            pstmt.executeUpdate();
            pstmt.close();

            // 2. trade 삭제 (trade_order와 연결된 데이터)
            String deleteTradeSql = "DELETE t FROM trade t " +
                    "INNER JOIN trade_order o ON t.order_id = o.order_id " +
                    "WHERE o.mem_id = ?";
            pstmt = conn.prepareStatement(deleteTradeSql);
            pstmt.setInt(1, memberId);
            pstmt.executeUpdate();
            pstmt.close();

            // 3. trade_order 삭제
            String deleteTradeOrderSql = "DELETE FROM trade_order WHERE mem_id = ?";
            pstmt = conn.prepareStatement(deleteTradeOrderSql);
            pstmt.setInt(1, memberId);
            pstmt.executeUpdate();
            pstmt.close();

            // 4. holding 삭제
            String deleteHoldingSql = "DELETE FROM holding WHERE mem_id = ?";
            pstmt = conn.prepareStatement(deleteHoldingSql);
            pstmt.setInt(1, memberId);
            pstmt.executeUpdate();
            pstmt.close();

            // 5. cash_history 삭제
            String deleteCashHistorySql = "DELETE FROM cash_history WHERE mem_id = ?";
            pstmt = conn.prepareStatement(deleteCashHistorySql);
            pstmt.setInt(1, memberId);
            pstmt.executeUpdate();
            pstmt.close();

            // 6. virtual_cash를 초기 자금(100만원)으로 리셋
            String resetCashSql = "UPDATE member SET virtual_cash = 1000000 WHERE mem_id = ?";
            pstmt = conn.prepareStatement(resetCashSql);
            pstmt.setInt(1, memberId);
            int updated = pstmt.executeUpdate();
            pstmt.close();

            return updated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 회원 탈퇴 처리 (모든 관련 데이터 삭제)
     * - trade_order, order_condition, trade, holding, cash_history 삭제
     * - member 삭제
     * @param memberId 회원 ID
     * @param conn DB 연결
     * @return 성공 시 true, 실패 시 false
     * @throws SQLException
     */
    public boolean deleteMemberWithAllData(int memberId, Connection conn) throws SQLException {
        try {
            // 1. order_condition 삭제
            String deleteOrderConditionSql = "DELETE oc FROM order_condition oc " +
                    "INNER JOIN trade_order o ON oc.order_id = o.order_id " +
                    "WHERE o.mem_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(deleteOrderConditionSql);
            pstmt.setInt(1, memberId);
            pstmt.executeUpdate();
            pstmt.close();

            // 2. trade 삭제
            String deleteTradeSql = "DELETE t FROM trade t " +
                    "INNER JOIN trade_order o ON t.order_id = o.order_id " +
                    "WHERE o.mem_id = ?";
            pstmt = conn.prepareStatement(deleteTradeSql);
            pstmt.setInt(1, memberId);
            pstmt.executeUpdate();
            pstmt.close();

            // 3. trade_order 삭제
            String deleteTradeOrderSql = "DELETE FROM trade_order WHERE mem_id = ?";
            pstmt = conn.prepareStatement(deleteTradeOrderSql);
            pstmt.setInt(1, memberId);
            pstmt.executeUpdate();
            pstmt.close();

            // 4. holding 삭제
            String deleteHoldingSql = "DELETE FROM holding WHERE mem_id = ?";
            pstmt = conn.prepareStatement(deleteHoldingSql);
            pstmt.setInt(1, memberId);
            pstmt.executeUpdate();
            pstmt.close();

            // 5. cash_history 삭제
            String deleteCashHistorySql = "DELETE FROM cash_history WHERE mem_id = ?";
            pstmt = conn.prepareStatement(deleteCashHistorySql);
            pstmt.setInt(1, memberId);
            pstmt.executeUpdate();
            pstmt.close();

            // 6. member 삭제
            String deleteMemberSql = "DELETE FROM member WHERE mem_id = ?";
            pstmt = conn.prepareStatement(deleteMemberSql);
            pstmt.setInt(1, memberId);
            int deleted = pstmt.executeUpdate();
            pstmt.close();

            return deleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }
}