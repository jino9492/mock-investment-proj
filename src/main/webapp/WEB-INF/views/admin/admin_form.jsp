<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<jsp:useBean id="now" class="java.util.Date" />
<c:set var="admin" value="${requestScope.admin}" />
<c:set var="isEdit" value="${requestScope.isEdit}" />
<c:set var="pageTitle" value="${isEdit ? '관리자 수정' : '관리자 등록'}" scope="request" />
<c:set var="pageStyles" value="admin/users.css" scope="request" />
<jsp:include page="/WEB-INF/fragments/admin/admin_layout_head.jsp" />
<jsp:include page="/WEB-INF/fragments/admin/admin_layout_header.jsp" />
<main class="users-page">
    <div class="page-header">
        <h1 class="page-header__title">${isEdit ? '관리자 수정' : '관리자 등록'}</h1>
        <p class="page-header__date" id="current-time">
            <fmt:formatDate value="${now}" pattern="yyyy년 MM월 dd일 HH:mm:ss" />
        </p>
    </div>
    <jsp:include page="/WEB-INF/fragments/admin/admin_status_alert.jsp" />

    <section class="stat-card" style="max-width: 800px;">
        <form method="post" action="${pageContext.request.contextPath}/admin/admins${isEdit ? '/form' : ''}" accept-charset="UTF-8">
            <c:if test="${isEdit}">
                <input type="hidden" name="action" value="update" />
                <input type="hidden" name="adminId" value="${admin.adminId}" />
            </c:if>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 16px;">
                <div class="form-field">
                    <label for="adminLoginId" class="form-field__label">로그인 아이디<span class="form-field__required">*</span></label>
                    <input type="text" id="adminLoginId" name="adminLoginId" class="form-field__input" 
                           value="${isEdit ? admin.adminUsername : ''}" 
                           required 
                           placeholder="로그인 아이디">
                </div>
                <div class="form-field">
                    <label for="adminName" class="form-field__label">관리자명<span class="form-field__required">*</span></label>
                    <input type="text" id="adminName" name="adminName" class="form-field__input" 
                           value="${isEdit ? admin.adminName : ''}" 
                           required placeholder="관리자명">
                </div>
            </div>

            <div class="form-field" style="margin-bottom: 16px;">
                <label for="adminPassword" class="form-field__label">
                    비밀번호
                    <c:if test="${!isEdit}">
                        <span class="form-field__required">*</span>
                    </c:if>
                    <c:if test="${isEdit}">
                        <span style="font-size: 12px; color: #666;">(변경할 경우에만 입력)</span>
                    </c:if>
                </label>
                <input type="password" id="adminPassword" name="adminPassword" class="form-field__input" 
                       ${isEdit ? '' : 'required'} 
                       minlength="6" placeholder="6자 이상">
            </div>

            <div style="display: flex; gap: 12px; justify-content: flex-end; margin-top: 24px;">
                <a href="${pageContext.request.contextPath}/admin/admins" 
                   class="modal__btn modal__btn--ghost" style="text-decoration: none; display: inline-flex; align-items: center; justify-content: center;">취소</a>
                <button type="submit" class="modal__btn modal__btn--primary">저장</button>
                <c:if test="${isEdit}">
                    <button type="button" class="modal__btn" 
                            style="background-color: #dc3545; color: white; border: none;"
                            onclick="if(confirm('정말 삭제하시겠습니까?')) { 
                                var form = document.createElement('form');
                                form.method = 'post';
                                form.action = '${pageContext.request.contextPath}/admin/admins/form';
                                var actionInput = document.createElement('input');
                                actionInput.type = 'hidden';
                                actionInput.name = 'action';
                                actionInput.value = 'delete';
                                var idInput = document.createElement('input');
                                idInput.type = 'hidden';
                                idInput.name = 'adminId';
                                idInput.value = '${admin.adminId}';
                                form.appendChild(actionInput);
                                form.appendChild(idInput);
                                document.body.appendChild(form);
                                form.submit();
                            }">삭제</button>
                </c:if>
            </div>
        </form>
    </section>
</main>
<jsp:include page="/WEB-INF/fragments/admin/admin_layout_footer.jsp" />

