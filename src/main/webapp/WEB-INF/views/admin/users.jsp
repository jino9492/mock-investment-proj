<%--
  Created by IntelliJ IDEA.
  User: js123
  Date: 25. 11. 23.
  Time: 오후 11:30
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>사용자 관리</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin/users.css"/>
</head>
<body>
<header id="user__header">
    <jsp:include page="/WEB-INF/fragments/common/header_admin.jsp" />
</header>
<aside id="user__aside">
    <jsp:include page="${pageContext.request.contextPath}/WEB-INF/fragments/common/sidebar_left/sidebar.jsp" />
</aside>
<main id="users-content">
    <!-- 페이지 헤더 -->
    <div class="page-header">
        <h1 class="page-header__title">회원관리</h1>
        <p class="page-header__date">2025년 11월 19일 02:00:00</p>
    </div>

    <!-- 통계 카드 (2열) -->
    <section class="stat-grid stat-grid--col-2">
        <article class="stat-card">
            <div class="stat-card__label">전체 회원 수</div>
            <div class="stat-card__value">#명</div>
            <div class="stat-card__change stat-card__change--down">
                ↓ 전일 대비 #%
            </div>
        </article>

        <article class="stat-card">
            <div class="stat-card__label">당일 접속자 수</div>
            <div class="stat-card__value">#명</div>
            <div class="stat-card__change stat-card__change--up">
                ↑ 전일 대비 #%
            </div>
        </article>
    </section>

    <!-- 통계 카드 (3열) -->
    <section class="stat-grid stat-grid--col-3">
        <article class="stat-card">
            <div class="stat-card__label">활동 사용자 수</div>
            <div class="stat-card__value">#명</div>
            <div class="stat-card__change stat-card__change--down">
                ↓ 전일 대비 #%
            </div>
        </article>

        <article class="stat-card">
            <div class="stat-card__label">휴면 사용자 수</div>
            <div class="stat-card__value">#명</div>
            <div class="stat-card__change stat-card__change--up">
                ↑ 전일 대비 #%
            </div>
        </article>

        <article class="stat-card">
            <div class="stat-card__label">탈퇴 사용자 수</div>
            <div class="stat-card__value">#명</div>
            <div class="stat-card__change stat-card__change--down">
                ↓ 전일 대비 #%
            </div>
        </article>
    </section>

    <!-- 필터 -->
    <section class="filter-bar">
        <!-- 회원 상태 -->
        <div class="filter-item">
            <label class="filter-item__label">회원 상태</label>
            <select class="filter-item__select">
                <option value="">전체</option>
                <option value="active">활동</option>
                <option value="dormant">휴면</option>
                <option value="deleted">탈퇴</option>
            </select>
        </div>

        <!-- 수익률 -->
        <div class="filter-item">
            <label class="filter-item__label">수익률</label>
            <select class="filter-item__select">
                <option value="">전체</option>
                <option value="10">10% 이상</option>
                <option value="30">30% 이상</option>
                <option value="50">50% 이상</option>
            </select>
        </div>

        <!-- 가입기간 -->
        <div class="filter-item">
            <label class="filter-item__label">가입 기간</label>
            <select class="filter-item__select">
                <option value="">전체</option>
                <option value="1m">1개월 이내</option>
                <option value="3m">3개월 이내</option>
                <option value="6m">6개월 이내</option>
                <option value="1y">1년 이내</option>
            </select>
        </div>

        <!-- 검색 -->
        <div class="filter-item filter-item--search">
            <label class="filter-item__label">검색</label>
            <div style="display: flex; gap: 8px;">
                <input type="text" class="filter-item__input"
                       placeholder="이름, ID, E-mail">
                <button type="button" class="filter-item__button">🔍</button>
            </div>
        </div>
    </section>

    <!-- 회원 목록 테이블 -->
    <section class="table-section">
        <!-- 테이블 헤더 (박스 밖) -->
        <div class="table-header">
            <h2 class="table-header__title">회원 목록</h2>
            <button class="table-header__add-btn--active">+</button>
        </div>

        <!-- 테이블 + 페이지네이션 (단일 흰색 박스) -->
        <div class="table-wrapper">
            <table class="data-table">
                <thead class="data-table__head">
                <tr>
                    <th class="data-table__header-cell">회원ID</th>
                    <th class="data-table__header-cell">닉네임</th>
                    <th class="data-table__header-cell">이메일</th>
                    <th class="data-table__header-cell">가입일</th>
                    <th class="data-table__header-cell">마지막 접속</th>
                    <th class="data-table__header-cell">상태</th>
                    <th class="data-table__header-cell">총 자산</th>
                    <th class="data-table__header-cell">수익률</th>
                    <th class="data-table__header-cell">상세</th>
                </tr>
                </thead>
                <tbody class="data-table__body">
                <tr class="data-table__row">
                    <td class="data-table__cell">0000001</td>
                    <td class="data-table__cell">홍길동</td>
                    <td class="data-table__cell">hongkd@dongyang.ac.kr</td>
                    <td class="data-table__cell">2025.11.24</td>
                    <td class="data-table__cell">2025.11.24 14:30</td>
                    <td class="data-table__cell">활성</td>
                    <td class="data-table__cell">1,000,000원</td>
                    <td class="data-table__cell">+12.5%</td>
                    <td class="data-table__cell">⋯</td>
                </tr>
                <!-- 여기에 나머지 행들 추가 -->
                </tbody>
            </table>

            <!-- 페이지네이션 -->
            <div class="pagination">
                <button class="pagination__btn">이전</button>
                <button class="pagination__btn pagination__btn--active">1</button>
                <button class="pagination__btn">2</button>
                <button class="pagination__btn">3</button>
                <button class="pagination__btn">4</button>
                <button class="pagination__btn">5</button>
                <button class="pagination__btn">다음</button>
            </div>
        </div>
    </section>
</main>
dddddd
</body>
</html>
