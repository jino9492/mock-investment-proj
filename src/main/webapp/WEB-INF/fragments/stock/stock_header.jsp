<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- 상단 종목 정보 -->
<header class="stock-header"
        <c:if test="${not empty stock}">
        data-stock-id="${stock.getStockId()}"
        </c:if>>
    <div class="stock-header__left">
        <div class="stock-header__logo"></div>
        <div class="stock-header__info">
            <div class="stock-header__name">${stock.getStockName()} ${stock.getStockSymbol()}</div>
            <div class="stock-header__price-row">
                <c:if test="${not empty daily and not empty daily.closePrice}">
                    <span class="stock-header__price"><fmt:formatNumber value="${daily.getClosePrice().abs()}" pattern="#,###" />원</span>
                    <span class="stock-header__diff">
                        <c:set var="isUp" value="${daily.getChangePrice() > 0}" />
                        어제보다&nbsp;<span class="stock-header__diff-value" style="color:${isUp ? "#F04452" : "#3182F6"}">
                        ${isUp ? "+" : ""}<fmt:formatNumber value="${daily.getChangePrice()}" pattern="#,###" />원 (${daily.getChangeRate().abs()}%)
                    </span>
                    </span>
                </c:if>
            </div>
        </div>
    </div>
</header>

<!-- 상단 탭 -->
<nav class="stock-tabs">
    <c:choose>
        <c:when test="${param.activeTab == 'chart' or empty param.activeTab}">
            <button class="stock-tabs__item stock-tabs__item--active">차트 · 호가</button>
            <a href="${pageContext.request.contextPath}/stock_info?stockID=${param.stockID}" class="stock-tabs__item">종목정보</a>
        </c:when>
        <c:otherwise>
            <a href="${pageContext.request.contextPath}/stock?stockID=${param.stockID}" class="stock-tabs__item">차트 · 호가</a>
            <button class="stock-tabs__item stock-tabs__item--active">종목정보</button>
        </c:otherwise>
    </c:choose>
    <button class="stock-tabs__item">뉴스 · 공시</button>
    <button class="stock-tabs__item">커뮤니티</button>
</nav>

