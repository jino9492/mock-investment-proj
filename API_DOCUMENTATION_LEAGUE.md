# 리그 페이지 API 문서

## 개요
리그 페이지에서 사용하는 RESTful API 엔드포인트 문서입니다.

---

## API 엔드포인트 목록

### 1. 계좌 요약 정보 조회

**엔드포인트:** `GET /api/v1/league/account/summary`

**설명:** 사용자의 계좌 요약 정보를 조회합니다.

**인증:** 세션 기반 인증 필요

**요청:**
```
GET /api/v1/league/account/summary
```

**응답 (성공 - 200):**
```json
{
  "totalAsset": 15000000,
  "stockValue": 8000000,
  "cashBalance": 7000000,
  "totalReturn": 50.0,
  "percentile": 15.5
}
```

**응답 필드:**
- `totalAsset` (long): 계좌 총 금액
- `stockValue` (long): 투자중인 금액 (보유주식 평가액)
- `cashBalance` (long): 보유 현금
- `totalReturn` (double): 총 수익률 (%)
- `percentile` (double): 상위 백분율 (%)

**에러 응답:**
- `401 Unauthorized`: 로그인이 필요합니다.
- `500 Internal Server Error`: 서버 오류가 발생했습니다.

**컨트롤러:** `LeagueAccountSummaryApiController`

---

### 2. 수익률 상위 종목 조회

**엔드포인트:** `GET /api/v1/league/portfolio/top-returns`

**설명:** 사용자가 보유한 주식 중 수익률 상위 N개를 조회합니다.

**인증:** 세션 기반 인증 필요

**요청:**
```
GET /api/v1/league/portfolio/top-returns?limit=3
```

**쿼리 파라미터:**
- `limit` (int, 선택): 조회할 개수 (기본값: 3)

**응답 (성공 - 200):**
```json
{
  "stocks": [
    {
      "stockName": "삼성전자",
      "currentPrice": 75000,
      "avgPurchasePrice": 70000,
      "changeRate": 2.5,
      "returnRate": 7.14
    },
    {
      "stockName": "SK하이닉스",
      "currentPrice": 120000,
      "avgPurchasePrice": 110000,
      "changeRate": 1.2,
      "returnRate": 9.09
    }
  ]
}
```

**응답 필드:**
- `stocks` (array): 종목 배열
  - `stockName` (string): 종목명
  - `currentPrice` (long): 현재가
  - `avgPurchasePrice` (long): 매수 평균가
  - `changeRate` (double): 당일 등락률 (%)
  - `returnRate` (double): 개별 수익률 (%)

**에러 응답:**
- `401 Unauthorized`: 로그인이 필요합니다.
- `500 Internal Server Error`: 서버 오류가 발생했습니다.

**컨트롤러:** `LeaguePortfolioTopReturnsApiController`

---

### 3. 주변 랭킹 조회

**엔드포인트:** `GET /api/v1/league/ranking/nearby`

**설명:** 현재 사용자 기준 주변 랭킹을 조회합니다. (위 2명, 본인, 아래 3명)

**인증:** 세션 기반 인증 필요

**요청:**
```
GET /api/v1/league/ranking/nearby
```

**응답 (성공 - 200):**
```json
{
  "rankings": [
    {
      "rank": 98,
      "nickname": "사용자A",
      "returnRate": 45.5,
      "isCurrentUser": false
    },
    {
      "rank": 99,
      "nickname": "사용자B",
      "returnRate": 44.2,
      "isCurrentUser": false
    },
    {
      "rank": 100,
      "nickname": "현재사용자",
      "returnRate": 43.8,
      "isCurrentUser": true
    }
  ]
}
```

**응답 필드:**
- `rankings` (array): 랭킹 배열
  - `rank` (int): 순위
  - `nickname` (string): 닉네임
  - `returnRate` (double): 수익률 (%)
  - `isCurrentUser` (boolean): 현재 사용자 여부

**에러 응답:**
- `401 Unauthorized`: 로그인이 필요합니다.
- `500 Internal Server Error`: 서버 오류가 발생했습니다.

**컨트롤러:** `LeagueRankingNearbyApiController`

---

### 4. 전체 포트폴리오 조회

**엔드포인트:** `GET /api/v1/league/portfolio/details`

**설명:** 사용자가 보유한 전체 포트폴리오를 조회합니다.

**인증:** 세션 기반 인증 필요

**요청:**
```
GET /api/v1/league/portfolio/details
```

**응답 (성공 - 200):**
```json
{
  "holdings": [
    {
      "stockId": 1,
      "stockName": "삼성전자",
      "quantity": 100,
      "avgPurchasePrice": 70000,
      "currentPrice": 75000,
      "changeRate": 2.5,
      "profitLoss": 500000,
      "returnRate": 7.14
    },
    {
      "stockId": 2,
      "stockName": "SK하이닉스",
      "quantity": 50,
      "avgPurchasePrice": 110000,
      "currentPrice": 120000,
      "changeRate": 1.2,
      "profitLoss": 500000,
      "returnRate": 9.09
    }
  ]
}
```

**응답 필드:**
- `holdings` (array): 보유 종목 배열
  - `stockId` (int): 주식 ID
  - `stockName` (string): 종목명
  - `quantity` (int): 보유 수량
  - `avgPurchasePrice` (long): 평균 매입가
  - `currentPrice` (long): 현재가
  - `changeRate` (double): 당일 등락률 (%)
  - `profitLoss` (long): 총 수익(손익금액)
  - `returnRate` (double): 수익률 (%)

**에러 응답:**
- `401 Unauthorized`: 로그인이 필요합니다.
- `500 Internal Server Error`: 서버 오류가 발생했습니다.

**컨트롤러:** `LeaguePortfolioDetailsApiController`

---

## 공통 사항

### 인증
모든 API는 세션 기반 인증을 사용합니다. 세션이 없거나 만료된 경우 `401 Unauthorized` 응답을 반환합니다.

### 에러 응답 형식
```json
{
  "error": "에러 메시지"
}
```

### 데이터 형식
- 금액: 정수형 (원 단위)
- 비율: 실수형 (퍼센트)
- 날짜/시간: ISO 8601 형식 (필요 시)

### 실시간 업데이트
- 클라이언트는 30초 간격으로 자동 갱신
- 초기 렌더링은 서버 사이드 데이터 사용
- 실시간 업데이트만 API 호출

---

## 사용 예시

### JavaScript (Fetch API)
```javascript
// 계좌 요약 정보 조회
const response = await fetch('/api/v1/league/account/summary');
const data = await response.json();

if (!response.ok) {
    if (response.status === 401) {
        // 로그인 페이지로 리다이렉트
        window.location.href = '/login';
    } else {
        console.error('에러:', data.error);
    }
    return;
}

console.log('계좌 총 금액:', data.totalAsset);
```

---

## 버전 정보
- API 버전: v1
- 최종 수정일: 2024년

