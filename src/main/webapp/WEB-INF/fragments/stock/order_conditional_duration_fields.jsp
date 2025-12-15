<%-- ============================================================
     조건 주문 유지 기간 드롭다운 Fragment
     
     공통으로 사용되는 주문 유지 기간 선택 드롭다운 필드입니다.
     구매/판매 조건 주문 모두에서 동일하게 사용됩니다.
     
     사용법:
     - durationDropdownId: 드롭다운 ID (기본값: "order-duration-dropdown")
     
     예시:
     <c:set var="durationDropdownId" value="buy-order-duration-dropdown" scope="request" />
     <%@ include file="order_conditional_duration_fields.jsp" %>
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- 드롭다운 ID 파라미터 초기화 (기본값 설정) --%>
<c:set var="durationDropdownId" value="${empty durationDropdownId ? 'order-duration-dropdown' : durationDropdownId}" scope="request" />

<%-- 주문 유지 기간 선택 드롭다운 --%>
<div class="order-card__row order-card__order-duration">
    <label class="order-card__label" for="${durationDropdownId}">주문 유지</label>
    <div class="order-card__field">
        <div class="order-card__dropdown" 
             id="${durationDropdownId}"
             data-dropdown
             data-order-duration-dropdown>
            <input type="hidden" 
                   name="orderDuration" 
                   value="1month" 
                   data-dropdown-input>
            <button type="button"
                    class="order-card__dropdown-toggle"
                    aria-haspopup="listbox"
                    aria-expanded="false"
                    aria-label="주문 유지 기간 선택"
                    aria-controls="${durationDropdownId}-list"
                    data-dropdown-toggle>
                <span class="order-card__dropdown-label" data-dropdown-label>1개월 · 1월 7일까지</span>
                <span class="order-card__dropdown-icon" aria-hidden="true">▼</span>
            </button>
            <div class="order-card__dropdown-list" 
                 id="${durationDropdownId}-list"
                 role="listbox" 
                 data-dropdown-list>
                <button type="button"
                        class="order-card__dropdown-option"
                        role="option"
                        aria-selected="false"
                        data-value="1week">
                    <span class="order-card__dropdown-option-label">1주일 동안</span>
                </button>
                <button type="button"
                        class="order-card__dropdown-option"
                        role="option"
                        aria-selected="true"
                        data-value="1month">
                    <span class="order-card__dropdown-option-label">1개월 동안</span>
                </button>
                <button type="button"
                        class="order-card__dropdown-option"
                        role="option"
                        aria-selected="false"
                        data-value="1year">
                    <span class="order-card__dropdown-option-label">1년 동안</span>
                </button>
                <button type="button"
                        class="order-card__dropdown-option"
                        role="option"
                        aria-selected="false"
                        data-value="indefinite">
                    <span class="order-card__dropdown-option-label">계속 유지하기</span>
                </button>
            </div>
        </div>
    </div>
</div>

