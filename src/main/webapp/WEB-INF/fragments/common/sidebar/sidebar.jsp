<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/sidebar/sidebar.css"/>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/sidebar/sidebar_stock_box.css">
<script src="${pageContext.request.contextPath}/static/js/common/sidebar/sidebar.js"></script>
<aside class="sidebar__container">
  <div class="sidebar__work-area">
    <c:choose>
      <c:when test="${isLoggedIn}">
        <div class="sidebar__title sidebar__title--account">내 가상계좌</div>
        <div class="sidebar__account-box" id="sidebar-account-amount">
          <c:choose>
            <c:when test="${not empty member}">
              <fmt:formatNumber value="${member.virtualCash}" pattern="#,###"/>원
            </c:when>
            <c:otherwise>
              0원
            </c:otherwise>
          </c:choose>
        </div>

        <div class="sidebar__my-investment selected">
          <div class="sidebar__title sidebar__title--investment">내 투자</div>
          <ul id="sidebar-holdings-list">
            <c:choose>
              <c:when test="${not empty holdings && fn:length(holdings) > 0}">
                <c:forEach var="holding" items="${holdings}" varStatus="status">
                  <li class="stock-box__container" data-stock-name="${holding.stockName}">
                    <div class="stock-box__company-profile"></div>
                    <div class="stock-box__company-wrapper">
                      <div class="stock-box__detail-wrapper">
                        <div class="stock-box__company-name">${holding.stockName}</div>
                        <div class="stock-box__stock-price">
                          <fmt:formatNumber value="${holding.currentPrice}" pattern="#,###"/>원
                        </div>
                      </div>
                      <div class="stock-box__detail-wrapper">
                        <div class="stock-box__stock-count">
                          <fmt:formatNumber value="${holding.quantity}" pattern="#,###"/>주
                        </div>
                        <div class="stock-box__comparison 
                          <c:choose>
                            <c:when test="${holding.profitLoss > 0}">stock-box__comparison--positive</c:when>
                            <c:when test="${holding.profitLoss < 0}">stock-box__comparison--negative</c:when>
                            <c:otherwise>stock-box__comparison--neutral</c:otherwise>
                          </c:choose>">
                          <c:choose>
                            <c:when test="${holding.profitLoss > 0}">+</c:when>
                            <c:when test="${holding.profitLoss < 0}">-</c:when>
                          </c:choose>
                          <c:set var="absProfitLoss" value="${holding.profitLoss >= 0 ? holding.profitLoss : -holding.profitLoss}" />
                          <fmt:formatNumber value="${absProfitLoss}" pattern="#,###"/>원
                          (<c:choose>
                            <c:when test="${holding.profitRate > 0}">+</c:when>
                            <c:when test="${holding.profitRate < 0}">-</c:when>
                          </c:choose>
                          <c:set var="absProfitRate" value="${holding.profitRate >= 0 ? holding.profitRate : -holding.profitRate}" />
                          <fmt:formatNumber value="${absProfitRate}" pattern="#,##0.00"/>%)
                        </div>
                      </div>
                    </div>
                  </li>
                </c:forEach>
              </c:when>
              <c:otherwise>
                <li class="stock-box__empty">
                  <div style="text-align: center; padding: 20px; color: #999; font-size: 14px;">
                    보유 종목이 없습니다.
                  </div>
                </li>
              </c:otherwise>
            </c:choose>
          </ul>
        </div>
        <div class="sidebar__favorite">
          <div class="sidebar__title">관심</div>
          <ul>
            <%@ include file="sidebar_stock_box.jsp" %>
            <%@ include file="sidebar_stock_box.jsp" %>
            <%@ include file="sidebar_stock_box.jsp" %>
            <%@ include file="sidebar_stock_box.jsp" %>
          </ul>
        </div>
      </c:when>
      <c:otherwise>
        <div class="sidebar__login-prompt">
          <div class="sidebar__login-prompt-text">로그인이 필요합니다</div>
          <a href="/login" class="sidebar__login-prompt-button">로그인</a>
        </div>
      </c:otherwise>
    </c:choose>
  </div>
  <div class="sidebar__right-rail">
    <button class="sidebar__right-rail-toggle">
      <span class="sidebar__arrow-icon"><img src="${pageContext.request.contextPath}/static/assets/toggle.png" alt=""></span>
    </button>
    <button class="sidebar__rail-item sidebar__investment-btn">
      <span class="sidebar__rail-icon"><img src="${pageContext.request.contextPath}/static/assets/graph-up.png" alt=""></span>
      <span>내 투자</span>
    </button>
    <div class="sidebar__rail-item sidebar__favorite-btn">
      <div class="sidebar__rail-icon"><img src="${pageContext.request.contextPath}/static/assets/heart.png" alt=""></div>
      <div>관심</div>
    </div>
  </div>
</aside>
