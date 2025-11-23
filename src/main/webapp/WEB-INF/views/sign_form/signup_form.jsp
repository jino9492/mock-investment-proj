<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/home/sign_form/signup_form.css"/>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8" />
    <title>MOCK 회원가입</title>
</head>
<body class="signup__page-wrap">
<header class="signup__header">
    <div class="signup__brand">로고</div>
    <button class="signup__close-button" aria-label="닫기">✕</button>
</header>

<main class="signup__content">
    <section class="signup__card">
        <h1 class="signup__card-title">회원가입</h1>

        <form class="signup__card-form">
            <div class="signup__card-field">
                <input
                        type="text"
                        class="signup__card-input"
                        placeholder="이메일"
                        name="email"
                />
            </div>

            <div class="signup__card-field">
                <input
                        type="password"
                        class="signup__card-input"
                        placeholder="비밀번호"
                        name="password"
                />
            </div>

            <div class="signup__card-field">
                <input
                        type="text"
                        class="signup__card-input"
                        placeholder="닉네임"
                        name="nickname"
                />
            </div>




            <div class="signup__agreements">
                <label class="signup__agreement-all">
                    <input type="checkbox" class="signup__agreement-checkbox" />
                    <span class="signup__agreement-all-label">필수 약관에 모두 동의</span>
                </label>

                <div class="signup__agreement-divider"></div>

                <div class="signup__agreement-list">
                    <label class="signup__agreement-item">
                        <input type="checkbox" class="signup__agreement-checkbox" />
                        <span class="signup__agreement-label">
                개인정보 수집 · 이용 동의
              </span>
                    </label>
                    <label class="signup__agreement-item">
                        <input type="checkbox" class="signup__agreement-checkbox" />
                        <span class="signup__agreement-label">
                개인정보 제 3자 제공 동의
              </span>
                    </label>
                    <label class="signup__agreement-item">
                        <input type="checkbox" class="signup__agreement-checkbox" />
                        <span class="signup__agreement-label">
                알림 수신 동의 (선택)
              </span>
                    </label>
                </div>
            </div>

            <button type="submit" class="signup__card-button">
                회원가입
            </button>
        </form>

        <p class="signup__card-footer">
        <span class="signup__card-footer-text">
          MOCK의 회원인가요?
        </span>
            <a href="login_form.jsp" class="signup__card-footer-link">로그인</a>
        </p>
    </section>
</main>
</body>
</html>
