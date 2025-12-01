<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%-- 포트폴리오 테이블 행 Fragment --%>
<%-- 사용법: <jsp:include page="/WEB-INF/fragments/league/table_row_portfolio_stock.jsp"> --%>
<%--         <jsp:param name="stockName" value="${stock.stockName}" /> --%>
<%--         <jsp:param name="currentPrice" value="${stock.currentPrice}" /> --%>
<%--         <jsp:param name="changeRate" value="${stock.changeRate}" /> --%>
<%--         </jsp:include> --%>

<%
    String stockNameParam = request.getParameter("stockName");
    String currentPriceParam = request.getParameter("currentPrice");
    String changeRateParam = request.getParameter("changeRate");
    
    request.setAttribute("stockName", stockNameParam);
    request.setAttribute("currentPrice", currentPriceParam);
    request.setAttribute("changeRate", changeRateParam);
%>

<tr>
    <td>${stockName}</td>
    <td><fmt:formatNumber value="${currentPrice}" pattern="#,###"/>원</td>
    <td>
        <jsp:include page="/WEB-INF/fragments/league/change_rate_display.jsp">
            <jsp:param name="changeRate" value="${changeRate}"/>
        </jsp:include>
    </td>
</tr>

