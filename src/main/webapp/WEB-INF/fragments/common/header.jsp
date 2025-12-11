<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
    // 어드민 사이드바 방식 참고: 현재 경로를 정확히 계산
    // 1. 컨트롤러에서 설정한 currentPage 우선 사용
    String currentPage = (String) request.getAttribute("currentPage");

    // 2. 없으면 servletPath 사용
    if (currentPage == null || currentPage.isEmpty()) {
        String servletPath = (String) request.getAttribute("javax.servlet.forward.servlet_path");
        if (servletPath == null) {
            servletPath = request.getServletPath();
        }
        if (servletPath == null || servletPath.isEmpty()) {
            String requestURI = request.getRequestURI();
            String contextPath = request.getContextPath();
            servletPath = requestURI.replaceFirst(contextPath, "");
            if (servletPath.isEmpty()) {
                servletPath = "/";
            }
        }
        currentPage = servletPath;
    }
    request.setAttribute("currentPage", currentPage);
%>

<script type="module" src="${pageContext.request.contextPath}/static/js/common/sidebar/header.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/header.css"/>

<header class="header__top-bar">
    <div class="header__top-inner">
        <div class="header__top-left">
            <div class="header__logo">MOCK</div>
            <ul class="header__top-menu">
                <li class="${currentPage == '/' || currentPage == '' || currentPage == '/index.jsp' ? 'selected' : ''}">
                    <a href="/">홈</a>
                </li>
                <li class="${currentPage == '/league' ? 'selected' : ''}">
                    <a href="/league">리그</a>
                </li>
                <li class="${currentPage == '/explore' || currentPage == '/search' ? 'selected' : ''}">
                    <a href="/explore">탐색</a>
                </li>
                <li class="${currentPage == '/news' ? 'selected' : ''}">
                    <a href="/news">뉴스</a>
                </li>
            </ul>
        </div>

        <div class="header__top-right">
            <!-- 상단 검색 버튼 -->
            <button class="header__search-box">
                <span class="header__search-placeholder">
                    <img src="${pageContext.request.contextPath}/static/assets/search_icon.png" alt="">
                    여기를 눌러 검색
                </span>
            </button>

            <!-- 검색 모달 오버레이 -->
            <div class="search-modal-overlay" id="searchModalOverlay">
                <div class="search-modal" role="dialog" aria-modal="true">

                    <!-- 검색 폼 -->
                    <form class="search-modal__form"
                          action="${pageContext.request.contextPath}/search"
                          method="get">
                        <div class="search-modal__input-wrap">
                            <img src="${pageContext.request.contextPath}/static/assets/search_icon.png"
                                 alt=""
                                 class="search-modal__search-icon" />
                            <input type="text"
                                   class="search-modal__input"
                                   name="q"
                                   placeholder="검색어를 입력하세요" />
                        </div>
                    </form>

                    <!-- 최근 검색 -->
                    <div class="search-modal__section">
                        <div class="search-modal__section-header">
                            <span class="search-modal__section-title">최근 검색</span>
                            <button type="button" class="search-recent__clear">전체 삭제</button>
                        </div>

                        <!-- JS가 칩을 채워 넣을 영역 -->
                        <div class="search-modal__chips" id="searchRecentChips">

                        </div>
                    </div>


                    <div class="search-modal__section">
                        <div class="search-modal__section-header">
                            <span class="search-modal__section-title">인기 검색</span>
                            <span class="search-modal__section-sub">현재 시각 기준</span>
                        </div>

                        <ul class="search-modal__list">
                            
                            <li class="search-modal__list-item">
                                <span class="search-modal__rank">1</span>
                                <span class="search-modal__name">주식회사A</span>
                                <span class="search-modal__change">+0.00%</span>
                            </li>
                            <li class="search-modal__list-item">
                                <span class="search-modal__rank">2</span>
                                <span class="search-modal__name">주식회사B</span>
                                <span class="search-modal__change">-0.15%</span>
                            </li>
                            <li class="search-modal__list-item">
                                <span class="search-modal__rank">3</span>
                                <span class="search-modal__name">주식회사C</span>
                                <span class="search-modal__change">+1.20%</span>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>

            <!-- 로그인 / 프로필 -->
            <c:if test="${!isLoggedIn}">
                <a class="header__btn-login" href="/login">로그인</a>
            </c:if>

            <c:if test="${isLoggedIn}">
                <div class="header__profile">
                    <button type="button" class="header__profile-btn">
                        PROFILE
                    </button>

                    <!-- 프로필 드롭다운 -->
                    <div class="header__profile-dropdown" id="headerProfileDropdown">
                        <div class="profile-menu__item profile-menu__user">
                            <span class="profile-menu__icon">👤</span>
                            <span class="profile-menu__name">
                                    ${member.memNickname}
                            </span>
                            <a href="/mypage" class="profile-menu__badge">내 정보</a>
                        </div>

                        <button type="button"
                                class="profile-menu__item profile-menu__logout"
                                onclick="location.href='/login'">
                            <span class="profile-menu__icon">↩</span>
                            <span class="profile-menu__text">로그아웃</span>
                        </button>
                    </div>
                </div>
            </c:if>
        </div>
    </div>
</header>
