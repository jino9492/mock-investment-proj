<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // HomeController가 attribute를 설정하도록 forward
    // HomeController는 /와 /home을 처리하므로, /home으로 forward
    request.getRequestDispatcher("/home").forward(request, response);
%>

