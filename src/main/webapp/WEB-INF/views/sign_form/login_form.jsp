<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8" />
    <title>MOCK 로그인</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/sign_form/login_form.css"/>
</head>
<body class="login__page-wrap">
<header class="login__header">
    <div class="login__brand">MOCK</div>
    <a class="login__close-button" aria-label="닫기" href="/">✕</a>
</header>

<main class="login__content">
    <section class="login__card">
        <h1 class="login__card-title">로그인</h1>

        <form class="login__card-form" method="POST">
            <div class="login__card-field">
                <input
                        type="email"
                        class="login__card-input"
                        placeholder="이메일"
                        name="email"
                />
            </div>

            <div class="login__card-field">
                <input
                        type="password"
                        class="login__card-input"
                        placeholder="비밀번호"
                        name="password"
                />
            </div>

            <button type="submit" class="login__card-button">로그인</button>
        </form>

        <p class="login__card-footer">
        <span class="login__card-footer-text">
          아직 MOCK의 회원이 아닌가요?
        </span>
            <a href="/signup" class="login__card-footer-link">가입하기</a>
        </p>
    </section>
</main>
</body>
</html>
