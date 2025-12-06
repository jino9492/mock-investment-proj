<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jps" uri="http://java.sun.com/jsp/jstl/core" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/logout/chart_logout.css"/>
<script src="${pageContext.request.contextPath}/static/js/logout/chart_logout.js"></script>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>주식 차트 · 주문</title>

</head>
<body class="stock-page">

<div class="stock-page__container">
    <!-- 상단 종목 정보 -->
    <header class="stock-header">
        <div class="stock-header__left">
            <div class="stock-header__logo"></div>
            <div class="stock-header__info">
                <div class="stock-header__name">기업A 000000</div>
                <div class="stock-header__price-row">
                    <span class="stock-header__price">000,000원</span>
                    <span class="stock-header__diff">
                        어제보다 <span class="stock-header__diff-value">+00,000원 (0.00%)</span>
                    </span>
                </div>
            </div>
        </div>
    </header>

    <!-- 상단 탭 -->
    <nav class="stock-tabs">
        <button class="stock-tabs__item stock-tabs__item--active">차트 · 호가</button>
        <a href="/stock_info" class="stock-tabs__item">종목정보</a>
        <button class="stock-tabs__item">뉴스 · 공시</button>
        <button class="stock-tabs__item">커뮤니티</button>
    </nav>

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
        <jsp:include page="/WEB-INF/fragments/stock/order_card_logout.jsp" />


        <!-- 내 주식 카드 -->
            <section class="my-stock-card">
                <h2 class="my-stock-card__title">내 주식</h2>

                <div class="my-stock-card__empty">
                    <p class="my-stock-card__empty-text">
                        내 주식을 보려면<br />
                        로그인이 필요해요
                    </p>
                    <button type="button" class="my-stock-card__login-btn">
                        로그인
                    </button>
                </div>
            </section>

    </main>
</div>

</body>
</html>
