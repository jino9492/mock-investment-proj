<%-- ============================================================
     조건 주문 편집 모달 Fragment
    
     조건 주문을 편집하기 위한 모달 팝업입니다.
     order_card.jsp 바깥 좌측에 오버레이 형태로 표시됩니다.
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="order-modal" id="conditional-order-edit-modal" aria-hidden="true" role="dialog" aria-labelledby="conditional-order-modal-title">
    <div class="order-modal__overlay" data-modal-close></div>
    <div class="order-modal__dialog" data-side="left">
        <%-- 헤더 --%>
        <div class="order-modal__header">
            <button type="button" class="order-modal__back-btn" data-modal-close aria-label="뒤로가기">
                <svg enable-background="new 0 0 24 24" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" width="24" height="24">
                    <path clip-rule="evenodd" d="m13.541 17.45c.2 0 .5-.1.6-.3.4-.4.4-.9 0-1.3l-3.8-3.8 3.9-3.9c.4-.4.4-.9 0-1.3s-.9-.4-1.3 0l-4.5 4.5c-.4.4-.4.9 0 1.3l4.5 4.5c.2.1.4.3.6.3" fill="#b0b8c1" fill-rule="evenodd"></path>
                </svg>
            </button>
            <h3 class="order-modal__title" id="conditional-order-modal-title">
                <span id="conditional-order-modal-stock-name"></span>
                <span id="conditional-order-modal-trade-type"></span>
            </h3>
            <div class="order-modal__header-spacer"></div>
        </div>

        <div class="order-modal__header-spacer-bottom"></div>
        
        <%-- 본문 --%>
        <form class="order-modal__form" onsubmit="return false;">
            <div class="order-modal__form-content">
                <%-- 주문 켜기 토글 --%>
                <div class="order-modal__toggle-row">
                    <span class="order-modal__label">주문 켜기</span>
                    <div class="order-modal__toggle-wrapper">
                        <input type="hidden" name="willActivate" value="true">
                        <button type="button" 
                                class="order-modal__toggle"
                                id="conditional-order-edit-toggle"
                                data-is-active="true"
                                aria-label="조건 주문 켬"
                                aria-pressed="true">
                            <span class="order-modal__toggle-handler"></span>
                        </button>
                    <input type="checkbox" 
                               id="conditional-order-edit-toggle-checkbox"
                               class="order-modal__toggle-checkbox"
                               checked
                               aria-hidden="true"
                               tabindex="-1">
                </div>
            </div>

                <%-- 감시 가격 입력 필드 --%>
                <c:set var="watchPriceInputId" value="conditional-order-edit-watch-price" scope="request" />
                <c:set var="watchPriceInfoId" value="conditional-order-edit-watch-price-info" scope="request" />
                <c:set var="watchPriceMinusBtnId" value="conditional-order-edit-watch-price-minus" scope="request" />
                <c:set var="watchPricePlusBtnId" value="conditional-order-edit-watch-price-plus" scope="request" />
                <%@ include file="order_conditional_watch_price_fields.jsp" %>
                
                <%-- 주문 가격 입력 필드 --%>
                <c:set var="priceLabel" value="주문 가격" scope="request" />
                <c:set var="priceInputId" value="conditional-order-edit-price" scope="request" />
                <c:set var="priceMinusBtnId" value="conditional-order-edit-price-minus" scope="request" />
                <c:set var="pricePlusBtnId" value="conditional-order-edit-price-plus" scope="request" />
                <%@ include file="order_conditional_price_fields.jsp" %>
                
                <%-- 주문 수량 입력 필드 --%>
                <c:set var="quantityInputId" value="conditional-order-edit-quantity" scope="request" />
                <c:set var="quantityMinusBtnId" value="conditional-order-edit-quantity-minus" scope="request" />
                <c:set var="quantityPlusBtnId" value="conditional-order-edit-quantity-plus" scope="request" />
                <%@ include file="order_conditional_quantity_fields.jsp" %>
                
                <%-- 주문 유지 드롭다운 --%>
                <c:set var="durationDropdownId" value="conditional-order-edit-duration-dropdown" scope="request" />
                <%@ include file="order_conditional_duration_fields.jsp" %>
                
                <div class="order-modal__form-spacer"></div>
            </div>

            <%-- 하단 버튼 --%>
            <div class="order-modal__actions">
                <button type="button" 
                        class="order-modal__btn order-modal__btn--delete"
                        id="conditional-order-edit-delete-btn">삭제하기</button>
                <button type="submit" 
                        class="order-modal__btn order-modal__btn--modify"
                        id="conditional-order-edit-modify-btn">수정하기</button>
            </div>
        </form>
    </div>
</div>
