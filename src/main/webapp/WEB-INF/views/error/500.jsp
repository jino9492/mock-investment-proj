<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"
         isErrorPage="true" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <title>500 | 서버 오류</title>
    <style>
        body {
            margin: 0;
            background-color: #F6F7F9;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
        }
        .error-wrap {
            height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .error-box {
            background: #fff;
            padding: 48px 56px;
            border-radius: 12px;
            box-shadow: 0 8px 24px rgba(0,0,0,0.08);
            text-align: center;
            width: 420px;
        }
        .error-code {
            font-size: 72px;
            font-weight: 700;
            color: #E74C3C;
            margin-bottom: 12px;
        }
        .error-title {
            font-size: 20px;
            font-weight: 600;
            margin-bottom: 8px;
        }
        .error-desc {
            font-size: 14px;
            color: #666;
            line-height: 1.6;
            margin-bottom: 24px;
        }
        .error-btn {
            display: inline-block;
            padding: 12px 20px;
            background: #2563EB;
            color: #fff;
            border-radius: 6px;
            text-decoration: none;
            font-size: 14px;
            font-weight: 500;
        }
        .error-btn:hover {
            background: #1E4FD7;
        }
    </style>
</head>
<body>

<div class="error-wrap">
    <div class="error-box">
        <div class="error-code">500</div>
        <div class="error-title">서버에서 문제가 발생했습니다</div>
        <div class="error-desc">
            일시적인 오류가 발생했습니다.<br>
            잠시 후 다시 시도해 주세요.
        </div>
        <a class="error-btn" href="<%=request.getContextPath()%>/">홈으로 이동</a>
    </div>
</div>

</body>
</html>
