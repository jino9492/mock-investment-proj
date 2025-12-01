<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jps" uri="http://java.sun.com/jsp/jstl/core" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/stock/chart.css"/>
<script src="${pageContext.request.contextPath}/static/js/stock/chart.js"></script>

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
        <section class="order-card">
            <h2 class="order-card__title">주문하기</h2>

            <!-- 주문 타입 hidden (지정가: LIMIT / 시장가: MARKET) -->
            <input type="hidden"
                   name="orderType"
                   value="LIMIT"
                   class="order-card__order-type-hidden" />

            <!-- 구매 / 판매 / 대기 탭 -->
            <div class="order-card__trade-type">
                <button class="order-card__trade-tab order-card__trade-tab--active">구매</button>
                <button class="order-card__trade-tab">판매</button>
                <button class="order-card__trade-tab">대기</button>
            </div>

            <!-- 주문유형 -->
            <div class="order-card__row">
                <label class="order-card__label">주문유형</label>
                <div class="order-card__field">
                    <select class="order-card__select_box">
                        <option value="1" selected>일반주문</option>
                        <option value="2">조건주문</option>
                    </select>
                </div>
            </div>

            <!-- 구매가격 (지정가 / 시장가 pill 버튼) -->
            <div class="order-card__row">
                <label class="order-card__label">구매가격</label>
                <div class="order-card__field order-card__field--split">
                    <button class="order-card__pill order-card__pill--active">
                        지정가
                    </button>
                    <button class="order-card__pill">
                        시장가
                    </button>
                </div>
            </div>

            <!-- 가격 입력 -->
            <div class="order-card__row">
                <label class="order-card__label">&nbsp;</label>
                <div class="order-card__field order-card__field--price">
                    <div class="input-wrapper">
                        <input type="text"
                               class="order-card__input"
                               placeholder="가격을 입력하세요" />
                        <span class="unit-text">원</span>
                    </div>
                    <button class="order-card__step-btn">-/+</button>
                </div>
            </div>

            <!-- 수량 -->
            <div class="order-card__row">
                <label class="order-card__label">수량</label>
                <div class="order-card__field order-card__field--qty">
                    <input type="text" class="order-card__input" placeholder="최대 0주 가능" />
                    <button class="order-card__step-btn">-/+</button>
                </div>
            </div>

            <hr class="order-card__divider" />

            <!-- 금액 정보 -->
            <div class="order-card__summary-row">
                <span class="order-card__summary-label">구매가능 금액</span>
                <span class="order-card__summary-value">00,000원</span>
            </div>
            <div class="order-card__summary-row">
                <span class="order-card__summary-label">총 주문 금액</span>
                <span class="order-card__summary-value">원</span>
            </div>

            <!-- 버튼 -->
            <button class="order-card__submit-btn">구매하기</button>
        </section>


        <!-- 내 주식 카드 -->
            <section class="my-stock-card">
                <h2 class="my-stock-card__title">내 주식</h2>

                <div class="my-stock-card__row">
                    <span>총 수익</span>
                    <span class="my-stock-card__profit">+00,000</span>
                </div>
                <div class="my-stock-card__row">
                    <span>총 금액</span>
                    <span>000,000원</span>
                </div>
                <div class="my-stock-card__row">
                    <span>수량</span>
                    <span>0주</span>
                </div>
                <div class="my-stock-card__row">
                    <span>1주 평균 금액</span>
                    <span>000,000원</span>
                </div>
            </section>

    </main>
</div>

</body>
</html>
