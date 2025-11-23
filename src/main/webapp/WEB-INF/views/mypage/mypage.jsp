<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="/default_layout.jsp">
    <jsp:param name="contentPage" value="/WEB-INF/views/mypage/mypage_content.jsp"/>
    <jsp:param name="menuItems" value="내 정보,자산,주문내역,수익분석"/>
    <jsp:param name="menuLinks" value="/mypage,/mypage/assets,/mypage/orders,/mypage/analysis"/>
</jsp:include>
