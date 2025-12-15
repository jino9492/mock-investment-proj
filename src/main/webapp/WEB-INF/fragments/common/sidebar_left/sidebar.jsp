<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/sidebar_left/sidebar.css">
<script src="${pageContext.request.contextPath}/static/js/common/sidebar_left/sidebar_left.js" defer></script>

<%
  String menuItemsParam = request.getParameter("menuItems");
  String menuLinksParam = request.getParameter("menuLinks");
  String showSiteLinkParam = request.getParameter("showSiteLink");

  String currentPath = (String) request.getAttribute("currentUrlPath");
  request.setAttribute("currentPage", currentPath);

  String[] menuItems = null;
  String[] menuLinks = null;
  boolean showSiteLink = "true".equals(showSiteLinkParam);

  if (menuItemsParam != null && !menuItemsParam.isEmpty()) {
    menuItems = menuItemsParam.split(",");
    request.setAttribute("menuItems", menuItems);
  }

  if (menuLinksParam != null && !menuLinksParam.isEmpty()) {
    menuLinks = menuLinksParam.split(",");
    request.setAttribute("menuLinks", menuLinks);
  }
  
  request.setAttribute("showSiteLink", showSiteLink);
%>

<aside class="sidebar-left__container">
  <c:if test="${showSiteLink}">
    <a href="${pageContext.request.contextPath}/" class="sidebar-left__site-link">
      <span>사이트 바로가기</span>
      <span class="sidebar-left__site-link-arrow">→</span>
    </a>
  </c:if>
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
      <c:set var="isActive" value="${currentPage eq link}" />
      <jsp:include page="sidebar_button.jsp">
        <jsp:param name="text" value="${titleText}"/>
        <jsp:param name="link" value="${link}"/>
        <jsp:param name="isActive" value="${isActive}"/>
      </jsp:include>
    </c:forEach>
  </c:if>
</aside>