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
    
    request.setAttribute("rank", rankParam);
    request.setAttribute("stockName", stockNameParam);
    request.setAttribute("currentPrice", currentPriceParam);
    request.setAttribute("avgPurchasePrice", avgPurchasePriceParam);
    request.setAttribute("changeRate", changeRateParam);
%>

<tr>
    <td>${rank}</td>
    <td>${stockName}</td>
    <td><fmt:formatNumber value="${currentPrice}" pattern="#,###"/>원</td>
    <td><fmt:formatNumber value="${avgPurchasePrice}" pattern="#,###"/>원</td>
    <td>
        <jsp:include page="/WEB-INF/fragments/league/change_rate_display.jsp">
            <jsp:param name="changeRate" value="${changeRate}"/>
        </jsp:include>
    </td>
</tr>

