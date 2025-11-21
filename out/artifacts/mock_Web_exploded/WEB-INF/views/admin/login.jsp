<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin/login.css"/>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>ADMINLOGIN</title>
</head>
<body>
<h1>관리자 로그인</h1>

<% if (request.getAttribute("errorMsg") != null) { %>
<p style="color: red;"><%= request.getAttribute("errorMsg") %></p>
<% } %>

<form action="<%= request.getContextPath() %>/admin" method="post">
    <label>아이디: <input type="text" name="adminId" required></label><br>
    <label>비밀번호: <input type="password" name="password" required></label><br>
    <button type="submit">로그인</button>
</form>
</body>
</html>