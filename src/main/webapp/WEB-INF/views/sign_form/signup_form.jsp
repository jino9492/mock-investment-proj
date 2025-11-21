<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>회원 가입</title>
    <style>
        body { font-family: Arial, sans-serif; background-color: #f4f7f6; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }
        .container {
            width: 400px;
            background: white;
            padding: 30px;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }
        h2 { text-align: center; color: #333; margin-bottom: 25px; }
        label {
            display: block;
            width: 100%;
            margin-top: 15px;
            font-weight: bold;
            color: #555;
            font-size: 0.95rem;
        }
        input[type="text"], input[type="password"], input[type="email"], input[type="date"] {
            width: 100%;
            padding: 10px;
            margin-top: 5px;
            border: 1px solid #ddd;
            border-radius: 6px;
            box-sizing: border-box;
            transition: border-color 0.3s;
        }
        input[type="text"]:focus, input[type="password"]:focus, input[type="email"]:focus, input[type="date"]:focus {
            border-color: #007bff;
            outline: none;
        }
        .radio-group label { display: inline-block; width: auto; font-weight: normal; margin-right: 15px; }
        .required::after { content: " *"; color: #dc3545; margin-left: 2px; }
        hr { border: 0; border-top: 1px solid #eee; margin: 20px 0; }
        .agreement p { font-size: 0.9rem; color: #666; }
        .agreement input[type="checkbox"] { margin-right: 8px; }

        input[type="submit"] {
            width: 100%;
            padding: 12px;
            background-color: #28a745;
            color: white;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-size: 1rem;
            margin-top: 25px;
            transition: background-color 0.3s;
        }
        input[type="submit"]:hover {
            background-color: #218838;
        }
        .error-message {
            color: #dc3545;
            text-align: center;
            margin-bottom: 15px;
            font-weight: bold;
        }
    </style>
</head>
<body>
<div class="container">
    <h2>회원 가입</h2>

    <!-- 컨트롤러에서 전달된 에러 메시지 표시 -->
    <% if (request.getAttribute("errorMsg") != null) { %>
    <p class="error-message"><%= request.getAttribute("errorMsg") %></p>
    <% } %>

    <!-- 액션을 /member로 설정하고 cmd=join을 hidden 필드로 전달 -->
    <form action="<%= request.getContextPath() %>/signup" method="post">
        <input type="hidden" name="cmd" value="join">

        <!-- 필수 입력 항목 -->
        <label for="nickname" class="required">닉네임</label>
        <input type="text" id="nickname" name="nickname" required value="<%= request.getParameter("nickname") != null ? request.getParameter("nickname") : "" %>"><br>

        <label for="password" class="required">비밀번호</label>
        <input type="password" id="password" name="password" required><br>

        <label for="name" class="required">이름</label>
        <input type="text" id="name" name="name" required value="<%= request.getParameter("name") != null ? request.getParameter("name") : "" %>"><br>

        <label for="email" class="required">이메일</label>
        <input type="email" id="email" name="email" required value="<%= request.getParameter("email") != null ? request.getParameter("email") : "" %>"><br>

        <!-- 선택/선언적 입력 항목 -->
        <label for="phone">연락처</label>
        <input type="text" id="phone" name="phone" value="<%= request.getParameter("phone") != null ? request.getParameter("phone") : "" %>"><br>

        <label for="birth">생년월일 (YYYY-MM-DD)</label>
        <input type="date" id="birth" name="birth" value="<%= request.getParameter("birth") != null ? request.getParameter("birth") : "" %>"><br>

        <label>프로필 공개 여부</label>
        <div class="radio-group">
            <%
                String memOpen = request.getParameter("memOpen");

                boolean isTrue = memOpen == null || "true".equalsIgnoreCase(memOpen);
                boolean isFalse = "false".equalsIgnoreCase(memOpen);
            %>
            <label style="width: auto;">
                <input type="radio" name="memOpen" value="true" <%= isTrue ? "checked" : "" %>> 공개
            </label>
            <label style="width: auto;">
                <input type="radio" name="memOpen" value="false" <%= isFalse ? "checked" : "" %>> 비공개
            </label>
        </div>

        <hr>
        <div class="agreement">
            <h3>약관 동의</h3>
            <p>
                <label style="width: auto;">
                    <input type="checkbox" name="agreeTerms" required <%= request.getParameter("agreeTerms") != null ? "checked" : "" %>> 필수 약관 동의
                </label>
            </p>
            <p>
                <label style="width: auto;">
                    <input type="checkbox" name="agreePrivacy" required <%= request.getParameter("agreePrivacy") != null ? "checked" : "" %>> 개인정보 수집/이용 동의
                </label>
            </p>
            <p>
                <label style="width: auto;">
                    <input type="checkbox" name="agreeThird" <%= request.getParameter("agreeThird") != null ? "checked" : "" %>> 제3자 정보 제공 동의 (선택)
                </label>
            </p>
        </div>

        <input type="submit" value="가입하기">
    </form>
</div>
</body>
</html>