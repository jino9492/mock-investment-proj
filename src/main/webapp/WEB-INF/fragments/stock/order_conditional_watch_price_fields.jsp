<%-- 
    조건 주문 감시 가격 입력 필드 fragment
    사용법: watchPriceInputId, watchPriceInfoId, watchPriceMinusBtnId, watchPricePlusBtnId 변수를 설정한 후 include
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!-- 감시 가격 입력 필드 ID 파라미터 -->
<c:if test="${empty watchPriceInputId}">
    <c:set var="watchPriceInputId" value="watch-price-input" scope="request" />
</c:if>
<c:if test="${empty watchPriceInfoId}">
    <c:set var="watchPriceInfoId" value="watch-price-info" scope="request" />
</c:if>
<c:if test="${empty watchPriceMinusBtnId}">
    <c:set var="watchPriceMinusBtnId" value="watch-price-minus" scope="request" />
</c:if>
<c:if test="${empty watchPricePlusBtnId}">
    <c:set var="watchPricePlusBtnId" value="watch-price-plus" scope="request" />
</c:if>

<!-- 감시 가격 -->
<div class="order-card__row order-card__watch-price">
    <label class="order-card__label" for="${watchPriceInputId}">감시 가격</label>
    <div class="order-card__field order-card__field--price">
        <div class="input-wrapper">
            <input type="text"
                   class="order-card__input"
                   id="${watchPriceInputId}"
                   placeholder="가격을 입력하세요"
                   aria-label="감시 가격"
                   <c:if test="${not empty daily and not empty daily.closePrice}">
                   data-current-price="${daily.closePrice.abs().intValue()}"
                   </c:if> />
            <span class="unit-text">원</span>
        </div>
        <div class="order-card__step-btn-group">
            <button type="button" class="order-card__step-btn order-card__step-btn--minus" id="${watchPriceMinusBtnId}">-</button>
            <div class="order-card__step-btn-divider"></div>
            <button type="button" class="order-card__step-btn order-card__step-btn--plus" id="${watchPricePlusBtnId}">+</button>
        </div>
    </div>
</div>
<div class="order-card__watch-price-info-wrapper">
    <span class="order-card__watch-price-info" id="${watchPriceInfoId}-current"
         <c:if test="${not empty daily and not empty daily.closePrice}">
         data-current-price="${daily.closePrice.abs().intValue()}"
         </c:if>
         <c:if test="${not empty holding}">
         data-avg-price="${holding.averagePrice}"
         </c:if>>
        현재가 보다 0.00%
    </span>
    <span class="order-card__watch-price-info" id="${watchPriceInfoId}-avg"
         <c:if test="${not empty daily and not empty daily.closePrice}">
         data-current-price="${daily.closePrice.abs().intValue()}"
         </c:if>
         <c:if test="${not empty holding}">
         data-avg-price="${holding.averagePrice}"
         </c:if>>
        <c:if test="${not empty holding}">내 평단 보다 0.00%</c:if>
    </span>
</div>

