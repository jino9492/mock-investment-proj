<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
    String currentPage = (String) request.getAttribute("currentPage");

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

<script type="module" src="${pageContext.request.contextPath}/static/js/common/header.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/header.css"/>

<header class="header__top-bar">
    <div class="header__top-inner">
        <div class="header__top-left">
            <a href="/" class="header__logo">MOCK</a>
            <ul class="header__top-menu">
                <li class="${currentPage == '/' || currentPage == '' || currentPage == '/index.jsp' ? 'selected' : ''}">
                    <a href="/">홈</a>
                </li>
                <li class="${currentPage == '/league' ? 'selected' : ''}">
                    <a href="/league">리그</a>
                </li>
                <li class="${currentPage == '/explore' || currentPage == '/search' ? 'selected' : ''}">
                    <div>탐색</div>
                </li>
                <li class="${currentPage == '/news' ? 'selected' : ''}">
                    <div>뉴스</div>
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
                                   id="searchModalInput"
                                   placeholder="검색어를 입력하세요"
                                   aria-label="종목 검색"
                                   aria-autocomplete="list"
                                   aria-controls="searchResults"
                                   aria-expanded="false" />
                            <button type="button"
                                    class="search-modal__clear-btn"
                                    id="searchModalClearBtn"
                                    style="display: none;"
                                    aria-label="검색어 지우기">
                                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="currentColor" viewBox="0 0 16 16">
                                    <path d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0zM5.354 4.646a.5.5 0 1 0-.708.708L7.293 8l-2.647 2.646a.5.5 0 0 0 .708.708L8 8.707l2.646 2.647a.5.5 0 0 0 .708-.708L8.707 8l2.647-2.646a.5.5 0 0 0-.708-.708L8 7.293 5.354 4.646z"/>
                                </svg>
                            </button>
                        </div>
                    </form>

                    <!-- 검색 결과 (검색어 입력 시 표시) -->
                    <div class="search-modal__section" id="searchResultsSection" style="display: none;">
                        <ul class="search-modal__results" id="searchResults" role="listbox" aria-label="검색 결과">
                            <!-- JavaScript가 동적으로 채워 넣을 영역 -->
                        </ul>
                    </div>

                    <!-- 최근 검색 (입력이 비어있을 때 표시) -->
                    <div class="search-modal__section" id="searchRecentSection">
                        <div class="search-modal__section-header">
                            <span class="search-modal__section-title">최근 검색</span>
                        </div>

                        <!-- JS가 칩을 채워 넣을 영역 -->
                        <div class="search-modal__chips" id="searchRecentChips">

                        </div>
                    </div>

                    <!-- 인기 종목 (입력이 비어있을 때 표시) -->
                    <div class="search-modal__section" id="searchPopularSection">
                        <div class="search-modal__section-header">
                            <span class="search-modal__section-title">인기 종목</span>
                            <span class="search-modal__section-sub">현재 시각 기준</span>
                        </div>

                        <ul class="search-modal__list" id="searchPopularStocks">
                            <!-- JavaScript가 동적으로 채워 넣을 영역 -->
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
                        <div class="profile-menu__item profile-menu__user" id="profileMenuUser">
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
