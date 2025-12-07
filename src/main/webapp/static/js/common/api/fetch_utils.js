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
                console.log(`순수 JS로 로드된 ${indexName} 데이터:`, data);

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
                if (!response.ok) {
                    throw new Error(`HTTP Error! Status: ${response.status} - Could not load ranking data for ${rankingCode}`);
                }
                return response.json();
            })
            .then(data => {
                console.log(`순수 JS로 로드된 랭킹 데이터:`, data);

                const rankingList = data || [];

                resolve(rankingList);
            })
            .catch(error => {

                console.error(`랭킹 데이터 로드 중 오류 발생 :`, error.message);
                reject(error);
            });
    });
}