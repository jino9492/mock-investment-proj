<%--
  User detail view (static prototype)
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="member" value="${requestScope.member}" />
<fmt:formatDate value="${member.memJoinAt}" pattern="yyyy-MM-dd" var="joinDate" />
<fmt:formatDate value="${member.memLastLoginAt}" pattern="yyyy-MM-dd HH:mm" var="lastLoginDate" />
<fmt:formatNumber value="${member.virtualCash}" type="number" groupingUsed="true" var="virtualCashFormatted" />
<c:set var="statusLabel" value="${empty member.memLastLoginAt ? '신규' : '활성'}" />
<html>
<head>
    <title>회원 상세 정보</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin/users.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin/user_detail.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/fragments/common/header_admin.jsp" />
<jsp:include page="${pageContext.request.contextPath}/WEB-INF/fragments/common/sidebar_left/sidebar.jsp" />
<main class="detail-page">
    <div class="detail-header-bar">
        <a class="detail-back" href="${pageContext.request.contextPath}/admin/users">← 회원 목록으로</a>
    </div>
    <!-- 알림 배너 -->
    <c:if test="${not empty param.status}">
        <div class="alert ${param.status eq 'update_success' ? 'alert--success' : 'alert--error'}">
            <c:choose>
                <c:when test="${param.status eq 'update_success'}">회원 정보가 업데이트되었습니다.</c:when>
                <c:when test="${param.status eq 'update_duplicate_email'}">이미 사용 중인 이메일입니다.</c:when>
                <c:when test="${param.status eq 'update_duplicate_nickname'}">이미 사용 중인 닉네임입니다.</c:when>
                <c:otherwise>요청을 처리하지 못했습니다. 잠시 후 다시 시도해주세요.</c:otherwise>
            </c:choose>
        </div>
    </c:if>
    <!-- 헤더 카드 -->
    <section class="member-card">
        <div class="member-card__header">
            <div>
                <p class="member-card__eyebrow">회원 상세 정보</p>
                <h1 class="member-card__title">${member.memNickname}</h1>
            </div>
            <span class="member-card__status">${statusLabel}</span>
        </div>
        <div class="member-card__meta">
            <div>
                <p class="member-card__label">회원 ID</p>
                <strong>${member.memId}</strong>
            </div>
            <div>
                <p class="member-card__label">총 거래 건수</p>
                <strong>0회</strong>
            </div>
        </div>
    </section>

    <section class="detail-section">
        <h4 class="detail-section__title">기본 정보</h4>
        <div class="detail-grid">
            <div class="detail-info">
                <p class="detail-info__label">회원 ID</p>
                <p class="detail-info__value">${member.memId}</p>
            </div>
            <div class="detail-info">
                <p class="detail-info__label">닉네임</p>
                <p class="detail-info__value">${member.memNickname}</p>
            </div>
            <div class="detail-info">
                <p class="detail-info__label">이메일</p>
                <p class="detail-info__value">${member.memEmail}</p>
            </div>
            <div class="detail-info">
                <p class="detail-info__label">가입일</p>
                <p class="detail-info__value">
                    <c:choose>
                        <c:when test="${not empty joinDate}">${joinDate}</c:when>
                        <c:otherwise>-</c:otherwise>
                    </c:choose>
                </p>
            </div>
            <div class="detail-info">
                <p class="detail-info__label">최종 접속</p>
                <p class="detail-info__value">
                    <c:choose>
                        <c:when test="${not empty lastLoginDate}">${lastLoginDate}</c:when>
                        <c:otherwise>-</c:otherwise>
                    </c:choose>
                </p>
            </div>
            <div class="detail-info">
                <p class="detail-info__label">상태</p>
                <span class="badge badge--success">${statusLabel}</span>
            </div>
            <div class="detail-info">
                <p class="detail-info__label">총 거래 건수</p>
                <p class="detail-info__value">0회</p>
            </div>
        </div>
    </section>

    <section class="detail-section">
        <h4 class="detail-section__title">자산 정보</h4>
        <div class="asset-grid">
            <article class="asset-card asset-card--blue">
                <p class="asset-card__label">예치금</p>
                <p class="asset-card__value">${virtualCashFormatted}원</p>
            </article>
            <article class="asset-card asset-card--purple">
                <p class="asset-card__label">총 자산</p>
                <p class="asset-card__value">${virtualCashFormatted}원</p>
            </article>
            <article class="asset-card asset-card--green">
                <p class="asset-card__label">수익률</p>
                <p class="asset-card__value">0.0%</p>
            </article>
            <article class="asset-card asset-card--green">
                <p class="asset-card__label">평가 손익</p>
                <p class="asset-card__value">0원</p>
            </article>
        </div>
    </section>
    <!-- 임의 데이터 지정 -->
    <section class="detail-section">
        <h4 class="detail-section__title">보유 종목</h4>
        <div class="table-card">
            <table>
                <thead>
                <tr>
                    <th>종목명</th>
                    <th>보유 수량</th>
                    <th>평균 단가</th>
                    <th>현재가</th>
                    <th>평가액</th>
                    <th>손익</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>삼성전자</td>
                    <td class="text-right">50주</td>
                    <td class="text-right">68,000원</td>
                    <td class="text-right">72,500원</td>
                    <td class="text-right font-strong">3,625,000원</td>
                    <td class="text-right">
                        <div class="gain gain--up">
                            <span>+6.62%</span>
                            <small>+225,000원</small>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td>SK하이닉스</td>
                    <td class="text-right">30주</td>
                    <td class="text-right">145,000원</td>
                    <td class="text-right">158,000원</td>
                    <td class="text-right font-strong">4,740,000원</td>
                    <td class="text-right">
                        <div class="gain gain--up">
                            <span>+8.97%</span>
                            <small>+390,000원</small>
                        </div>
                    </td>
                </tr>
                <tr>
                    <td>NAVER</td>
                    <td class="text-right">20주</td>
                    <td class="text-right">195,000원</td>
                    <td class="text-right">203,000원</td>
                    <td class="text-right font-strong">4,060,000원</td>
                    <td class="text-right">
                        <div class="gain gain--up">
                            <span>+4.10%</span>
                            <small>+160,000원</small>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </section>

    <section class="detail-section">
        <h4 class="detail-section__title">최근 거래 내역</h4>
        <div class="table-card">
            <table>
                <thead>
                <tr>
                    <th>일시</th>
                    <th>구분</th>
                    <th>종목명</th>
                    <th>수량</th>
                    <th>체결가</th>
                    <th>거래금액</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>2025-11-19 14:25</td>
                    <td><span class="badge badge--sell">매수</span></td>
                    <td>삼성전자</td>
                    <td class="text-right">10주</td>
                    <td class="text-right">72,500원</td>
                    <td class="text-right font-strong">725,000원</td>
                </tr>
                <tr>
                    <td>2025-11-19 10:15</td>
                    <td><span class="badge badge--buy">매도</span></td>
                    <td>카카오</td>
                    <td class="text-right">15주</td>
                    <td class="text-right">54,200원</td>
                    <td class="text-right font-strong">813,000원</td>
                </tr>
                <tr>
                    <td>2025-11-18 15:30</td>
                    <td><span class="badge badge--sell">매수</span></td>
                    <td>SK하이닉스</td>
                    <td class="text-right">5주</td>
                    <td class="text-right">158,000원</td>
                    <td class="text-right font-strong">790,000원</td>
                </tr>
                </tbody>
            </table>
        </div>
    </section>

    <section class="detail-section">
        <div class="detail-actions">
            <button type="button" class="btn btn--accent" data-detail-modal-open="member-edit-modal">회원정보 수정</button>
            <form id="member-delete-form" method="post" action="${pageContext.request.contextPath}/admin/users/detail" style="display: none;">
                <input type="hidden" name="action" value="delete"/>
                <input type="hidden" name="memberId" value="${member.memId}"/>
            </form>
            <button type="button" class="btn btn--danger" id="member-delete-trigger">회원 탈퇴 처리</button>
        </div>
        <p class="notice__footnote">* 탈퇴 시 모든 거래 내역과 자산 정보가 삭제됩니다.</p>
    </section>

    <div class="modal modal--detail" id="member-edit-modal" aria-hidden="true">
        <div class="modal__overlay" data-modal-close></div>
        <div class="modal__dialog member-detail" role="dialog" aria-modal="true" aria-labelledby="member-edit-title">
            <div class="modal__header">
                <h3 id="member-edit-title">회원 정보 수정</h3>
                <button type="button" class="modal__close-btn" data-modal-close>&times;</button>
            </div>
            <form method="post" action="${pageContext.request.contextPath}/admin/users/detail" class="modal-form">
                <input type="hidden" name="action" value="update"/>
                <input type="hidden" name="memberId" value="${member.memId}"/>
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                    <div class="form-field">
                        <label class="form-field__label" for="editEmail">이메일</label>
                        <input type="email" id="editEmail" name="memberEmail" class="form-field__input" value="${member.memEmail}" readonly style="background-color: #f5f5f5; cursor: not-allowed;">
                    </div>
                    <div class="form-field">
                        <label class="form-field__label" for="editNickname">닉네임</label>
                        <input type="text" id="editNickname" name="memberNickname" class="form-field__input" required value="${member.memNickname}">
                    </div>
                </div>
                <div class="form-field">
                    <label class="form-field__label" for="editVirtualCash">보유자금</label>
                    <input type="number" id="editVirtualCash" name="virtualCash" class="form-field__input" required min="0" value="${member.virtualCash}" placeholder="보유자금을 입력하세요">
                </div>
                <div class="modal__actions">
                    <button type="button" class="modal__btn modal__btn--ghost" data-modal-close>취소</button>
                    <button type="submit" class="modal__btn modal__btn--primary">저장</button>
                </div>
            </form>
        </div>
    </div>
</main>
<script src="${pageContext.request.contextPath}/static/js/admin/user_detail.js"></script>
</body>
</html>

