<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<jsp:useBean id="now" class="java.util.Date" />
<c:set var="memberStats" value="${requestScope.memberStats}" />
<c:set var="memberList" value="${requestScope.memberList}" />
<c:set var="filteredCount" value="${requestScope.filteredCount}" />
<c:set var="keyword" value="${requestScope.keyword}" />
<c:set var="statusFilter" value="${requestScope.statusFilter}" />
<c:set var="profitFilter" value="${requestScope.profitFilter}" />
<c:set var="joinFilter" value="${requestScope.joinFilter}" />
<html>
<head>
    <title>사용자 관리</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin/users.css"/>
</head>
<body>
<jsp:include page="/WEB-INF/fragments/common/header_admin.jsp" />
<jsp:include page="/WEB-INF/fragments/common/sidebar_left/sidebar.jsp">
    <jsp:param name="menuItems" value="대시보드,회원 관리,거래 관리" />
    <jsp:param name="menuLinks" value="/admin/dashboard,/admin/users,/admin/trades" />
</jsp:include>
<main class="users-page">
    <!-- 페이지 헤더 -->
    <div class="page-header">
        <h1 class="page-header__title">회원관리</h1>
        <p class="page-header__date">
            <fmt:formatDate value="${now}" pattern="yyyy년 MM월 dd일 HH:mm:ss" />
        </p>
    </div>

    <c:if test="${not empty param.status}">
        <c:set var="statusValue" value="${param.status}" />
        <div class="alert ${statusValue eq 'success' ? 'alert--success' : 'alert--error'}">
            <c:choose>
                <c:when test="${statusValue eq 'success'}">새 회원이 성공적으로 추가되었습니다.</c:when>
                <c:when test="${statusValue eq 'duplicate_email'}">이미 등록된 이메일입니다.</c:when>
                <c:when test="${statusValue eq 'duplicate_nickname'}">이미 사용 중인 닉네임입니다.</c:when>
                <c:when test="${statusValue eq 'invalid'}">필수 입력 항목을 다시 확인해 주세요.</c:when>
                <c:when test="${statusValue eq 'delete_success'}">회원이 성공적으로 삭제되었습니다.</c:when>
                <c:when test="${statusValue eq 'not_found'}">요청한 회원을 찾을 수 없습니다.</c:when>
                <c:otherwise>회원 저장에 실패했습니다. 잠시 후 다시 시도해 주세요.</c:otherwise>
            </c:choose>
        </div>
    </c:if>

    <!-- 통계 카드 (2열) -->
    <section class="stat-grid stat-grid--col-2">
        <article class="stat-card">
            <div class="stat-card__label">전체 회원 수</div>
            <div class="stat-card__value">
                <c:out value="${memberStats.totalCount}" default="0" />
                명
            </div>
            <div class="stat-card__change stat-card__change--down">
                전일 대비 변동 없음
            </div>
        </article>

        <article class="stat-card">
            <div class="stat-card__label">당일 접속자 수</div>
            <div class="stat-card__value">
                <c:out value="${memberStats.todayLoginCount}" default="0" />
                명
            </div>
            <div class="stat-card__change stat-card__change--up">
                전일 대비 변동 없음
            </div>
        </article>
    </section>

    <!-- 통계 카드 (3열) -->
    <section class="stat-grid stat-grid--col-3">
        <article class="stat-card">
            <div class="stat-card__label">활동 사용자 수</div>
            <div class="stat-card__value">
                <c:out value="${memberStats.activeCount}" default="0" />
                명
            </div>
            <div class="stat-card__change stat-card__change--down">
                전일 대비 변동 없음
            </div>
        </article>

        <article class="stat-card">
            <div class="stat-card__label">휴면 사용자 수</div>
            <div class="stat-card__value">
                <c:out value="${memberStats.dormantCount}" default="0" />
                명
            </div>
            <div class="stat-card__change stat-card__change--up">
                전일 대비 변동 없음
            </div>
        </article>

        <article class="stat-card">
            <div class="stat-card__label">탈퇴 사용자 수</div>
            <div class="stat-card__value">-</div>
            <div class="stat-card__change stat-card__change--down">
                전일 대비 변동 없음
            </div>
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
<script src="${pageContext.request.contextPath}/static/js/admin/users.js"></script>
</body>
</html>
