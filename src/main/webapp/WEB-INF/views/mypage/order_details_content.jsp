<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jps" uri="http://java.sun.com/jsp/jstl/core" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/mypage/order_details.css"/>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/mypage/mypage.css"/>
<script src="${pageContext.request.contextPath}/static/js/mypage/order_details.js"></script>

<body>

<div class="my-page">
    <jsp:include page="/WEB-INF/fragments/common/sidebar_left/sidebar.jsp">
        <jsp:param name="menuItems" value="내 정보,자산,주문내역,수익분석"/>
        <jsp:param name="menuLinks" value="/mypage,/mypage/assets,/mypage/orders,/mypage/analysis"/>
    </jsp:include>

    <main class="my-page__content">
        <section class="my-page__section">
            <div class="my-page__section-header">
                <div class="my-page__title-wrap">
                    <div class="my-page__title-row">
                        <h1 class="my-page__title">자산</h1>
                    </div>
                </div>
            </div>

            <!-- 여기부터 주문내역 전용 레이아웃(왼쪽/오른쪽) -->
            <div class="order-page">
                <!-- 왼쪽 : 자산 / 주문 리스트 -->
                <section class="order-page__left">

                    <section class="order-section">
                    <header class="order-header">
                        <div class="order-tabs">
                            <button class="order-tabs__item order-tabs__item--active" data-tab="all">전체</button>
                            <button class="order-tabs__item" data-tab="condition">조건주문</button>
                        </div>
                    </header>


                        <div class="order-section__header">
                            <h2 class="order-section__title">대기중인 주문</h2>
                            <span class="order-section__count" id="pending-count">0건</span>
                        </div>
                        <ul class="order-list" id="pending-list">
                            <!-- JS에서 li 렌더링 -->
                        </ul>
                    </section>

                    <!-- 완료된 주문 -->
                    <section class="order-section order-section--completed">
                        <div class="order-section__header">
                            <h2 class="order-section__title">완료된 주문</h2>
                            <button class="order-section__filter">
                                <select id="completed-month-label" class="order-section__filter">
                                    <option value="ALL">전체</option>
                                </select>

                            </button>
                        </div>
                        <div id="completed-groups">
                            <!-- JS에서 날짜별 그룹 렌더링 -->
                        </div>
                    </section>
                </section>

                <!-- 오른쪽 : 조건구매 컴포넌트 -->
                <aside class="order-page__right">
                    <section id="condition-order-panel" class="condition-order-panel">
                        <!-- JS에서 조건구매 카드 렌더링 -->
                    </section>
                </aside>
            </div>
        </section>
    </main>
</div>