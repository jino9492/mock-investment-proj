<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!-- 페이지별 JavaScript -->
<c:if test="${not empty pageScripts}">
    <c:choose>
        <c:when test="${pageScripts.getClass().name == 'java.lang.String'}">
            <c:forTokens var="script" items="${pageScripts}" delims=",">
                <script src="${pageContext.request.contextPath}/static/js/${script.trim()}"></script>
            </c:forTokens>
        </c:when>
        <c:otherwise>
            <c:forEach var="script" items="${pageScripts}">
                <script src="${pageContext.request.contextPath}/static/js/${script}"></script>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</c:if>
</body>
</html>

