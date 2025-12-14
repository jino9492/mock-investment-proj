<%-- ============================================================
     조건 주문 감시 가격 입력 필드 Fragment
     
     사용법:
     - watchPriceInputId: 감시 가격 입력 필드 ID (기본값: "watch-price-input")
     - watchPriceInfoId: 감시 가격 정보 표시 영역 ID (기본값: "watch-price-info")
     - watchPriceMinusBtnId: 감시 가격 감소 버튼 ID (기본값: "watch-price-minus")
     - watchPricePlusBtnId: 감시 가격 증가 버튼 ID (기본값: "watch-price-plus")
     
     예시:
     <c:set var="watchPriceInputId" value="custom-watch-price-input" scope="request" />
     <%@ include file="order_conditional_watch_price_fields.jsp" %>
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- 감시 가격 필드 ID 파라미터 초기화 (기본값 설정) --%>
<c:set var="watchPriceInputId" value="${empty watchPriceInputId ? 'watch-price-input' : watchPriceInputId}" scope="request" />
<c:set var="watchPriceInfoId" value="${empty watchPriceInfoId ? 'watch-price-info' : watchPriceInfoId}" scope="request" />
<c:set var="watchPriceMinusBtnId" value="${empty watchPriceMinusBtnId ? 'watch-price-minus' : watchPriceMinusBtnId}" scope="request" />
<c:set var="watchPricePlusBtnId" value="${empty watchPricePlusBtnId ? 'watch-price-plus' : watchPricePlusBtnId}" scope="request" />

<%-- 현재가 추출 (상위 페이지에서 설정된 경우 사용) --%>
<c:choose>
    <c:when test="${not empty currentPrice}">
        <c:set var="currentPrice" value="${currentPrice}" scope="request" />
    </c:when>
    <c:when test="${not empty daily and not empty daily.closePrice}">
        <c:set var="currentPrice" value="${daily.closePrice.abs().intValue()}" scope="request" />
    </c:when>
    <c:otherwise>
        <c:set var="currentPrice" value="" scope="request" />
    </c:otherwise>
</c:choose>

<%-- 감시 가격 입력 필드 --%>
<div class="order-card__row order-card__watch-price">
    <label class="order-card__label" for="${watchPriceInputId}">감시 가격</label>
    <div class="order-card__field order-card__field--price">
        <div class="input-wrapper">
            <input type="text"
                   class="order-card__input"
                   id="${watchPriceInputId}"
                   placeholder="가격을 입력하세요"
                   aria-label="감시 가격"
                   <c:if test="${not empty currentPrice}">data-current-price="${currentPrice}"</c:if> />
            <span class="unit-text">원</span>
        </div>
        <div class="order-card__step-btn-group">
            <button type="button" 
                    class="order-card__step-btn order-card__step-btn--minus" 
                    id="${watchPriceMinusBtnId}"
                    aria-label="감시 가격 감소">-</button>
            <div class="order-card__step-btn-divider"></div>
            <button type="button" 
                    class="order-card__step-btn order-card__step-btn--plus" 
                    id="${watchPricePlusBtnId}"
                    aria-label="감시 가격 증가">+</button>
        </div>
    </div>
</div>

<%-- 감시 가격 정보 표시 영역 --%>
<div class="order-card__watch-price-info" 
     id="${watchPriceInfoId}"
     <c:if test="${not empty currentPrice}">data-current-price="${currentPrice}"</c:if>
     <c:if test="${not empty holding and not empty holding.averagePrice}">data-avg-price="${holding.averagePrice}"</c:if>>
    현재가 보다 0.00%
</div>

