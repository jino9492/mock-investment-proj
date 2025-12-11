<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <base href="${pageContext.request.contextPath}">
    <meta charset="UTF-8" />
    <link rel="stylesheet" href="static/css/common/default_layout.css">
    <title>MOCK</title>
</head>
<body>
<div class="layout__page-wrap">
    <div class="layout__inner">
        <!-- 상단 네비게이션 -->
        <jsp:include page="WEB-INF/fragments/common/header.jsp" />
        <c:choose>
            <c:when test="${not empty param.contentPage}">
                <jsp:include page="${param.contentPage}" flush="true" />
            </c:when>
            <c:otherwise>
                <p>페이지 내용을 찾을 수 없습니다.</p>
            </c:otherwise>
        </c:choose>
    </div>
    <!-- 오른쪽 사이드 -->
    <jsp:include page="WEB-INF/fragments/common/sidebar/sidebar.jsp" />
</div>

</body>
</html>
