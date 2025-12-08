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