<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/sidebar_left/sidebar.css">
<aside class="sidebar-left__container">
  <jsp:include page="/WEB-INF/fragments/common/sidebar_left/sidebar_button.jsp">
    <jsp:param name="menuItems" value="로그인,대시보드,회원 관리,거래 관리" />
    <jsp:param name="menuLinks" value="/admin,/admin/dashboard,/admin/users,/admin/trades" />
  </jsp:include>
</aside>
