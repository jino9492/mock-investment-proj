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

/**
 * 회원 관련 비즈니스 로직 서비스
 * - 회원 가입, 로그인, 조회, 수정, 삭제
 * - 트랜잭션 관리 및 연결 리소스 관리
 * - 중복 체크 및 유효성 검증
 */
public class MemberService {
    private final MemberDAO memberDAO = new MemberDAO();

    /**
     * 회원 가입 처리
     * - 이메일 중복 체크
     * - 닉네임 중복 체크
     * - 트랜잭션 내에서 회원 정보 저장
     * @return 가입 결과 (SUCCESS, DUPLICATE_EMAIL, DUPLICATE_NICKNAME, FAIL_INSERT)
     */
    public MemberStatus.JoinResult join(MemberDTO member) throws SQLException {
        return executeInTransaction(conn -> {
            if (memberDAO.isEmailDuplicate(member.getMemEmail(), conn)) {
                return TransactionResult.rollback(MemberStatus.JoinResult.DUPLICATE_EMAIL);
            }
            if (memberDAO.isNicknameDuplicate(member.getMemNickname(), conn)) {
                return TransactionResult.rollback(MemberStatus.JoinResult.DUPLICATE_NICKNAME);
            }

            int result = memberDAO.insertMember(member, conn);
            if (result > 0) {
                return TransactionResult.commit(MemberStatus.JoinResult.SUCCESS);
            }
            return TransactionResult.rollback(MemberStatus.JoinResult.FAIL_INSERT);
        });
    }

    /**
     * 회원 로그인 처리
     * - 이메일로 회원 조회
     * - 비밀번호 검증
     * - 로그인 성공 시 최종 로그인 시간 갱신
     * @return 로그인 성공 시 MemberDTO, 실패 시 null
     */
    public MemberDTO login(String email, String plainPassword) throws SQLException {
        return withConnection(conn -> {
            MemberDTO member = memberDAO.selectMemberByEmail(email, conn);
            if (member == null) {
                return null;
            }

            if (PasswordHasher.checkPassword(plainPassword, member.getMemPassword())) {
                memberDAO.renewLastLogin(email, member.getMemLastLoginIP(), conn);
                member.setMemPassword(null);
                return member;
            }
            return null;
        });
    }

    /**
     * 회원 ID로 회원 정보 조회
     * @return 회원 정보, 없으면 null
     */
    public MemberDTO getMemberById(int memberId) throws SQLException {
        return withConnection(conn -> memberDAO.selectMemberById(memberId, conn));
    }

    /**
     * 회원 계정 정보 수정
     * - 이메일 중복 체크 (다른 회원이 사용 중인지 확인)
     * - 닉네임 중복 체크 (다른 회원이 사용 중인지 확인)
     * - 비밀번호는 null이면 변경하지 않음
     * - 보유자금은 null이면 변경하지 않음
     * @return 수정 결과 (SUCCESS, DUPLICATE_EMAIL, DUPLICATE_NICKNAME, FAIL)
     */
    public MemberStatus.UpdateResult updateMemberAccount(int memberId, String email, String nickname, String rawPassword, Integer virtualCash) throws SQLException {
        return executeInTransaction(conn -> {
            if (memberDAO.isEmailDuplicate(email, conn)) {
                MemberDTO existing = memberDAO.selectMemberByEmail(email, conn);
                if (existing != null && existing.getMemId() != memberId) {
                    return TransactionResult.rollback(MemberStatus.UpdateResult.DUPLICATE_EMAIL);
                }
            }

            if (memberDAO.isNicknameDuplicate(nickname, conn)) {
                String sql = "SELECT mem_id FROM member WHERE mem_nickname = ?";
                PreparedStatement pstmt = null;
                ResultSet rs = null;
                try {
                    pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, nickname);
                    rs = pstmt.executeQuery();
                    if (rs.next() && rs.getInt("mem_id") != memberId) {
                        return TransactionResult.rollback(MemberStatus.UpdateResult.DUPLICATE_NICKNAME);
                    }
                } finally {
                    DBManager.close(null, pstmt, rs);
                }
            }

            int updated = memberDAO.updateMemberAccount(memberId, email, nickname, rawPassword, virtualCash, conn);
            if (updated > 0) {
                return TransactionResult.commit(MemberStatus.UpdateResult.SUCCESS);
            }
            return TransactionResult.rollback(MemberStatus.UpdateResult.FAIL);
        });
    }

    /**
     * 회원 삭제 처리
     * - 트랜잭션 내에서 회원 삭제
     * @return 삭제 성공 시 true, 실패 시 false
     */
    public boolean deleteMember(int memberId) throws SQLException {
        return executeInTransaction(conn -> {
            int deleted = memberDAO.deleteMember(memberId, conn);
            if (deleted > 0) {
                return TransactionResult.commit(Boolean.TRUE);
            }
            return TransactionResult.rollback(Boolean.FALSE);
        });
    }

    /**
     * 필터 조건에 맞는 회원 목록 조회
     * @param keyword 검색 키워드 (회원 ID, 이메일, 닉네임)
     * @param statusFilter 상태 필터 (active, dormant)
     * @param profitFilter 수익률 필터 (10, 30, 50)
     * @param joinFilter 가입일 필터 (1m, 3m, 6m, 1y)
     * @return 필터링된 회원 목록
     */
    public List<MemberDTO> getMembers(String keyword, String statusFilter, String profitFilter, String joinFilter) throws SQLException {
        return withConnection(conn -> memberDAO.findMembers(keyword, statusFilter, profitFilter, joinFilter, conn));
    }

    /**
     * 회원 통계 정보 조회
     * - 전체 회원 수
     * - 당일 접속자 수
     * - 활동 사용자 수
     * - 휴면 사용자 수
     * @return 회원 통계 정보
     */
    public MemberStatsDTO getMemberStats() throws SQLException {
        return withConnection(memberDAO::fetchMemberStats);
    }

    /**
     * 단순 DB 연결 헬퍼 메서드
     * - 연결 획득 → 작업 수행 → 연결 해제
     * - 트랜잭션 없이 단순 조회 작업에 사용
     */
    private <T> T withConnection(SqlFunction<T> function) throws SQLException {
        Connection conn = null;
        try {
            conn = DBManager.getConnection();
            return function.apply(conn);
        } finally {
            DBManager.close(conn, null);
        }
    }

    /**
     * 트랜잭션 관리 헬퍼 메서드
     * - 자동 커밋 비활성화
     * - 작업 수행 후 commit/rollback 결정
     * - 예외 발생 시 자동 rollback
     * - finally에서 자동 커밋 복구 및 연결 해제
     */
    private <T> T executeInTransaction(TransactionCallback<T> callback) throws SQLException {
        Connection conn = null;
        try {
            conn = DBManager.getConnection();
            conn.setAutoCommit(false);
            TransactionResult<T> result = callback.doInTransaction(conn);
            if (result.commit()) {
                conn.commit();
            } else {
                conn.rollback();
            }
            return result.value();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
            DBManager.close(conn, null);
        }
    }

    /**
     * 단순 SQL 작업을 위한 함수형 인터페이스
     */
    @FunctionalInterface
    private interface SqlFunction<T> {
        T apply(Connection conn) throws SQLException;
    }

    /**
     * 트랜잭션 내 작업을 위한 콜백 인터페이스
     */
    @FunctionalInterface
    private interface TransactionCallback<T> {
        TransactionResult<T> doInTransaction(Connection conn) throws SQLException;
    }

    /**
     * 트랜잭션 결과를 담는 내부 클래스
     * - commit 여부와 반환 값을 함께 관리
     */
    private static final class TransactionResult<T> {
        private final T value;
        private final boolean commit;

        private TransactionResult(T value, boolean commit) {
            this.value = value;
            this.commit = commit;
        }

        static <T> TransactionResult<T> commit(T value) {
            return new TransactionResult<>(value, true);
        }

        static <T> TransactionResult<T> rollback(T value) {
            return new TransactionResult<>(value, false);
        }

        T value() {
            return value;
        }

        boolean commit() {
            return commit;
        }
    }
}
