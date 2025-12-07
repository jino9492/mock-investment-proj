import {loadIndexSparklineData, loadRankingData} from "../common/api/fetch_utils.js";

document.addEventListener('DOMContentLoaded', function () {
    const indexNames = document.querySelectorAll('.main__index-name');
    const indexValues = document.querySelectorAll('.main__index-value');
    const loadSparklinePromises = [];
    const loadRankingPromises = [];
    let activeHoverElement = null;

    indexNames.forEach((e) => {
        const indexName = e.innerHTML;

        if (indexName) {
            loadSparklinePromises.push(loadIndexSparklineData(indexName));
        }
    });

    loadRankingPromises.push(loadRankingData());

    Promise.all(loadSparklinePromises)
        .then(results => {
            console.log("모든 스파크라인 데이터 로드 완료 :", results);

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

    // JavaScript 코드
// loadRankingPromises가 하나의 랭킹만 불러왔다면 results[0]에 데이터가 있다고 가정합니다.
    const rankingCodes = ['gainers']; // 예시로 하나의 랭킹 코드로 가정

    Promise.all(loadRankingPromises)
        .then(results => {
            console.log("모든 랭킹 데이터 로드 완료 :", results);
            const rankingList = results[0] || [];

            let formattedDate = '';

            if (rankingList[0].lastUpdate) {
                const updateDate = new Date(rankingList[0].lastUpdate);

                const hour = updateDate.getHours();
                const min = updateDate.getMinutes();

                formattedDate = `${hour.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')}`;
            } else {
                formattedDate = '-';
            }
            const timeSpan = document.querySelector('.main__table-header-row span:nth-child(1)');
            timeSpan.textContent = "순위 · 오늘 " + formattedDate + " 기준"

            rankingList.forEach((item, index) => {
                const rank = index + 1;
                const actualPrice = Math.abs(item.curPrice);

                const isPositive = item.fluRt > 0;
                const isNegative = item.fluRt < 0;

                const priceClass = isPositive ? 'main__text-red' : (isNegative ? 'main__text-blue' : 'main__text-zero');

                const rateSign = isPositive ? '+' : (isNegative ? '-' : '');

                const selector = `.main__list-row--${rank}`;
                const targetRow = document.querySelector(selector);

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

                    targetRow.addEventListener('mouseenter', () => {
                        if (!item.stkCode) return;

                        if (activeHoverElement && activeHoverElement !== targetRow) {
                            activeHoverElement.classList.remove('is-active-hover');
                        }

                        const url = new URL(window.location.href);

                        targetRow.classList.add('is-active-hover');
                        activeHoverElement = targetRow;

                        url.searchParams.set('code', item.stkCode.replace('_AL', ''));
                        window.history.pushState({ path: url.href }, '', url.href);

                    });

                    targetRow.addEventListener('click', ()=>{
                        if (!item.stkCode) return;

                        const url = new URL(window.location.href);
                        url.pathname = url.pathname.replace(/\/$/, '') + '/stock';
                        console.log(url)
                        url.searchParams.set('code', item.stkCode.replace('_AL', ''));
                        window.location.href = url.href;
                    })

                } else {
                    console.warn(`순위 ${rank}에 해당하는 DOM 요소(.main__list-row--${rank})를 찾을 수 없습니다.`);
                }
            });
        })
        .catch(error => {
            const failedCode = "알 수 없는 랭킹";
            console.error(`데이터 로드 실패 (${failedCode}):`, error.message);

            const errorElement = document.querySelector('.main__list-row--1');
            if (errorElement) {
                errorElement.innerHTML = `<span style="color:red; grid-column: 1 / span 4;">데이터 로드 실패 (${failedCode})</span>`;
            }
        });
});
