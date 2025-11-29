<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%-- script 태그 중복 생성 방지--%>
<c:if test="${empty requestScope.D3_LIBRARY_LOADED}">
    <script src="https://d3js.org/d3.v7.min.js"></script>
    <c:set var="D3_LIBRARY_LOADED" value="true" scope="request" />
</c:if>
<c:if test="${empty requestScope.SPARKLINE_JS_LOADED}">
    <script src="${pageContext.request.contextPath}/static/js/common/chart/sparkline.js"></script>
    <c:set var="SPARKLINE_JS_LOADED" value="true" scope="request" />
</c:if>

<c:set var="identifier" value="${param.identifier}" />
<div id="${identifier}" class="sparkline-container"></div>