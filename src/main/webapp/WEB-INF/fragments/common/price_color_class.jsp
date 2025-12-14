<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- 가격 변동 색상 클래스 유틸리티 Fragment --%>
<%-- 사용법: <jsp:include page="/WEB-INF/fragments/common/price_color_class.jsp"> --%>
<%--         <jsp:param name="value" value="${accountSummary.totalReturn}" /> --%>
<%--         </jsp:include> --%>
<%-- 결과: price-color--up, price-color--down, price-color--neutral 중 하나가 requestScope.priceColorClass에 설정됨 --%>

<%
    String valueParam = request.getParameter("value");
    double value = 0.0;
    if (valueParam != null && !valueParam.isEmpty()) {
        try {
            value = Double.parseDouble(valueParam);
        } catch (NumberFormatException e) {
            value = 0.0;
        }
    }
    
    String colorClass;
    if (value == 0) {
        colorClass = "price-color--neutral";
    } else if (value > 0) {
        colorClass = "price-color--up";
    } else {
        colorClass = "price-color--down";
    }
    
    request.setAttribute("priceColorClass", colorClass);
%>

