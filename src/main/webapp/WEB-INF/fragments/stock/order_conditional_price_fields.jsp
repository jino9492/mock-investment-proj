<%-- ============================================================
     조건 주문 가격 입력 필드 Fragment
     
     사용법:
     - priceLabel: 가격 필드 라벨 (기본값: "구매가격")
     - priceInputId: 가격 입력 필드 ID (기본값: "conditional-order-price-input")
     - priceMinusBtnId: 가격 감소 버튼 ID (기본값: "conditional-order-price-minus")
     - pricePlusBtnId: 가격 증가 버튼 ID (기본값: "conditional-order-price-plus")
     
     예시:
     <c:set var="priceLabel" value="판매가격" scope="request" />
     <c:set var="priceInputId" value="custom-price-input" scope="request" />
     <%@ include file="order_conditional_price_fields.jsp" %>
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- 가격 필드 ID 파라미터 초기화 (기본값 설정) --%>
<c:set var="priceInputId" value="${empty priceInputId ? 'conditional-order-price-input' : priceInputId}" scope="request" />
<c:set var="priceMinusBtnId" value="${empty priceMinusBtnId ? 'conditional-order-price-minus' : priceMinusBtnId}" scope="request" />
<c:set var="pricePlusBtnId" value="${empty pricePlusBtnId ? 'conditional-order-price-plus' : pricePlusBtnId}" scope="request" />
<c:set var="priceLabel" value="${empty priceLabel ? '구매가격' : priceLabel}" scope="request" />

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

<%-- 가격 모드 선택 (지정가 / 시장가) --%>
<div class="order-card__row">
    <label class="order-card__label" id="${priceInputId}-mode-label">${priceLabel}</label>
    <div class="order-card__field order-card__field--split" 
         role="radiogroup" 
         aria-required="true"
         aria-labelledby="${priceInputId}-mode-label">
        <button type="button" 
                class="order-card__pill order-card__pill--active" 
                role="radio" 
                aria-checked="true" 
                aria-label="지정가 선택"
                data-state="checked"
                data-price-mode="limit">지정가</button>
        <button type="button" 
                class="order-card__pill" 
                role="radio" 
                aria-checked="false" 
                aria-label="시장가 선택"
                data-state="unchecked"
                data-price-mode="market">시장가</button>
    </div>
</div>

<%-- 가격 입력 필드 --%>
<div class="order-card__row">
    <label class="order-card__label order-card__label--visually-hidden" for="${priceInputId}">${priceLabel}</label>
    <div class="order-card__field order-card__field--price">
        <div class="input-wrapper">
            <input type="text"
                   class="order-card__input"
                   id="${priceInputId}"
                   placeholder="가격을 입력하세요"
                   aria-label="${priceLabel}"
                   <c:if test="${not empty currentPrice}">data-current-price="${currentPrice}"</c:if> />
            <span class="unit-text">원</span>
        </div>
        <div class="order-card__step-btn-group">
            <button type="button" 
                    class="order-card__step-btn order-card__step-btn--minus" 
                    id="${priceMinusBtnId}"
                    aria-label="${priceLabel} 감소">-</button>
            <div class="order-card__step-btn-divider"></div>
            <button type="button" 
                    class="order-card__step-btn order-card__step-btn--plus" 
                    id="${pricePlusBtnId}"
                    aria-label="${priceLabel} 증가">+</button>
        </div>
    </div>
</div>

