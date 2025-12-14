// fetch_utils.js

export function loadIndexSparklineData(indexName) {
    return new Promise((resolve, reject) => {
        const url = `/api/index/sparkline?code=${encodeURIComponent(indexName)}`;
        fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP Error! Status: ${response.status} - Could not load ${indexName}`);
                }
                return response.json();
            })
            .then(data => {
                const prices = data.prices || [];
                const overnights = data.overnights || [];

                const resultObject = {
                    prices: prices,
                    overnights: overnights
                };

                resolve(resultObject);
            })
            .catch(error => {
                if (!error.indexName) {
                    error.indexName = indexName;
                }

                console.error(`데이터 로드 중 오류 발생 (${indexName}):`, error.message);
                reject(error);
            });
    });
}

export function loadRankingData(limit = 100) {
    const url = `/api/rank/items`;

    return new Promise((resolve, reject) => {
        fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(response => {
                // rankingCode 변수가 이 스코프에 없으므로, 에러 메시지 수정
                if (!response.ok) {
                    throw new Error(`HTTP Error! Status: ${response.status} - Could not load ranking data`);
                }
                return response.json();
            })
            .then(data => {
                const rankingList = data || [];

                resolve(rankingList);
            })
            .catch(error => {

                console.error(`랭킹 데이터 로드 중 오류 발생 :`, error.message);
                reject(error);
            });
    });
}

export function loadRealtimeData(code) {
    // 💡 단일 파라미터(code)만 사용
    const url = `/api/socket?code=${code}`;

    return new Promise((resolve, reject) => {
        fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(response => {
                if (!response.ok) {
                    // rankingCode 변수가 이 스코프에 없으므로, 에러 메시지 수정
                    throw new Error(`HTTP Error! Status: ${response.status} - Could not load realtime data for ${code}`);
                }
                // 서버가 응답 본문을 보내지 않거나 null을 보낼 수 있으므로 안전하게 처리
                return response.json().catch(() => null);
            })
            .then(data => {
                // data가 null일 경우, 빈 배열을 반환하여 이후 처리 로직의 오류를 방지
                const realtimeData = data || [];

                // loadRealtimeData는 단일 종목을 요청하므로, 결과는 [종목객체] 형태이거나 종목객체일 수 있음.
                // 배열이 아니면 배열로 감싸서 반환하여 이후 로직 통일
                resolve(Array.isArray(realtimeData) ? realtimeData : [realtimeData]);
            })
            .catch(error => {
                console.error(`실시간 데이터 로드 중 오류 발생 (${code}) :`, error.message);
                reject(error);
            });
    });
}

export async function fetchStockChartData(stockCode) {
    // 1. 요청할 기본 URL 정의
    const baseUrl = '/api/stock/chart';

    // 2. URLSearchParams를 사용하여 쿼리 파라미터 (code=...) 생성
    const params = new URLSearchParams({
        code: stockCode
    });

    // 3. 최종 URL 결합 ( /api/stock/chart?code=005930 )
    const url = `${baseUrl}?${params.toString()}`;

    console.log(`요청 URL: ${url}`);

    try {
        // GET 요청 보내기
        const response = await fetch(url, {
            method: 'GET', // GET 요청 명시
            headers: {
                'Content-Type': 'application/json' // 일반적으로 JSON 응답을 기대
            }
        });

        // HTTP 상태 코드 확인 (4xx, 5xx 에러 처리)
        if (!response.ok) {
            // 에러 응답 본문을 읽기 시도
            const errorText = await response.text();
            throw new Error(`HTTP 오류! 상태 코드: ${response.status} (${errorText})`);
        }

        // 응답 본문을 JSON으로 파싱하여 반환
        const data = await response.json();
        return data;

    } catch (error) {
        console.error('API 요청 중 오류 발생:', error.message);
        // 에러를 호출자에게 다시 던지거나 적절한 오류 값을 반환
        return null;
    }
}