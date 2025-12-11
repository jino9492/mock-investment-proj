<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<h2>"${searchQuery}" 검색 결과</h2>

<c:if test="${empty searchResults}">
    <p>검색 결과가 없습니다.</p>
</c:if>

<ul>
    <c:forEach var="item" items="${searchResults}">
        <li>
            <a href="/stock/${item.stockCode}">
                    ${item.stockName} (${item.stockCode})
            </a>
        </li>
    </c:forEach>
</ul>
