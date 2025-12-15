<%-- ============================================================
     조건 주문 수량 입력 필드 Fragment
     
     사용법:
     - quantityInputId: 수량 입력 필드 ID (기본값: "conditional-order-quantity-input")
     - quantityMinusBtnId: 수량 감소 버튼 ID (기본값: "conditional-order-quantity-minus")
     - quantityPlusBtnId: 수량 증가 버튼 ID (기본값: "conditional-order-quantity-plus")
     - maxQuantity: 최대 수량 (기본값: 0, 상위 페이지에서 설정 가능)
     
     예시:
     <c:set var="quantityInputId" value="custom-quantity-input" scope="request" />
     <c:set var="maxQuantity" value="100" scope="request" />
     <%@ include file="order_conditional_quantity_fields.jsp" %>
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- 수량 필드 ID 파라미터 초기화 (기본값 설정) --%>
<c:set var="quantityInputId" value="${empty quantityInputId ? 'conditional-order-quantity-input' : quantityInputId}" scope="request" />
<c:set var="quantityMinusBtnId" value="${empty quantityMinusBtnId ? 'conditional-order-quantity-minus' : quantityMinusBtnId}" scope="request" />
<c:set var="quantityPlusBtnId" value="${empty quantityPlusBtnId ? 'conditional-order-quantity-plus' : quantityPlusBtnId}" scope="request" />
<c:set var="maxQuantity" value="${empty maxQuantity ? 0 : maxQuantity}" scope="request" />

<%-- 수량 입력 필드 --%>
<div class="order-card__row">
    <label class="order-card__label" for="${quantityInputId}">수량</label>
    <div class="order-card__field order-card__field--qty">
        <input type="text" 
               class="order-card__input" 
               id="${quantityInputId}" 
               placeholder="수량을 입력하세요"
               aria-label="주문 수량"
               <c:if test="${maxQuantity > 0}">data-max-quantity="${maxQuantity}"</c:if> />
        <div class="order-card__step-btn-group">
            <button type="button" 
                    class="order-card__step-btn order-card__step-btn--minus" 
                    id="${quantityMinusBtnId}"
                    aria-label="수량 감소">-</button>
            <div class="order-card__step-btn-divider"></div>
            <button type="button" 
                    class="order-card__step-btn order-card__step-btn--plus" 
                    id="${quantityPlusBtnId}"
                    aria-label="수량 증가">+</button>
        </div>
    </div>
</div>

