<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/toggle.css">
<%
  String defaultState = request.getParameter("isActive");
  String id = request.getParameter("id");
  request.setAttribute("state", defaultState);
  request.setAttribute("id", id);
%>
<div>
  <input type="checkbox" id="${id}" class="toggle__checkbox" ${state ? "checked" : ''}>
  <label for="${id}" class="toggle__label"></label>
</div>