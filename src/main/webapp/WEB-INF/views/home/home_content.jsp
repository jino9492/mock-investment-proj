<%@ page import="com.dongyang.mock.model.DTO.MemberDTO" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/home/main_page.css"/>
<script type="module" src="${pageContext.request.contextPath}/static/js/home/main_page.js"></script>
<main class="main__content">
  <div class="main__sub-title">
    <span class="main__dot-green"></span>
    <strong>국내 장 열림/닫힘</strong>
  </div>
  <!-- 계좌 / 뉴스 -->
  <section class="main__top-section">

    <!-- 계좌 카드 + 로그인 팝업 -->
    <div class="main__card-login-wrap">
      <section class="main__card-account ${isLoggedIn ? "logged-in" : ""}">
        <h1 class="main__page-title">○○○님의 가상 주식 계좌</h1>
        <div class="main__summary-grid">
          <div class="main__summary-box">

            <div class="main__summary-label">계좌 총 잔액</div>
            <div class="main__summary-value">0,000,000원</div>
          </div>
          <div class="main__summary-box">
            <div class="main__summary-label">상위</div>
            <div class="main__summary-value">0%</div>
          </div>
        </div>

        <div class="main__summary-box" style="margin-bottom: 10px;">
          <div class="main__summary-label">투자중인 금액</div>
          <div class="main__summary-value">
            0,000,000원 <span>(00.00%)</span>
          </div>
        </div>

        <div class="main__divider"></div>

        <table class="main__table-simple">
          <thead>
          <tr>
            <th></th>
            <th>주식 명</th>
            <th>총 금액</th>
            <th>총 수익</th>
          </tr>
          </thead>
          <tbody>
          <tr>
            <td>1</td>
            <td>주식회사이름</td>
            <td>000원</td>
            <td><span class="main__text-purple">000원  +/-0.00%</span></td>
          </tr>
          <tr>
            <td>2</td>
            <td>주식회사이름</td>
            <td>000원</td>
            <td><span class="main__text-purple">000원   +/-0.00%</span></td>
          </tr>
          <tr>
            <td>3</td>
            <td>주식회사이름</td>
            <td>000원</td>
            <td><span class="main__text-purple">000원   +/-0.00%</span></td>
          </tr>
          </tbody>
        </table>
      </section>

      <c:if test="${!isLoggedIn}">
        <!-- 로그인 팝업 -->
        <div class="login__modal">
          <div class="login__modal-content-wrapper">
            <div class="login__modal-title">로그인하고 <span style="color: #5F9BEE">모의투자</span>를 시작하세요!</div>

            <a class="login__modal-login" href="/login">로그인</a>
            <a class="login__modal-signup" href="/signup">회원가입</a>
          </div>
        </div>
      </c:if>
    </div>



    <section class="main__news_card">
      <div class="main__news_card-header">
        <div class="main__news_card-title">주요 뉴스</div>
      </div>

      <div class="main__news-columns">
        <!-- 국내 뉴스 블록 -->
        <section class="main__news-block">
          <div class="main__news-block-header">
            <span class="main__news-block-title">국내 뉴스</span>
            <button class="main__news-plus">+</button>
          </div>

          <div class="main__news-main">
            <div class="main__news-thumb"></div>

            <div class="main__news-main-right">
              <div class="main__news-headline">
                제목제목제목제목제목제목제목제목제목제목제목제목제목제목제목
              </div>
            </div>
          </div>

          <ul class="main__news-list">
            <li>테스트 메시지 입니다.</li>
            <li>테스트 메시지 입니다.</li>
            <li>테스트 메시지 입니다.</li>
            <li>테스트 메시지 입니다.</li>
          </ul>
        </section>

        <!-- 해외 뉴스 블록 -->
        <section class="main__news-block">
          <div class="main__news-block-header">
            <span class="main__news-block-title">해외 뉴스</span>
            <button class="main__news-plus">+</button>
          </div>

          <div class="main__news-main">
            <div class="main__news-thumb"></div>

            <div class="main__news-main-right">
              <div class="main__news-headline">
                제목제목제목제목제목제목제목제목제목제목제목제목제목제목제목
              </div>
            </div>
          </div>

          <ul class="main__news-list">
            <li>테스트 메시지 입니다. 테스트 메시지 입니다. 테스트 메시지 입니다.</li>
            <li>테스트 메시지 입니다.</li>
            <li>테스트 메시지 입니다.</li>
            <li>테스트 메시지 입니다.</li>

          </ul>
        </section>
      </div>
    </section>

  </section>


  <section class="main__index-section">
    <div class="main__index-row">
      <c:forEach var="id" items="${['KOSPI', 'KOSDAQ', 'KRX100']}">
        <div class="main__index-thumb">
          <jsp:include page="/WEB-INF/fragments/common/chart/sparkline.jsp">
            <jsp:param name="identifier" value="${id}"/>
          </jsp:include>
        </div>
        <div class="main__index-card">
          <div class="main__index-name">${id}</div>
          <div class="main__index-value">0,000.00</div>
        </div>
      </c:forEach>
<%--      <button class="main__index-arrow"> › </button>--%>
    </div>
  </section>

  <!-- 실시간 차트 -->
  <section>
    <div class="main__section-title">실시간 차트</div>

    <div class="main__bottom-section">

      <section>
        <div class="main__table-header-row">
          <span>순위 · 오늘 13:11 기준</span>
          <span></span>
          <span>현재가</span>
          <span>등락률</span>
        </div>

        <div class="main__list-row">
          <span>1</span>
          <span><span class="main__company-profile"></span>주식회사이름</span>
          <span>000원</span>
          <span class="main__text-purple">+/-0.00%</span>
        </div>

        <div class="main__list-row">
          <span>2</span>
          <span><span class="main__company-profile"></span>주식회사이름</span>
          <span>000원</span>
          <span class="main__text-purple">+/-0.00%</span>
        </div>

        <div class="main__list-row">
          <span>3</span>
          <span><span class="main__company-profile"></span>주식회사이름</span>
          <span>000원</span>
          <span class="main__text-purple">+/-0.00%</span>
        </div>

        <div class="main__list-row">
          <span>4</span>
          <span><span class="main__company-profile"></span>주식회사이름</span>
          <span>000원</span>
          <span class="main__text-purple">+/-0.00%</span>
        </div>

        <div class="main__list-row">
          <span>5</span>
          <span><span class="main__company-profile"></span>주식회사이름</span>
          <span>000원</span>
          <span class="main__text-purple">+/-0.00%</span>
        </div>

        <div class="main__list-row">
          <span>6</span>
          <span><span class="main__company-profile"></span>주식회사이름</span>
          <span>000원</span>
          <span class="main__text-purple">+/-0.00%</span>
        </div>
      </section>

      <!-- 오른쪽 차트 카드 -->
      <section class="main__card chart-card">
        <div class="main__chart-header">
          <strong>주식회사이름A</strong>
          <span>000원  +/-0.00%</span>
        </div>

        <div class="main__chart-box">차트</div>


        <div class="main__chart-info">
          <div>
            <span class="main__chart-info-label">기업명</span>
            <span>○○</span>
          </div>
          <div>
            <span class="main__chart-info-label">시가총액</span>
            <span>000,000,000,000원</span>
          </div>
          <div>
            <span class="main__chart-info-label">상장일</span>
            <span>0000.00.00</span>
          </div>
          <div>
            <span class="main__chart-info-label">발행주식수</span>
            <span>00,000주</span>
          </div>
        </div>

        <div class="main__chip-area">
          <div class="main__chip-title">주요 사업</div>
          <div class="main__chip-box"></div>
        </div>
      </section>

    </div>
  </section>

</main>