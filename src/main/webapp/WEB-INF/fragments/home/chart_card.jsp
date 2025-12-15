<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!-- 오른쪽 차트 카드 -->
<script type="module" src="${pageContext.request.contextPath}/static/js/home/chart_card.js"></script>
<section class="main__card chart-card">
      <div class="main__chart-header">
        <strong></strong>
        <span></span>
      </div>
      <jsp:include page="${pageContext.request.contextPath}/WEB-INF/fragments/common/chart/candle_chart.jsp"/>
      <div class="main__chart-info">
        <div>
          <span class="main__chart-info-label">기업명</span>
          <span></span>
        </div>
        <div>
          <span class="main__chart-info-label">시가총액</span>
          <span></span>
        </div>
        <div>
          <span class="main__chart-info-label">상장일</span>
          <span></span>
        </div>
        <div>
          <span class="main__chart-info-label">액면가</span>
          <span></span>
        </div>
      </div>
<%--  <div class="main__chip-area">--%>
<%--    <div class="main__chip-title">주요 사업</div>--%>
<%--    <div class="main__chip-box"></div>--%>
<%--  </div>--%>
</section>