<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/header.css"/>
<header class="header__top-bar" style="border-bottom: #17171B 0.5px solid;">
  <div class="header__top-inner">
    <div class="header__top-left">
      <div class="header__logo">MOCK</div>
    </div>
    <div class="header__top-right">
      <a href="${pageContext.request.contextPath}/admin/logout" class="header__btn-logout">로그아웃</a>
    </div>
  </div>
</header>