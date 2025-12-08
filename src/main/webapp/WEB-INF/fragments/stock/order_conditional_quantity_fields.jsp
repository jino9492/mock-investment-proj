<%-- 
    조건 주문 수량 입력 필드 fragment
    사용법: quantityInputId, quantityMinusBtnId, quantityPlusBtnId 변수를 설정한 후 include
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- 수량 입력 필드 ID 파라미터 -->
<c:if test="${empty quantityInputId}">
    <c:set var="quantityInputId" value="conditional-order-quantity-input" scope="request" />
</c:if>
<c:if test="${empty quantityMinusBtnId}">
    <c:set var="quantityMinusBtnId" value="conditional-order-quantity-minus" scope="request" />
</c:if>
<c:if test="${empty quantityPlusBtnId}">
    <c:set var="quantityPlusBtnId" value="conditional-order-quantity-plus" scope="request" />
</c:if>
<c:if test="${empty maxQuantity}">
    <c:set var="maxQuantity" value="0" scope="request" />
</c:if>

<!-- 수량 -->
<div class="order-card__row">
    <label class="order-card__label" for="${quantityInputId}">수량</label>
    <div class="order-card__field order-card__field--qty">
        <input type="text" 
               class="order-card__input" 
               id="${quantityInputId}" 
               placeholder="수량을 입력하세요" />
        <div class="order-card__step-btn-group">
            <button type="button" class="order-card__step-btn order-card__step-btn--minus" id="${quantityMinusBtnId}">-</button>
            <div class="order-card__step-btn-divider"></div>
            <button type="button" class="order-card__step-btn order-card__step-btn--plus" id="${quantityPlusBtnId}">+</button>
        </div>
    </div>
</div>

