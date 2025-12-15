# Order Card JSP 피드백 문서

**파일 경로**: `src/main/webapp/WEB-INF/fragments/stock/order_card.jsp`  
**생성 일자**: 2024년

## 개요

이 문서는 `order_card.jsp` 파일에 대한 코드 리뷰 피드백을 제공합니다. 주문 카드 컴포넌트의 구조, 접근성, 보안, 유지보수성 측면에서 분석한 결과를 포함합니다.

---

## 1. 구조 및 유지보수성

### 🔴 심각 (Critical)

#### 1.1 코드 중복 (DRY 위반) ✅ **수정 완료**
**문제점**: 조건 주문 필드(`order-card__conditional-order`)와 판매 조건 주문 필드(`order-card__conditional-order--sell`)가 거의 동일한 구조를 가지고 있었습니다.

**수정 내용**:
- ✅ **감시 가격 필드 fragment 생성**: `order_conditional_watch_price_fields.jsp`
  - 구매용과 판매용 감시 가격 필드를 하나의 fragment로 통합
  - ID를 파라미터로 받아 재사용 가능하도록 구현
- ✅ **총 주문 금액 표시 fragment 생성**: `order_conditional_total_amount.jsp`
  - 중복되던 총 주문 금액 표시를 공통 fragment로 분리
- ✅ **기존 fragment 활용**: 이미 분리되어 있던 필드들
  - `order_conditional_price_fields.jsp` (가격 입력 필드)
  - `order_conditional_quantity_fields.jsp` (수량 입력 필드)
  - `order_conditional_duration_fields.jsp` (주문 유지 기간 드롭다운)

**수정 전**:
- 감시 가격 필드가 구매용(167-197줄)과 판매용(226-256줄)에 중복
- 총 주문 금액 표시가 구매용(215-221줄)과 판매용(274-280줄)에 중복

**수정 후**:
```jsp
<%-- 구매 조건 주문 --%>
<c:set var="watchPriceInputId" value="watch-price-input" scope="request" />
<c:set var="watchPriceInfoId" value="watch-price-info" scope="request" />
<c:set var="watchPriceMinusBtnId" value="watch-price-minus" scope="request" />
<c:set var="watchPricePlusBtnId" value="watch-price-plus" scope="request" />
<%@ include file="order_conditional_watch_price_fields.jsp" %>

<%-- 판매 조건 주문 --%>
<c:set var="watchPriceInputId" value="conditional-sell-watch-price-input" scope="request" />
<c:set var="watchPriceInfoId" value="conditional-sell-watch-price-info" scope="request" />
<c:set var="watchPriceMinusBtnId" value="conditional-sell-watch-price-minus" scope="request" />
<c:set var="watchPricePlusBtnId" value="conditional-sell-watch-price-plus" scope="request" />
<%@ include file="order_conditional_watch_price_fields.jsp" %>

<%-- 총 주문 금액 (구매/판매 공통) --%>
<%@ include file="order_conditional_total_amount.jsp" %>
```

**수정 일자**: 2024년 (최근)
**상태**: ✅ 완료 - 모든 중복 코드가 fragment로 분리되어 유지보수성과 일관성이 향상되었습니다.

#### 1.2 인라인 스타일 사용
**문제점**: `style="display: block;"` 및 `style="display: none;"` 사용 (70, 140, 282, 408번 줄)

**영향**:
- CSS와 HTML의 분리 원칙 위반
- 반응형 디자인 대응 어려움
- JavaScript와의 충돌 가능성

**권장사항**:
```jsp
<!-- 대신 -->
<div class="order-card__general-order order-card__general-order--visible">

/* CSS에서 */
.order-card__general-order {
    display: none;
}
.order-card__general-order--visible {
    display: block;
}
```

### 🟡 중요 (Important)

#### 1.3 ID 불일치 문제 ✅ **수정 완료**
**문제점**: JSP 파일의 버튼 ID가 `id="order-submit-button"`이었지만, JavaScript 파일들(`info.js`, `chart.js`)은 `getElementById("order-submit-btn")`을 사용하여 버튼을 찾고 있었습니다.

**영향**:
- JavaScript 이벤트 핸들러가 연결되지 않음
- 주문 제출 기능이 작동하지 않음
- 사용자가 주문을 생성할 수 없음

**수정 내용**:
```jsp
<!-- 수정 전 -->
<button class="order-card__submit-btn" id="order-submit-button">${submitButtonText}</button>

<!-- 수정 후 -->
<button class="order-card__submit-btn" id="order-submit-btn">${submitButtonText}</button>
```

**수정 일자**: 2024년 (최근)
**상태**: ✅ 해결됨 - 버튼 ID를 `order-submit-btn`으로 통일하여 JavaScript 이벤트 핸들러가 정상적으로 연결되도록 수정했습니다.

#### 1.4 하드코딩된 텍스트
**문제점**: 
- "최대 0주 가능" (114, 217, 343번 줄)
- "0원" (131, 144, 175번 줄)
- "구매하기" (447번 줄)

**영역**: 다국어 지원 및 유지보수성

**권장사항**:
```jsp
<!-- JSP 리소스 번들 사용 또는 서버 측 변수로 처리 -->
<fmt:message key="order.max.quantity" var="maxQuantityText"/>
<input ... placeholder="${maxQuantityText}" />
```

#### 1.5 일관성 없는 ID 부여
**문제점**: 
- 일반 주문 필드에는 ID가 있음 (`order-price-input`, `order-quantity-input`)
- 조건 주문 필드의 가격/수량 입력에는 ID가 없음 (200, 217, 326, 343번 줄)

**영향**: JavaScript 선택자 사용 시 일관성 부족

**권장사항**: 모든 입력 필드에 일관된 ID 또는 `data-*` 속성 부여

---

## 2. 접근성 (Accessibility)

### 🟡 중요 (Important)

#### 2.1 Label 연결 누락
**문제점**: 많은 `<input>` 요소들이 `<label>`과 `for` 속성으로 연결되지 않았습니다.

**예시**:
```jsp
<label class="order-card__label">구매가격</label>
<!-- label과 input이 연결되지 않음 -->
<input type="text" class="order-card__input" id="order-price-input" ... />
```

**권장사항**:
```jsp
<label class="order-card__label" for="order-price-input">구매가격</label>
<input type="text" 
       class="order-card__input" 
       id="order-price-input"
       aria-label="구매가격"
       ... />
```

#### 2.2 빈 Label 사용
**문제점**: `&nbsp;`를 사용한 빈 레이블 (90, 197, 323번 줄)

**영향**: 스크린 리더 사용자 경험 저하

**권장사항**:
```jsp
<!-- 방법 1: aria-label 사용 -->
<label class="order-card__label" style="visibility: hidden;">가격</label>
<input type="text" aria-label="주문 가격" ... />

<!-- 방법 2: 빈 label 제거하고 input에 직접 aria-label -->
<input type="text" aria-label="주문 가격" ... />
```

#### 2.3 ARIA 속성 불완전
**문제점**: 
- `role="radiogroup"` 내의 버튼들이 `role="radio"`를 사용하지만, 실제 라디오 그룹의 접근성 패턴과 완전히 일치하지 않음
- `aria-describedby`를 사용하여 도움말 텍스트 연결 가능

**권장사항**:
```jsp
<div class="order-card__field order-card__field--split" 
     role="radiogroup" 
     aria-labelledby="price-mode-label"
     aria-required="true">
    <button type="button" 
            role="radio" 
            aria-checked="true"
            aria-label="지정가 주문">
        지정가
    </button>
</div>
```

---

## 3. 보안

### 🟡 중요 (Important)

#### 3.1 XSS 방지 확인
**현재 상태**: JSTL `<c:out>` 태그를 사용하지 않고 직접 출력하는 부분이 있습니다.

**검토 필요 항목**:
- `${order.tradeType}` (414번 줄) - `fn:escapeXml()` 권장
- `${order.orderId}` (428, 430번 줄) - 숫자이지만 확인 필요

**권장사항**:
```jsp
<!-- 안전한 출력 -->
<span class="order-card__wait-order-type order-card__wait-order-type--${fn:escapeXml(order.tradeType)}">
    <c:choose>
        <c:when test="${order.tradeType == 'buy'}">구매</c:when>
        <c:otherwise>판매</c:otherwise>
    </c:choose>
</span>
```

또는 `<c:out value="${order.tradeType}" />` 사용

#### 3.2 데이터 검증
**문제점**: 클라이언트 측 검증만으로는 부족하며, 서버 측 검증이 필수입니다.

**확인 필요**: 
- `order-submit-btn` 클릭 시 JavaScript에서 검증하지만, 서버 측에서도 재검증해야 함

---

## 4. 사용자 경험 (UX)

### 🟡 중요 (Important)

#### 4.1 플레이스홀더 텍스트 개선
**문제점**: 
- "최대 0주 가능" - 실제 최대 수량을 동적으로 표시해야 함

**권장사항**:
```jsp
<input type="text" 
       class="order-card__input" 
       id="order-quantity-input" 
       placeholder="최대 <fmt:formatNumber value="${maxQuantity}" pattern="#,###"/>주 가능" />
```

#### 4.2 에러 메시지 표시 위치
**문제점**: JavaScript에서 `alert()`를 사용하여 에러 표시

**권장사항**: 
- 인라인 에러 메시지 영역 추가
- 접근성을 고려한 에러 메시지 구조

#### 4.3 로딩 상태 표시
**문제점**: 주문 제출 중 로딩 상태 표시가 없음

**권장사항**: 제출 버튼에 로딩 스피너 및 비활성화 상태 추가

---

## 5. 데이터 바인딩

### 🟡 중요 (Important)

#### 5.1 데이터 속성 일관성
**문제점**: 
- 일부 요소는 `data-current-price` 사용 (97, 156, 169번 줄)
- 일부 요소는 동적 바인딩이 없음

**권장사항**: 모든 가격 관련 요소에 일관되게 데이터 속성 부여

#### 5.2 빈 값 처리
**문제점**: 
- `${daily.closePrice}`가 null일 때의 처리 (97번 줄)
- `${member.virtualCash}`가 null일 때의 처리 (130번 줄)

**현재 상태**: `<c:if>` 태그로 감싸져 있어 괜찮지만, 추가 검증 권장

**권장사항**:
```jsp
<c:choose>
    <c:when test="${not empty daily and not empty daily.closePrice}">
        data-current-price="${daily.closePrice.intValue()}"
    </c:when>
    <c:otherwise>
        data-current-price="0"
    </c:otherwise>
</c:choose>
```

---

## 6. 코드 품질

### 🟢 개선 권장 (Enhancement)

#### 6.1 주석 개선
**현재 상태**: 주석이 있지만 더 구체적인 설명 추가 권장

**예시**:
```jsp
<!-- 주문유형 (대기 탭에서는 숨김) -->
<!-- 더 구체적으로 -->
<!-- 주문유형 선택 드롭다운: 일반 주문 / 조건 주문 선택 (대기 탭에서는 display:none으로 숨김 처리) -->
```

#### 6.2 네이밍 일관성
**문제점**: 
- `order-card__general-order` vs `order-card__conditional-order`
- 일부 클래스명에 일관성 부족

**권장사항**: BEM(Block Element Modifier) 네이밍 컨벤션을 엄격히 준수

---

## 7. 성능

### 🟢 개선 권장 (Enhancement)

#### 7.1 조건부 렌더링
**현재 상태**: 모든 필드가 DOM에 렌더링되고 `display: none`으로 숨김

**영향**: 초기 렌더링 비용이 높을 수 있음

**권장사항**: 
- 서버 측에서 조건부로 fragment include
- 또는 클라이언트 측에서 필요한 경우에만 동적 생성

---

## 8. 우선순위별 개선 사항 요약

### 우선순위 1 (즉시 수정 권장)
1. ✅ **ID 불일치 문제 해결** (`order-submit-button` → `order-submit-btn`) - **완료**
2. ⏳ 인라인 스타일 제거
3. ⏳ Label 접근성 개선 (`for` 속성 연결)

### 우선순위 2 (단기 개선)
4. ✅ **코드 중복 제거 (조건 주문 필드)** - **완료** (감시 가격 필드와 총 주문 금액 표시를 fragment로 분리)
5. ⏳ XSS 방지 강화 (`fn:escapeXml` 사용)
6. ⏳ 모든 입력 필드에 ID 부여

### 우선순위 3 (중기 개선)
7. ✅ 하드코딩된 텍스트 리소스 번들로 이전
8. ✅ 에러 메시지 UX 개선
9. ✅ 로딩 상태 표시 추가

### 우선순위 4 (장기 개선)
10. ✅ 다국어 지원 준비
11. ✅ 조건부 렌더링 최적화

---

## 9. 긍정적인 측면

### 잘 구현된 부분
1. ✅ BEM 네이밍 컨벤션 대체로 잘 준수
2. ✅ ARIA 속성 기본적으로 사용 (`role`, `aria-checked`, `aria-expanded` 등)
3. ✅ JSTL 태그를 적절히 활용하여 서버 측 로직 처리
4. ✅ 데이터 속성(`data-*`)을 활용한 JavaScript 연동 구조
5. ✅ 의미 있는 HTML 요소 사용 (`<section>`, `<label>` 등)

---

## 10. 참고 자료

- [W3C WAI-ARIA Authoring Practices](https://www.w3.org/WAI/ARIA/apg/)
- [JSP Best Practices](https://www.oracle.com/java/technologies/jsp-best-practices.html)
- [OWASP XSS Prevention Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html)

---

---

## 11. 최근 수정 이력

### 2024년 (최근)
- ✅ **버튼 ID 불일치 문제 해결**: `order-submit-button` → `order-submit-btn`으로 변경하여 JavaScript 이벤트 핸들러가 정상적으로 연결되도록 수정했습니다.
  - **영향**: 주문 제출 기능이 정상적으로 작동합니다.
  - **관련 파일**: `src/main/webapp/WEB-INF/fragments/stock/order_card.jsp`

- ✅ **코드 중복 제거 (DRY 원칙 적용)**: 조건 주문 필드의 중복 코드를 fragment로 분리했습니다.
  - **생성된 fragment**:
    - `order_conditional_watch_price_fields.jsp` - 감시 가격 필드 (구매/판매 공통)
    - `order_conditional_total_amount.jsp` - 총 주문 금액 표시 (구매/판매 공통)
  - **영향**: 유지보수 비용 감소, 버그 수정 시 한 곳만 수정하면 됨, 일관성 향상
  - **관련 파일**: 
    - `src/main/webapp/WEB-INF/fragments/stock/order_card.jsp`
    - `src/main/webapp/WEB-INF/fragments/stock/order_conditional_watch_price_fields.jsp` (신규)
    - `src/main/webapp/WEB-INF/fragments/stock/order_conditional_total_amount.jsp` (신규)

- ✅ **종목 ID 자동 수집**: URL 파라미터 대신 페이지의 `stock` 객체에서 종목 ID를 자동으로 가져오도록 수정했습니다.
  - **영향**: "종목 ID가 필요합니다" 오류 해결, URL 파라미터에 의존하지 않음
  - **관련 파일**: 
    - `src/main/webapp/WEB-INF/fragments/stock/order_card.jsp`
    - `src/main/webapp/static/js/common/stock/stock_order_card.js`

---

**작성자**: AI 코드 리뷰어  
**리뷰 날짜**: 2024년  
**최종 업데이트**: 2024년 (버튼 ID 불일치 문제 수정)  
**다음 리뷰 권장 시기**: 주요 수정 사항 적용 후

