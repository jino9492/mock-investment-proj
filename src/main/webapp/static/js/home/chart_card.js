import {getPriceColorClass, getPriceColorCode, formatPriceChange} from "../common/price_color_utils.js";

const stockDetailContainer = document.querySelector('.chart-card');

export async function fetchAndUpdateStockDetail(stkCode) {
    if (!stkCode) {
        console.warn("주식 코드가 유효하지 않습니다.");
        return;
    }

    const endpoint = `/api/stock/detail?code=${stkCode}`;

    try {
        console.log(`새로운 코드(${stkCode})로 데이터 요청: ${endpoint}`);

        const response = await fetch(endpoint);

        if (!response.ok) {
            throw new Error(`HTTP Error: ${response.status}`);
        }

        const data = await response.json();

        renderStockData(data);
    } catch (error) {
        console.error('데이터 갱신 실패:', error);
        if (stockDetailContainer) {
            stockDetailContainer.innerHTML = `<p class="error-message">데이터 로드 중 오류 발생: ${error.message}</p>`;
        }
    }
}

function renderStockData(stockData) {
    if (!stockDetailContainer) return;

    const titleDiv = stockDetailContainer.querySelector('.main__chart-header strong');
    const infoDivs = stockDetailContainer.querySelectorAll('.main__chart-info-label+span');
    const innerTitleDiv = infoDivs[0];
    const mcapDiv = infoDivs[1];
    const listingDateDiv = infoDivs[2];
    const faceDiv = infoDivs[3];

    rerenderPrice();

    titleDiv.innerHTML = stockData.companyName;
    innerTitleDiv.innerHTML = stockData.companyName;
    mcapDiv.innerHTML = stockData.marketCap;
    listingDateDiv.innerHTML = stockData.listingDate;
    faceDiv.innerHTML = stockData.faceValue;

    console.log(`UI 갱신 완료: ${stockData.companyName}`);
}

export function rerenderPrice(){
    const priceDiv = stockDetailContainer.querySelector('.main__chart-header span');

    const selectedStock = document.querySelector('.is-active-hover');

    if(selectedStock && priceDiv){
        let price = selectedStock.querySelector('span:nth-child(3)').innerHTML;
        let change = selectedStock.querySelector('span:nth-child(4)').innerHTML;
        priceDiv.innerHTML =`
            ${price}
            <span class=${getPriceColorClass((parseFloat(change.replace('+', ''))))}>(${change})</span>
        `
    }
}

document.addEventListener('DOMContentLoaded', function () {
    window.addEventListener('popstate', function(event) {
        const urlParams = new URLSearchParams(window.location.search);
        const code = urlParams.get('code');

        if (code) {
            fetchAndUpdateStockDetail(code);
        } else {
            if (stockDetailContainer) {
                stockDetailContainer.innerHTML = '<p>조회할 종목 코드가 없습니다.</p>';
            }
        }
    });

    const initialParams = new URLSearchParams(window.location.search);
    const initialCode = initialParams.get('code');
    if (initialCode) {
        fetchAndUpdateStockDetail(initialCode);
    }
});