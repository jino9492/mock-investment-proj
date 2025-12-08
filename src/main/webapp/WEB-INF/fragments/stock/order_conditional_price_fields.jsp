<%-- 
    조건 주문 가격 입력 필드 fragment
    사용법: priceLabel 변수를 설정한 후 include
    예: <c:set var="priceLabel" value="구매가격" />
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- 가격 입력 필드 ID 파라미터 -->
<c:if test="${empty priceInputId}">
    <c:set var="priceInputId" value="conditional-order-price-input" scope="request" />
</c:if>
<c:if test="${empty priceMinusBtnId}">
    <c:set var="priceMinusBtnId" value="conditional-order-price-minus" scope="request" />
</c:if>
<c:if test="${empty pricePlusBtnId}">
    <c:set var="pricePlusBtnId" value="conditional-order-price-plus" scope="request" />
</c:if>
<c:if test="${empty priceLabel}">
    <c:set var="priceLabel" value="구매가격" scope="request" />
</c:if>

<!-- ${priceLabel} (지정가 / 시장가 pill) -->
<div class="order-card__row">
    <label class="order-card__label">${priceLabel}</label>
    <div class="order-card__field order-card__field--split" role="radiogroup" aria-required="true">
        <button type="button" 
                class="order-card__pill order-card__pill--active" 
                role="radio" 
                aria-checked="true" 
                data-state="checked">지정가</button>
        <button type="button" 
                class="order-card__pill" 
                role="radio" 
                aria-checked="false" 
                data-state="unchecked">시장가</button>
    </div>
</div>

<!-- 가격 입력 -->
<div class="order-card__row">
    <label class="order-card__label" for="${priceInputId}" style="visibility: hidden;">${priceLabel}</label>
    <div class="order-card__field order-card__field--price">
        <div class="input-wrapper">
            <input type="text"
                   class="order-card__input"
                   id="${priceInputId}"
                   placeholder="가격을 입력하세요"
                   aria-label="${priceLabel}"
                   <c:if test="${not empty daily and not empty daily.closePrice}">
                   data-current-price="${daily.closePrice.intValue()}"
                   </c:if> />
            <span class="unit-text">원</span>
        </div>
        <div class="order-card__step-btn-group">
            <button type="button" class="order-card__step-btn order-card__step-btn--minus" id="${priceMinusBtnId}">-</button>
            <div class="order-card__step-btn-divider"></div>
            <button type="button" class="order-card__step-btn order-card__step-btn--plus" id="${pricePlusBtnId}">+</button>
        </div>
    </div>
</div>

