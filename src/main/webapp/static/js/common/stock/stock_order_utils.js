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
 * @param {HTMLElement} watchPriceInfo - 감시 가격 정보 표시 요소
 */
export function updateWatchPriceInfo(watchPriceInput, watchPriceInfo) {
    if (!watchPriceInput || !watchPriceInfo) return;

    const watchPriceValue = watchPriceInput.value.replace(/,/g, "");
    const watchPrice = parseFloat(watchPriceValue);
    
    const currentPriceStr = watchPriceInfo.getAttribute("data-current-price");
    const avgPriceStr = watchPriceInfo.getAttribute("data-avg-price");
    
    if (!currentPriceStr) {
        watchPriceInfo.textContent = "현재가 보다 0.00%";
        watchPriceInfo.style.color = getPriceColorCode(0);
        return;
    }

    const currentPrice = parseFloat(currentPriceStr);
    
    if (isNaN(watchPrice) || watchPrice <= 0) {
        // 감시 가격이 입력되지 않았거나 유효하지 않으면 기본값 표시
        if (avgPriceStr) {
            watchPriceInfo.textContent = "현재가 보다 0.00% 내 평단 보다 0.00%";
        } else {
            watchPriceInfo.textContent = "현재가 보다 0.00%";
        }
        watchPriceInfo.style.color = getPriceColorCode(0);
        return;
    }

    // 감시 가격 기준으로 현재가의 변동률 계산
    // (현재가 - 감시가격) / 감시가격 * 100
    // 음수: 현재가가 감시가격보다 낮음, 양수: 현재가가 감시가격보다 높음
    const watchPriceDiff = ((currentPrice - watchPrice) / watchPrice) * 100;
    
    // 보유 주식이 있는 경우 평단가 비교
    let finalText = "";
    
    if (avgPriceStr) {
        const avgPrice = parseFloat(avgPriceStr);
        if (!isNaN(avgPrice) && avgPrice > 0) {
            const avgPriceDiff = ((currentPrice - avgPrice) / avgPrice) * 100;
            
            // 감시가격 기준 텍스트 생성
            const watchPriceText = watchPriceDiff === 0 
                ? `현재가 보다 0.00%`
                : `현재가 보다 ${formatPriceChange(watchPriceDiff)}%`;
            
            // 평단가 기준 텍스트 생성
            const avgPriceText = avgPriceDiff === 0
                ? `평단 보다 0.00%`
                : `평단 보다 ${formatPriceChange(avgPriceDiff)}%`;
            
            finalText = `${watchPriceText} 내 ${avgPriceText}`;
        } else {
            // 평단가가 없거나 유효하지 않은 경우
            finalText = watchPriceDiff === 0
                ? `현재가 보다 0.00%`
                : `현재가 보다 ${formatPriceChange(watchPriceDiff)}%`;
        }
    } else {
        // 평단가 정보가 없는 경우
        finalText = watchPriceDiff === 0
            ? `현재가 보다 0.00%`
            : `현재가 보다 ${formatPriceChange(watchPriceDiff)}%`;
    }
    
    // 색상 결정: watchPriceDiff 기준으로 색상 결정
    const finalColor = getPriceColorCode(watchPriceDiff);
    
    watchPriceInfo.textContent = finalText;
    watchPriceInfo.style.color = finalColor;
}

