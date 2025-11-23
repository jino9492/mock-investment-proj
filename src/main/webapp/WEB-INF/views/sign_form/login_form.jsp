<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>로그인</title>
    <style>
        body { font-family: Arial, sans-serif; background-color: #f4f7f6; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }
        .container { width: 350px; background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }
        h2 { text-align: center; color: #333; margin-bottom: 25px; }
        label { display: block; width: 100%; margin-top: 15px; font-weight: bold; color: #555; font-size: 0.95rem; }
        input[type="text"], input[type="password"] { width: 100%; padding: 10px; margin-top: 5px; border: 1px solid #ddd; border-radius: 6px; box-sizing: border-box; }
        input[type="submit"] { width: 100%; padding: 12px; background-color: #007bff; color: white; border: none; border-radius: 6px; cursor: pointer; font-size: 1rem; margin-top: 25px; transition: background-color 0.3s; }
        input[type="submit"]:hover { background-color: #0056b3; }
        .message { text-align: center; margin-bottom: 15px; font-weight: bold; }
        .error-message { color: #dc3545; }
        .success-message { color: #28a745; }
    </style>
</head>
<body>
<div class="container">
    <h2>로그인</h2>

    <!-- 회원 가입 성공 메시지 (doPost(cmd=join) 성공 시) -->
    <% if (request.getAttribute("msg") != null) { %>
    <p class="message success-message"><%= request.getAttribute("msg") %></p>
    <% } %>
    <!-- 로그인 실패 메시지 (doPost(cmd=login) 실패 시) -->
    <% if (request.getAttribute("errorMsg") != null) { %>
    <p class="message error-message"><%= request.getAttribute("errorMsg") %></p>
    <% } %>

    <!-- form action은 /member, hidden cmd는 login으로 설정 -->
    <form action="<%= request.getContextPath() %>/login" method="post">
        <input type="hidden" name="cmd" value="login">

        <label for="email">이메일</label>
        <input type="text" id="email" name="email" required><br>

        <label for="password">비밀번호</label>
        <input type="password" id="password" name="password" required><br>

        <input type="submit" value="로그인">
    </form>

    <p style="text-align: center; margin-top: 20px;">
        <!-- 회원 가입 폼으로 이동 -->
        <a href="<%= request.getContextPath() %>/signup">회원 가입</a>
    </p>
</div>
</body>
</html>