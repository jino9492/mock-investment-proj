package com.dongyang.mock.controller.admin;

import com.dongyang.mock.model.DTO.MemberDTO;
import com.dongyang.mock.model.DTO.MemberStatsDTO;
import com.dongyang.mock.model.DTO.MemberAssetDTO;
import com.dongyang.mock.model.DTO.MemberHoldingDTO;
import com.dongyang.mock.model.DTO.TradeDTO;
import com.dongyang.mock.model.status.MemberStatus;
import com.dongyang.mock.service.MemberService;
import com.dongyang.mock.service.TradeService;
import com.dongyang.mock.util.DateUtility;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

/**
 * 관리자 회원 관리 컨트롤러
 * - 회원 목록 조회 및 필터링
 * - 회원 상세 정보 조회
 * - 신규 회원 생성
 * - 회원 정보 수정 (닉네임, 보유자금)
 * - 회원 삭제
 */
@WebServlet(urlPatterns = {"/admin/users", "/admin/users/detail"})
public class AdminUsersController extends BaseAdminController {

    private final MemberService memberService = new MemberService();
    private final TradeService tradeService = new TradeService();

    /**
     * GET 요청 처리
     * - /admin/users: 회원 목록 페이지
     * - /admin/users/detail: 회원 상세 페이지
     */
    @Override
    protected void handleGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String servletPath = req.getServletPath();
        if ("/admin/users/detail".equals(servletPath)) {
            setCurrentPath(req, "/admin/users");
            handleUserDetail(req, resp);
        } else {
            setCurrentPath(req, "/admin/users");
            handleUsersPage(req, resp);
        }
    }

    /**
     * POST 요청 처리
     * - /admin/users: 신규 회원 생성
     * - /admin/users/detail: 회원 정보 수정/삭제
     */
    @Override
    protected void handlePost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String servletPath = req.getServletPath();
        if ("/admin/users/detail".equals(servletPath)) {
            handleDetailAction(req, resp);
        } else {
            handleCreateMember(req, resp);
        }
    }

    /**
     * 회원 목록 페이지 처리
     * - 필터 조건에 맞는 회원 목록 조회 (키워드, 상태, 수익률, 가입일)
     * - 회원 통계 정보 조회
     * - JSP에 데이터 전달하여 목록 페이지 렌더링
     */
    private void handleUsersPage(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String keyword = trim(req.getParameter("keyword"));
        String statusFilter = trim(req.getParameter("statusFilter"));
        String profitFilter = trim(req.getParameter("profitFilter"));
        String joinFilter = trim(req.getParameter("joinFilter"));

        try {
            List<MemberDTO> members = memberService.getMembers(keyword, statusFilter, profitFilter, joinFilter);
            MemberStatsDTO stats = orDefault(
                memberService.getMemberStats(),
                MemberStatsDTO::new
            );

            req.setAttribute("memberList", members);
            req.setAttribute("filteredCount", members != null ? members.size() : 0);
            req.setAttribute("memberStats", stats);
        } catch (SQLException e) {
            handleSQLException(e, "Failed to load member list");
        }

        req.setAttribute("keyword", keyword);
        req.setAttribute("statusFilter", statusFilter);
        req.setAttribute("profitFilter", profitFilter);
        req.setAttribute("joinFilter", joinFilter);

        req.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(req, resp);
    }

    /**
     * 신규 회원 생성 처리
     * - 이메일, 닉네임, 비밀번호 검증
     * - 기본 예치금 1,000,000원 설정
     * - 약관 동의 정보 저장
     * - 중복 체크 후 회원 등록
     */
    private void handleCreateMember(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = trim(req.getParameter("memberEmail"));
        String nickname = trim(req.getParameter("memberNickname"));
        String password = req.getParameter("memberPassword");

        if (isBlank(email) || isBlank(nickname) || isBlank(password)) {
            redirectWithStatus(req, resp, "invalid");
            return;
        }

        Timestamp now = new Timestamp(System.currentTimeMillis());
        MemberDTO newMember = new MemberDTO();
        newMember.setMemEmail(email);
        newMember.setMemPassword(password);
        newMember.setMemNickname(nickname);
        newMember.setMemLastLoginIP(req.getRemoteAddr());
        newMember.setMemLastLoginAt(null);
        newMember.setAgreePrivacyAt(DateUtility.getCheckTime(req.getParameter("agreePrivacy"), now));
        newMember.setAgreeThirdAt(DateUtility.getCheckTime(req.getParameter("agreeThird"), now));
        newMember.setAgreeNotificationAt(DateUtility.getCheckTime(req.getParameter("agreeNotification"), now));
        newMember.setVirtualCash(DEFAULT_VIRTUAL_CASH);

        MemberStatus.JoinResult status = MemberStatus.JoinResult.FAIL_INSERT;
        try {
            status = memberService.join(newMember);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        redirectWithStatus(req, resp, status.name().toLowerCase());
    }

    /**
     * 상태 메시지와 함께 리다이렉트
     * @param status 리다이렉트 URL에 포함할 상태 값
     */
    private void redirectWithStatus(HttpServletRequest req, HttpServletResponse resp, String status) throws IOException {
        redirectWithStatus(req, resp, "/admin/users", status);
    }

    /**
     * 회원 상세 정보 조회
     * - 회원 ID로 회원 정보 조회
     * - 회원이 없으면 에러 페이지로 리다이렉트
     * - 상세 정보 페이지로 포워딩
     */
    private void handleUserDetail(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer memberId = parseIntSafely(req.getParameter("memberId"));
        if (memberId == null) {
            redirectWithStatus(req, resp, "/admin/users", "not_found");
            return;
        }

        try {
            MemberDTO member = memberService.getMemberById(memberId);
            if (member == null) {
                redirectWithStatus(req, resp, "/admin/users", "not_found");
                return;
            }

            // 회원 자산 정보 조회
            MemberAssetDTO asset = memberService.getMemberAsset(memberId);
            
            // 보유 종목 목록 조회
            List<MemberHoldingDTO> holdings = memberService.getMemberHoldings(memberId);
            
            // 최근 거래 내역 조회 (최대 10건)
            List<TradeDTO> recentTrades = tradeService.getMemberRecentTrades(memberId, 10);

            req.setAttribute("member", member);
            req.setAttribute("asset", asset);
            req.setAttribute("holdings", holdings);
            req.setAttribute("recentTrades", recentTrades);
            req.getRequestDispatcher("/WEB-INF/views/admin/user_detail.jsp").forward(req, resp);
        } catch (SQLException e) {
            handleSQLException(e, "Failed to load member detail");
        }
    }

    /**
     * 회원 상세 페이지에서의 액션 라우팅
     * - update: 회원 정보 수정
     * - delete: 회원 삭제
     */
    private void handleDetailAction(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");
        Integer memberId = parseIntSafely(req.getParameter("memberId"));
        
        if (memberId == null) {
            redirectWithStatus(req, resp, "/admin/users", "invalid");
            return;
        }

        if ("update".equals(action)) {
            handleMemberUpdate(req, resp, memberId);
        } else if ("delete".equals(action)) {
            handleMemberDelete(req, resp, memberId);
        } else {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * 회원 정보 수정 처리
     * - 닉네임 수정 가능
     * - 보유자금 수정 가능
     * - 이메일은 수정 불가 (읽기 전용)
     * - 비밀번호는 수정 불가
     * - 닉네임 중복 체크
     */
    private void handleMemberUpdate(HttpServletRequest req, HttpServletResponse resp, int memberId) throws IOException {
        String nickname = trim(req.getParameter("memberNickname"));
        String virtualCashStr = trim(req.getParameter("virtualCash"));

        String basePath = "/admin/users/detail";
        String baseQuery = "memberId=" + memberId + "&status=";
        
        if (isBlank(nickname)) {
            redirectWithQuery(req, resp, basePath, baseQuery + encode("update_fail"));
            return;
        }

        Integer virtualCash = parseIntSafely(virtualCashStr);
        if (virtualCash != null && virtualCash < 0) {
            redirectWithQuery(req, resp, basePath, baseQuery + encode("update_fail"));
            return;
        }

        try {
            // 이메일은 수정하지 않고, 비밀번호도 수정하지 않음 (null 전달)
            MemberDTO existingMember = memberService.getMemberById(memberId);
            if (existingMember == null) {
                redirectWithQuery(req, resp, basePath, baseQuery + encode("update_fail"));
                return;
            }

            MemberDTO dto = new MemberDTO();
            dto.setMemId(memberId);
            dto.setMemEmail(existingMember.getMemEmail());
            dto.setMemNickname(nickname);
            dto.setVirtualCash(virtualCash);
            
            // 닉네임과 보유자금 수정 (이메일은 기존 값 유지, 비밀번호는 null로 전달하여 변경하지 않음)
            MemberStatus.UpdateResult result = memberService.updateMemberAccount(dto);
            String status;
            switch (result) {
                case SUCCESS -> status = "update_success";
                case DUPLICATE_NICKNAME -> status = "update_duplicate_nickname";
                default -> status = "update_fail";
            }
            redirectWithQuery(req, resp, basePath, baseQuery + encode(status));
        } catch (SQLException e) {
            e.printStackTrace();
            redirectWithQuery(req, resp, basePath, baseQuery + encode("update_fail"));
        }
    }

    /**
     * 회원 삭제 처리
     * - 회원 ID로 회원 삭제
     * - 삭제 성공 시 회원 목록으로 리다이렉트
     * - 삭제 실패 시 상세 페이지로 리다이렉트
     */
    private void handleMemberDelete(HttpServletRequest req, HttpServletResponse resp, int memberId) throws IOException {
        try {
            boolean deleted = memberService.deleteMember(memberId);
            if (deleted) {
                redirectWithStatus(req, resp, "/admin/users", "delete_success");
            } else {
                redirectWithQuery(req, resp, "/admin/users/detail", 
                    "memberId=" + memberId + "&status=" + encode("delete_fail"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            redirectWithQuery(req, resp, "/admin/users/detail", 
                "memberId=" + memberId + "&status=" + encode("delete_fail"));
        }
    }
}

