<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="/WEB-INF/fragments/common/header_admin.jsp" />
<jsp:include page="/WEB-INF/fragments/common/sidebar_left/sidebar.jsp">
    <jsp:param name="menuItems" value="대시보드,회원 관리,거래 관리,시스템 설정" />
    <jsp:param name="menuLinks" value="/admin/dashboard,/admin/users,/admin/trades,/admin/settings" />
    <jsp:param name="showSiteLink" value="true" />
</jsp:include>

