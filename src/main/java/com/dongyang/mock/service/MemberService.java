package com.dongyang.mock.service;

import com.dongyang.mock.model.DAO.MemberDAO;
import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.model.DTO.MemberStatsDTO;
import com.dongyang.mock.model.status.MemberStatus;
import com.dongyang.mock.util.*;
import com.dongyang.mock.util.db.DBManager;

import java.sql.Connection;
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
        return DBManager.executeInTransaction(conn -> {
            if (memberDAO.isEmailDuplicate(member.getMemEmail(), conn)) {
                return DBManager.TransactionResult.rollback(MemberStatus.JoinResult.DUPLICATE_EMAIL);
            }

            if (memberDAO.isNicknameDuplicate(member.getMemNickname(), conn)) {
                return DBManager.TransactionResult.rollback(MemberStatus.JoinResult.DUPLICATE_NICKNAME);
            }

            if (member.getAgreeThirdAt() == null || member.getAgreePrivacyAt() == null) {
                return DBManager.TransactionResult.rollback(MemberStatus.JoinResult.REQUIRED_AGREEMENT_MISSING);
            }

            int result = memberDAO.insertMember(member, conn);

            if (result > 0) {
                return DBManager.TransactionResult.commit(MemberStatus.JoinResult.SUCCESS);
            }

            return DBManager.TransactionResult.rollback(MemberStatus.JoinResult.FAIL_INSERT);
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
        return DBManager.withConnection(conn -> {
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
        return DBManager.withConnection(conn -> memberDAO.selectMemberById(memberId, conn));
    }

    /**
     * 회원 계정 정보 수정
     * - 이메일 중복 체크 (다른 회원이 사용 중인지 확인)
     * - 닉네임 중복 체크 (다른 회원이 사용 중인지 확인)
     * - 비밀번호는 null이면 변경하지 않음
     * - 보유자금은 null이면 변경하지 않음
     * @return 수정 결과 (SUCCESS, DUPLICATE_EMAIL, DUPLICATE_NICKNAME, FAIL)
     */
    public MemberStatus.UpdateResult updateMemberAccount(MemberDTO member) throws SQLException {
        final int memberId = member.getMemId();
        final String nickname = member.getMemNickname();

        return DBManager.executeInTransaction(conn -> {
            if (nickname != null && !nickname.trim().isEmpty()) {
                if (memberDAO.isNicknameDuplicateByOtherId(nickname, memberId, conn)) {
                    return DBManager.TransactionResult.rollback(MemberStatus.UpdateResult.DUPLICATE_NICKNAME);
                }
            }
            int updated = memberDAO.updateMemberAccount(member, conn);

            if (updated > 0) {
                return DBManager.TransactionResult.commit(MemberStatus.UpdateResult.SUCCESS);
            }
            return DBManager.TransactionResult.rollback(MemberStatus.UpdateResult.FAIL);
        });
    }

    /**
     * 회원 삭제 처리
     * - 트랜잭션 내에서 회원 삭제
     * @return 삭제 성공 시 true, 실패 시 false
     */
    public boolean deleteMember(int memberId) throws SQLException {
        return DBManager.executeInTransaction(conn -> {
            int deleted = memberDAO.deleteMember(memberId, conn);
            if (deleted > 0) {
                return DBManager.TransactionResult.commit(Boolean.TRUE);
            }
            return DBManager.TransactionResult.rollback(Boolean.FALSE);
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
        return DBManager.withConnection(conn -> memberDAO.findMembers(keyword, statusFilter, profitFilter, joinFilter, conn));
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
        return DBManager.withConnection(memberDAO::fetchMemberStats);
    }
}
