<%-- 
    조건 주문 유지 기간 드롭다운 fragment
    공통으로 사용되는 주문 유지 드롭다운 필드
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!-- 주문 유지 -->
<div class="order-card__row order-card__order-duration">
    <label class="order-card__label">주문 유지</label>
    <div class="order-card__field">
        <div class="order-card__dropdown" data-dropdown>
            <input type="hidden" name="orderDuration" value="1month" data-dropdown-input>
            <button type="button"
                    class="order-card__dropdown-toggle"
                    aria-haspopup="listbox"
                    aria-expanded="false"
                    data-dropdown-toggle>
                <span class="order-card__dropdown-label" data-dropdown-label>1개월 · 1월 7일까지</span>
                <span class="order-card__dropdown-icon" aria-hidden="true">▼</span>
            </button>
            <div class="order-card__dropdown-list" role="listbox" data-dropdown-list>
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

