// main_page.js
import {loadIndexSparklineData, loadRankingData, loadRealtimeData} from "../common/api/fetch_utils.js";

document.addEventListener('DOMContentLoaded', function () {
    const indexNames = document.querySelectorAll('.main__index-name');
    const indexValues = document.querySelectorAll('.main__index-value');
    const loadSparklinePromises = [];
    const loadRankingPromises = [];
    let activeHoverElement = null;

    // 💡 전역 변수로 관리할 맵 (종목 코드 -> DOM 요소 및 데이터)
    const realtimeDOMMap = {};
    const updateInterval = 1000; // 1초마다 폴링

    indexNames.forEach((e) => {
        const indexName = e.innerHTML;

        if (indexName) {
            loadSparklinePromises.push(loadIndexSparklineData(indexName));
        }
    });

    loadRankingPromises.push(loadRankingData());

    Promise.all(loadSparklinePromises)
        .then(results => {
            results.forEach((dataObject, i) => {
                const prices = dataObject.prices || [];
                const overnights = dataObject.overnights || [];

                const color = overnights[overnights.length - 1] > 0 ? "#FF5252" : "#42A5F5";
                indexValues[i].innerHTML = (prices[prices.length - 1] / 100.0).toLocaleString('ko-KR');
                indexValues[i].style.color = color;
            });
        })
        .catch(error => {
            const failedIndex = error.indexName || "알 수 없는 지수";
            console.error(`데이터 로드 실패 (${failedIndex}):`, error.message);

            if (failedIndex !== "알 수 없는 지수") {
                d3.select('#' + failedIndex).html("데이터 로드 실패");
            }
        });

    Promise.all(loadRankingPromises)
        .then(results => {
            const rankingList = results[0] || [];

            let formattedDate = '';

            if (rankingList[0] && rankingList[0].lastUpdate) {
                const updateDate = new Date(rankingList[0].lastUpdate);

                const hour = updateDate.getHours();
                const min = updateDate.getMinutes();

                formattedDate = `${hour.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')}`;
            } else {
                formattedDate = '-';
            }
            const timeSpan = document.querySelector('.main__table-header-row span:nth-child(1)');
            timeSpan.textContent = "순위 · 오늘 " + formattedDate + " 기준"

            const subscribedCodes = [];

            rankingList.forEach((item, index) => {
                const rank = index + 1;
                const stkCode = item.stkCode.replace('_AL', '');
                const actualPrice = Math.abs(item.curPrice);

                const isPositive = item.fluRt > 0;
                const isNegative = item.fluRt < 0;

                const priceClass = isPositive ? 'main__text-red' : (isNegative ? 'main__text-blue' : 'main__text-zero');

                const rateSign = isPositive ? '+' : (isNegative ? '-' : '');

                const selector = `.main__list-row--${rank}`;
                const targetRow = document.querySelector(selector);

                subscribedCodes.push(stkCode); // 실시간 구독 리스트에 추가

                if (index === 0) {
                    activeHoverElement = targetRow;
                    targetRow.classList.add('is-active-hover');
                }

                if (targetRow) {

                    // 1. 순위 번호 (첫 번째 <span>)
                    const rankSpan = targetRow.querySelector('span:nth-child(1)');
                    if(rankSpan) rankSpan.textContent = item.nowRank;

                    // 2. 회사 이름 (두 번째 <span>)
                    const nameSpan = targetRow.querySelector('span:nth-child(2)');
                    if (nameSpan) {
                        nameSpan.innerHTML = `<span class="main__company-profile"></span>${item.stkName}`;
                    }

                    // 3. 현재가 (세 번째 <span>)
                    const priceSpan = targetRow.querySelector('span:nth-child(3)');
                    if (priceSpan) {
                        priceSpan.textContent = actualPrice.toLocaleString() + "원";
                    }

                    // 4. 등락률 (네 번째 <span>)
                    const rateSpan = targetRow.querySelector('span:nth-child(4)');
                    if (rateSpan) {
                        rateSpan.textContent = `${rateSign}${Math.abs(item.fluRt).toFixed(2)}%`;
                        rateSpan.className = 'main__text-rate ' + priceClass;
                    }

                    // 💡 DOM 맵핑 (실시간 업데이트를 위해 필요)
                    realtimeDOMMap[stkCode] = {
                        priceSpan: priceSpan,
                        rateSpan: rateSpan
                    };

                    targetRow.addEventListener('mouseenter', () => {
                        if (!item.stkCode) return;

                        if (activeHoverElement && activeHoverElement !== targetRow) {
                            activeHoverElement.classList.remove('is-active-hover');
                        }

                        const url = new URL(window.location.href);

                        targetRow.classList.add('is-active-hover');
                        activeHoverElement = targetRow;

                        url.searchParams.set('code', stkCode);
                        window.history.pushState({ path: url.href }, '', url.href);

                    });

                    targetRow.addEventListener('click', ()=>{
                        if (!item.stkCode) return;

                        const url = new URL(window.location.href);
                        url.pathname = url.pathname.replace(/\/$/, '') + '/stock';
                        url.searchParams.set('code', stkCode);
                        window.location.href = url.href;
                    })

                } else {
                    console.warn(`순위 ${rank}에 해당하는 DOM 요소(.main__list-row--${rank})를 찾을 수 없습니다.`);
                }
            });

            // 💡 랭킹 로드 완료 후 실시간 업데이트 시작
            startRealtimeUpdates(subscribedCodes);

        })
        .catch(error => {
            const failedCode = "알 수 없는 랭킹";
            console.error(`데이터 로드 실패 (${failedCode}):`, error.message);

            const errorElement = document.querySelector('.main__list-row--1');
            if (errorElement) {
                errorElement.innerHTML = `<span style="color:red; grid-column: 1 / span 4;">데이터 로드 실패 (${failedCode})</span>`;
            }
        });

    /**
     * 서버에 실시간 데이터를 폴링 방식으로 요청하고 화면을 업데이트합니다.
     * loadRealtimeData는 HTTP 기반이므로 setInterval을 사용합니다.
     */
    function startRealtimeUpdates(codes) {
        if (codes.length === 0) return;

        // 모든 코드를 쉼표로 연결하여 쿼리 파라미터로 전송
        const codeString = codes.join(',');
        const url = `/api/socket?code=${codeString}`;

        const fetchAndUpdate = () => {

            fetch(url)
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`HTTP Error! Status: ${response.status}`);
                    }
                    return response.json();
                })
                .then(realtimeDataList => {

                    realtimeDataList.forEach(realtimeItem => {
                        const code = realtimeItem.item;

                        const mappedElements = realtimeDOMMap[code];

                        if (mappedElements) {
                            // 현재가 ('20')와 등락률 ('12') 키를 사용하여 업데이트
                            const currentPrice = parseFloat(realtimeItem.values['20']);
                            const changeRate = parseFloat(realtimeItem.values['12']);

                            // 현재가 업데이트
                            mappedElements.priceSpan.textContent = Math.abs(currentPrice).toLocaleString() + "원";

                            // 등락률 업데이트
                            const isPositive = changeRate > 0;
                            const isNegative = changeRate < 0;

                            const rateSign = isPositive ? '+' : (isNegative ? '' : '');
                            const priceClass = isPositive ? 'main__text-red' : (isNegative ? 'main__text-blue' : 'main__text-zero');

                            mappedElements.rateSpan.textContent = `${rateSign}${Math.abs(changeRate).toFixed(2)}%`;
                            mappedElements.rateSpan.className = 'main__text-rate ' + priceClass;
                        }
                    });

                })
                .catch(error => {
                    console.error("실시간 데이터 업데이트 중 오류 발생:", error.message);
                });
        };

        // 1초마다 함수 실행 시작
        setInterval(fetchAndUpdate, updateInterval);

        // 최초 1회 즉시 실행
        fetchAndUpdate();
    }
});