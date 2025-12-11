<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>ADMIN LOGIN</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin/login.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/default_layout.css"/>
</head>
<body>

<main class="main-content">
    <h1>ADMIN LOGIN</h1>

    <% if (request.getAttribute("errorMsg") != null) { %>
    <p style="color: red;"><%= request.getAttribute("errorMsg") %></p>
    <% } %>

    <form action="${pageContext.request.contextPath}/admin" method="post">
        <label>아이디: <input type="text" name="adminId" required></label>
        <label>비밀번호: <input type="password" name="password" required></label>
        <button type="submit">로그인</button>
    </form>
</main>
</body>
</html>