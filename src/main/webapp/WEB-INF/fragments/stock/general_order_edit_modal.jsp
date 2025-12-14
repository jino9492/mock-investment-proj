<%-- ============================================================
     일반 주문 편집 모달 Fragment
    
     일반 주문을 편집하기 위한 모달 팝업입니다.
     order_card.jsp 위에 오버레이 형태로 표시됩니다.
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="order-modal order-modal--general" id="general-order-edit-modal" aria-hidden="true" role="dialog" aria-labelledby="general-order-modal-title">
    <div class="order-modal__overlay" data-modal-close></div>
    <div class="order-modal__dialog order-modal__dialog--center" data-side="center">
        <%-- 헤더 --%>
        <div class="order-modal__header order-modal__header--general">
            <div class="order-modal__header-left">
                <h3 class="order-modal__title order-modal__title--general" id="general-order-modal-title">
                    <span id="general-order-modal-trade-type"></span>
                    <span id="general-order-modal-quantity"></span>
                </h3>
                <p class="order-modal__subtitle" id="general-order-modal-price-per-share"></p>
            </div>
            <button type="button" class="order-modal__close-btn" data-modal-close aria-label="닫기">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M18 6L6 18M6 6L18 18" stroke="#6B7280" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            </button>
        </div>

        <div class="order-modal__header-spacer-bottom"></div>
        
        <%-- 본문 --%>
        <form class="order-modal__form" onsubmit="return false;">
            <div class="order-modal__form-content">
                <%-- 가격 입력 필드 (지정가/시장가 선택 포함) --%>
                <c:set var="priceLabel" value="가격" scope="request" />
                <c:set var="priceInputId" value="general-order-edit-price" scope="request" />
                <c:set var="priceMinusBtnId" value="general-order-edit-price-minus" scope="request" />
                <c:set var="pricePlusBtnId" value="general-order-edit-price-plus" scope="request" />
                <%@ include file="order_conditional_price_fields.jsp" %>

                <%-- 수량 입력 필드 --%>
                <c:set var="quantityInputId" value="general-order-edit-quantity" scope="request" />
                <c:set var="quantityMinusBtnId" value="general-order-edit-quantity-minus" scope="request" />
                <c:set var="quantityPlusBtnId" value="general-order-edit-quantity-plus" scope="request" />
                <%@ include file="order_conditional_quantity_fields.jsp" %>

                <div class="order-modal__form-spacer"></div>

                <%-- 총 주문 금액 --%>
                <div class="order-modal__summary-row">
                    <span class="order-modal__summary-label">총 주문 금액</span>
                    <span class="order-modal__summary-value" id="general-order-edit-total-amount">
                        <fmt:formatNumber value="0" pattern="#,###"/>원
                    </span>
                </div>
            </div>

            <%-- 하단 버튼 --%>
            <div class="order-modal__actions order-modal__actions--single">
                <button type="submit" 
                        class="order-modal__btn order-modal__btn--modify order-modal__btn--full"
                        id="general-order-edit-modify-btn">수정하기</button>
            </div>
        </form>
    </div>
</div>

