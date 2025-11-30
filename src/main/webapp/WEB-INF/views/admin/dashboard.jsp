<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="pageTitle" value="대시보드" scope="request" />
<c:set var="pageStyles" value="admin/dashboard.css" scope="request" />
<jsp:include page="/WEB-INF/fragments/admin/admin_layout_head.jsp" />
<jsp:include page="/WEB-INF/fragments/admin/admin_layout_header.jsp" />

<main id="dashboard__content">
    <div class="dashboard__header">
        <h1>대시보드</h1>
        <p class="dashboard__date">
            <fmt:formatDate value="<%= new java.util.Date() %>"
                            pattern="yyyy년 MM월 dd일 HH:mm:ss"/>
        </p>
    </div>

    <!-- 통계 카드 -->
    <div class="stats-grid">
        <div class="stat-card">
            <div class="stat-label">전체 회원 수</div>
            <div class="stat-value">
                <fmt:formatNumber value="${stats.totalUsers}" pattern="#,###"/>명
            </div>
            <div class="stat-change ${stats.userGrowthRate >= 0 ? 'positive' : 'negative'}">
                ${stats.userGrowthRate >= 0 ? '↑' : '↓'}
                전일 대비 <fmt:formatNumber value="${stats.userGrowthRate}" pattern="0.0"/>%
            </div>
        </div>

        <div class="stat-card">
            <div class="stat-label">당일 접속자 수</div>
            <div class="stat-value">
                <fmt:formatNumber value="${stats.todayVisitors}" pattern="#,###"/>명
            </div>
            <div class="stat-change positive">↓ 전일 대비 0.0%</div>
        </div>

        <div class="stat-card">
            <div class="stat-label">당일 거래량</div>
            <div class="stat-value">
                <fmt:formatNumber value="${stats.todayTrades}" pattern="#,###"/>건
            </div>
            <div class="stat-change positive">↓ 전일 대비 0.0%</div>
        </div>

        <div class="stat-card">
            <div class="stat-label">거래 금액</div>
            <div class="stat-value">
                <fmt:formatNumber value="${stats.todayAmount}" pattern="#,###"/>원
            </div>
            <div class="stat-change positive">↓ 전일 대비 0.0%</div>
        </div>
    </div>

    <!-- 그래프 섹션 -->
    <div class="chart-grid">
        <div>
            <h2 class="chart-title">시간대별 접속자 추이</h2>
            <div class="chart-card">
                <div class="chart-placeholder">
                    <span>선 그래프</span>
                </div>
            </div>
        </div>

        <div>
            <h2 class="chart-title">주간 거래량</h2>
            <div class="chart-card">
                <div class="chart-placeholder">
                    <span>막대 그래프</span>
                </div>
            </div>
        </div>

    </div>

    <!-- TOP 5 테이블 -->
    <div class="table-grid">
        <div>
            <h2 class="table-title">인기종목 TOP 5</h2>
            <div class="table-card">
                <table class="dashboard-table">
                    <thead>
                    <tr>
                        <th>종목명</th>
                        <th>거래건수</th>
                        <th>거래금액</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${topStocks}" var="stock">
                        <tr>
                            <td>${stock.stockName}</td>
                            <td><fmt:formatNumber value="${stock.tradeCount}" pattern="#,###"/></td>
                            <td><fmt:formatNumber value="${stock.tradeAmount}" pattern="#,###"/>원</td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty topStocks}">
                        <tr>
                            <td colspan="3" class="table-empty">데이터가 없습니다.</td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </div>


        <!-- 수익률 TOP 5 -->
        <div>
            <h2 class="table-title">수익률 TOP 5</h2>
            <div class="table-card">

                <table class="dashboard-table">
                    <thead>
                    <tr>
                        <th>순위</th>
                        <th>종목명</th>
                        <th>수익률</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${topReturnStocks}" var="stock" varStatus="status">
                        <tr>
                            <td>${status.count}</td>
                            <td>${stock.stockName}</td>
                            <td class="${stock.returnRate >= 0 ? 'positive' : 'negative'}">
                                    ${stock.returnRate >= 0 ? '+' : ''}
                                <fmt:formatNumber value="${stock.returnRate}" pattern="0.0"/>%
                            </td>
                        </tr>
                    </c:forEach>

                    <c:if test="${empty topReturnStocks}">
                        <tr>
                            <td colspan="3" class="table-empty">데이터가 없습니다.</td>
                        </tr>
                    </c:if>
                    </tbody>
                </table>
            </div>
        </div>

    </div>

    <!-- API 연동 상태 -->
    <div class="status-section">
        <h2 class="section-title">API 연동 상태</h2>
        <div class="status-card">
            <span>한국투자증권 API</span>
            <span class="status-badge active">정상</span>
        </div>
    </div>

    <!-- 서버 상태 -->
    <div class="status-section">
        <h2 class="section-title">서버 상태</h2>
        <div class="server-stats">
            <div class="server-stat">
                <span>CPU 사용률</span>
                <div class="progress-bar">
                    <div class="progress-fill" style="width: 0%"></div>
                </div>
                <span>0%</span>
            </div>
            <div class="server-stat">
                <span>메모리 사용률</span>
                <div class="progress-bar">
                    <div class="progress-fill" style="width: 0%"></div>
                </div>
                <span>0%</span>
            </div>
            <div class="server-stat">
                <span>디스크 사용률</span>
                <div class="progress-bar">
                    <div class="progress-fill" style="width: 0%"></div>
                </div>
                <span>0%</span>
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