<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/toggle.css">
<%
  String isActiveString = request.getParameter("isActive");
  String id = request.getParameter("id");
  String name = request.getParameter("name");

  boolean isActiveBool = Boolean.parseBoolean(isActiveString);

  request.setAttribute("isActive", isActiveBool);
  request.setAttribute("toggleId", id);
  request.setAttribute("toggleName", name);
%>
<div>
  <input
          type="checkbox"
          id="${toggleId}"
          class="toggle__checkbox"
          name="${toggleName}"
  ${isActive ? "checked" : ''}
  >
  <label for="${toggleId}" class="toggle__label"></label>
</div>