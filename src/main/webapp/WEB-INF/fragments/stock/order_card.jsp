<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%-- 변수 초기화 --%>
<c:set var="maxQuantity" value="${empty maxQuantity ? 0 : maxQuantity}" scope="page" />
<c:set var="submitButtonText" value="${empty submitButtonText ? '구매하기' : submitButtonText}" scope="page" />
<c:set var="availableCashAmount" value="0" scope="page" />
<c:if test="${not empty member}">
    <c:set var="availableCashAmount" value="${member.virtualCash}" scope="page" />
</c:if>

<section class="order-card order-card--buy"
         <c:if test="${not empty stock and not empty stock.stockId}">
         data-stock-id="${stock.getStockId()}"
         </c:if>>
    <h2 class="order-card__title">주문하기</h2>

    <!-- 주문 타입 hidden (LIMIT / MARKET) -->
    <input type="hidden"
           name="orderType"
           value="LIMIT"
           class="order-card__order-type-hidden" />

    <!-- 구매 / 판매 / 대기 탭 -->
    <div class="order-card__trade-type" role="radiogroup" aria-required="true">
        <button type="button" 
                class="order-card__trade-tab order-card__trade-tab--buy order-card__trade-tab--active" 
                role="radio" 
                aria-checked="true" 
                data-state="checked">구매</button>
        <button type="button" 
                class="order-card__trade-tab order-card__trade-tab--sell" 
                role="radio" 
                aria-checked="false" 
                data-state="unchecked">판매</button>
        <button type="button" 
                class="order-card__trade-tab order-card__trade-tab--wait" 
                role="radio" 
                aria-checked="false" 
                data-state="unchecked">대기</button>
    </div>

    <!-- 주문유형 (대기 탭에서는 숨김) -->
    <div class="order-card__row order-card__order-type-row">
        <label class="order-card__label">주문유형</label>
        <div class="order-card__field">
            <div class="order-card__dropdown" data-dropdown data-order-type-dropdown>
                <input type="hidden" name="orderType" value="general" data-dropdown-input>
                <button type="button"
                        class="order-card__dropdown-toggle"
                        aria-haspopup="listbox"
                        aria-expanded="false"
                        data-dropdown-toggle>
                    <span class="order-card__dropdown-label" data-dropdown-label>일반 주문</span>
                    <span class="order-card__dropdown-icon" aria-hidden="true">▼</span>
                </button>
                <div class="order-card__dropdown-list" role="listbox" data-dropdown-list>
                    <button type="button"
                            class="order-card__dropdown-option"
                            role="option"
                            aria-selected="true"
                            data-value="general">
                        <span class="order-card__dropdown-option-label">일반 주문</span>
                    </button>
                    <button type="button"
                            class="order-card__dropdown-option"
                            role="option"
                            aria-selected="false"
                            data-value="conditional">
                        <span class="order-card__dropdown-option-label">조건 주문</span>
                        <span class="order-card__dropdown-option-desc">지정한 가격이 되면 자동으로 주문</span>
                    </button>
                </div>
            </div>
        </div>
    </div>

    <!-- 일반 주문 필드 -->
    <div class="order-card__general-order order-card__general-order--visible">
        <!-- 구매가격 (지정가 / 시장가 pill) -->
        <div class="order-card__row">
            <label class="order-card__label">구매가격</label>
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
            <label class="order-card__label" for="order-price-input" style="visibility: hidden;">구매가격</label>
            <div class="order-card__field order-card__field--price">
                <div class="input-wrapper">
                    <input type="text"
                           class="order-card__input"
                           id="order-price-input"
                           placeholder="가격을 입력하세요"
                           aria-label="구매가격"
                           <c:if test="${not empty daily and not empty daily.closePrice}">
                           data-current-price="${daily.closePrice.intValue()}"
                           </c:if> />
                    <span class="unit-text">원</span>
                </div>
                <div class="order-card__step-btn-group">
                    <button type="button" class="order-card__step-btn order-card__step-btn--minus" id="order-price-minus">-</button>
                    <div class="order-card__step-btn-divider"></div>
                    <button type="button" class="order-card__step-btn order-card__step-btn--plus" id="order-price-plus">+</button>
                </div>
            </div>
        </div>

        <!-- 수량 -->
        <div class="order-card__row">
            <label class="order-card__label" for="order-quantity-input">수량</label>
            <div class="order-card__field order-card__field--qty">
                <input type="text" 
                       class="order-card__input" 
                       id="order-quantity-input" 
                       placeholder="최대 <fmt:formatNumber value="${maxQuantity}" pattern="#,###"/>주 가능" />
                <div class="order-card__step-btn-group">
                    <button type="button" class="order-card__step-btn order-card__step-btn--minus" id="order-quantity-minus">-</button>
                    <div class="order-card__step-btn-divider"></div>
                    <button type="button" class="order-card__step-btn order-card__step-btn--plus" id="order-quantity-plus">+</button>
                </div>
            </div>
        </div>

        <hr class="order-card__divider" />

        <!-- 금액 정보 -->
        <div class="order-card__summary-row">
            <span class="order-card__summary-label">구매가능 금액</span>
            <span class="order-card__summary-value" 
                  id="order-available-amount"
                  data-available-cash="${availableCashAmount}">
                <fmt:formatNumber value="${availableCashAmount}" pattern="#,###"/>원
            </span>
        </div>
        <div class="order-card__summary-row">
            <span class="order-card__summary-label">총 주문 금액</span>
            <span class="order-card__summary-value" id="order-total-amount">
                <fmt:formatNumber value="0" pattern="#,###"/>원
            </span>
        </div>
    </div>

    <!-- 조건 주문 필드 -->
    <div class="order-card__conditional-order order-card__conditional-order--hidden">
        <!-- 구매 가능 금액 -->
        <div class="order-card__summary-row">
            <span class="order-card__summary-label">구매가능 금액</span>
            <span class="order-card__summary-value">
                <fmt:formatNumber value="${availableCashAmount}" pattern="#,###"/>원
            </span>
        </div>

        <%-- 공통 감시 가격 필드 fragment --%>
        <c:set var="watchPriceInputId" value="watch-price-input" scope="request" />
        <c:set var="watchPriceInfoId" value="watch-price-info" scope="request" />
        <c:set var="watchPriceMinusBtnId" value="watch-price-minus" scope="request" />
        <c:set var="watchPricePlusBtnId" value="watch-price-plus" scope="request" />
        <%@ include file="order_conditional_watch_price_fields.jsp" %>

        <%-- 공통 가격 입력 필드 fragment --%>
        <c:set var="priceLabel" value="구매가격" scope="request" />
        <c:set var="priceInputId" value="conditional-order-price-input" scope="request" />
        <c:set var="priceMinusBtnId" value="conditional-order-price-minus" scope="request" />
        <c:set var="pricePlusBtnId" value="conditional-order-price-plus" scope="request" />
        <%@ include file="order_conditional_price_fields.jsp" %>

        <%-- 공통 수량 입력 필드 fragment --%>
        <c:set var="quantityInputId" value="conditional-order-quantity-input" scope="request" />
        <c:set var="quantityMinusBtnId" value="conditional-order-quantity-minus" scope="request" />
        <c:set var="quantityPlusBtnId" value="conditional-order-quantity-plus" scope="request" />
        <%@ include file="order_conditional_quantity_fields.jsp" %>

        <%-- 공통 주문 유지 드롭다운 fragment --%>
        <%@ include file="order_conditional_duration_fields.jsp" %>

        <%-- 공통 총 주문 금액 표시 fragment --%>
        <%@ include file="order_conditional_total_amount.jsp" %>
    </div>

    <!-- 판매 조건 주문 필드 -->
    <div class="order-card__conditional-order--sell order-card__conditional-order--hidden">
        <%-- 공통 감시 가격 필드 fragment (판매용) --%>
        <c:set var="watchPriceInputId" value="conditional-sell-watch-price-input" scope="request" />
        <c:set var="watchPriceInfoId" value="conditional-sell-watch-price-info" scope="request" />
        <c:set var="watchPriceMinusBtnId" value="conditional-sell-watch-price-minus" scope="request" />
        <c:set var="watchPricePlusBtnId" value="conditional-sell-watch-price-plus" scope="request" />
        <%@ include file="order_conditional_watch_price_fields.jsp" %>

        <%-- 공통 가격 입력 필드 fragment (판매용) --%>
        <c:set var="priceLabel" value="판매가격" scope="request" />
        <c:set var="priceInputId" value="conditional-sell-price-input" scope="request" />
        <c:set var="priceMinusBtnId" value="conditional-sell-price-minus" scope="request" />
        <c:set var="pricePlusBtnId" value="conditional-sell-price-plus" scope="request" />
        <%@ include file="order_conditional_price_fields.jsp" %>

        <%-- 공통 수량 입력 필드 fragment (판매용) --%>
        <c:set var="quantityInputId" value="conditional-sell-quantity-input" scope="request" />
        <c:set var="quantityMinusBtnId" value="conditional-sell-quantity-minus" scope="request" />
        <c:set var="quantityPlusBtnId" value="conditional-sell-quantity-plus" scope="request" />
        <%@ include file="order_conditional_quantity_fields.jsp" %>

        <%-- 공통 주문 유지 드롭다운 fragment --%>
        <%@ include file="order_conditional_duration_fields.jsp" %>

        <%-- 공통 총 주문 금액 표시 fragment --%>
        <%@ include file="order_conditional_total_amount.jsp" %>
    </div>

    <!-- 대기 주문 목록 -->
    <div class="order-card__wait-orders order-card__wait-orders--hidden">
        <c:choose>
            <c:when test="${not empty pendingOrders && fn:length(pendingOrders) > 0}">
                <c:forEach var="order" items="${pendingOrders}">
                    <div class="order-card__wait-order-item">
                        <div class="order-card__wait-order-info">
                            <span class="order-card__wait-order-type order-card__wait-order-type--${order.tradeType}">
                                <c:choose>
                                    <c:when test="${order.tradeType == 'buy'}">구매</c:when>
                                    <c:otherwise>판매</c:otherwise>
                                </c:choose>
                                <fmt:formatNumber value="${order.quantity}" pattern="#,###"/>주
                            </span>
                            <span class="order-card__wait-order-price">
                                주당 <fmt:formatNumber value="${order.price}" pattern="#,###"/>원
                            </span>
                        </div>
                        <div class="order-card__wait-order-actions">
                            <button type="button" 
                                    class="order-card__wait-order-btn order-card__wait-order-btn--edit"
                                    data-order-id="${order.orderId}">수정</button>
                            <button type="button" 
                                    class="order-card__wait-order-btn order-card__wait-order-btn--cancel"
                                    data-order-id="${order.orderId}">취소</button>
                        </div>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="order-card__wait-empty">
                    <p class="order-card__wait-empty-text">대기중인 주문이 없어요</p>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

    <!-- 버튼 (대기 탭에서는 숨김) -->
    <c:choose>
        <c:when test="${isLoggedIn}">
            <button class="order-card__submit-btn" id="order-submit-btn">${submitButtonText}</button>
        </c:when>
        <c:otherwise>
            <a href="${pageContext.request.contextPath}/login" class="order-card__submit-btn" id="order-submit-link" style="text-decoration: none; display: flex; align-items: center; justify-content: center;">로그인하고 구매하기</a>
        </c:otherwise>
    </c:choose>
</section>
