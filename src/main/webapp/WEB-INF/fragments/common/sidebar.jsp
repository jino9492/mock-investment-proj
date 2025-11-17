<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/common/sidebar.css"/>
<script src="static/js/common/sidebar.js"></script>
<aside class="sidebar__container">
  <div class="sidebar__work-area">
    <div class="sidebar__account-title">내 가상 계좌</div>
    <div class="sidebar__account-box">0,000,000원</div>

    <div class="sidebar__my-investment selected">asdf</div>
    <div class="sidebar__favorite">qwer</div>
  </div>
  <div class="sidebar__right-rail">

    <button class="sidebar__right-rail-toggle">
      <span class="sidebar__arrow-icon"><img src="${pageContext.request.contextPath}/static/assets/toggle.png" alt=""></span>
    </button>

    <button class="sidebar__rail-item sidebar__investment-btn">
      <span class="sidebar__rail-icon">icon</span>
      <span>내 투자</span>
    </button>

    <div class="sidebar__rail-item sidebar__favorite-btn">
      <div class="sidebar__rail-icon">icon</div>
      <div>관심</div>
    </div>
  </div>
</aside>
