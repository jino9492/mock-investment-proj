<%-- ============================================================
     조건 주문 총 주문 금액 표시 Fragment
     
     공통으로 사용되는 총 주문 금액 표시 영역입니다.
     JavaScript에서 동적으로 업데이트됩니다.
     
     사용법:
     - totalAmountId: 총 주문 금액 표시 영역 ID (기본값: 없음, CSS 선택자로 선택)
     
     예시:
     <c:set var="totalAmountId" value="conditional-buy-total-amount" scope="request" />
     <%@ include file="order_conditional_total_amount.jsp" %>
     ============================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- 총 주문 금액 표시 (초기값: 0원) --%>
<div class="order-card__summary-row">
    <span class="order-card__summary-label">총 주문 금액</span>
    <span class="order-card__summary-value" 
          <c:if test="${not empty totalAmountId}">id="${totalAmountId}"</c:if>
          data-total-amount="0">
        <fmt:formatNumber value="0" pattern="#,###"/>원
    </span>
</div>

