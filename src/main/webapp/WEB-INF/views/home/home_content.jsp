<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

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
        <h1 class="league__page-title">
          ${not empty member.memNickname ? member.memNickname : '○○○'}님의 가상 주식 계좌
        </h1>
        <div class="main__summary-grid">
          <div class="main__summary-box">

            <div class="main__summary-label">계좌 총 잔액</div>
            <div class="league__summary-value" id="account-total-asset">
              <fmt:formatNumber value="${not empty accountSummary ? accountSummary.totalAsset : 0}" pattern="#,###"/>원
            </div>
          </div>
          <div class="main__summary-box">
            <div class="main__summary-label">상위</div>
            <div class="league__summary-value" id="account-percentile">
              <fmt:formatNumber value="${not empty accountSummary ? accountSummary.percentile : 0}" pattern="0.0"/>%
            </div>
          </div>
        </div>

        <div class="league__summary-box" style="margin-bottom: 10px;">
          <div class="league__summary-label">투자중인 금액</div>
          <div class="league__summary-value" id="account-stock-value">
            <c:if test="${not empty accountSummary}">
              <fmt:formatNumber value="${accountSummary.stockValue}" pattern="#,###"/>원
              <span class="<c:choose><c:when test="${accountSummary.totalReturn == 0}">league__text-gray</c:when><c:when test="${accountSummary.totalReturn > 0}">league__text-red</c:when><c:otherwise>league__text-blue</c:otherwise></c:choose>">
                                (<c:if test="${accountSummary.totalReturn > 0}">+</c:if><fmt:formatNumber value="${accountSummary.totalReturn}" pattern="0.00"/>%)
                            </span>
            </c:if>
            <c:if test="${empty accountSummary}">
              0원 <span>(0.00%)</span>
            </c:if>
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
          <tbody id="top-return-stocks-tbody">
          <c:choose>
            <c:when test="${not empty topReturnStocks}">
              <c:forEach items="${topReturnStocks}" var="stock" varStatus="status">
                <jsp:include page="/WEB-INF/fragments/league/table_row_top_return_stock.jsp">
                  <jsp:param name="rank" value="${status.count}"/>
                  <jsp:param name="stockName" value="${stock.stockName}"/>
                  <jsp:param name="currentPrice" value="${stock.currentPrice}"/>
                  <jsp:param name="avgPurchasePrice" value="${stock.avgPurchasePrice}"/>
                  <jsp:param name="changeRate" value="${stock.changeRate}"/>
                </jsp:include>
              </c:forEach>
            </c:when>
            <c:otherwise>
              <jsp:include page="/WEB-INF/fragments/league/empty_state_message.jsp">
                <jsp:param name="colspan" value="5"/>
                <jsp:param name="message" value="보유하고 있는 주식이 없습니다."/>
              </jsp:include>
            </c:otherwise>
          </c:choose>
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
      <c:forEach var="id" items="${['KOSPI', 'KOSDAQ', 'KRX100', '대형주', '중형주', '소형주']}">
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
  <section class="main__stock-section">
    <div class="main__section-title">실시간 차트</div>
    <div class="main__bottom-section">

      <section>
        <div class="main__table-header-row">
          <span>순위 · 오늘 XX:XX 기준</span>
          <span></span>
          <span>현재가</span>
          <span>등락률</span>
        </div>

        <c:forEach var="rank" begin="1" end="100" step="1">
          <jsp:include page="${pageContext.request.contextPath}/WEB-INF/fragments/home/rank_item.jsp">
            <jsp:param name="rank" value="${rank}"/>
          </jsp:include>
        </c:forEach>
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