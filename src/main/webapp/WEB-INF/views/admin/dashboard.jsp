<%--
  Created by IntelliJ IDEA.
  User: js123
  Date: 25. 11. 23.
  Time: 오후 11:30
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>대쉬보드</title>
</head>
<body>
    <header id="header">
        <jsp:include page="/WEB-INF/fragments/common/header_admin.jsp" />
    </header>
    <aside id="aside">
        <jsp:include page="${pageContext.request.contextPath}/WEB-INF/fragments/common/sidebar_left/sidebar.jsp" />
    </aside>
    <main id="main">
        
    </main>
</body>
</html>
