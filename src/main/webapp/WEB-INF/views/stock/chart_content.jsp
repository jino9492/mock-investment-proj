<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jps" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/stock/chart.css"/>
<script type="module" src="${pageContext.request.contextPath}/static/js/stock/chart.js"></script>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>주식 차트 · 주문</title>

</head>
<body class="stock-page">

<div class="stock-page__container">
    <!-- 상단 종목 정보 및 탭 -->
    <jsp:include page="/WEB-INF/fragments/stock/stock_header.jsp">
        <jsp:param name="activeTab" value="chart"/>
    </jsp:include>

    <!-- 메인 영역 -->
    <main class="stock-main">
        <!-- 왼쪽: 차트 & 시세 -->
        <section class="stock-main__left">

            <!-- 차트 박스 -->
            <section class="stock-chart-card">
                <!-- 기간 탭 -->
                <div class="stock-range-tabs">
                    <select class="stock-range-tabs__select">
                        <option value="1">1분</option>
                        <option value="3">3분</option>
                        <option value="5" selected>5분</option>
                        <option value="10">10분</option>
                        <option value="15">15분</option>
                        <option value="30">30분</option>
                        <option value="60">60분</option>
                    </select>
                    <button class="stock-range-tabs__item">일</button>
                    <button class="stock-range-tabs__item stock-range-tabs__item--active">주</button>
                    <button class="stock-range-tabs__item">월</button>
                    <button class="stock-range-tabs__item">년</button>
                    <button class="stock-range-tabs__item stock-range-tabs__item--icon">icon</button>
                </div>
                <div class="stock-chart-card__content">차트</div>
            </section>

            <!-- 시세 박스 -->
            <section class="stock-quote-card">
                <div class="stock-quote-card__content">시세</div>
            </section>
        </section>


        <!-- 주문하기 카드 -->
        <jsp:include page="/WEB-INF/fragments/stock/order_card.jsp" />


            <!-- 내 주식 카드 -->
            <section class="my-stock-card" 
                     <c:if test="${not empty stock and not empty stock.stockId}">
                     data-stock-id="${stock.stockId}"
                     </c:if>>
                <h2 class="my-stock-card__title">내 주식</h2>

                <c:choose>
                    <c:when test="${not empty holding}">
                        <div class="my-stock-card__row">
                            <span>총 수익</span>
                            <span class="my-stock-card__profit 
                                <c:choose>
                                    <c:when test="${holding.profitLoss > 0}">my-stock-card__profit--positive</c:when>
                                    <c:when test="${holding.profitLoss < 0}">my-stock-card__profit--negative</c:when>
                                    <c:otherwise>my-stock-card__profit--neutral</c:otherwise>
                                </c:choose>">
                                <c:set var="absProfitLoss" value="${holding.profitLoss >= 0 ? holding.profitLoss : -holding.profitLoss}" />
                                <fmt:formatNumber value="${absProfitLoss}" pattern="#,###"/>원
                                <c:if test="${holding.profitRate != 0}">
                                    (<fmt:formatNumber value="${holding.profitRate >= 0 ? holding.profitRate : -holding.profitRate}" pattern="#,##0.00"/>%)
                                </c:if>
                            </span>
                        </div>
                        <div class="my-stock-card__row">
                            <span>총 금액</span>
                            <span id="my-stock-evaluation-amount">
                                <c:set var="absEvaluationAmount" value="${holding.evaluationAmount >= 0 ? holding.evaluationAmount : -holding.evaluationAmount}" />
                                <fmt:formatNumber value="${absEvaluationAmount}" pattern="#,###"/>원
                            </span>
                        </div>
                        <div class="my-stock-card__row">
                            <span>수량</span>
                            <span id="my-stock-quantity">
                                <fmt:formatNumber value="${holding.quantity}" pattern="#,###"/>주
                            </span>
                        </div>
                        <div class="my-stock-card__row">
                            <span>1주 평균 금액</span>
                            <span id="my-stock-average-price">
                                <c:set var="averageAmount" value="${(holding.averagePrice - holding.currentPrice) * holding.quantity}" />
                                <fmt:formatNumber value="${averageAmount >= 0 ? averageAmount : -averageAmount}" pattern="#,###"/>원
                            </span>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="my-stock-card__row">
                            <span>총 수익</span>
                            <span class="my-stock-card__profit my-stock-card__profit--neutral">0</span>
                        </div>
                        <div class="my-stock-card__row">
                            <span>총 금액</span>
                            <span id="my-stock-evaluation-amount">0원</span>
                        </div>
                        <div class="my-stock-card__row">
                            <span>수량</span>
                            <span id="my-stock-quantity">0주</span>
                        </div>
                        <div class="my-stock-card__row">
                            <span>1주 평균 금액</span>
                            <span id="my-stock-average-price">0원</span>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>

    </main>
</div>

</body>
</html>
