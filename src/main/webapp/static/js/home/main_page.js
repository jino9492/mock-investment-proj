// main_page.js
import {loadIndexSparklineData, loadRankingData, loadRealtimeData} from "../common/api/fetch_utils.js";
import {getPriceColorClass} from "../common/price_color_utils.js";
import {registerToSocket} from "../common/api/socket.js";
import {fetchAndUpdateStockDetail} from "./chart_card.js";
import {loadChartByUrlCode} from "../common/chart/candle_chart.js";

document.addEventListener('DOMContentLoaded', function () {
    const indexNames = document.querySelectorAll('.main__index-name');
    const indexValues = document.querySelectorAll('.main__index-value');
    const loadSparklinePromises = [];
    const loadRankingPromises = [];
    let activeHoverElement = null;

    // 전역 변수로 관리할 맵 (종목 코드 -> DOM 요소)
    const realtimeDOMMap = {};
    const updateInterval = 2000; // 2초마다 폴링 간격

    // =========================================================================
    // ⭐ 반짝임 효과 적용 함수
    // =========================================================================
    /**
     * DOM 요소에 반짝임 효과 클래스를 적용하고 지정된 시간 후 제거합니다.
     * @param {HTMLElement} element - 효과를 적용할 DOM 요소
     * @param {boolean} isPositive - 양수(빨강)인지 여부
     */
    function applyFlashEffect(element, isPositive) {
        const className = isPositive ? 'text-highlight-up' : 'text-highlight-down';

        element.classList.remove('text-highlight-up', 'text-highlight-down');

        // 강제로 Reflow를 유도하여 애니메이션을 재실행
        void element.offsetWidth;

        element.classList.add(className);

        // 500ms(0.5초) 후 클래스 제거
        setTimeout(() => {
            element.classList.remove(className);
        }, 500);
    }
    // =========================================================================


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
        .then(async results => {
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

                const priceClass = getPriceColorClass(item.fluRt);

                const rateSign = isPositive ? '+' : (isNegative ? '-' : '');

                const selector = `.main__list-row--${rank}`;
                const targetRow = document.querySelector(selector);

                subscribedCodes.push(stkCode);

                if (index === 0) {
                    activeHoverElement = targetRow;
                    targetRow.classList.add('is-active-hover');

                    const url = new URL(window.location.href);
                    url.searchParams.set('code', stkCode);
                    window.history.pushState({path: url.href}, '', url.href);

                    loadChartByUrlCode();
                    fetchAndUpdateStockDetail(stkCode);
                }

                if (targetRow) {

                    const rankSpan = targetRow.querySelector('span:nth-child(1)');
                    if (rankSpan) rankSpan.textContent = item.nowRank;

                    const nameSpan = targetRow.querySelector('span:nth-child(2)');
                    if (nameSpan) {
                        nameSpan.innerHTML = `<span class="main__company-profile"></span>${item.stkName}`;
                    }

                    const priceSpan = targetRow.querySelector('span:nth-child(3)');
                    if (priceSpan) {
                        priceSpan.textContent = actualPrice.toLocaleString() + "원";
                    }

                    const rateSpan = targetRow.querySelector('span:nth-child(4)');
                    if (rateSpan) {
                        rateSpan.textContent = `${rateSign}${Math.abs(item.fluRt).toFixed(2)}%`;
                        rateSpan.className = 'main__text-rate ' + priceClass;
                    }

                    // DOM 맵에 priceSpan과 rateSpan을 저장
                    realtimeDOMMap[stkCode] = {
                        priceSpan: priceSpan,
                        rateSpan: rateSpan
                    };

                    targetRow.addEventListener('mouseenter', () => {
                        if (!item.stkCode) return;

                        loadChartByUrlCode();

                        if (activeHoverElement && activeHoverElement !== targetRow) {
                            activeHoverElement.classList.remove('is-active-hover');
                        }

                        const url = new URL(window.location.href);

                        targetRow.classList.add('is-active-hover');
                        activeHoverElement = targetRow;

                        url.searchParams.set('code', stkCode);
                        window.history.pushState({path: url.href}, '', url.href);

                        fetchAndUpdateStockDetail(stkCode);
                    });

                    targetRow.addEventListener('click', () => {
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

            const socket = await registerToSocket(subscribedCodes);
            startRealtimeUpdates(subscribedCodes, socket);

        })
        .catch(error => {
            const failedCode = "알 수 없는 랭킹";
            console.error(`데이터 로드 실패 (${failedCode}):`, error.message);

            const errorElement = document.querySelector('.main__list-row--1');
            if (errorElement) {
                errorElement.innerHTML = `<span style="color:red; grid-column: 1 / span 4;">데이터 로드 실패 (${failedCode})</span>`;
            }
        });

    function startRealtimeUpdates(codes, socket) {
        if (codes.length === 0) return;

        // const ITEM_DELAY_MS = 40; // 개별 종목 요청 사이의 지연 시간 (ms)
        let lastResponse = {};
        const fetchAndUpdate = async () => {
            // for (const code of codes) {
            //     try {
            //         const dataArray = await loadRealtimeData(code);
            //         const realtimeItem = dataArray && dataArray.length > 0 ? dataArray[0] : null;
            //
            //         if (!realtimeItem.realtime) {
            //             continue;
            //         }
            //
            //         const mappedElements = realtimeDOMMap[code];
            //         const priceString = realtimeItem.realtime.curPrice;
            //         const rateString = realtimeItem.realtime.fluRt;
            //
            //         if (mappedElements && priceString && rateString) {
            //             const currentPrice = parseFloat(priceString.replace(/^[+-]/, ''));
            //             const changeRate = parseFloat(rateString);
            //
            //             if (!isNaN(currentPrice) && !isNaN(changeRate)) {
            //                 const oldRateTextRaw = mappedElements.rateSpan.textContent.replace(/[%]/g, '');
            //
            //                 const newRateText = `${rateString}%`;
            //
            //                 const isPositive = changeRate > 0;
            //                 const priceClass = getPriceColorClass(changeRate);
            //
            //                 const oldRateFormatted = parseFloat(oldRateTextRaw).toFixed(2);
            //                 const newRateFormatted = parseFloat(rateString).toFixed(2);
            //                 if (newRateFormatted !== oldRateFormatted) {
            //                     mappedElements.rateSpan.textContent = newRateText;
            //                     mappedElements.rateSpan.className = 'main__text-rate ' + priceClass;
            //                     applyFlashEffect(mappedElements.rateSpan, isPositive);
            //                 }
            //             }
            //         }
            //
            //         await new Promise(resolve => setTimeout(resolve, ITEM_DELAY_MS));
            //
            //     } catch (error) {
            //         console.error(`[${code}] 데이터 업데이트 중 오류 발생:`, error.message);
            //     }
            // }

            let currentResponse = socket.getRecentResponse();

            if ((JSON.stringify(currentResponse) !== JSON.stringify(lastResponse)) && lastResponse.data != null){
                console.log("changed!");
            }

            lastResponse = currentResponse;

            console.log("순차 업데이트 주기 완료.");
        };

        setInterval(fetchAndUpdate, updateInterval);

        fetchAndUpdate();
    }
});