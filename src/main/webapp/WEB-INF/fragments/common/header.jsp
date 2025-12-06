<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/header.css"/>
<header class="header__top-bar">
  <div class="header__top-inner">
    <div class="header__top-left">
      <div class="header__logo">MOCK</div>
      <ul class="header__top-menu">
        <li class="selected"><a href="/">홈</a></li>
        <li>리그</li>
        <li>탐색</li>
        <li>뉴스</li>
      </ul>
    </div>

    <div class="header__top-right">
      <button class="header__search-box">
        <span class="header__search-placeholder"><img src="${pageContext.request.contextPath}/static/assets/search_icon.png" alt="">여기를 눌러 검색</span>
      </button>
      <c:if test="${!isLoggedIn}">
        <a class="header__btn-login" href="/login">로그인</a>
      </c:if>
      <c:if test="${isLoggedIn}">
        <a class="header__btn-login" href="/mypage">마이페이지</a>
      </c:if>
    </div>
  </div>
</header>