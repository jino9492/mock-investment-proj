/**
 * Stock Order 공통 유틸리티 함수
 * 주문 관련 공통 함수들을 제공합니다.
 */
import {getPriceColorCode, formatPriceChange} from "../price_color_utils.js";

/**
 * 숫자를 천 단위 콤마가 포함된 문자열로 포맷팅
 * @param {number} num - 포맷팅할 숫자
 * @returns {string} 포맷팅된 문자열
 */
export function formatNumber(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

/**
 * 총 주문 금액 계산 및 표시 업데이트
 * @param {HTMLElement} priceInputElement - 가격 입력 요소
 * @param {HTMLElement} quantityInputElement - 수량 입력 요소
 * @param {HTMLElement} totalAmountElement - 총 금액 표시 요소
 */
export function calculateTotalAmount(priceInputElement, quantityInputElement, totalAmountElement) {
    if (!priceInputElement || !quantityInputElement || !totalAmountElement) return;

    const price = parseFloat(priceInputElement.value.replace(/,/g, "")) || 0;
    const quantity = parseInt(quantityInputElement.value.replace(/,/g, "")) || 0;
    const total = price * quantity;

    totalAmountElement.textContent = formatNumber(total) + "원";
}

/**
 * 구매 가능 금액 초기화
 * @param {HTMLElement} availableAmountElement - 구매 가능 금액 표시 요소
 */
export function initializeAvailableAmount(availableAmountElement) {
    if (!availableAmountElement) return;

    const availableCash = availableAmountElement.getAttribute("data-available-cash");
    if (availableCash) {
        const cash = parseInt(availableCash) || 0;
        availableAmountElement.textContent = formatNumber(cash) + "원";
    } else {
        availableAmountElement.textContent = "0원";
    }
}

/**
 * 현재가를 기본값으로 설정
 * @param {HTMLElement} priceInputElement - 가격 입력 요소
 * @param {HTMLElement} orderTypeHidden - 주문 유형 hidden input
 */
export function initializeCurrentPrice(priceInputElement, orderTypeHidden) {
    if (!priceInputElement) return;

    const currentPrice = priceInputElement.getAttribute("data-current-price");
    if (currentPrice && !priceInputElement.value) {
        // 지정가 모드이고 값이 없을 때만 설정
        const isLimitMode = orderTypeHidden && orderTypeHidden.value === "LIMIT";
        if (isLimitMode) {
            priceInputElement.value = formatNumber(parseInt(currentPrice));
        }
    }
}

/**
 * 수량 초기화 (기본값 없음)
 * @param {HTMLElement} quantityInputElement - 수량 입력 요소
 */
export function initializeQuantity(quantityInputElement) {
    if (!quantityInputElement) return;
    // 기본값 설정하지 않음
}

/**
 * 감시 가격과 현재가 비교 계산 및 표시 업데이트
 * @param {HTMLElement} watchPriceInput - 감시 가격 입력 요소
 * @param {HTMLElement} watchPriceInfo - 감시 가격 정보 표시 요소 (기존 호환성을 위해 유지, 실제로는 wrapper 요소)
 */
export function updateWatchPriceInfo(watchPriceInput, watchPriceInfo) {
    if (!watchPriceInput) return;

    const watchPriceValue = watchPriceInput.value.replace(/,/g, "");
    const watchPrice = parseFloat(watchPriceValue);
    
    // watchPriceInfo가 wrapper인 경우와 아닌 경우 모두 처리
    // 새로운 구조: wrapper 내부에 -current와 -avg 요소가 있음
    let watchPriceInfoCurrent = null;
    let watchPriceInfoAvg = null;
    
    if (watchPriceInfo) {
        // wrapper 요소인지 확인 (클래스나 자식 요소 확인)
        const isWrapper = watchPriceInfo.classList && watchPriceInfo.classList.contains('order-card__watch-price-info-wrapper');
        
        if (isWrapper) {
            // 새로운 구조 (wrapper)
            watchPriceInfoCurrent = watchPriceInfo.querySelector('[id$="-current"]');
            watchPriceInfoAvg = watchPriceInfo.querySelector('[id$="-avg"]');
            
            // querySelector로 찾지 못한 경우, ID를 통해 찾기 시도
            if (!watchPriceInfoCurrent || !watchPriceInfoAvg) {
                // wrapper 내부의 첫 번째 요소의 ID를 기반으로 찾기
                const firstChild = watchPriceInfo.querySelector('.order-card__watch-price-info');
                if (firstChild && firstChild.id) {
                    const baseId = firstChild.id.replace(/-current$|-avg$/, '');
                    watchPriceInfoCurrent = watchPriceInfoCurrent || document.getElementById(baseId + '-current');
                    watchPriceInfoAvg = watchPriceInfoAvg || document.getElementById(baseId + '-avg');
                }
            }
        } else {
            // 단일 요소인지 확인
            if (watchPriceInfo.id && (watchPriceInfo.id.endsWith('-current') || watchPriceInfo.id.endsWith('-avg'))) {
                // -current 또는 -avg로 끝나는 요소
                if (watchPriceInfo.id.endsWith('-current')) {
                    watchPriceInfoCurrent = watchPriceInfo;
                } else {
                    watchPriceInfoAvg = watchPriceInfo;
                }
                
                // 나머지 요소 찾기
                const baseId = watchPriceInfo.id.replace(/-current$|-avg$/, '');
                if (!watchPriceInfoCurrent) {
                    watchPriceInfoCurrent = document.getElementById(baseId + '-current');
                }
                if (!watchPriceInfoAvg) {
                    watchPriceInfoAvg = document.getElementById(baseId + '-avg');
                }
            } else {
                // 기존 구조 (단일 요소) - 호환성 유지
                watchPriceInfoCurrent = watchPriceInfo;
            }
        }
    }
    
    // 현재가 비교 정보 업데이트
    if (watchPriceInfoCurrent) {
        const currentPriceStr = watchPriceInfoCurrent.getAttribute("data-current-price");
        
        if (!currentPriceStr) {
            watchPriceInfoCurrent.textContent = "현재가 보다 0.00%";
            watchPriceInfoCurrent.style.color = getPriceColorCode(0);
        } else {
            const currentPrice = parseFloat(currentPriceStr);
            
            if (isNaN(watchPrice) || watchPrice <= 0) {
                watchPriceInfoCurrent.textContent = "현재가 보다 0.00%";
                watchPriceInfoCurrent.style.color = getPriceColorCode(0);
            } else {
                // 감시 가격 기준으로 현재가의 변동률 계산
                // (현재가 - 감시가격) / 감시가격 * 100
                const watchPriceDiff = ((currentPrice - watchPrice) / watchPrice) * 100;
                
                const watchPriceText = watchPriceDiff === 0 
                    ? `현재가 보다 0.00%`
                    : `현재가 보다 ${formatPriceChange(watchPriceDiff)}%`;
                
                watchPriceInfoCurrent.textContent = watchPriceText;
                watchPriceInfoCurrent.style.color = getPriceColorCode(watchPriceDiff);
            }
        }
    }
    
    // 평단가 비교 정보 업데이트
    if (watchPriceInfoAvg) {
        const avgPriceStr = watchPriceInfoAvg.getAttribute("data-avg-price");
        
        if (!avgPriceStr) {
            watchPriceInfoAvg.textContent = "";
            watchPriceInfoAvg.style.display = "none";
        } else {
            const avgPrice = parseFloat(avgPriceStr);
            
            if (isNaN(avgPrice) || avgPrice <= 0) {
                watchPriceInfoAvg.textContent = "";
                watchPriceInfoAvg.style.display = "none";
            } else if (isNaN(watchPrice) || watchPrice <= 0) {
                watchPriceInfoAvg.textContent = "내 평단 보다 0.00%";
                watchPriceInfoAvg.style.color = getPriceColorCode(0);
                watchPriceInfoAvg.style.display = "";
            } else {
                // 평단가 기준으로 감시가격의 변동률 계산
                // (감시가격 - 평단가) / 평단가 * 100
                // 음수: 감시가격이 평단가보다 낮음, 양수: 감시가격이 평단가보다 높음
                const avgPriceDiff = ((watchPrice - avgPrice) / avgPrice) * 100;
                
                const avgPriceText = avgPriceDiff === 0
                    ? `내 평단 보다 0.00%`
                    : `내 평단 보다 ${formatPriceChange(avgPriceDiff)}%`;
                
                watchPriceInfoAvg.textContent = avgPriceText;
                watchPriceInfoAvg.style.color = getPriceColorCode(avgPriceDiff);
                watchPriceInfoAvg.style.display = "";
            }
        }
    }
}

