<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jps" uri="http://java.sun.com/jsp/jstl/core" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/mypage/analysis.css"/>
<script src="${pageContext.request.contextPath}/static/js/mypage/analysis.js" defer></script>

<div class="analysis-page">
    <jsp:include page="/WEB-INF/fragments/common/sidebar_left/sidebar.jsp">
        <jsp:param name="menuItems" value="내 정보,주문내역,수익분석"/>
        <jsp:param name="menuLinks" value="/mypage,/mypage/orders,/mypage/analysis"/>
    </jsp:include>


    <!-- 메인 컨텐츠 -->
    <main class="analysis__content">
        <h1 class="analysis__title">수익 분석</h1>
        <div class="analysis__layout">
            <!-- 상단 필터 -->
            <div class="analysis__filters">
                <div class="analysis__pill-select" id="analysis-viewType">
                <button type="button" class="analysis__pill-selectBtn" id="analysis-viewTypeBtn">
                        <span id="analysis-viewTypeLabel">월</span>
                        <span class="analysis__chev" aria-hidden="true">▾</span>
                    </button>

                    <div class="analysis__pill-menu" id="analysis-viewTypeMenu" aria-hidden="true">
                        <button type="button" class="analysis__pill-item" data-view="day">일</button>
                        <button type="button" class="analysis__pill-item" data-view="week">주</button>
                        <button type="button" class="analysis__pill-item is-active" data-view="month">월</button>
                        <button type="button" class="analysis__pill-item" data-view="year">년</button>
                        <button type="button" class="analysis__pill-item" data-view="all">전체</button>
                    </div>
                </div>

                <button type="button" class="analysis__pill" id="analysis-dateBtn">
                    <span id="analysis-dateLabel"></span>
                </button>

                <!-- 숨김 input: 실제 값 저장용(원하면 서버로 넘기기 쉬움) -->
                <input type="month" id="analysis-monthInput" class="analysis__monthInput" />
                <input type="date" id="analysis-dateInput" class="analysis__monthInput" />

            </div>

            <!-- 요약 -->
            <div class="analysis__summary">
                <div class="analysis__summaryLabel">총 실현수익</div>

                <div class="analysis__summaryRow">
                    <div class="analysis__summaryMain" id="analysis-totalProfit">+-000,000원</div>
                    <div class="analysis__summarySub">
                        판매수익 <span id="analysis-sellProfit">+-000,000원</span>
                    </div>
                </div>
            </div>

            <!-- 테이블 -->
            <div class="analysis__table">
                <div class="analysis__thead">
                    <div class="analysis__th">판매일</div>
                    <div class="analysis__th">종목명</div>
                    <div class="analysis__th analysis__th--right">총 판매수익</div>
                    <div class="analysis__th analysis__th--right">수익률</div>
                    <div class="analysis__th analysis__th--right">총 판매금액</div>
                    <div class="analysis__th analysis__th--right">총 구매금액</div>
                    <div class="analysis__th analysis__th--right">판매수량</div>
                </div>

                <div class="analysis__tbody" id="analysis-tbody">
                    <!-- JS로 렌더링 -->
                </div>
            </div>
        </div>

    </main>


</div>

