<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!doctype html>
<html>
<head>
    <meta charset="UTF-8" />
    <title>404 - 페이지를 찾을 수 없음</title>

    <style>
        body {
            margin: 0;
            height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #f6f7f9;
        }

        .error-box {
            text-align: center;
            background: #ffffff;
            padding: 40px 48px;
            border-radius: 12px;
            box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08);
        }

        .error-box h1 {
            margin: 0 0 12px;
            font-size: 56px;
            font-weight: 700;
            color: #2563eb;
        }

        .error-box p {
            margin: 0 0 24px;
            font-size: 14px;
            color: #555;
        }

        .error-box a {
            display: inline-block;
            padding: 10px 18px;
            background-color: #2563eb;
            color: #ffffff;
            border-radius: 6px;
            text-decoration: none;
            font-size: 14px;
            font-weight: 500;
        }

        .error-box a:hover {
            background-color: #1e4fd7;
        }
    </style>
</head>

<body>
<div class="error-box">
    <h1>404</h1>
    <p>요청하신 페이지를 찾을 수 없어요!</p>
    <p>서비스 이용에 불편을 드려 죄송합니다.</p>
    <a href="<%=request.getContextPath()%>/">홈으로</a>
</div>
</body>
</html>
