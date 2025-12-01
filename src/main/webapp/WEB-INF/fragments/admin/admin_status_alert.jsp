<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:if test="${not empty param.status}">
    <c:set var="statusValue" value="${param.status}" />
    <div class="alert ${statusValue eq 'success' or statusValue eq 'update_success' or statusValue eq 'delete_success' ? 'alert--success' : 'alert--error'}">
        <c:choose>
            <c:when test="${statusValue eq 'success'}">새 회원이 성공적으로 추가되었습니다.</c:when>
            <c:when test="${statusValue eq 'update_success'}">회원 정보가 업데이트되었습니다.</c:when>
            <c:when test="${statusValue eq 'delete_success'}">회원이 성공적으로 삭제되었습니다.</c:when>
            <c:when test="${statusValue eq 'duplicate_email'}">이미 등록된 이메일입니다.</c:when>
            <c:when test="${statusValue eq 'update_duplicate_email'}">이미 사용 중인 이메일입니다.</c:when>
            <c:when test="${statusValue eq 'duplicate_nickname'}">이미 사용 중인 닉네임입니다.</c:when>
            <c:when test="${statusValue eq 'update_duplicate_nickname'}">이미 사용 중인 닉네임입니다.</c:when>
            <c:when test="${statusValue eq 'invalid'}">필수 입력 항목을 다시 확인해 주세요.</c:when>
            <c:when test="${statusValue eq 'not_found'}">요청한 회원을 찾을 수 없습니다.</c:when>
            <c:when test="${statusValue eq 'update_fail'}">회원 정보 업데이트에 실패했습니다.</c:when>
            <c:when test="${statusValue eq 'delete_fail'}">회원 삭제에 실패했습니다.</c:when>
            <c:otherwise>요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.</c:otherwise>
        </c:choose>
    </div>
</c:if>

