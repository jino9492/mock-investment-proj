<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%
  String textParam = request.getParameter("text");
  String linkParam = request.getParameter("link");
  String isActiveParam = request.getParameter("isActive");

  System.out.println(isActiveParam);

  request.setAttribute("text", textParam);
  request.setAttribute("link", linkParam);
  request.setAttribute("isActive", isActiveParam);
%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/sidebar_left/sidebar_button.css">
<a href="${link}" class="sidebar-left__btn ${isActive ? 'active' : ''}">
  <span class="sidebar-left__btn-text">${text}</span>
</a>