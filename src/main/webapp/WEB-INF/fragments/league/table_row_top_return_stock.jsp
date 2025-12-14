<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- 상위 수익률 종목 테이블 행 Fragment --%>
<%-- 사용법: <jsp:include page="/WEB-INF/fragments/league/table_row_top_return_stock.jsp"> --%>
<%--         <jsp:param name="rank" value="${status.count}" /> --%>
<%--         <jsp:param name="stockName" value="${stock.stockName}" /> --%>
<%--         <jsp:param name="currentPrice" value="${stock.currentPrice}" /> --%>
<%--         <jsp:param name="avgPurchasePrice" value="${stock.avgPurchasePrice}" /> --%>
<%--         <jsp:param name="changeRate" value="${stock.changeRate}" /> --%>
<%--         </jsp:include> --%>

<%
    String rankParam = request.getParameter("rank");
    String stockNameParam = request.getParameter("stockName");
    String currentPriceParam = request.getParameter("currentPrice");
    String avgPurchasePriceParam = request.getParameter("avgPurchasePrice");
    String changeRateParam = request.getParameter("changeRate");
    
    // 숫자 파라미터를 Long으로 변환
    Long currentPrice = 0L;
    Long avgPurchasePrice = 0L;
    Double changeRate = 0.0;
    
    try {
        if (currentPriceParam != null && !currentPriceParam.isEmpty()) {
            currentPrice = Long.parseLong(currentPriceParam);
        }
    } catch (NumberFormatException e) {
        currentPrice = 0L;
    }
    
    try {
        if (avgPurchasePriceParam != null && !avgPurchasePriceParam.isEmpty()) {
            avgPurchasePrice = Long.parseLong(avgPurchasePriceParam);
        }
    } catch (NumberFormatException e) {
        avgPurchasePrice = 0L;
    }
    
    try {
        if (changeRateParam != null && !changeRateParam.isEmpty()) {
            changeRate = Double.parseDouble(changeRateParam);
        }
    } catch (NumberFormatException e) {
        changeRate = 0.0;
    }
    
    request.setAttribute("rank", rankParam);
    request.setAttribute("stockName", stockNameParam);
    request.setAttribute("currentPrice", currentPrice);
    request.setAttribute("avgPurchasePrice", avgPurchasePrice);
    request.setAttribute("changeRate", changeRate);
%>

<tr>
    <td>${rank}</td>
    <td>${stockName}</td>
    <td><fmt:formatNumber value="${currentPrice}" pattern="#,###"/>원</td>
    <td>
        <jsp:include page="/WEB-INF/fragments/common/price_color_class.jsp">
            <jsp:param name="value" value="${changeRate}"/>
        </jsp:include>
        <span class="${priceColorClass}">
            <c:if test="${changeRate > 0}">+</c:if><fmt:formatNumber value="${changeRate}" pattern="0.00"/>%
        </span>
    </td>
</tr>

