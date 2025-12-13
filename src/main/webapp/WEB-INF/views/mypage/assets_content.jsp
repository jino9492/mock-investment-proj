<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jps" uri="http://java.sun.com/jsp/jstl/core" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/mypage/assets.css"/>
<script src="${pageContext.request.contextPath}/static/js/mypage/assets.js" defer></script>

<div class="assets-page">
    <jsp:include page="/WEB-INF/fragments/common/sidebar_left/sidebar.jsp">
        <jsp:param name="menuItems" value="내 정보,자산,주문내역,수익분석"/>
        <jsp:param name="menuLinks" value="/mypage,/mypage/assets,/mypage/orders,/mypage/analysis"/>
    </jsp:include>


    <!-- 메인 컨텐츠 -->
    <main class="assets__content">
        <h1 class="assets__title">자산</h1>
        <div class="assets__layout">
            <!-- 상단 필터 -->
            <div class="assets__filters">
                <div class="assets__pill-select" id="assets-viewType">
                <button type="button" class="assets__pill-selectBtn" id="assets-viewTypeBtn">
                        <span id="assets-viewTypeLabel">월</span>
                        <span class="assets__chev" aria-hidden="true">▾</span>
                    </button>

                    <div class="assets__pill-menu" id="assets-viewTypeMenu" aria-hidden="true">
                        <button type="button" class="assets__pill-item is-active" data-view="month">월</button>
                        <button type="button" class="assets__pill-item" data-view="day">일</button>
                    </div>
                </div>

                <button type="button" class="assets__pill" id="assets-dateBtn">
                    <span id="assets-dateLabel">2000년 11월</span>
                </button>

                <!-- 숨김 input: 실제 값 저장용(원하면 서버로 넘기기 쉬움) -->
                <input type="month" id="assets-monthInput" class="assets__monthInput" value="2000-11" />
                <input type="date" id="assets-dateInput" class="assets__monthInput" value="2000-11-01" />

            </div>

            <!-- 요약 -->
            <div class="assets__summary">
                <div class="assets__summaryLabel">총 실현수익</div>

                <div class="assets__summaryRow">
                    <div class="assets__summaryMain" id="assets-totalProfit">+-000,000원</div>
                    <div class="assets__summarySub">
                        판매수익 <span id="assets-sellProfit">+-000,000원</span>
                    </div>
                </div>

                <div class="assets__tabs" role="tablist">
                    <button class="assets__tab is-active" type="button" data-tab="realized" role="tab" aria-selected="true">
                        총 실현수익
                    </button>
                    <button class="assets__tab" type="button" data-tab="group" role="tab" aria-selected="false">
                        일별 / 종목별 합계
                    </button>
                </div>
            </div>

            <!-- 테이블 -->
            <div class="assets__table">
                <div class="assets__thead">
                    <div class="assets__th">판매일</div>
                    <div class="assets__th">종목명</div>
                    <div class="assets__th">상세</div>
                    <div class="assets__th assets__th--right">총 판매수익</div>
                    <div class="assets__th assets__th--right">수익률</div>
                    <div class="assets__th assets__th--right">판매수량</div>
                </div>

                <div class="assets__tbody" id="assets-tbody">
                    <!-- JS로 렌더링 -->
                </div>
            </div>
        </div>

    </main>


</div>
