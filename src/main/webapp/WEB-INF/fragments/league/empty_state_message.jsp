<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%-- 빈 상태 메시지 Fragment --%>
<%-- 사용법: <jsp:include page="/WEB-INF/fragments/league/empty_state_message.jsp"> --%>
<%--         <jsp:param name="colspan" value="5" /> --%>
<%--         <jsp:param name="message" value="보유하고 있는 주식이 없습니다." /> --%>
<%--         </jsp:include> --%>

<%
    String colspanParam = request.getParameter("colspan");
    String messageParam = request.getParameter("message");
    
    int colspan = 5;
    if (colspanParam != null && !colspanParam.isEmpty()) {
        try {
            colspan = Integer.parseInt(colspanParam);
        } catch (NumberFormatException e) {
            colspan = 5;
        }
    }
    
    String message = "보유하고 있는 주식이 없습니다.";
    if (messageParam != null && !messageParam.isEmpty()) {
        message = messageParam;
    }
    
    request.setAttribute("colspan", colspan);
    request.setAttribute("message", message);
%>

<tr>
    <td colspan="${colspan}" style="text-align: center; color: #999; padding: 20px 0; font-size: 16px;">
        ${message}
    </td>
</tr>

