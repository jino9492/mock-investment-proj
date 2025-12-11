<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- 등락률 표시 Fragment --%>
<%-- 사용법: <jsp:include page="/WEB-INF/fragments/league/change_rate_display.jsp"> --%>
<%--         <jsp:param name="changeRate" value="${stock.changeRate}" /> --%>
<%--         </jsp:include> --%>

<%
    String changeRateParam = request.getParameter("changeRate");
    double changeRate = 0.0;
    if (changeRateParam != null && !changeRateParam.isEmpty()) {
        try {
            changeRate = Double.parseDouble(changeRateParam);
        } catch (NumberFormatException e) {
            changeRate = 0.0;
        }
    }
    request.setAttribute("changeRate", changeRate);
%>

<span class="<c:choose>
    <c:when test="${changeRate == 0}">price-color--neutral</c:when>
    <c:when test="${changeRate > 0}">price-color--up</c:when>
    <c:otherwise>price-color--down</c:otherwise>
</c:choose>">
    <c:if test="${changeRate > 0}">+</c:if><fmt:formatNumber value="${changeRate}" pattern="0.00"/>%
</span>

