<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jps" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/stock/info.css"/>
<script type="module" src="${pageContext.request.contextPath}/static/js/stock/info.js"></script>

<body class="stock-info-page">
<main class="stock-info__container">

    <!-- 상단 종목 정보 및 탭 -->
    <jsp:include page="/WEB-INF/fragments/stock/stock_header.jsp">
        <jsp:param name="activeTab" value="info"/>
    </jsp:include>

    <div class="stock-info__layout">
        <!-- 왼쪽 메인 영역 -->
        <div class="stock-info__main">

            <!-- 상단 기업 정보 헤더 -->
            <header class="stock-info__header">
                <div class="stock-info__header-left">
                    <h1 class="stock-info__header-title">${stock.getStockName()}</h1>
                    <span class="stock-info__header-meta">${stock.getStockSymbol()} · ${stock.getMarketType() == 0 ? "코스피" : "코스닥"}</span>
                </div>
                <div class="stock-info__header-update">! 매 주 1회 업데이트</div>
            </header>

            <!-- 기본 기업 정보 영역 -->
            <section class="stock-info__basic">

                <!-- 1행 -->
                <div class="stock-info__basic-row">

                    <!-- 왼쪽 그룹 -->
                    <div class="stock-info__basic-group">
                        <div class="stock-info__basic-cell stock-info__basic-cell--label">기업명</div>
                        <div class="stock-info__basic-cell stock-info__basic-cell--align-right">
                            <div class="stock-info__basic-value">${stock.getStockName()}</div>
                        </div>
                    </div>

                    <!-- 오른쪽 그룹 -->
                    <div class="stock-info__basic-group">
                        <div class="stock-info__basic-cell stock-info__basic-cell--label">시가총액</div>
                        <div class="stock-info__basic-cell stock-info__basic-cell--align-right">
                            <div class="stock-info__basic-value"><fmt:formatNumber value="${detail.getMarketCap()}" pattern="#,###" />억 원</div>
                        </div>
                    </div>

                </div>

                <!-- 2행 -->
                <div class="stock-info__basic-row">

                    <div class="stock-info__basic-group">
                        <div class="stock-info__basic-cell stock-info__basic-cell--label">권면액</div>
                        <div class="stock-info__basic-cell stock-info__basic-cell--align-right">
                            <div class="stock-info__basic-value"><fmt:formatNumber value="${detail.getFaceValue()}" pattern="#,###" />원</div>
                        </div>
                    </div>

                    <div class="stock-info__basic-group">
                        <div class="stock-info__basic-cell stock-info__basic-cell--label">결산월</div>
                        <div class="stock-info__basic-cell stock-info__basic-cell--align-right">
                            <div class="stock-info__basic-value">${detail.getSettlementMonth()}월</div>
                        </div>
                    </div>

                </div>

                <!-- 3행 -->
                <div class="stock-info__basic-row">

                    <div class="stock-info__basic-group">
                        <div class="stock-info__basic-cell stock-info__basic-cell--label">상장일</div>
                        <div class="stock-info__basic-cell stock-info__basic-cell--align-right">
                            <div class="stock-info__basic-value">
                                ${detail.getListingDate()}
                            </div>
                        </div>
                    </div>

                    <div class="stock-info__basic-group">
                        <div class="stock-info__basic-cell stock-info__basic-cell--label">현재 상태</div>
                        <div class="stock-info__basic-cell stock-info__basic-cell--align-right">
                            <div class="stock-info__basic-value">
                                ${stock.getStatus()}
<%--                                <div class="stock-info__basic-subtext">25년 11월 30일 기준</div>--%>
                            </div>
                        </div>
                    </div>

                </div>

            </section>


            <!-- 주요 사업 -->
<%--            <section class="stock-info-section">--%>
<%--                <h2 class="stock-info-section__title">주요 사업</h2>--%>
<%--                <div class="stock-info__business-card"></div>--%>
<%--            </section>--%>

            <!-- 투자 지표 및 재무 상태 -->
            <section class="stock-info-section">
                <h2 class="stock-info-section__title">투자지표 및 재무상태</h2>

                <div class="stock-info__metrics">
                    <!-- 왼쪽 카드 -->
                    <div class="stock-info__metrics-card stock-info__metrics-card--half">
                        <div class="stock-info__metrics-row">
                            <span class="stock-info__metrics-label">PER</span>
                            <span class="stock-info__metrics-value">${indicator.getPer()}배</span>
                        </div>
                        <div class="stock-info__metrics-row">
                            <span class="stock-info__metrics-label">PBR</span>
                            <span class="stock-info__metrics-value">${indicator.getPbr()}배</span>
                        </div>
                    </div>

                    <!-- 오른쪽 카드 -->
                    <div class="stock-info__metrics-card stock-info__metrics-card--half">
                        <div class="stock-info__metrics-row">
                            <span class="stock-info__metrics-label">ROE</span>
                            <span class="stock-info__metrics-value">${indicator.getRoe()}%</span>
                        </div>
                        <div class="stock-info__metrics-row">
                            <span class="stock-info__metrics-label">EPS</span>
                            <span class="stock-info__metrics-value"><fmt:formatNumber value="${indicator.getEps().intValue()}" pattern="#,###" />원</span>
                        </div>
                        <div class="stock-info__metrics-row stock-info__metrics-row--last">
                            <span class="stock-info__metrics-label">BPS</span>
                            <span class="stock-info__metrics-value"><fmt:formatNumber value="${indicator.getBps().intValue()}" pattern="#,###" />원</span>
                        </div>
                    </div>
                </div>

                <!-- 하단 재무 상태 카드 -->
                <div class="stock-info__finance-card">
                    <div class="stock-info__finance-row">
                        <span class="stock-info__finance-label">매출액</span>
                        <span class="stock-info__finance-value"><fmt:formatNumber value="${summary.getRevenue().intValue()}" pattern="#,###" />억 원</span>
                    </div>
                    <div class="stock-info__finance-row">
                        <span class="stock-info__finance-label">영업이익</span>
                        <span class="stock-info__finance-value"><fmt:formatNumber value="${summary.getOperatingProfit().intValue()}" pattern="#,###" />억 원</span>
                    </div>
                    <div class="stock-info__finance-row">
                        <span class="stock-info__finance-label">신용비율</span>
                        <span class="stock-info__finance-value">${summary.getCreditRatio()}</span>
                    </div>
<%--                    <div class="stock-info__finance-row stock-info__finance-row--last">--%>
<%--                        <span class="stock-info__finance-label">잔고율</span>--%>
<%--                        <span class="stock-info__finance-value">0.00</span>--%>
<%--                    </div>--%>
                </div>

            </section>
        </div>

        <!-- 오른쪽 주문 카드 사이드바 -->
        <aside class="stock-info__sidebar">
            <jsp:include page="/WEB-INF/fragments/stock/order_card.jsp" />
        </aside>
    </div>

</main>
</body>