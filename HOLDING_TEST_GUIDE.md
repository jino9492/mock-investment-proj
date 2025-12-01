# Holding 데이터 입력 및 검증 가이드

리그 페이지에서 사용되는 holding 데이터를 입력하고 검증하는 방법을 안내합니다.

## 기능 개요

1. **HoldingDAO**: holding 테이블에 데이터를 입력/조회하는 DAO 클래스
2. **HoldingTestApiController**: holding 데이터 입력 및 리그 페이지 기능 검증을 위한 API 컨트롤러

## API 엔드포인트

### 1. Holding 데이터 입력
**POST** `/api/v1/test/holding/insert`

#### 요청 파라미터
- `stock_id` (필수): 주식 ID
- `quantity` (필수): 보유 수량 (양수)
- `buy_price` (필수): 매수 가격 (양수)

#### 요청 예시
```bash
POST /api/v1/test/holding/insert
Content-Type: application/x-www-form-urlencoded

stock_id=1&quantity=10&buy_price=50000
```

또는 JavaScript에서:
```javascript
fetch('/api/v1/test/holding/insert', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: 'stock_id=1&quantity=10&buy_price=50000'
})
.then(response => response.json())
.then(data => console.log(data));
```

#### 응답 예시
```json
{
    "success": true,
    "message": "Holding 데이터가 성공적으로 입력되었습니다.",
    "mem_id": 1,
    "stock_id": 1,
    "quantity": 10,
    "buy_price": 50000
}
```

### 2. Holding 데이터 검증
**GET** `/api/v1/test/holding/verify`

리그 페이지의 모든 기능이 holding 데이터를 기반으로 제대로 작동하는지 검증합니다.

#### 요청 예시
```bash
GET /api/v1/test/holding/verify
```

또는 JavaScript에서:
```javascript
fetch('/api/v1/test/holding/verify')
.then(response => response.json())
.then(data => {
    console.log('검증 결과:', data);
    console.log('보유 주식 수:', data.holding_data.count);
    console.log('계좌 총 자산:', data.account_summary.total_asset);
    console.log('투자중인 금액:', data.account_summary.stock_value);
    console.log('수익률 상위 종목:', data.top_return_stocks.count);
    console.log('포트폴리오 종목 수:', data.portfolio_details.count);
    console.log('주변 랭킹 수:', data.nearby_rankings.count);
    console.log('검증 상태:', data.validation.is_valid);
    if (data.validation.issues.length > 0) {
        console.log('문제점:', data.validation.issues);
    }
});
```

#### 응답 예시
```json
{
    "success": true,
    "message": "리그 페이지 기능 검증 완료",
    "mem_id": 1,
    "holding_data": {
        "count": 3,
        "holdings": [
            {
                "memId": 1,
                "stockId": 1,
                "stockName": "삼성전자",
                "quantity": 10,
                "avgBuyPrice": 50000
            },
            {
                "memId": 1,
                "stockId": 2,
                "stockName": "SK하이닉스",
                "quantity": 5,
                "avgBuyPrice": 120000
            }
        ]
    },
    "account_summary": {
        "total_asset": 15000000,
        "stock_value": 5000000,
        "cash_balance": 10000000,
        "total_return": 50.0,
        "percentile": 25.5
    },
    "top_return_stocks": {
        "count": 3,
        "stocks": [
            {
                "stockName": "삼성전자",
                "currentPrice": 55000,
                "avgPurchasePrice": 50000,
                "changeRate": 2.5,
                "returnRate": 10.0
            }
        ]
    },
    "portfolio_details": {
        "count": 3,
        "stocks": [...]
    },
    "nearby_rankings": {
        "count": 6,
        "rankings": [...]
    },
    "validation": {
        "is_valid": true,
        "issues": []
    }
}
```

## 사용 시나리오

### 시나리오 1: Holding 데이터 입력 후 검증

1. **로그인**: 먼저 로그인하여 세션을 생성합니다.

2. **Holding 데이터 입력**:
   ```bash
   POST /api/v1/test/holding/insert
   stock_id=1&quantity=10&buy_price=50000
   ```

3. **검증 실행**:
   ```bash
   GET /api/v1/test/holding/verify
   ```

4. **결과 확인**: 
   - `validation.is_valid`가 `true`인지 확인
   - `account_summary.stock_value`가 0보다 큰지 확인
   - `top_return_stocks.count`가 0보다 큰지 확인

### 시나리오 2: 여러 종목 입력

같은 회원에게 여러 종목의 holding 데이터를 입력할 수 있습니다:

```javascript
// 삼성전자 10주 매수
await fetch('/api/v1/test/holding/insert', {
    method: 'POST',
    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    body: 'stock_id=1&quantity=10&buy_price=50000'
});

// SK하이닉스 5주 매수
await fetch('/api/v1/test/holding/insert', {
    method: 'POST',
    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    body: 'stock_id=2&quantity=5&buy_price=120000'
});

// 검증
const result = await fetch('/api/v1/test/holding/verify').then(r => r.json());
console.log('보유 종목 수:', result.holding_data.count); // 2
```

### 시나리오 3: 동일 종목 추가 매수

같은 종목을 추가로 매수하면 평균 매수 가격이 자동으로 재계산됩니다:

```javascript
// 첫 매수: 10주 @ 50,000원
await fetch('/api/v1/test/holding/insert', {
    method: 'POST',
    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    body: 'stock_id=1&quantity=10&buy_price=50000'
});

// 추가 매수: 5주 @ 60,000원
// 평균 매수 가격 = (10 * 50000 + 5 * 60000) / 15 = 53,333원
await fetch('/api/v1/test/holding/insert', {
    method: 'POST',
    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
    body: 'stock_id=1&quantity=5&buy_price=60000'
});
```

## 주의사항

1. **로그인 필수**: 모든 API는 로그인한 사용자만 사용할 수 있습니다.

2. **stock_price_daily 데이터 필요**: 
   - 리그 페이지의 주가 정보는 `stock_price_daily` 테이블에서 조회됩니다.
   - holding 데이터를 입력했지만 `stock_price_daily`에 해당 주식의 가격 데이터가 없으면:
     - `account_summary.stock_value`가 0으로 표시될 수 있습니다.
     - `top_return_stocks`가 비어있을 수 있습니다.

3. **필수 테이블**:
   - `member`: 회원 정보
   - `stock`: 주식 정보
   - `stock_price_daily`: 일일 주가 정보
   - `holding`: 보유 주식 정보
   - `cash_history`: 현금 거래 내역 (초기 자본금 계산용)

4. **검증 실패 시 확인 사항**:
   - `validation.issues` 배열에 문제점이 표시됩니다.
   - 보유 주식이 있지만 평가액이 0인 경우: `stock_price_daily` 데이터 확인
   - 보유 주식이 있지만 수익률 상위 종목이 없는 경우: `stock_price_daily` 데이터 확인

## 테스트 체크리스트

- [ ] 로그인 상태 확인
- [ ] stock 테이블에 주식 데이터 존재 확인
- [ ] stock_price_daily 테이블에 해당 주식의 가격 데이터 존재 확인
- [ ] Holding 데이터 입력 성공 확인
- [ ] 검증 API로 리그 페이지 기능 확인
- [ ] 계좌 요약 정보 정상 조회 확인
- [ ] 수익률 상위 종목 정상 조회 확인
- [ ] 포트폴리오 상세 정상 조회 확인
- [ ] 주변 랭킹 정상 조회 확인

## 문제 해결

### 문제: "보유 주식이 있지만 평가액이 0입니다"
**원인**: `stock_price_daily` 테이블에 해당 주식의 가격 데이터가 없습니다.
**해결**: `stock_price_daily` 테이블에 해당 `stock_id`의 최신 가격 데이터를 입력하세요.

### 문제: "보유 주식이 있지만 수익률 상위 종목이 조회되지 않습니다"
**원인**: `stock_price_daily` 테이블에 가격 데이터가 없거나, `avg_buy_price`가 0입니다.
**해결**: 
1. `stock_price_daily` 테이블에 가격 데이터 입력
2. holding 테이블의 `avg_buy_price`가 0보다 큰지 확인

### 문제: "Holding 데이터 입력에 실패했습니다"
**원인**: 
- `stock_id`가 `stock` 테이블에 존재하지 않음
- 데이터베이스 연결 오류
- 트랜잭션 오류

**해결**:
1. `stock` 테이블에 해당 `stock_id`가 존재하는지 확인
2. 데이터베이스 연결 상태 확인
3. 서버 로그 확인

