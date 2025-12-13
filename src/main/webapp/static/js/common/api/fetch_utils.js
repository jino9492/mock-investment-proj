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