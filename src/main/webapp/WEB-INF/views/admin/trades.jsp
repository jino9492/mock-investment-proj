<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:useBean id="now" class="java.util.Date" />
<c:set var="startDate" value="${requestScope.startDate}" />
<c:set var="endDate" value="${requestScope.endDate}" />
<c:set var="typeFilter" value="${requestScope.typeFilter}" />
<c:set var="statusFilter" value="${requestScope.statusFilter}" />
<c:set var="stockKeyword" value="${requestScope.stockKeyword}" />
<c:set var="userKeyword" value="${requestScope.userKeyword}" />
<c:set var="pageTitle" value="거래관리" scope="request" />
<c:set var="pageStyles" value="admin/users.css,admin/trades.css,admin/member_table.css" scope="request" />
<jsp:include page="/WEB-INF/fragments/admin/admin_layout_head.jsp" />
<jsp:include page="/WEB-INF/fragments/admin/admin_layout_header.jsp" />
<main class="trades-page">
    <div class="page-header">
        <h1 class="page-header__title">거래관리</h1>
        <p class="page-header__date" id="current-time">
            <fmt:formatDate value="${now}" pattern="yyyy년 MM월 dd일 HH:mm:ss" />
        </p>
    </div>

        <!-- 통계 카드 (2열) -->
        <c:set var="stats" value="${requestScope.tradeStats}" />
        <section class="stat-grid stat-grid--col-2">
            <article class="stat-card">
                <div class="stat-card__label">오늘 거래 건수</div>
                <div class="stat-card__value">
                    <fmt:formatNumber value="${not empty stats ? stats.todayTradeCount : 0}" pattern="#,###"/>건
                </div>
                <c:choose>
                    <c:when test="${not empty stats}">
                        <c:set var="tradeCountChangeRate" value="${stats.tradeCountChangeRate}" />
                        <div class="stat-card__change ${tradeCountChangeRate >= 0 ? 'stat-card__change--up' : 'stat-card__change--down'}">
                            전일 대비 <fmt:formatNumber value="${tradeCountChangeRate}" pattern="#,##0.0"/>%
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="stat-card__change stat-card__change--down">
                            전일 대비 0.0%
                        </div>
                    </c:otherwise>
                </c:choose>
            </article>

            <article class="stat-card">
                <div class="stat-card__label">오늘 거래 금액</div>
                <div class="stat-card__value">
                    <fmt:formatNumber value="${not empty stats ? stats.todayTradeAmount : 0}" pattern="#,###"/>원
                </div>
                <c:choose>
                    <c:when test="${not empty stats}">
                        <c:set var="tradeAmountChangeRate" value="${stats.tradeAmountChangeRate}" />
                        <div class="stat-card__change ${tradeAmountChangeRate >= 0 ? 'stat-card__change--up' : 'stat-card__change--down'}">
                            전일 대비 <fmt:formatNumber value="${tradeAmountChangeRate}" pattern="#,##0.0"/>%
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="stat-card__change stat-card__change--down">
                            전일 대비 0.0%
                        </div>
                    </c:otherwise>
                </c:choose>
            </article>
        </section>

        <!-- 통계 카드 (3열) -->
        <section class="stat-grid stat-grid--col-3">
            <article class="stat-card">
                <div class="stat-card__label">매수 건수</div>
                <div class="stat-card__value">
                    <fmt:formatNumber value="${not empty stats ? stats.buyCount : 0}" pattern="#,###"/>건
                </div>
                <c:choose>
                    <c:when test="${not empty stats}">
                        <c:set var="buyCountChangeRate" value="${stats.buyCountChangeRate}" />
                        <div class="stat-card__change ${buyCountChangeRate >= 0 ? 'stat-card__change--up' : 'stat-card__change--down'}">
                            전일 대비 <fmt:formatNumber value="${buyCountChangeRate}" pattern="#,##0.0"/>%
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="stat-card__change stat-card__change--down">
                            전일 대비 0.0%
                        </div>
                    </c:otherwise>
                </c:choose>
            </article>

            <article class="stat-card">
                <div class="stat-card__label">매도 건수</div>
                <div class="stat-card__value">
                    <fmt:formatNumber value="${not empty stats ? stats.sellCount : 0}" pattern="#,###"/>건
                </div>
                <c:choose>
                    <c:when test="${not empty stats}">
                        <c:set var="sellCountChangeRate" value="${stats.sellCountChangeRate}" />
                        <div class="stat-card__change ${sellCountChangeRate >= 0 ? 'stat-card__change--up' : 'stat-card__change--down'}">
                            전일 대비 <fmt:formatNumber value="${sellCountChangeRate}" pattern="#,##0.0"/>%
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="stat-card__change stat-card__change--down">
                            전일 대비 0.0%
                        </div>
                    </c:otherwise>
                </c:choose>
            </article>

            <article class="stat-card">
                <div class="stat-card__label">거래 취소 건수</div>
                <div class="stat-card__value">
                    <fmt:formatNumber value="${not empty stats ? stats.cancelledCount : 0}" pattern="#,###"/>건
                </div>
                <c:choose>
                    <c:when test="${not empty stats}">
                        <c:set var="cancelledCountChangeRate" value="${stats.cancelledCountChangeRate}" />
                        <div class="stat-card__change ${cancelledCountChangeRate >= 0 ? 'stat-card__change--up' : 'stat-card__change--down'}">
                            전일 대비 <fmt:formatNumber value="${cancelledCountChangeRate}" pattern="#,##0.0"/>%
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="stat-card__change stat-card__change--down">
                            전일 대비 0.0%
                        </div>
                    </c:otherwise>
                </c:choose>
            </article>
        </section>

        <!-- 필터 UI -->
        <form class="filter-bar" method="get" action="${pageContext.request.contextPath}/admin/trades">
            <!-- 날짜 범위 -->
            <div class="filter-item">
                <label class="filter-item__label" for="startDate">시작일</label>
                <input type="date" id="startDate" name="startDate" class="filter-item__input filter-item__input--date" 
                       value="${startDate}" onchange="this.form.submit()" />
            </div>
            <div class="filter-item">
                <label class="filter-item__label" for="endDate">종료일</label>
                <input type="date" id="endDate" name="endDate" class="filter-item__input filter-item__input--date" 
                       value="${endDate}" onchange="this.form.submit()" />
            </div>

            <!-- 구분 -->
            <div class="filter-item">
                <label class="filter-item__label" for="typeFilter">구분</label>
                <select class="filter-item__select" id="typeFilter" name="typeFilter" onchange="this.form.submit()">
                    <option value="" <c:if test="${empty typeFilter}">selected</c:if>>전체</option>
                    <option value="buy" <c:if test="${typeFilter eq 'buy'}">selected</c:if>>매수</option>
                    <option value="sell" <c:if test="${typeFilter eq 'sell'}">selected</c:if>>매도</option>
                </select>
            </div>

            <!-- 상태 -->
            <div class="filter-item">
                <label class="filter-item__label" for="statusFilter">상태</label>
                <select class="filter-item__select" id="statusFilter" name="statusFilter" onchange="this.form.submit()">
                    <option value="" <c:if test="${empty statusFilter}">selected</c:if>>전체</option>
                    <option value="settled" <c:if test="${statusFilter eq 'settled'}">selected</c:if>>체결</option>
                    <option value="cancelled" <c:if test="${statusFilter eq 'cancelled'}">selected</c:if>>취소</option>
                </select>
            </div>

            <!-- 종목 검색 -->
            <div class="filter-item filter-item--search">
                <label class="filter-item__label" for="stockSearch">종목 검색</label>
                <input type="text" id="stockSearch" name="stockKeyword" class="filter-item__input"
                       placeholder="거래ID, 종목" value="${stockKeyword}" />
            </div>

            <!-- 유저 검색 -->
            <div class="filter-item filter-item--search">
                <label class="filter-item__label" for="userSearch">유저 검색</label>
                <input type="text" id="userSearch" name="userKeyword" class="filter-item__input"
                       placeholder="이름, ID, e-mail ..." value="${userKeyword}" />
            </div>

            <!-- 검색 버튼 -->
            <div class="filter-item filter-item--search-btn">
                <label class="filter-item__label" style="visibility: hidden;">검색</label>
                <button type="submit" class="filter-item__button">🔍</button>
            </div>
        </form>

        <!-- 거래 내역 테이블 -->
        <section class="table-section">
            <div class="table-header">
                <h2 class="table-header__title">거래 내역</h2>
            </div>

            <div class="table-wrapper">
                <table class="data-table">
                    <thead class="data-table__head">
                    <tr>
                        <th class="data-table__header-cell">거래ID</th>
                        <th class="data-table__header-cell">시간</th>
                        <th class="data-table__header-cell">회원</th>
                        <th class="data-table__header-cell">구분</th>
                        <th class="data-table__header-cell">종목</th>
                        <th class="data-table__header-cell">수량</th>
                        <th class="data-table__header-cell">체결가</th>
                        <th class="data-table__header-cell">거래금액</th>
                        <th class="data-table__header-cell">상태</th>
                    </tr>
                    </thead>
                    <tbody class="data-table__body">
                    <c:choose>
                        <c:when test="${not empty tradeList}">
                            <c:forEach var="trade" items="${tradeList}">
                                <tr class="data-table__row">
                                    <td class="data-table__cell">T${trade.tradeId}</td>
                                    <td class="data-table__cell">
                                        <fmt:formatDate value="${trade.tradeDate}" pattern="yyyy-MM-dd HH:mm:ss" />
                                    </td>
                                    <td class="data-table__cell">${trade.memberNickname}</td>
                                    <td class="data-table__cell">
                                        <c:choose>
                                            <c:when test="${trade.tradeType eq 'buy'}">
                                                <span class="badge badge--buy">매수</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge--sell">매도</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="data-table__cell">${trade.stockName}</td>
                                    <td class="data-table__cell">${trade.quantity}주</td>
                                    <td class="data-table__cell">
                                        <fmt:formatNumber value="${trade.price}" pattern="#,###"/>원
                                    </td>
                                    <td class="data-table__cell">
                                        <fmt:formatNumber value="${trade.totalAmount}" pattern="#,###"/>원
                                    </td>
                                    <td class="data-table__cell">
                                        <c:choose>
                                            <c:when test="${trade.status eq 'settled'}">
                                                <span class="badge badge--settled">체결</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge--cancelled">취소</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr>
                                <td class="data-table__cell" colspan="9">등록된 거래 내역이 없습니다.</td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
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
<jsp:include page="/WEB-INF/fragments/admin/admin_layout_footer.jsp" />
