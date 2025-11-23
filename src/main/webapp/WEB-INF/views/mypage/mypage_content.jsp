<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
                        <button class="my-page__edit-button">정보 수정</button>
                    </div>
                    <span class="my-page__subtitle">프로필</span>
                </div>

            </div>


            <!-- 프로필 카드 -->
            <div class="profile-card">

                <div class="profile-card__body">
                    <div class="profile-card__avatar"></div>
                    <div class="profile-card__nickname">닉네임</div>
                </div>
            </div>

            <!-- 기본 정보 카드 -->
            <div class="info-card">
                <div class="info-card__list">
                    <div class="info-card__item">
                        <span class="info-card__label">이름</span>
                        <span class="info-card__value">홍길동</span>
                    </div>

                    <div class="info-card__item">
                        <span class="info-card__label">생년월일</span>
                        <span class="info-card__value">0000.00.00</span>
                    </div>

                    <div class="info-card__item">
                        <span class="info-card__label">휴대폰 번호</span>
                        <span class="info-card__value">010-0000-0000</span>
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
    </main>
</div>
