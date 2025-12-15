<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         isErrorPage="true" %>
<%
    Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
    String requestUri  = (String) request.getAttribute("jakarta.servlet.error.request_uri");
    Throwable exception = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>오류 발생</title>
    <style>
        body {
            margin: 0;
            background-color: #F6F7F9;
        }
        .error-wrap {
            height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .error-box {
            background: #fff;
            padding: 44px 52px;
            border-radius: 12px;
            box-shadow: 0 8px 24px rgba(0,0,0,0.08);
            width: 480px;
        }
        .error-title {
            font-size: 22px;
            font-weight: 700;
            margin-bottom: 12px;
        }
        .error-desc {
            font-size: 14px;
            color: #555;
            line-height: 1.7;
            margin-bottom: 20px;
        }
        .error-meta {
            font-size: 12px;
            color: #888;
            background: #F3F4F6;
            padding: 12px;
            border-radius: 6px;
            margin-bottom: 24px;
        }
        .error-actions a {
            display: inline-block;
            margin-right: 8px;
            padding: 10px 16px;
            border-radius: 6px;
            text-decoration: none;
            font-size: 13px;
            font-weight: 500;
        }
        .btn-home {
            background: #2563EB;
            color: #fff;
        }
        .btn-back {
            background: #E5E7EB;
            color: #111;
        }
    </style>
</head>
<body>

<div class="error-wrap">
    <div class="error-box">
        <div class="error-title">문제가 발생했습니다</div>
        <div class="error-desc">
            요청을 처리하는 도중 오류가 발생했습니다.<br>
            문제가 지속되면 관리자에게 문의해주세요.
        </div>

        <div class="error-meta">
            상태 코드 : <%= statusCode != null ? statusCode : "알 수 없음" %><br>
            요청 경로 : <%= requestUri != null ? requestUri : "-" %>
        </div>

        <div class="error-actions">
            <a class="btn-home" href="<%=request.getContextPath()%>/">홈으로</a>
            <a class="btn-back" href="javascript:history.back()">이전 페이지</a>
        </div>
    </div>
</div>

</body>
</html>
