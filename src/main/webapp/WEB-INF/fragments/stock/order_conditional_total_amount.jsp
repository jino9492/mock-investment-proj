<%-- 
    조건 주문 총 주문 금액 표시 fragment
    공통으로 사용되는 총 주문 금액 표시 영역
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- 총 주문 금액 -->
<div class="order-card__summary-row">
    <span class="order-card__summary-label">총 주문 금액</span>
    <span class="order-card__summary-value">
        <fmt:formatNumber value="0" pattern="#,###"/>원
    </span>
</div>

