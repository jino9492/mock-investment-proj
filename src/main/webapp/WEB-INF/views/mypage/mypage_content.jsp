<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="jps" uri="http://java.sun.com/jsp/jstl/core" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/mypage/mypage.css"/>

<div class="my-page">
    <jsp:include page="/WEB-INF/fragments/common/sidebar_left/sidebar.jsp">
        <jsp:param name="menuItems" value="내 정보,자산,주문내역,수익분석"/>
        <jsp:param name="menuLinks" value="/mypage,/mypage/assets,/mypage/orders,/mypage/analysis"/>
    </jsp:include>


    <!-- 메인 컨텐츠 -->
    <main class="my-page__content">
        <section class="my-page__section my-page__section--profile">
            <div class="my-page__section-header">
                <div class="my-page__title-wrap">
                    <div class="my-page__title-row">
                        <h1 class="my-page__title">내 정보</h1>
                        <form action="${pageContext.request.contextPath}/mypage" method="get">
                            <button class="my-page__edit-button" name="modify" value="1">정보 수정</button>
                        </form>
                    </div>
                </div>

            </div>

            <!-- 프로필 카드 -->
            <span class="my-page__subtitle">프로필</span>
            <div class="profile-card">
                <div class="profile-card__body">
                    <div class="profile-card__avatar"></div>
                    <div class="profile-card__item-wrapper">
                        <div class="profile-card__item emphasize">닉네임</div>
                        <div class="profile-card__item">example@gmail.com</div>
                    </div>
                </div>
            </div>
        </section>
        <c:if test="${modify == null or modify == 0}">
            <section class="my-page__section my-page__section--asset">
                <span class="my-page__subtitle">자산</span>
                <div class="profile-card">
                    <div class="profile-card__body">
                        <div class="profile-card__item">1,000,000원</div>
                    </div>
                </div>

                <span class="my-page__subtitle--small">11월 수익</span>
                <div class="profile-card">
                    <div class="profile-card__body">
                        <div class="profile-card__item">100,000원</div>
                    </div>
                </div>

                <div class="profile-card">
                    <div class="profile-card__body flex-column">
                        <div class="profile-card__item-wrapper">
                            <div class="profile-card__item">주문 가능 금액</div>
                            <div class="profile-card__item">100,000원</div>
                        </div>
                        <div class="profile-card__item-wrapper">
                            <div class="profile-card__item">주문 가능 금액</div>
                            <div class="profile-card__item">100,000원</div>
                        </div>
                    </div>
                </div>
            </section>


            <!-- 약관 섹션 -->
            <section class="my-page__section my-page__section--agreement">
                <div class="my-page__section-title">약관 및 개인정보 처리 동의</div>

                <div class="agreement-card">
                    <div class="agreement-card__title">약관 및 개인정보 처리 동의</div>
                    <ul class="agreement-card__list">
                        <li class="agreement-card__item">
                  <span class="agreement-card__item-text">
                    개인(신용)정보 수집이용 동의(회원가입/휴대폰 본인인증)
                  </span>
                            <span class="agreement-card__item-icon">&gt;</span>
                        </li>
                        <li class="agreement-card__item">
                  <span class="agreement-card__item-text">
                    준회원 이용약관
                  </span>
                            <span class="agreement-card__item-icon">&gt;</span>
                        </li>
                        <li class="agreement-card__item">
                  <span class="agreement-card__item-text">
                    개인정보 제3자 제공 동의
                  </span>
                            <span class="agreement-card__item-icon">&gt;</span>
                        </li>
                    </ul>
                </div>
            </section>
        </c:if>
        <c:if test="${modify == 1}">
            <section class="my-page__section my-page__section--modify">
                <span class="my-page__subtitle">정보 수정</span>
                <div class="profile-card">
                    <div class="profile-card__body flex-column">
                        <div class="profile-card__item-wrapper">
                            <div class="profile-card__item"><label for="new-name">새 닉네임</label></div>
                            <div class="profile-card__item"><input type="text" id="new-name"/></div>
                        </div>
                        <div class="profile-card__item-wrapper">
                            <div class="profile-card__item"><label for="new-password">새 비밀번호</label></div>
                            <div class="profile-card__item"><input type="text" id="new-password"/></div>
                        </div>
                        <div class="profile-card__item-wrapper">
                            <div class="profile-card__item"><label for="new-password-check">새 비밀번호 확인</label></div>
                            <div class="profile-card__item"><input type="text" id="new-password-check"/></div>
                        </div>
                        <div class="profile-card__item-wrapper">
                            <div class="profile-card__item"><label for="notification-toggle">알림 설정</label></div>
                            <jsp:include page="${pageContext.request.contextPath}/WEB-INF/fragments/common/toggle.jsp">
                                <jsp:param name="isActive" value="false"/>
                                <jsp:param name="id" value="notification-toggle" />
                            </jsp:include>
                        </div>
                    </div>
                </div>
            </section>
        </c:if>
    </main>
</div>
