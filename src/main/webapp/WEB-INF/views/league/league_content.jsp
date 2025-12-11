<%--
 * 리그 페이지 메인 콘텐츠
 * 
 * 기능:
 * - 계좌 요약 정보 표시 (총 금액, 상위 백분율, 투자중인 금액)
 * - 수익률 상위 3개 종목 표시
 * - 주변 랭킹 표시
 * - 전체 포트폴리오 표시
 * 
 * 데이터 바인딩:
 * - 서버 사이드 렌더링: 초기 데이터는 JSP에서 렌더링
 * - 클라이언트 사이드 업데이트: 30초 간격으로 API 호출하여 실시간 갱신
 * 
 * 컴포넌트:
 * - 등락률 표시: /WEB-INF/fragments/league/change_rate_display.jsp
 * - 빈 상태 메시지: /WEB-INF/fragments/league/empty_state_message.jsp
 * - 테이블 행: /WEB-INF/fragments/league/table_row_*.jsp
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/league/league_page.css"/>
<script src="${pageContext.request.contextPath}/static/js/league/league_page.js"></script>

<main class="league__content">

    <div class="main__sub-title">
        <span class="main__dot-green"></span>
        <strong>국내 장 열림/닫힘</strong>
    </div>
    
    <%-- 
     * 상단 섹션: 계좌 요약 + 주식 보유 목록 + 순위
     * 레이아웃: Grid 2열 (1.1fr : 1.2fr)
     * - 왼쪽: 계좌 카드 (계좌 요약 + 수익률 상위 3개 종목)
     * - 오른쪽: 랭킹 카드 (주변 랭킹)
     --%>
    <section class="league__top-section">
        <%-- 왼쪽: 계좌 카드 영역 --%>
        <div>
            <section class="league__card-account">
                <h1 class="league__page-title">
                    ${not empty member.memNickname ? member.memNickname : '○○○'}님의 가상 주식 계좌
                </h1>
                
                <%-- 계좌 요약 정보 (총 금액, 상위 백분율, 투자중인 금액) --%>
                <div class="league__summary-grid">
                    <div class="league__summary-box">
                        <div class="league__summary-label">계좌 총 금액</div>
                        <div class="league__summary-value" id="account-total-asset">
                            <fmt:formatNumber value="${not empty accountSummary ? accountSummary.totalAsset : 0}" pattern="#,###"/>원
                        </div>
                    </div>
                    <div class="league__summary-box">
                        <div class="league__summary-label">상위</div>
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
                            <span class="<c:choose><c:when test="${accountSummary.totalReturn == 0}">price-color--neutral</c:when><c:when test="${accountSummary.totalReturn > 0}">price-color--up</c:when><c:otherwise>price-color--down</c:otherwise></c:choose>">
                                (<c:if test="${accountSummary.totalReturn > 0}">+</c:if><fmt:formatNumber value="${accountSummary.totalReturn}" pattern="0.00"/>%)
                            </span>
                        </c:if>
                        <c:if test="${empty accountSummary}">
                            0원 <span>(0.00%)</span>
                        </c:if>
                    </div>
                </div>

                <div class="league__divider"></div>

                <%-- 
                 * 수익률 상위 3개 종목 테이블
                 * 컬럼: 순위, 주식명, 현재가, 매수 평균가, 등락률
                 * 데이터: topReturnStocks (서버 사이드 렌더링)
                 --%>
                <table class="league__table-simple">
                    <thead>
                    <tr>
                        <th></th>
                        <th>주식 명</th>
                        <th>현재가</th>
                        <th>매수 평균가</th>
                        <th>등락률</th>
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
        </div>

        <%-- 
         * 오른쪽: 랭킹 카드
         * 현재 사용자 기준 위 2명, 본인, 아래 3명 표시
         * 정보: 순위, 닉네임, 수익률
         --%>
        <section class="league__rank-card">
            <div class="league__rank-title">순위</div>
            <ul class="league__rank-list" id="rankings-list">
                <c:choose>
                    <c:when test="${not empty nearbyRankings}">
                        <c:forEach items="${nearbyRankings}" var="ranking">
                            <li class="league__rank-item <c:if test="${ranking.currentUser}">active</c:if>">
                                <span class="league__rank-number"><fmt:formatNumber value="${ranking.rank}" pattern="00000"/></span>
                                <span class="league__rank-name">${ranking.nickname}</span>
                                <span class="league__rank-return">
                                    <c:if test="${ranking.returnRate > 0}">+</c:if><fmt:formatNumber value="${ranking.returnRate}" pattern="0.00"/>%
                                </span>
                            </li>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <!-- JavaScript로 동적 생성 -->
                    </c:otherwise>
                </c:choose>
            </ul>
        </section>
    </section>

    <%-- 
     * 하단 섹션: 전체 포트폴리오
     * 사용자가 보유한 모든 주식 목록 표시
     * 컬럼: 주식명, 현재가, 등락률
     --%>
    <section class="league__bottom-section">
        <div class="league__section-title">내 투자</div>
        <table class="league__investment-table">
            <thead>
            <tr>
                <th>주식 명</th>
                <th>현재가</th>
                <th>등락률</th>
            </tr>
            </thead>
            <tbody id="portfolio-tbody">
                <c:choose>
                    <c:when test="${not empty portfolioDetails}">
                        <c:forEach items="${portfolioDetails}" var="stock">
                            <jsp:include page="/WEB-INF/fragments/league/table_row_portfolio_stock.jsp">
                                <jsp:param name="stockName" value="${stock.stockName}"/>
                                <jsp:param name="currentPrice" value="${stock.currentPrice}"/>
                                <jsp:param name="changeRate" value="${stock.changeRate}"/>
                            </jsp:include>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <jsp:include page="/WEB-INF/fragments/league/empty_state_message.jsp">
                            <jsp:param name="colspan" value="3"/>
                            <jsp:param name="message" value="보유하고 있는 주식이 없습니다."/>
                        </jsp:include>
                    </c:otherwise>
                </c:choose>
            </tbody>
        </table>
    </section>
</main>
