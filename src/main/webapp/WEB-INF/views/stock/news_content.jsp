<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jps" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="isLoggedIn" value="${not empty sessionScope.member}" />

<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/stock/order-card.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/stock/news.css"/>
<script src="${pageContext.request.contextPath}/static/js/stock/news.js"></script>
<script src="${pageContext.request.contextPath}/static/js/common/stock/order_card.js"></script>

<body class="stock-news-page">
<main class="stock-news__container">

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
        <a href="/stock_info" class="stock-tabs__item">종목정보</a>
        <a href="/stock_news" class="stock-tabs__item stock-tabs__item--active">뉴스 · 공시</a>
        <a href="#" class="stock-tabs__item">커뮤니티</a>
    </nav>

    <div class="stock-news__layout">
        <!-- 왼쪽 메인 영역 -->
        <div class="stock-news__main">

            <!-- 기업 뉴스 / 공시 영역 -->
            <section class="stock-news">

                <!-- 좌측 탭 -->
                <nav class="stock-news__nav">
                    <button
                            type="button"
                            class="stock-news__nav-item stock-news__nav-item--active"
                            data-target="stock-news-panel-news">
                        뉴스
                    </button>
                    <button
                            type="button"
                            class="stock-news__nav-item"
                            data-target="stock-news-panel-disclosure">
                        공시
                    </button>
                </nav>

                <!-- 우측 컨텐츠 -->
                <div class="stock-news__content">

                    <!-- 뉴스 패널 -->
                    <section class="stock-news__panel stock-news__panel--active"
                             id="stock-news-panel-news">
                        <header class="stock-news__header">
                            <h2 class="stock-news__title">기업A 뉴스</h2>

                            <div class="stock-news__sort-tabs">
                                <button type="button"
                                        class="stock-news__sort-btn stock-news__sort-btn--active">
                                    최신순
                                </button>
                                <button type="button" class="stock-news__sort-btn">
                                    관련도순
                                </button>
                            </div>
                        </header>

                        <ul class="stock-news__list">
                            <li class="stock-news__item">
                                <div class="stock-news__item-body">
                                    <a href="#" class="stock-news__item-title">
                                        이 대통령 주말에 재계 총수들 만난다… 한·미 관세 협상 후속 논의
                                    </a>
                                    <p class="stock-news__item-summary">
                                        이재용 삼성전자 회장, 최태원 SK그룹 회장 등 관련 내용 요약 텍스트...
                                    </p>
                                    <div class="stock-news__item-meta">
                                        <span>0시간 전</span>
                                        <span class="stock-news__item-source">· ○○뉴스</span>
                                    </div>
                                </div>
                                <div class="stock-news__thumbnail"></div>
                            </li>
                            <li class="stock-news__item">
                                <div class="stock-news__item-body">
                                    <a href="#" class="stock-news__item-title">
                                        이 대통령 주말에 재계 총수들 만난다… 한·미 관세 협상 후속 논의
                                    </a>
                                    <p class="stock-news__item-summary">
                                        이재용 삼성전자 회장, 최태원 SK그룹 회장 등 관련 내용 요약 텍스트...
                                    </p>
                                    <div class="stock-news__item-meta">
                                        <span>0시간 전</span>
                                        <span class="stock-news__item-source">· ○○뉴스</span>
                                    </div>
                                </div>
                                <div class="stock-news__thumbnail"></div>
                            </li>
                            <li class="stock-news__item">
                                <div class="stock-news__item-body">
                                    <a href="#" class="stock-news__item-title">
                                        이 대통령 주말에 재계 총수들 만난다… 한·미 관세 협상 후속 논의
                                    </a>
                                    <p class="stock-news__item-summary">
                                        이재용 삼성전자 회장, 최태원 SK그룹 회장 등 관련 내용 요약 텍스트...
                                    </p>
                                    <div class="stock-news__item-meta">
                                        <span>0시간 전</span>
                                        <span class="stock-news__item-source">· ○○뉴스</span>
                                    </div>
                                </div>
                                <div class="stock-news__thumbnail"></div>
                            </li>
                            <li class="stock-news__item">
                                <div class="stock-news__item-body">
                                    <a href="#" class="stock-news__item-title">
                                        이 대통령 주말에 재계 총수들 만난다… 한·미 관세 협상 후속 논의
                                    </a>
                                    <p class="stock-news__item-summary">
                                        이재용 삼성전자 회장, 최태원 SK그룹 회장 등 관련 내용 요약 텍스트...
                                    </p>
                                    <div class="stock-news__item-meta">
                                        <span>0시간 전</span>
                                        <span class="stock-news__item-source">· ○○뉴스</span>
                                    </div>
                                </div>
                                <div class="stock-news__thumbnail"></div>
                            </li>
                            <li class="stock-news__item">
                                <div class="stock-news__item-body">
                                    <a href="#" class="stock-news__item-title">
                                        이 대통령 주말에 재계 총수들 만난다… 한·미 관세 협상 후속 논의
                                    </a>
                                    <p class="stock-news__item-summary">
                                        이재용 삼성전자 회장, 최태원 SK그룹 회장 등 관련 내용 요약 텍스트...
                                    </p>
                                    <div class="stock-news__item-meta">
                                        <span>0시간 전</span>
                                        <span class="stock-news__item-source">· ○○뉴스</span>
                                    </div>
                                </div>
                                <div class="stock-news__thumbnail"></div>
                            </li>
                            <li class="stock-news__item">
                                <div class="stock-news__item-body">
                                    <a href="#" class="stock-news__item-title">
                                        이 대통령 주말에 재계 총수들 만난다… 한·미 관세 협상 후속 논의
                                    </a>
                                    <p class="stock-news__item-summary">
                                        이재용 삼성전자 회장, 최태원 SK그룹 회장 등 관련 내용 요약 텍스트...
                                    </p>
                                    <div class="stock-news__item-meta">
                                        <span>0시간 전</span>
                                        <span class="stock-news__item-source">· ○○뉴스</span>
                                    </div>
                                </div>
                                <div class="stock-news__thumbnail"></div>
                            </li>

                            <li class="stock-news__item">
                                <div class="stock-news__item-body">
                                    <a href="#" class="stock-news__item-title">
                                        이 대통령 주말에 재계 총수들 만난다… 한·미 관세 협상 후속 논의
                                    </a>
                                    <p class="stock-news__item-summary">
                                        이재용 삼성전자 회장, 최태원 SK그룹 회장 등 관련 내용 요약 텍스트...
                                    </p>
                                    <div class="stock-news__item-meta">
                                        <span>0시간 전</span>
                                        <span class="stock-news__item-source">· ○○뉴스</span>
                                    </div>
                                </div>
                                <div class="stock-news__thumbnail"></div>
                            </li>

                            <li class="stock-news__item">
                                <div class="stock-news__item-body">
                                    <a href="#" class="stock-news__item-title">
                                        이 대통령 주말에 재계 총수들 만난다… 한·미 관세 협상 후속 논의
                                    </a>
                                    <p class="stock-news__item-summary">
                                        이재용 삼성전자 회장, 최태원 SK그룹 회장 등 관련 내용 요약 텍스트...
                                    </p>
                                    <div class="stock-news__item-meta">
                                        <span>0시간 전</span>
                                        <span class="stock-news__item-source">· ○○뉴스</span>
                                    </div>
                                </div>
                                <div class="stock-news__thumbnail"></div>
                            </li>


                        </ul>
                        <div class="stock-news__pagination"></div>
                    </section>

                    <!-- 공시 패널 -->
                    <section class="stock-news__panel"
                             id="stock-news-panel-disclosure">
                        <header class="stock-news__header stock-news__header--disclosure">
                            <h2 class="stock-news__title">기업A 공시</h2>
                        </header>

                        <div class="stock-news__year">2025년</div>

                        <ul class="stock-disclosure__list">
                            <li class="stock-disclosure__item">
                                <div class="stock-disclosure__date">
                                    <span class="stock-disclosure__day">00월 00일</span>
                                </div>
                                <div class="stock-disclosure__body">
                                    <a href="#" class="stock-disclosure__title">
                                        2025년 9월 확정실적 발표
                                    </a>
                                    <p class="stock-disclosure__summary">
                                        영업이익 11조 3,833억원, 전년보다 61.9% 증가
                                    </p>
                                </div>
                            </li>

                            <li class="stock-disclosure__item">
                                <div class="stock-disclosure__date">
                                    <span class="stock-disclosure__day">00월 00일</span>
                                </div>
                                <div class="stock-disclosure__body">
                                    <a href="#" class="stock-disclosure__title">
                                        2025년 9월 확정실적 발표
                                    </a>
                                    <p class="stock-disclosure__summary">
                                        영업이익 11조 3,833억원, 전년보다 61.9% 증가
                                    </p>
                                </div>
                            </li>

                            <li class="stock-disclosure__item">
                                <div class="stock-disclosure__date">
                                    <span class="stock-disclosure__day">00월 00일</span>
                                </div>
                                <div class="stock-disclosure__body">
                                    <a href="#" class="stock-disclosure__title">
                                        2025년 9월 확정실적 발표
                                    </a>
                                    <p class="stock-disclosure__summary">
                                        영업이익 11조 3,833억원, 전년보다 61.9% 증가
                                    </p>
                                </div>
                            </li>

                            <li class="stock-disclosure__item">
                                <div class="stock-disclosure__date">
                                    <span class="stock-disclosure__day">00월 00일</span>
                                </div>
                                <div class="stock-disclosure__body">
                                    <a href="#" class="stock-disclosure__title">
                                        2025년 9월 확정실적 발표
                                    </a>
                                    <p class="stock-disclosure__summary">
                                        영업이익 11조 3,833억원, 전년보다 61.9% 증가
                                    </p>
                                </div>
                            </li>

                            <li class="stock-disclosure__item">
                                <div class="stock-disclosure__date">
                                    <span class="stock-disclosure__day">00월 00일</span>
                                </div>
                                <div class="stock-disclosure__body">
                                    <a href="#" class="stock-disclosure__title">
                                        2025년 9월 확정실적 발표
                                    </a>
                                    <p class="stock-disclosure__summary">
                                        영업이익 11조 3,833억원, 전년보다 61.9% 증가
                                    </p>
                                </div>
                            </li>

                            <li class="stock-disclosure__item">
                                <div class="stock-disclosure__date">
                                    <span class="stock-disclosure__day">00월 00일</span>
                                </div>
                                <div class="stock-disclosure__body">
                                    <a href="#" class="stock-disclosure__title">
                                        2025년 9월 확정실적 발표
                                    </a>
                                    <p class="stock-disclosure__summary">
                                        영업이익 11조 3,833억원, 전년보다 61.9% 증가
                                    </p>
                                </div>
                            </li>


                        </ul>
                        <div class="stock-disclosure__pagination"></div>
                    </section>

                </div>
            </section>

        </div>

        <!-- 오른쪽 주문 카드 사이드바 -->
        <aside class="stock-news__sidebar">
            <jsp:include page="/WEB-INF/fragments/stock/order_card.jsp" />
        </aside>
    </div>

</main>
</body>