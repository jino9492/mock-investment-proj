# Order Card 기능 체크

## ✅ 구현 완료

### 기본 기능
- 구매/판매/대기 탭 전환
- 주문 유형 선택 (일반/조건 주문)
- 지정가/시장가 모드
- 가격/수량 입력 및 증감 버튼
- 최대 수량 자동 계산
- 총 주문 금액 실시간 계산
- 일반/조건 주문 제출

### 조건 주문
- 감시 가격 입력 및 정보 표시
- 조건 주문 가격/수량 입력
- 주문 유지 기간 선택
- 조건 주문 제출
- 조건 주문 활성화/비활성화 토글 ✅
- 조건 주문 수정 ✅
- 조건 주문 삭제 ✅
- 조건 충족 감시 및 자동 주문 실행 ✅

### 대기 주문
- 대기 주문 목록 표시 (조건 주문 상위 배치)
- 일반 주문 취소
- 조건 주문 편집 모달
- ⚠️ 일반 주문 수정 버튼 (HTML만 존재, 이벤트 핸들러 미구현)

### 실시간 업데이트
- 구매 가능 금액 폴링 (5초마다)
- 보유 정보 폴링 (5초마다)

## 조건주문 작동 순서

1. 주문 유형 선택 → 조건 주문
2. 감시 가격 입력
3. 구매/판매 가격 입력
4. 수량 입력
5. 주문 유지 기간 선택
6. 주문 제출
7. 대기 주문 목록에 표시
8. 백엔드 스케줄러가 1분마다 조건 충족 체크
9. 조건 충족 시 자동 일반 주문 생성
10. 만료된 조건 주문 자동 삭제

## API 엔드포인트

- `POST /api/order`: 주문 생성
- `DELETE /api/order?orderId={orderId}`: 주문 취소
- `PUT /api/order?orderId={orderId}&action=toggle&isActive={true|false}`: 조건 주문 토글
- `PUT /api/order?orderId={orderId}&watchPrice={price}&price={price}&quantity={qty}&isActive={bool}&orderDuration={duration}`: 조건 주문 수정
- `GET /api/order?stockId={stockId}`: 구매 가능 금액 조회
- `GET /api/holding?stockId={stockId}`: 보유 정보 조회

## 구현 파일

**백엔드**
- `OrderController.java`: API 엔드포인트
- `OrderService.java`: 비즈니스 로직
- `OrderDAO.java`, `OrderConditionDAO.java`: DB 접근
- `ConditionalOrderCheckTask.java`: 조건 주문 감시 스케줄러 (1분마다 실행)

**프론트엔드**
- `chart.js`, `info.js`: 페이지별 로직
- `stock_order_card.js`: 공통 주문 카드 로직
- `stock_order_utils.js`: 유틸리티 함수

## ⚠️ 미구현 기능

- 일반 주문 수정: HTML 버튼만 존재, 클릭 이벤트 핸들러 미구현

## 상태

조건 주문 관련 기능 모두 구현 완료 ✅
일반 주문 수정 기능은 버튼만 있고 이벤트 핸들러 필요
