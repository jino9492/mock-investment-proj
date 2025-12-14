<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/mypage/order_details.css"/>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/mypage/mypage.css"/>

<div class="my-page">
    <jsp:include page="/WEB-INF/fragments/common/sidebar_left/sidebar.jsp">
        <jsp:param name="menuItems" value="내 정보,주문내역,수익분석"/>
        <jsp:param name="menuLinks" value="/mypage,/mypage/orders,/mypage/analysis"/>
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

<!-- 데이터를 안전하게 전달 (서버에서 생성한 JSON 사용) -->
<script type="application/json" id="pending-orders-json-data">
[
<c:if test="${not empty pendingOrdersJson}">
<c:forEach var="orderJson" items="${pendingOrdersJson}" varStatus="status">
<c:out value="${orderJson}" escapeXml="false"/><c:if test="${!status.last}">,</c:if>
</c:forEach>
</c:if>
]
</script>

<script type="application/json" id="completed-orders-json-data">
[
<c:if test="${not empty completedOrdersJson}">
<c:forEach var="orderJson" items="${completedOrdersJson}" varStatus="status">
<c:out value="${orderJson}" escapeXml="false"/><c:if test="${!status.last}">,</c:if>
</c:forEach>
</c:if>
]
</script>

<script>
    const BACKEND_PENDING_ORDERS = [];
    const BACKEND_COMPLETED_ORDERS = [];
    
    try {
        const pendingDataEl = document.getElementById('pending-orders-json-data');
        if (pendingDataEl && pendingDataEl.textContent.trim()) {
            const pendingData = JSON.parse(pendingDataEl.textContent);
            BACKEND_PENDING_ORDERS.push(...pendingData);
        }
    } catch (e) {
        console.error('대기 주문 데이터 파싱 오류:', e);
    }
    
    try {
        const completedDataEl = document.getElementById('completed-orders-json-data');
        if (completedDataEl && completedDataEl.textContent.trim()) {
            const completedData = JSON.parse(completedDataEl.textContent);
            BACKEND_COMPLETED_ORDERS.push(...completedData);
        }
    } catch (e) {
        console.error('완료 주문 데이터 파싱 오류:', e);
    }
</script>

<script>
    // 백엔드 데이터를 프론트엔드 형식으로 변환
    try {
        BACKEND_PENDING_ORDERS.forEach(order => {
            const side = order.side || 'BUY';
            const typeLabel = (order.hasCondition ? '조건 ' : '') + (side === 'BUY' ? '구매' : '판매');
            const sideLabel = side === 'BUY' ? '구매' : '판매';
            const priceText = order.price != null ? new Intl.NumberFormat('ko-KR').format(order.price) + '원' : '시장가';
            const sharesLabel = new Intl.NumberFormat('ko-KR').format(order.quantity) + '주';
            
            let createdAt = '-';
            if (order.placedAt) {
                try {
                    const date = new Date(order.placedAt);
                    if (!isNaN(date.getTime())) {
                        createdAt = (date.getMonth() + 1) + '.' + date.getDate();
                    }
                } catch (e) {}
            }
            
            let condition = null;
            if (order.hasCondition && order.condition) {
                let validUntil = '무기한';
                if (order.condition.validUntil) {
                    try {
                        const date = new Date(order.condition.validUntil);
                        if (!isNaN(date.getTime())) {
                            validUntil = (date.getMonth() + 1) + '월 ' + date.getDate() + '일까지';
                        }
                    } catch (e) {}
                }
                
                let orderPrice = '시장가';
                if (order.price != null) {
                    orderPrice = '지정가 ' + new Intl.NumberFormat('ko-KR').format(order.price) + '원';
                }
                
                condition = {
                    profitCondition: side === 'BUY' ? '가격 하락' : '가격 상승',
                    orderPrice: orderPrice,
                    orderQty: sharesLabel,
                    validUntil: validUntil,
                    trigger: '정규장일 때만'
                };
            }
            
            // 원본 데이터에 프론트엔드 형식 추가
            order.id = String(order.orderId);
            order.status = '대기';
            order.corpName = order.stockName || '';
            order.typeLabel = typeLabel;
            order.sharesLabel = sharesLabel;
            order.priceText = priceText;
            order.sideLabel = sideLabel;
            order.createdAt = createdAt;
            order.condition = condition;
        });
        
        BACKEND_COMPLETED_ORDERS.forEach(order => {
            const side = order.side || 'BUY';
            const status = order.status || 'FILLED';
            const sideLabel = side === 'BUY' ? '구매' : '판매';
            let typeLabel = '';
            if (status === 'FILLED') {
                typeLabel = side === 'BUY' ? '구매체결' : '판매체결';
            } else if (status === 'CANCELLED') {
                typeLabel = side === 'BUY' ? '구매취소' : '판매취소';
            } else {
                typeLabel = status;
            }
            
            let priceText = '-';
            if (status !== 'CANCELLED' && order.price != null) {
                priceText = new Intl.NumberFormat('ko-KR').format(order.price) + '원';
            }
            
            const sharesLabel = new Intl.NumberFormat('ko-KR').format(order.quantity) + '주';
            
            let date = '-';
            const dateStr = order.cancelledAt || order.placedAt;
            if (dateStr) {
                try {
                    const dateObj = new Date(dateStr);
                    if (!isNaN(dateObj.getTime())) {
                        date = (dateObj.getMonth() + 1) + '.' + dateObj.getDate();
                    }
                } catch (e) {}
            }
            
            // 원본 데이터에 프론트엔드 형식 추가
            order.id = String(order.orderId);
            order.date = date;
            order.corpName = order.stockName || '';
            order.typeLabel = typeLabel;
            order.sharesLabel = sharesLabel;
            order.priceText = priceText;
            order.sideLabel = sideLabel;
            order.hasCondition = false;
        });
    } catch (e) {
        console.error('데이터 변환 오류:', e);
    }
</script>
<script src="${pageContext.request.contextPath}/static/js/mypage/order_details.js"></script>
