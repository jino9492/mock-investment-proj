<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
  // 어드민 사이드바 방식 참고: 현재 경로를 정확히 계산
  // 1. 컨트롤러에서 설정한 currentPage 우선 사용
  String currentPage = (String) request.getAttribute("currentPage");
  
  // 2. 없으면 servletPath 사용
  if (currentPage == null || currentPage.isEmpty()) {
    String servletPath = (String) request.getAttribute("javax.servlet.forward.servlet_path");
    if (servletPath == null) {
      servletPath = request.getServletPath();
    }
    if (servletPath == null || servletPath.isEmpty()) {
      String requestURI = request.getRequestURI();
      String contextPath = request.getContextPath();
      servletPath = requestURI.replaceFirst(contextPath, "");
      if (servletPath.isEmpty()) {
        servletPath = "/";
      }
    }
    currentPage = servletPath;
  }
  request.setAttribute("currentPage", currentPage);
%>

<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/header.css"/>
<header class="header__top-bar">
  <div class="header__top-inner">
    <div class="header__top-left">
      <div class="header__logo">MOCK</div>
        <ul class="header__top-menu">
          <li class="${currentPage == '/' || currentPage == '' || currentPage == '/index.jsp' ? 'selected' : ''}"><a href="/">홈</a></li>
          <li class="${currentPage == '/league' ? 'selected' : ''}"><a href="/league">리그</a></li>
          <li class="${currentPage == '/explore' || currentPage == '/search' ? 'selected' : ''}"><a href="/">탐색</a></li>
          <li class="${currentPage == '/news' ? 'selected' : ''}"><a href="/">뉴스</a></li>
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