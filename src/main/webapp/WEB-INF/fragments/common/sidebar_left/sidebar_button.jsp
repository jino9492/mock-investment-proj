<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/sidebar_left/sidebar_button.css">

<%
  String menuItemsParam = request.getParameter("menuItems");
  String menuLinksParam = request.getParameter("menuLinks");

  // 서블릿에서 설정한 현재 경로 가져오기
  String currentPage = (String) request.getAttribute("currentPath");

  // 없으면 다른 방법 시도
  if (currentPage == null) {
    currentPage = (String) request.getAttribute("javax.servlet.forward.request_uri");
  }

  if (currentPage == null) {
    String currentURL = request.getRequestURI();
    String contextPath = request.getContextPath();
    currentPage = currentURL.replace(contextPath, "");
  }

  // 선행 슬래시 정규화
  if (!currentPage.startsWith("/")) {
    currentPage = "/" + currentPage;
  }
  request.setAttribute("currentPage", currentPage);

  String[] menuItems = null;
  String[] menuLinks = null;

  if (menuItemsParam != null && !menuItemsParam.isEmpty()) {
    menuItems = menuItemsParam.split(",");
    request.setAttribute("menuItems", menuItems);
  }

  if (menuLinksParam != null && !menuLinksParam.isEmpty()) {
    menuLinks = menuLinksParam.split(",");
    request.setAttribute("menuLinks", menuLinks);
  }
%>

<c:if test="${not empty menuItems}">
  <c:forEach var="title" items="${menuItems}" varStatus="status">
    <%-- 메뉴 제목 설정 --%>
    <c:set var="titleText" value="${title.trim()}" />

    <%-- 메뉴 링크 설정 --%>
    <c:set var="rawLink" value="${(not empty menuLinks and status.index < fn:length(menuLinks)) ? menuLinks[status.index] : '#'}" />

    <%-- link 변수에 초기값 할당 및 트림 --%>
    <c:set var="link" value="${rawLink.trim()}" />

    <%-- 슬래시(/) 접두사 추가 로직 (JSTL에서는 JEXL/JSP EL 함수를 사용하거나, C:choose로 조건을 분리) --%>
    <c:choose>
      <c:when test="${!link.startsWith('/') and link != '#'}">
        <c:set var="link" value="/${link}" />
      </c:when>
      <c:otherwise>
        <%-- link가 이미 '/'로 시작하거나 '#'인 경우는 그대로 사용 --%>
      </c:otherwise>
    </c:choose>

    <%-- 활성화(Active) 상태 확인 --%>
    <c:set var="isActive" value="${fn:contains(currentPage, link) and link != '#'}" />

    <a href="${link}" class="sidebar-left__btn ${isActive ? 'active' : ''}">
      <span class="sidebar-left__btn-text">${titleText}</span>
    </a>
  </c:forEach>
</c:if>