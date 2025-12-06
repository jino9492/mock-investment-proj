<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<section class="order-card" data-is-logged-in="${isLoggedIn}">
    <h2 class="order-card__title">주문하기</h2>

    <!-- 주문 타입 hidden (LIMIT / MARKET) -->
    <input type="hidden"
           name="orderType"
           value="LIMIT"
           class="order-card__order-type-hidden" />

    <!-- 구매 / 판매 / 대기 탭 -->
    <div class="order-card__trade-type">
        <button class="order-card__trade-tab order-card__trade-tab--active">구매</button>
        <button class="order-card__trade-tab">판매</button>
        <button class="order-card__trade-tab">대기</button>
    </div>

    <!-- 주문유형 -->
    <div class="order-card__row">
        <label class="order-card__label">주문유형</label>
        <div class="order-card__field">
            <select class="order-card__select_box">
                <option value="1" selected>일반주문</option>
                <option value="2">조건주문</option>
            </select>
        </div>
    </div>

    <!-- 구매가격 (지정가 / 시장가 pill) -->
    <div class="order-card__row">
        <label class="order-card__label">구매가격</label>
        <div class="order-card__field order-card__field--split">
            <button class="order-card__pill order-card__pill--active">
                지정가
            </button>
            <button class="order-card__pill">
                시장가
            </button>
        </div>
    </div>

    <!-- 가격 입력 -->
    <div class="order-card__row">
        <label class="order-card__label">&nbsp;</label>
        <div class="order-card__field order-card__field--price">
            <div class="input-wrapper">
                <input type="text"
                       class="order-card__input"
                       placeholder="가격을 입력하세요" />
                <span class="unit-text">원</span>
            </div>
            <button class="order-card__step-btn">-/+</button>
        </div>
    </div>

    <!-- 수량 -->
    <div class="order-card__row">
        <label class="order-card__label">수량</label>
        <div class="order-card__field order-card__field--qty">
            <input type="text" class="order-card__input" placeholder="최대 0주 가능" />
            <button class="order-card__step-btn">-/+</button>
        </div>
    </div>

    <hr class="order-card__divider" />

    <!-- 금액 정보 -->
    <div class="order-card__summary-row">
        <span class="order-card__summary-label">구매가능 금액</span>
        <span class="order-card__summary-value">00,000원</span>
    </div>
    <div class="order-card__summary-row">
        <span class="order-card__summary-label">총 주문 금액</span>
        <span class="order-card__summary-value">원</span>
    </div>


    <!-- 로그인 안내 + 버튼 -->
    <div class="order-card__footer">

        <button type="button"
                class="order-card__submit-btn">
            <c:choose>
                <c:when test="${not isLoggedIn}">
                    로그인하고 구매하기
                </c:when>
                <c:otherwise>
                    구매하기
                </c:otherwise>
            </c:choose>
        </button>
    </div>
</section>
