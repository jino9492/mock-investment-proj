<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<section class="my-stock-card" data-is-logged-in="${isLoggedIn}">
    <h2 class="my-stock-card__title">내 주식</h2>

    <c:choose>
        <c:when test="${isLoggedIn}">
            <div class="my-stock-card__content my-stock-card__content--logged-in">
                <div class="my-stock-card__row">
                    <span>총 수익</span>
                    <span class="my-stock-card__profit">+00,000</span>
                </div>
                <div class="my-stock-card__row">
                    <span>총 금액</span>
                    <span>000,000원</span>
                </div>
                <div class="my-stock-card__row">
                    <span>수량</span>
                    <span>0주</span>
                </div>
                <div class="my-stock-card__row">
                    <span>1주 평균 금액</span>
                    <span>000,000원</span>
                </div>
            </div>
        </c:when>


        <c:otherwise>
            <div class="my-stock-card__content my-stock-card__content--logged-out">
                <p class="my-stock-card__empty-text">
                    내 주식을 보려면<br/>
                    로그인이 필요해요
                </p>

                <button type="button"
                        class="my-stock-card__login-btn">
                    로그인
                </button>
            </div>
        </c:otherwise>
    </c:choose>
</section>