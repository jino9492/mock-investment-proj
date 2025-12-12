# Order Card JSP 피드백 문서

**파일 경로**: `src/main/webapp/WEB-INF/fragments/stock/order_card.jsp`  

## 개요

이 문서는 `order_card.jsp` 파일에 대한 미해결 개선 사항을 기록합니다.

---

## 1. 구조 및 유지보수성

### 🔴 심각 (Critical)

#### 1.1 인라인 스타일 사용
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

#### 1.2 하드코딩된 텍스트
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

#### 1.3 일관성 없는 ID 부여
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
1. 인라인 스타일 제거
2. Label 접근성 개선 (`for` 속성 연결)

### 우선순위 2 (단기 개선)
3. XSS 방지 강화 (`fn:escapeXml` 사용)
4. 모든 입력 필드에 ID 부여

### 우선순위 3 (중기 개선)
5. 하드코딩된 텍스트 리소스 번들로 이전
6. 에러 메시지 UX 개선
7. 로딩 상태 표시 추가

### 우선순위 4 (장기 개선)
8. 다국어 지원 준비
9. 조건부 렌더링 최적화

---

**작성자**: AI 코드 리뷰어  
**최종 업데이트**: 2024년
