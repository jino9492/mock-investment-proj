/**
 * 가격 변동 색상 유틸리티 함수
 * 전날 대비 상승/하락/변동없음에 따른 색상 클래스 및 코드 반환
 */

/**
 * 가격 변동에 따른 색상 클래스 반환
 * @param {number} value - 변동값 (양수: 상승, 음수: 하락, 0: 변동없음)
 * @returns {string} CSS 클래스명 ('price-color--up', 'price-color--down', 'price-color--neutral')
 */
function getPriceColorClass(value) {
    if (value == null || isNaN(value)) {
        return 'price-color--neutral';
    }
    if (value > 0) return 'price-color--up';
    if (value < 0) return 'price-color--down';
    return 'price-color--neutral';
}

/**
 * 가격 변동에 따른 색상 코드 반환
 * @param {number} value - 변동값
 * @returns {string} 색상 코드 (#F04452, #3182F6, #666666)
 */
function getPriceColorCode(value) {
    if (value == null || isNaN(value)) {
        return '#666666';
    }
    if (value > 0) return '#F04452';
    if (value < 0) return '#3182F6';
    return '#666666';
}

/**
 * 가격 변동에 따른 부호 포함 텍스트 반환
 * @param {number} value - 변동값
 * @param {number} decimals - 소수점 자릿수 (기본값: 2)
 * @returns {string} 포맷된 텍스트 (예: "+1.23%", "-0.50%", "0.00%")
 */
function formatPriceChange(value, decimals = 2) {
    if (value == null || isNaN(value)) {
        return `0.${'0'.repeat(decimals)}`;
    }
    if (value > 0) return `+${value.toFixed(decimals)}`;
    if (value < 0) return value.toFixed(decimals);
    return `0.${'0'.repeat(decimals)}`;
}

// 함수들을 export
export { getPriceColorClass, getPriceColorCode, formatPriceChange };

