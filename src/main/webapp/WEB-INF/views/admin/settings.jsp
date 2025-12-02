<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="시스템 설정" scope="request" />
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:useBean id="now" class="java.util.Date" />
<c:set var="pageStyles" value="admin/dashboard.css" scope="request" />
<jsp:include page="/WEB-INF/fragments/admin/admin_layout_head.jsp" />
<jsp:include page="/WEB-INF/fragments/admin/admin_layout_header.jsp" />
<main id="dashboard__content">
    <div class="dashboard__header">
        <h1>시스템 설정</h1>
        <p class="dashboard__date">
            <fmt:formatDate value="${now}" pattern="yyyy년 MM월 dd일 HH:mm:ss" />
        </p>
    </div>

    <!-- API 연동 상태 -->
    <div class="status-section">
        <h2 class="section-title">API 연동 상태</h2>
        <div class="status-card">
            <span>한국투자증권 API</span>
            <c:set var="apiStatus" value="${requestScope.apiStatus}" />
            <c:choose>
                <c:when test="${not empty apiStatus and apiStatus.connected}">
                    <span class="status-badge active">${apiStatus.status}</span>
                </c:when>
                <c:otherwise>
                    <span class="status-badge" style="background: #FFEBEE; color: #C62828;">${not empty apiStatus ? apiStatus.status : '오류'}</span>
                </c:otherwise>
            </c:choose>
        </div>
        <c:if test="${not empty apiStatus and apiStatus.responseTime > 0}">
            <div style="font-size: 12px; color: #666; margin-top: 8px; padding-left: 20px;">
                응답 시간: ${apiStatus.responseTime}ms
            </div>
        </c:if>
    </div>

    <!-- 서버 상태 -->
    <div class="status-section">
        <h2 class="section-title">서버 상태</h2>
        <div class="server-stats">
            <c:set var="serverStats" value="${requestScope.serverStats}" />
            <div class="server-stat">
                <span>CPU 사용률</span>
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${not empty serverStats ? serverStats.cpuUsage : 0}%"></div>
                </div>
                <span><fmt:formatNumber value="${not empty serverStats ? serverStats.cpuUsage : 0}" pattern="0.0"/>%</span>
            </div>
            <div class="server-stat">
                <span>메모리 사용률</span>
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${not empty serverStats ? serverStats.memoryUsage : 0}%"></div>
                </div>
                <span><fmt:formatNumber value="${not empty serverStats ? serverStats.memoryUsage : 0}" pattern="0.0"/>%</span>
            </div>
            <div class="server-stat">
                <span>디스크 사용률</span>
                <div class="progress-bar">
                    <div class="progress-fill" style="width: ${not empty serverStats ? serverStats.diskUsage : 0}%"></div>
                </div>
                <span><fmt:formatNumber value="${not empty serverStats ? serverStats.diskUsage : 0}" pattern="0.0"/>%</span>
            </div>
        </div>
    </div>

    <!-- 시스템 설정 -->
    <div class="system-settings">
        <h2 class="section-title">시스템 설정</h2>
        <div class="settings-list">
            <div class="setting-item">거래 제한 설정</div>
            <div class="setting-item">API 설정</div>
            <div class="setting-item">백업 관리</div>
        </div>
    </div>
</main>
<jsp:include page="/WEB-INF/fragments/admin/admin_layout_footer.jsp" />
