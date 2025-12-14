import {registerToSocket} from "../common/api/socket.js";
import {getPriceColorClass} from "../common/price_color_utils.js";
import {loadRealtimeData} from "../common/api/fetch_utils.js";

document.addEventListener('DOMContentLoaded', function () {
    const stockHeader = document.querySelector('.stock-header');
    let params = new URLSearchParams(document.location.search);
    let code = params.get("code");
    const stockCode = code;

    if (!stockCode) {
        console.warn("(종목 코드)를 찾을 수 없습니다.");
        return;
    }

    const priceElement = document.querySelector('.stock-header__price');
    const diffValueElement = document.querySelector('.stock-header__diff-value');

    // =========================================================================
    // ⭐ 반짝임 효과 적용 함수 (main_page.js와 동일하게 복사)
    // =========================================================================
    /**
     * DOM 요소에 반짝임 효과 클래스를 적용하고 지정된 시간 후 제거합니다.
     * @param {HTMLElement} element - 효과를 적용할 DOM 요소
     * @param {boolean} isPositive - 양수(빨강)인지 여부
     */
    function applyFlashEffect(element, isPositive) {
        const className = isPositive ? 'text-highlight-up' : 'text-highlight-down';

        element.classList.remove('text-highlight-up', 'text-highlight-down');

        const targetElement = element.closest('.stock-header__price-row') || element;

        targetElement.classList.remove('text-highlight-up', 'text-highlight-down');
        void targetElement.offsetWidth;

        targetElement.classList.add(className);

        setTimeout(() => {
            targetElement.classList.remove(className);
        }, 500);
    }
    // =========================================================================

    const initializeStockData = async (code) => {
        try {
            const data = await loadRealtimeData(code);

            if (data && data.price) {
                const initialPrice = parseFloat(data.price);
                const initialRate = parseFloat(data.fluRt);
                const initialChangePrice = parseFloat(data.changePrice);

                if (priceElement && !isNaN(initialPrice)) {
                    priceElement.textContent = initialPrice.toLocaleString('ko-KR') + '원';
                }

                if (diffValueElement && !isNaN(initialRate) && !isNaN(initialChangePrice)) {
                    const isPositive = initialRate > 0;
                    const sign = isPositive ? '+' : (initialRate < 0 ? '' : '');
                    const priceClass = getPriceColorClass(initialRate);

                    diffValueElement.textContent =
                        `${sign}${initialChangePrice.toLocaleString('ko-KR')}원 (${Math.abs(initialRate).toFixed(2)}%)`;

                    diffValueElement.className = 'stock-header__diff-value ' + priceClass;
                }
            }
        } catch (error) {
            console.error(`초기 가격 로드 실패 (${code}):`, error.message);
        }
    };
    const startRealtimeUpdates = async (code) => {
        const socket = await registerToSocket();
        socket.registerDataUpdateCallback((currentResponse)=>{
            if (currentResponse.data && currentResponse.data.length > 0) {
                const res = currentResponse.data.find(item => item.item === code);

                if (res && res.values) {
                    const priceString = res.values["10"];
                    const changePriceString = res.values["11"];
                    const changeRateString = res.values["12"];

                    const currentPrice = parseFloat(priceString.replace(/^[+-]/, ''));
                    const changePrice = parseFloat(changePriceString);
                    const changeRate = parseFloat(changeRateString);

                    if (!isNaN(currentPrice) && !isNaN(changePrice) && !isNaN(changeRate)) {
                        if (priceElement) {
                            priceElement.textContent = currentPrice.toLocaleString('ko-KR') + '원';
                            console.log(currentPrice)
                            document.querySelector('.order-card__input').setAttribute('data-current-price', currentPrice.toString())
                            document.querySelector('.order-card__input').value = currentPrice.toLocaleString('ko-KR');
                        }

                        if (diffValueElement) {
                            const isPositive = changeRate > 0;
                            const isNegative = changeRate < 0;
                            const sign = isPositive ? '+' : (isNegative ? '' : '');

                            const priceClass = getPriceColorClass(changeRate);

                            diffValueElement.textContent =
                                `${sign}${changePrice.toLocaleString('ko-KR')}원 (${Math.abs(changeRate).toFixed(2)}%)`;

                            diffValueElement.className = 'stock-header__diff-value ' + priceClass;

                            applyFlashEffect(diffValueElement, isPositive);
                        }
                    }
                }
            }
        });
    };

    initializeStockData(stockCode);
    startRealtimeUpdates(stockCode);
});