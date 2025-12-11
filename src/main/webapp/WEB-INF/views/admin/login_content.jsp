<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
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

    <form action="${pageContext.request.contextPath}/admin" method="post">
        <label>아이디: <input type="text" name="adminId" required></label>
        <label>비밀번호: <input type="password" name="password" required></label>
        <% 
        String method = request.getMethod();
        if ("POST".equals(method) && request.getAttribute("errorMsg") != null && !((String)request.getAttribute("errorMsg")).trim().isEmpty()) { 
        %>
        <div style="color: red;">
            <%= request.getAttribute("errorMsg") %>
        </div>
        <% } %>
        <button type="submit">로그인</button>
    </form>
</main>
</body>
</html>