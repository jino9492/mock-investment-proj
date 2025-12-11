<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/admin/member_table.css" />
<div class="table-wrapper">
  <table class="data-table">
    <thead class="data-table__head">
    <tr>
      <th class="data-table__header-cell">회원ID</th>
      <th class="data-table__header-cell">닉네임</th>
      <th class="data-table__header-cell">이메일</th>
      <th class="data-table__header-cell">가입일</th>
      <th class="data-table__header-cell">마지막 접속</th>
      <th class="data-table__header-cell">상태</th>
      <th class="data-table__header-cell">총 자산</th>
      <th class="data-table__header-cell">수익률</th>
      <th class="data-table__header-cell">상세</th>
    </tr>
    </thead>
    <tbody class="data-table__body">
    <c:choose>
      <c:when test="${not empty memberList}">
        <c:forEach var="member" items="${memberList}">
          <tr class="data-table__row">
            <td class="data-table__cell">${member.memId}</td>
            <td class="data-table__cell">${member.memNickname}</td>
            <td class="data-table__cell">${member.memEmail}</td>
            <td class="data-table__cell">
              <c:choose>
                <c:when test="${not empty member.memJoinAt}">
                  <fmt:formatDate value="${member.memJoinAt}" pattern="yyyy.MM.dd" />
                </c:when>
                <c:otherwise>-</c:otherwise>
              </c:choose>
            </td>
            <td class="data-table__cell">
              <c:choose>
                <c:when test="${not empty member.memLastLoginAt}">
                  <fmt:formatDate value="${member.memLastLoginAt}" pattern="yyyy.MM.dd HH:mm" />
                </c:when>
                <c:otherwise>-</c:otherwise>
              </c:choose>
            </td>
            <td class="data-table__cell">
              <c:out value="${member.statusLabel}" />
            </td>
            <td class="data-table__cell">
              <fmt:formatNumber value="${member.virtualCash}" type="number" groupingUsed="true" />
              원
            </td>
            <td class="data-table__cell">
              <c:choose>
                <c:when test="${not empty member.profitRate}">
                  <fmt:formatNumber value="${member.profitRate}" type="number" maxFractionDigits="1" />%
                </c:when>
                <c:otherwise>-</c:otherwise>
              </c:choose>
            </td>
            <td class="data-table__cell">
              <a class="data-table__detail-link"
                 href="${pageContext.request.contextPath}/admin/users/detail?memberId=${member.memId}"
                 aria-label="${member.memNickname} 상세 정보 보기">
                상세 보기
              </a>
            </td>
          </tr>
        </c:forEach>
      </c:when>
      <c:otherwise>
        <tr>
          <td class="data-table__cell" colspan="9">등록된 회원이 없습니다.</td>
        </tr>
      </c:otherwise>
    </c:choose>
    </tbody>
  </table>

  <!-- 페이지네이션 -->
  <div class="pagination">
    <button class="pagination__btn">이전</button>
    <button class="pagination__btn">1</button>
    <button class="pagination__btn">2</button>
    <button class="pagination__btn">3</button>
    <button class="pagination__btn">4</button>
    <button class="pagination__btn">5</button>
    <button class="pagination__btn">다음</button>
  </div>
</div>