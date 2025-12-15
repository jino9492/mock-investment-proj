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
    if (!code) {
        return Promise.reject(new Error("종목 코드가 제공되지 않았습니다."));
    }

    const url = `/api/price?code=${code}`;

    return new Promise((resolve, reject) => {
        fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(response => {
                // 1. HTTP 상태 코드 검사
                if (!response.ok) {
                    if (response.status === 404) {
                        throw new Error(`[${code}] 종목의 실시간 데이터가 캐시에 존재하지 않습니다. (404 Not Found)`);
                    }
                    throw new Error(`HTTP Error: ${response.status} - 실시간 데이터 로드 실패`);
                }

                const contentType = response.headers.get('content-type');
                if (contentType && contentType.includes('application/json')) {
                    return response.json();
                }

                return null;
            })
            .then(data => {
                if (data) {
                    resolve(data);
                } else {
                    resolve(null);
                }
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