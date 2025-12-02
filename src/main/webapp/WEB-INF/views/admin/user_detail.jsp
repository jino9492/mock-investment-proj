<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:useBean id="now" class="java.util.Date" />
<c:set var="member" value="${requestScope.member}" />
<c:set var="pageTitle" value="회원 상세 정보" scope="request" />
<c:set var="pageStyles" value="admin/users.css,admin/user_detail.css" scope="request" />
<c:set var="pageScripts" value="admin/user_detail.js" scope="request" />
<fmt:formatDate value="${member.memJoinAt}" pattern="yyyy-MM-dd" var="joinDate" />
<fmt:formatDate value="${member.memLastLoginAt}" pattern="yyyy-MM-dd HH:mm" var="lastLoginDate" />
<fmt:formatNumber value="${member.virtualCash}" type="number" groupingUsed="true" var="virtualCashFormatted" />
<c:set var="statusLabel" value="${empty member.memLastLoginAt ? '신규' : '활성'}" />
<jsp:include page="/WEB-INF/fragments/admin/admin_layout_head.jsp" />
<jsp:include page="/WEB-INF/fragments/admin/admin_layout_header.jsp" />
<main class="users-page">
    <div class="page-header">
        <div style="display: flex; align-items: center; gap: 16px; margin-bottom: 8px;">
            <a href="${pageContext.request.contextPath}/admin/users" style="color: #1C1E23; text-decoration: none; font-weight: 600; font-size: 14px;">← 회원 목록으로</a>
        </div>
        <h1 class="page-header__title">회원 상세 정보</h1>
        <p class="page-header__date">
            <fmt:formatDate value="${now}" pattern="yyyy년 MM월 dd일 HH:mm:ss" />
        </p>
    </div>
    <jsp:include page="/WEB-INF/fragments/admin/admin_status_alert.jsp" />
    <!-- 헤더 카드 -->
    <section class="stat-card" style="margin-bottom: 20px;">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
            <div>
                <p style="font-size: 13px; color: #8C90A2; margin-bottom: 4px;">회원 상세 정보</p>
                <h2 style="font-size: 28px; font-weight: 700; color: #17171B; margin: 0;">${member.memNickname}</h2>
            </div>
            <span class="badge badge--success">${statusLabel}</span>
        </div>
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 16px;">
            <div>
                <p style="font-size: 12px; color: #8C90A2; margin-bottom: 4px;">회원 ID</p>
                <strong style="font-size: 16px; font-weight: 600; color: #1C1E23;">${member.memId}</strong>
            </div>
            <div>
                <p style="font-size: 12px; color: #8C90A2; margin-bottom: 4px;">총 거래 건수</p>
                <strong style="font-size: 16px; font-weight: 600; color: #1C1E23;">0회</strong>
            </div>
        </div>
    </section>

    <section class="stat-card" style="margin-bottom: 20px;">
        <h4 style="font-size: 18px; font-weight: 600; color: #17171B; margin-bottom: 16px;">기본 정보</h4>
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

    <section class="stat-card" style="margin-bottom: 20px;">
        <h4 style="font-size: 18px; font-weight: 600; color: #17171B; margin-bottom: 16px;">자산 정보</h4>
        <c:set var="asset" value="${requestScope.asset}" />
        <div class="asset-grid">
            <article class="asset-card asset-card--blue">
                <p class="asset-card__label">예치금</p>
                <p class="asset-card__value"><fmt:formatNumber value="${not empty asset ? asset.virtualCash : 0}" pattern="#,###"/>원</p>
            </article>
            <article class="asset-card asset-card--purple">
                <p class="asset-card__label">총 자산</p>
                <p class="asset-card__value"><fmt:formatNumber value="${not empty asset ? asset.totalAsset : 0}" pattern="#,###"/>원</p>
            </article>
            <article class="asset-card asset-card--green">
                <p class="asset-card__label">수익률</p>
                <p class="asset-card__value"><fmt:formatNumber value="${not empty asset ? asset.profitRate : 0}" pattern="#,##0.0"/>%</p>
            </article>
            <article class="asset-card asset-card--green">
                <p class="asset-card__label">평가 손익</p>
                <p class="asset-card__value"><fmt:formatNumber value="${not empty asset ? asset.profitLoss : 0}" pattern="#,###"/>원</p>
            </article>
        </div>
    </section>
    <section class="stat-card" style="margin-bottom: 20px;">
        <h4 style="font-size: 18px; font-weight: 600; color: #17171B; margin-bottom: 16px;">보유 종목</h4>
        <c:set var="holdings" value="${requestScope.holdings}" />
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
                <c:choose>
                    <c:when test="${not empty holdings}">
                        <c:forEach items="${holdings}" var="holding">
                            <tr>
                                <td>${holding.stockName}</td>
                                <td class="text-right"><fmt:formatNumber value="${holding.quantity}" pattern="#,###"/>주</td>
                                <td class="text-right"><fmt:formatNumber value="${holding.averagePrice}" pattern="#,###"/>원</td>
                                <td class="text-right"><fmt:formatNumber value="${holding.currentPrice}" pattern="#,###"/>원</td>
                                <td class="text-right font-strong"><fmt:formatNumber value="${holding.evaluationAmount}" pattern="#,###"/>원</td>
                                <td class="text-right">
                                    <c:choose>
                                        <c:when test="${holding.profitLoss > 0}">
                                            <div class="gain gain--up">
                                                <span>+<fmt:formatNumber value="${holding.profitRate}" pattern="#,##0.00"/>%</span>
                                                <small>+<fmt:formatNumber value="${holding.profitLoss}" pattern="#,###"/>원</small>
                                            </div>
                                        </c:when>
                                        <c:when test="${holding.profitLoss < 0}">
                                            <div class="gain" style="color: #C0392B;">
                                                <span><fmt:formatNumber value="${holding.profitRate}" pattern="#,##0.00"/>%</span>
                                                <small><fmt:formatNumber value="${holding.profitLoss}" pattern="#,###"/>원</small>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="gain" style="color: #666;">
                                                <span>0.00%</span>
                                                <small>0원</small>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="6" style="text-align: center; padding: 40px; color: #999;">보유 종목이 없습니다.</td>
                        </tr>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </section>

    <section class="stat-card" style="margin-bottom: 20px;">
        <h4 style="font-size: 18px; font-weight: 600; color: #17171B; margin-bottom: 16px;">최근 거래 내역</h4>
        <c:set var="recentTrades" value="${requestScope.recentTrades}" />
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
                <c:choose>
                    <c:when test="${not empty recentTrades}">
                        <c:forEach items="${recentTrades}" var="trade">
                            <tr>
                                <td><fmt:formatDate value="${trade.tradeDate}" pattern="yyyy-MM-dd HH:mm"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${trade.tradeType eq 'buy'}">
                                            <span class="badge badge--buy">매수</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge badge--sell">매도</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${trade.stockName}</td>
                                <td class="text-right"><fmt:formatNumber value="${trade.quantity}" pattern="#,###"/>주</td>
                                <td class="text-right"><fmt:formatNumber value="${trade.price}" pattern="#,###"/>원</td>
                                <td class="text-right font-strong"><fmt:formatNumber value="${trade.totalAmount}" pattern="#,###"/>원</td>
                            </tr>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <tr>
                            <td colspan="6" style="text-align: center; padding: 40px; color: #999;">거래 내역이 없습니다.</td>
                        </tr>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </section>

    <section class="stat-card" style="margin-bottom: 20px;">
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
<jsp:include page="/WEB-INF/fragments/admin/admin_layout_footer.jsp" />

