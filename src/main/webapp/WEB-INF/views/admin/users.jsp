<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:useBean id="now" class="java.util.Date" />
<c:set var="memberStats" value="${requestScope.memberStats}" />
<c:set var="memberList" value="${requestScope.memberList}" />
<c:set var="filteredCount" value="${requestScope.filteredCount}" />
<c:set var="keyword" value="${requestScope.keyword}" />
<c:set var="statusFilter" value="${requestScope.statusFilter}" />
<c:set var="profitFilter" value="${requestScope.profitFilter}" />
<c:set var="joinFilter" value="${requestScope.joinFilter}" />
<c:set var="pageTitle" value="회원관리" scope="request" />
<c:set var="pageStyles" value="admin/users.css" scope="request" />
<c:set var="pageScripts" value="admin/users.js" scope="request" />
<jsp:include page="/WEB-INF/fragments/admin/admin_layout_head.jsp" />
<jsp:include page="/WEB-INF/fragments/admin/admin_layout_header.jsp" />
<main class="users-page">
    <div class="page-header">
        <h1 class="page-header__title">회원관리</h1>
        <p class="page-header__date" id="current-time">
            <fmt:formatDate value="${now}" pattern="yyyy년 MM월 dd일 HH:mm:ss" />
        </p>
    </div>
    <jsp:include page="/WEB-INF/fragments/admin/admin_status_alert.jsp" />

        <!-- 통계 카드 (2열) -->
        <section class="stat-grid stat-grid--col-2">
            <article class="stat-card">
                <div class="stat-card__label">전체 회원 수</div>
                <div class="stat-card__value">
                    <fmt:formatNumber value="${not empty memberStats ? memberStats.totalCount : 0}" pattern="#,###"/>명
                </div>
                <c:choose>
                    <c:when test="${not empty memberStats}">
                        <c:set var="totalCountChange" value="${memberStats.totalCountChange}" />
                        <c:choose>
                            <c:when test="${totalCountChange > 0}">
                                <div class="stat-card__change stat-card__change--up">
                                    전일 대비 +<fmt:formatNumber value="${totalCountChange}" pattern="#,###"/>명
                                </div>
                            </c:when>
                            <c:when test="${totalCountChange < 0}">
                                <div class="stat-card__change stat-card__change--down">
                                    전일 대비 <fmt:formatNumber value="${totalCountChange}" pattern="#,###"/>명
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="stat-card__change stat-card__change--neutral">
                                    전일 대비 0명
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise>
                        <div class="stat-card__change stat-card__change--down">
                            전일 대비 -명
                        </div>
                    </c:otherwise>
                </c:choose>
            </article>

            <article class="stat-card">
                <div class="stat-card__label">당일 접속자 수</div>
                <div class="stat-card__value">
                    <fmt:formatNumber value="${not empty memberStats ? memberStats.todayLoginCount : 0}" pattern="#,###"/>명
                </div>
                <c:choose>
                    <c:when test="${not empty memberStats}">
                        <c:set var="loginCountChange" value="${memberStats.todayLoginCountChange}" />
                        <c:choose>
                            <c:when test="${loginCountChange > 0}">
                                <div class="stat-card__change stat-card__change--up">
                                    전일 대비 +<fmt:formatNumber value="${loginCountChange}" pattern="#,###"/>명
                                </div>
                            </c:when>
                            <c:when test="${loginCountChange < 0}">
                                <div class="stat-card__change stat-card__change--down">
                                    전일 대비 <fmt:formatNumber value="${loginCountChange}" pattern="#,###"/>명
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="stat-card__change stat-card__change--neutral">
                                    전일 대비 0명
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise>
                        <div class="stat-card__change stat-card__change--down">
                            전일 대비 -명
                        </div>
                    </c:otherwise>
                </c:choose>
            </article>
        </section>

        <!-- 통계 카드 (3열) -->
        <section class="stat-grid stat-grid--col-3">
            <article class="stat-card">
                <div class="stat-card__label">활동 사용자 수</div>
                <div class="stat-card__value">
                    <fmt:formatNumber value="${not empty memberStats ? memberStats.activeCount : 0}" pattern="#,###"/>명
                </div>
                <c:choose>
                    <c:when test="${not empty memberStats}">
                        <c:set var="activeCountChange" value="${memberStats.activeCountChange}" />
                        <c:choose>
                            <c:when test="${activeCountChange > 0}">
                                <div class="stat-card__change stat-card__change--up">
                                    전일 대비 +<fmt:formatNumber value="${activeCountChange}" pattern="#,###"/>명
                                </div>
                            </c:when>
                            <c:when test="${activeCountChange < 0}">
                                <div class="stat-card__change stat-card__change--down">
                                    전일 대비 <fmt:formatNumber value="${activeCountChange}" pattern="#,###"/>명
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="stat-card__change stat-card__change--neutral">
                                    전일 대비 0명
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise>
                        <div class="stat-card__change stat-card__change--down">
                            전일 대비 -명
                        </div>
                    </c:otherwise>
                </c:choose>
            </article>

            <article class="stat-card">
                <div class="stat-card__label">휴면 사용자 수</div>
                <div class="stat-card__value">
                    <fmt:formatNumber value="${not empty memberStats ? memberStats.dormantCount : 0}" pattern="#,###"/>명
                </div>
                <c:choose>
                    <c:when test="${not empty memberStats}">
                        <c:set var="dormantCountChange" value="${memberStats.dormantCountChange}" />
                        <c:choose>
                            <c:when test="${dormantCountChange > 0}">
                                <div class="stat-card__change stat-card__change--up">
                                    전일 대비 +<fmt:formatNumber value="${dormantCountChange}" pattern="#,###"/>명
                                </div>
                            </c:when>
                            <c:when test="${dormantCountChange < 0}">
                                <div class="stat-card__change stat-card__change--down">
                                    전일 대비 <fmt:formatNumber value="${dormantCountChange}" pattern="#,###"/>명
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="stat-card__change stat-card__change--neutral">
                                    전일 대비 0명
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise>
                        <div class="stat-card__change stat-card__change--down">
                            전일 대비 -명
                        </div>
                    </c:otherwise>
                </c:choose>
            </article>

            <article class="stat-card">
                <div class="stat-card__label">탈퇴 사용자 수</div>
                <div class="stat-card__value">
                    <c:choose>
                        <c:when test="${not empty memberStats and memberStats.deletedCount > 0}">
                            <fmt:formatNumber value="${memberStats.deletedCount}" pattern="#,###"/>명
                        </c:when>
                        <c:otherwise>-</c:otherwise>
                    </c:choose>
                </div>
                <c:choose>
                    <c:when test="${not empty memberStats}">
                        <c:set var="deletedCountChange" value="${memberStats.deletedCountChange}" />
                        <c:choose>
                            <c:when test="${deletedCountChange > 0}">
                                <div class="stat-card__change stat-card__change--up">
                                    전일 대비 +<fmt:formatNumber value="${deletedCountChange}" pattern="#,###"/>명
                                </div>
                            </c:when>
                            <c:when test="${deletedCountChange < 0}">
                                <div class="stat-card__change stat-card__change--down">
                                    전일 대비 <fmt:formatNumber value="${deletedCountChange}" pattern="#,###"/>명
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="stat-card__change stat-card__change--neutral">
                                    전일 대비 0명
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </c:when>
                    <c:otherwise>
                        <div class="stat-card__change stat-card__change--down">
                            전일 대비 -명
                        </div>
                    </c:otherwise>
                </c:choose>
            </article>
        </section>

        <!-- 필터 UI -->
        <form class="filter-bar" method="get" action="${pageContext.request.contextPath}/admin/users">
            <!-- 회원 상태 -->
            <div class="filter-item">
                <label class="filter-item__label" for="statusFilter">회원 상태</label>
                <select class="filter-item__select" id="statusFilter" name="statusFilter" onchange="this.form.submit()">
                    <option value="" <c:if test="${empty statusFilter}">selected</c:if>>전체</option>
                    <option value="active" <c:if test="${statusFilter eq 'active'}">selected</c:if>>활동</option>
                    <option value="dormant" <c:if test="${statusFilter eq 'dormant'}">selected</c:if>>휴면</option>
                </select>
            </div>

            <!-- 수익률 -->
            <div class="filter-item">
                <label class="filter-item__label" for="profitFilter">수익률</label>
                <select class="filter-item__select" id="profitFilter" name="profitFilter" onchange="this.form.submit()">
                    <option value="" <c:if test="${empty profitFilter}">selected</c:if>>전체</option>
                    <option value="10" <c:if test="${profitFilter eq '10'}">selected</c:if>>10% 이상</option>
                    <option value="30" <c:if test="${profitFilter eq '30'}">selected</c:if>>30% 이상</option>
                    <option value="50" <c:if test="${profitFilter eq '50'}">selected</c:if>>50% 이상</option>
                </select>
            </div>

            <!-- 가입기간 -->
            <div class="filter-item">
                <label class="filter-item__label" for="joinFilter">가입 기간</label>
                <select class="filter-item__select" id="joinFilter" name="joinFilter" onchange="this.form.submit()">
                    <option value="" <c:if test="${empty joinFilter}">selected</c:if>>전체</option>
                    <option value="1m" <c:if test="${joinFilter eq '1m'}">selected</c:if>>최근 1개월</option>
                    <option value="3m" <c:if test="${joinFilter eq '3m'}">selected</c:if>>최근 3개월</option>
                    <option value="6m" <c:if test="${joinFilter eq '6m'}">selected</c:if>>최근 6개월</option>
                    <option value="1y" <c:if test="${joinFilter eq '1y'}">selected</c:if>>최근 1년</option>
                </select>
            </div>

            <!-- 검색 -->
            <div class="filter-item filter-item--search">
                <label class="filter-item__label" for="memberSearch">검색</label>
                <div style="display: flex; gap: 8px;">
                    <input type="text" id="memberSearch" name="keyword" class="filter-item__input"
                        placeholder="이름, ID, E-mail" value="${keyword}" />
                    <button type="submit" class="filter-item__button">🔍</button>
                </div>
            </div>
        </form>

        <!-- 회원 목록 테이블 -->
        <section class="table-section">
            <div class="table-header">
                <h2 class="table-header__title">회원 목록</h2>
                <button type="button" class="table-header__add-btn--active" data-modal-open="member-create-modal">+</button>
            </div>

            <jsp:include page="${pageContext.request.contextPath}/WEB-INF/fragments/admin/member_table.jsp">
                <jsp:param name="memberList" value="${memberList}"/>
            </jsp:include>
        </section>

        <!-- 회원 추가 모달 -->
        <div class="modal" id="member-create-modal" aria-hidden="true">
            <div class="modal__overlay" data-modal-close></div>
            <div class="modal__dialog" role="dialog" aria-modal="true" aria-labelledby="member-modal-title">
                <div class="modal__header">
                    <h3 id="member-modal-title">회원 추가</h3>
                    <button type="button" class="modal__close-btn" data-modal-close>&times;</button>
                </div>
                <form id="member-create-form" class="modal-form" method="post" action="${pageContext.request.contextPath}/admin/users" accept-charset="UTF-8">
                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                        <div class="form-field">
                            <label for="memberEmail" class="form-field__label">이메일<span class="form-field__required">*</span></label>
                            <input type="email" id="memberEmail" name="memberEmail" class="form-field__input" required placeholder="user@example.com">
                        </div>
                        <div class="form-field">
                            <label for="memberNickname" class="form-field__label">닉네임<span class="form-field__required">*</span></label>
                            <input type="text" id="memberNickname" name="memberNickname" class="form-field__input" required placeholder="닉네임">
                        </div>
                    </div>
                    <div class="form-field">
                        <label for="memberPassword" class="form-field__label">임시 비밀번호<span class="form-field__required">*</span></label>
                        <input type="password" id="memberPassword" name="memberPassword" class="form-field__input" required minlength="6" placeholder="6자 이상">
                    </div>
                    <fieldset class="form-field form-field--inline">
                        <legend class="form-field__label">약관 동의</legend>
                        <label class="form-checkbox">
                            <input type="checkbox" name="agreeNotification">
                            <span>알림 수신 동의</span>
                        </label>
                    </fieldset>
                    <div class="modal__actions">
                        <button type="button" class="modal__btn modal__btn--ghost" data-modal-close>취소</button>
                        <button type="submit" class="modal__btn modal__btn--primary">저장</button>
                    </div>
                </form>
            </div>
        </div>
    </main>
<jsp:include page="/WEB-INF/fragments/admin/admin_layout_footer.jsp" />
