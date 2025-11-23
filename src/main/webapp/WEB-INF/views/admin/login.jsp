<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin/login.css"/>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/sidebar_left/sidebar.css"/>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/sidebar_left/sidebar_button.css"/>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>LOGIN</title>
</head>
<body>
<%--임시 헤더--%>
<jsp:include page="${pageContext.request.contextPath}/WEB-INF/fragments/common/header.jsp" />
<main class="">

</main>
<%--<h1>ADMIN LOGIN</h1>--%>

<%--<% if (request.getAttribute("errorMsg") != null) { %>--%>
<%--<p style="color: red;"><%= request.getAttribute("errorMsg") %></p>--%>
<%--<% } %>--%>

<%--<form action="<%= request.getContextPath() %>/admin" method="post">--%>
<%--    <label><input type="text" name="adminId" required></label><br>--%>
<%--    <label><input type="password" name="password" required></label><br>--%>
<%--    <button type="submit">로그인</button>--%>
<%--</form>--%>

<jsp:include page="${pageContext.request.contextPath}/WEB-INF/fragments/common/sidebar_left/sidebar.jsp" />

<div class="main-content">
    <p>임시</p>
</div>
</body>
</html>