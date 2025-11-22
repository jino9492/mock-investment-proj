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

<div class="sidebar">
    <jsp:include page="/WEB-INF/fragments/common/sidebar_left/sidebar_button.jsp">
        <jsp:param name="menuItems" value="로그인,대시보드,회원 관리,거래 관리" />
        <jsp:param name="menuLinks" value="/admin,/admin/dashboard,/admin/users,/admin/trades" />
    </jsp:include>
</div>

<div class="main-content">
    <h1>관리자 페이지</h1>
    <p>콘텐츠 영역</p>
</div>

<div class="main-content">
    <p>임시</p>
</div>
</body>
</html>