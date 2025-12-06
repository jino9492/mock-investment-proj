<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jps" uri="http://java.sun.com/jsp/jstl/core" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/logout/info_logout.css"/>
<script src="${pageContext.request.contextPath}/static/js/logout/info_logout.js"></script>

<body class="stock-info-page">
<main class="stock-info__container">

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
        <a href="/stock" class="stock-tabs__item">차트 · 호가</a>
        <button class="stock-tabs__item stock-tabs__item--active">종목정보</button>
        <button class="stock-tabs__item">뉴스 · 공시</button>
        <button class="stock-tabs__item">커뮤니티</button>
    </nav>

    <div class="stock-info__layout">
        <!-- 왼쪽 메인 영역 -->
        <div class="stock-info__main">

            <!-- 상단 기업 정보 헤더 -->
            <header class="stock-info__header">
                <div class="stock-info__header-left">
                    <h1 class="stock-info__header-title">기업A</h1>
                    <span class="stock-info__header-meta">000000 · 코스피/코스닥</span>
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
                            <div class="stock-info__basic-value">기업A</div>
                        </div>
                    </div>

                    <!-- 오른쪽 그룹 -->
                    <div class="stock-info__basic-group">
                        <div class="stock-info__basic-cell stock-info__basic-cell--label">시가총액</div>
                        <div class="stock-info__basic-cell stock-info__basic-cell--align-right">
                            <div class="stock-info__basic-value">000조 0,000억원</div>
                        </div>
                    </div>

                </div>

                <!-- 2행 -->
                <div class="stock-info__basic-row">

                    <div class="stock-info__basic-group">
                        <div class="stock-info__basic-cell stock-info__basic-cell--label">권면액</div>
                        <div class="stock-info__basic-cell stock-info__basic-cell--align-right">
                            <div class="stock-info__basic-value">000원</div>
                        </div>
                    </div>

                    <div class="stock-info__basic-group">
                        <div class="stock-info__basic-cell stock-info__basic-cell--label">결산월</div>
                        <div class="stock-info__basic-cell stock-info__basic-cell--align-right">
                            <div class="stock-info__basic-value">00월</div>
                        </div>
                    </div>

                </div>

                <!-- 3행 -->
                <div class="stock-info__basic-row">

                    <div class="stock-info__basic-group">
                        <div class="stock-info__basic-cell stock-info__basic-cell--label">상장일</div>
                        <div class="stock-info__basic-cell stock-info__basic-cell--align-right">
                            <div class="stock-info__basic-value">
                                0000년 00월 00일
                                <div class="stock-info__basic-subtext">0000년 설립</div>
                            </div>
                        </div>
                    </div>

                    <div class="stock-info__basic-group">
                        <div class="stock-info__basic-cell stock-info__basic-cell--label">발행주식수</div>
                        <div class="stock-info__basic-cell stock-info__basic-cell--align-right">
                            <div class="stock-info__basic-value">
                                000,000,000주
                                <div class="stock-info__basic-subtext">25년 11월 30일 기준</div>
                            </div>
                        </div>
                    </div>

                </div>

            </section>


            <!-- 주요 사업 -->
            <section class="stock-info-section">
                <h2 class="stock-info-section__title">주요 사업</h2>
                <div class="stock-info__business-card"></div>
            </section>

            <!-- 투자 지표 및 재무 상태 -->
            <section class="stock-info-section">
                <h2 class="stock-info-section__title">투자 지표 및 재무 상태</h2>

                <div class="stock-info__metrics">
                    <!-- 왼쪽 카드 -->
                    <div class="stock-info__metrics-card stock-info__metrics-card--half">
                        <div class="stock-info__metrics-row">
                            <span class="stock-info__metrics-label">PER</span>
                            <span class="stock-info__metrics-value">기업A</span>
                        </div>
                        <div class="stock-info__metrics-row">
                            <span class="stock-info__metrics-label">PBR</span>
                            <span class="stock-info__metrics-value">000원</span>
                        </div>
                    </div>

                    <!-- 오른쪽 카드 -->
                    <div class="stock-info__metrics-card stock-info__metrics-card--half">
                        <div class="stock-info__metrics-row">
                            <span class="stock-info__metrics-label">ROE</span>
                            <span class="stock-info__metrics-value">000조 0,000억원</span>
                        </div>
                        <div class="stock-info__metrics-row">
                            <span class="stock-info__metrics-label">EPS</span>
                            <span class="stock-info__metrics-value">00월</span>
                        </div>
                        <div class="stock-info__metrics-row stock-info__metrics-row--last">
                            <span class="stock-info__metrics-label">BPS</span>
                            <span class="stock-info__metrics-value">000,000,000주</span>
                        </div>
                    </div>
                </div>

                <!-- 하단 재무 상태 카드 -->
                <div class="stock-info__finance-card">
                    <div class="stock-info__finance-row">
                        <span class="stock-info__finance-label">매출액</span>
                        <span class="stock-info__finance-value">0,000원</span>
                    </div>
                    <div class="stock-info__finance-row">
                        <span class="stock-info__finance-label">영업이익</span>
                        <span class="stock-info__finance-value">+/-0000</span>
                    </div>
                    <div class="stock-info__finance-row">
                        <span class="stock-info__finance-label">신용비율</span>
                        <span class="stock-info__finance-value">0.00</span>
                    </div>
                    <div class="stock-info__finance-row stock-info__finance-row--last">
                        <span class="stock-info__finance-label">잔고율</span>
                        <span class="stock-info__finance-value">0.00</span>
                    </div>
                </div>

            </section>
        </div>

        <!-- 오른쪽 주문 카드 사이드바 -->
        <aside class="stock-info__sidebar">
            <jsp:include page="/WEB-INF/fragments/stock/order_card_logout.jsp" />
        </aside>
    </div>

</main>
</body>