<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:useBean id="now" class="java.util.Date" />
<c:set var="adminList" value="${requestScope.adminList}" />
<c:set var="pageTitle" value="관리자 관리" scope="request" />
<c:set var="pageStyles" value="admin/users.css,admin/member_table.css" scope="request" />
<jsp:include page="/WEB-INF/fragments/admin/admin_layout_head.jsp" />
<jsp:include page="/WEB-INF/fragments/admin/admin_layout_header.jsp" />
<main class="users-page">
    <div class="page-header">
        <h1 class="page-header__title">관리자 관리</h1>
        <p class="page-header__date" id="current-time">
            <fmt:formatDate value="${now}" pattern="yyyy년 MM월 dd일 HH:mm:ss" />
        </p>
    </div>
    <jsp:include page="/WEB-INF/fragments/admin/admin_status_alert.jsp" />

    <!-- 통계 카드 -->
    <section class="stat-grid stat-grid--col-2">
        <article class="stat-card">
            <div class="stat-card__label">전체 관리자 수</div>
            <div class="stat-card__value">
                <fmt:formatNumber value="${not empty adminList ? adminList.size() : 0}" pattern="#,###"/>명
            </div>
        </article>
    </section>

    <!-- 관리자 목록 테이블 -->
    <section class="table-section">
        <div class="table-header">
            <h2 class="table-header__title">관리자 목록</h2>
            <a href="${pageContext.request.contextPath}/admin/admins/form" class="table-header__add-btn--active">+</a>
        </div>

        <div class="table-wrapper">
            <table class="data-table">
                <thead class="data-table__head">
                    <tr>
                        <th class="data-table__header-cell">관리자 ID</th>
                        <th class="data-table__header-cell">로그인 아이디</th>
                        <th class="data-table__header-cell">관리자명</th>
                        <th class="data-table__header-cell">마지막 로그인</th>
                        <th class="data-table__header-cell">마지막 로그인 IP</th>
                        <th class="data-table__header-cell">생성일</th>
                        <th class="data-table__header-cell">작업</th>
                    </tr>
                </thead>
                <tbody class="data-table__body">
                    <c:choose>
                        <c:when test="${not empty adminList}">
                            <c:forEach var="admin" items="${adminList}">
                                <tr class="data-table__row">
                                    <td class="data-table__cell">${admin.adminId}</td>
                                    <td class="data-table__cell">${admin.adminUsername}</td>
                                    <td class="data-table__cell">${admin.adminName}</td>
                                    <td class="data-table__cell">
                                        <c:choose>
                                            <c:when test="${not empty admin.lastLoginAt}">
                                                <fmt:formatDate value="${admin.lastLoginAt}" pattern="yyyy-MM-dd HH:mm:ss" />
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="data-table__cell">
                                        <c:choose>
                                            <c:when test="${not empty admin.lastLoginIp}">
                                                <c:choose>
                                                    <c:when test="${admin.lastLoginIp eq '0:0:0:0:0:0:0:1' or admin.lastLoginIp eq '::1' or admin.lastLoginIp eq '127.0.0.1'}">
                                                        로컬
                                                    </c:when>
                                                    <c:otherwise>
                                                        ${admin.lastLoginIp}
                                                    </c:otherwise>
                                                </c:choose>
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="data-table__cell">
                                        <c:choose>
                                            <c:when test="${not empty admin.createdAt}">
                                                <fmt:formatDate value="${admin.createdAt}" pattern="yyyy-MM-dd HH:mm:ss" />
                                            </c:when>
                                            <c:otherwise>-</c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="data-table__cell">
                                        <a href="${pageContext.request.contextPath}/admin/admins/form?adminId=${admin.adminId}" 
                                           class="data-table__detail-link">수정</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <tr class="data-table__row">
                                <td colspan="7" class="data-table__cell" style="text-align: center; padding: 40px; color: #999;">
                                    등록된 관리자가 없습니다.
                                </td>
                            </tr>
                        </c:otherwise>
                    </c:choose>
                </tbody>
            </table>
        </div>
    </section>
</main>
<jsp:include page="/WEB-INF/fragments/admin/admin_layout_footer.jsp" />

