<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>ADMIN LOGIN</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin/login.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/default_layout.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/sidebar_left/sidebar.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/sidebar_left/sidebar_button.css"/>
</head>
<body>
<header id="header">
    <jsp:include page="/WEB-INF/fragments/common/header_admin.jsp" />
</header>

<main class="main-content">
    <h1>ADMIN LOGIN</h1>

    <% if (request.getAttribute("errorMsg") != null) { %>
    <p style="color: red;"><%= request.getAttribute("errorMsg") %></p>
    <% } %>

    <form action="${pageContext.request.contextPath}/admin" method="post">
        <label>아이디: <input type="text" name="adminId" required></label><br>
        <label>비밀번호: <input type="password" name="password" required></label><br>
        <button type="submit">로그인</button>
    </form>
</main>
</body>
</html>