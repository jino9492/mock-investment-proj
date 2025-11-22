<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>

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

  String[] menuItems = null;
  String[] menuLinks = null;

  if (menuItemsParam != null && !menuItemsParam.isEmpty()) {
    menuItems = menuItemsParam.split(",");
  }

  if (menuLinksParam != null && !menuLinksParam.isEmpty()) {
    menuLinks = menuLinksParam.split(",");
  }
%>

<%--<!-- 디버깅 -->--%>
<%--<div style="background: yellow; padding: 10px; margin-bottom: 20px;">--%>
<%--  <strong>Debug Info:</strong><br>--%>
<%--  currentPath attribute: [<%= request.getAttribute("currentPath") %>]<br>--%>
<%--  forward URI: [<%= request.getAttribute("javax.servlet.forward.request_uri") %>]<br>--%>
<%--  request URI: [<%= request.getRequestURI() %>]<br>--%>
<%--  currentPage (최종): [<%= currentPage %>]<br>--%>
<%--  <% if (menuLinks != null) {--%>
<%--    for (int i = 0; i < menuLinks.length; i++) {--%>
<%--      out.println("link[" + i + "]: [" + menuLinks[i].trim() + "]<br>");--%>
<%--    }--%>
<%--  } %>--%>
<%--</div>--%>

<%
  if (menuItems != null && menuItems.length > 0) {
    for (int i = 0; i < menuItems.length; i++) {
      String title = menuItems[i].trim();
      String link = (menuLinks != null && i < menuLinks.length) ? menuLinks[i].trim() : "#";

      if (!link.startsWith("/") && !link.equals("#")) {
        link = "/" + link;
      }

      boolean isActive = currentPage.equals(link);
%>
<a href="<%= link %>" class="sidebar-btn <%= isActive ? "active" : "" %>">
  <span class="sidebar-btn-text"><%= title %></span>
</a>
<%
    }
  }
%>