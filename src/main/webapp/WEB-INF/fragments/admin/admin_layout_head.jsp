<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${pageTitle}" default="관리자" /></title>
    <!-- 공통 CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/header.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/sidebar_left/sidebar.css"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/sidebar_left/sidebar_button.css"/>
    <!-- 페이지별 CSS -->
    <c:if test="${not empty pageStyles}">
        <c:choose>
            <c:when test="${pageStyles.getClass().name == 'java.lang.String'}">
                <c:forTokens var="style" items="${pageStyles}" delims=",">
                    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/${style.trim()}"/>
                </c:forTokens>
            </c:when>
            <c:otherwise>
                <c:forEach var="style" items="${pageStyles}">
                    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/${style}"/>
                </c:forEach>
            </c:otherwise>
        </c:choose>
    </c:if>
</head>
<body>

