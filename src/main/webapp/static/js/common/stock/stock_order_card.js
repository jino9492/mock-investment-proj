/**
 * Stock Order Card 공통 로직
 * 주문 카드 관련 이벤트 핸들러 및 로직을 제공합니다.
 */

import { formatNumber, calculateTotalAmount, updateWatchPriceInfo } from './stock_order_utils.js';

/**
 * 가격 증감 버튼 이벤트 핸들러 설정
 * @param {HTMLElement} priceMinusBtn - 가격 감소 버튼
 * @param {HTMLElement} pricePlusBtn - 가격 증가 버튼
 * @param {HTMLElement} priceInputElement - 가격 입력 요소
 * @param {HTMLElement} quantityInputElement - 수량 입력 요소
 * @param {HTMLElement} totalAmountElement - 총 금액 표시 요소
 * @param {Function} calculateTotalAmountCallback - 총 금액 계산 콜백 함수 (선택적, 없으면 공통 함수 사용)
 */
export function setupPriceStepButtons(priceMinusBtn, pricePlusBtn, priceInputElement, quantityInputElement, totalAmountElement, calculateTotalAmountCallback = null) {
    if (!priceMinusBtn || !pricePlusBtn || !priceInputElement) return;

    const calcTotal = calculateTotalAmountCallback || (() => calculateTotalAmount(priceInputElement, quantityInputElement, totalAmountElement));

    priceMinusBtn.addEventListener("click", function() {
        if (priceInputElement.readOnly) return; // 시장가 모드에서는 동작 안 함

        let currentPrice = parseFloat(priceInputElement.value.replace(/,/g, "")) || 0;
        currentPrice = Math.max(0, currentPrice - 5);
        priceInputElement.value = formatNumber(currentPrice);
        calcTotal();
    });

    pricePlusBtn.addEventListener("click", function() {
        if (priceInputElement.readOnly) return; // 시장가 모드에서는 동작 안 함

        let currentPrice = parseFloat(priceInputElement.value.replace(/,/g, "")) || 0;
        currentPrice = currentPrice + 5;
        priceInputElement.value = formatNumber(currentPrice);
        calcTotal();
    });
}

/**
 * 수량 증감 버튼 이벤트 핸들러 설정
 * @param {HTMLElement} quantityMinusBtn - 수량 감소 버튼
 * @param {HTMLElement} quantityPlusBtn - 수량 증가 버튼
 * @param {HTMLElement} quantityInputElement - 수량 입력 요소
 * @param {HTMLElement} priceInputElement - 가격 입력 요소
 * @param {HTMLElement} totalAmountElement - 총 금액 표시 요소
 * @param {number|Function} maxQuantity - 최대 수량 (구매: 예치금/주가, 판매: 보유 수량) 또는 최대값을 계산하는 함수
 * @param {Function} calculateTotalAmountCallback - 총 금액 계산 콜백 함수 (선택적, 없으면 공통 함수 사용)
 */
export function setupQuantityStepButtons(quantityMinusBtn, quantityPlusBtn, quantityInputElement, priceInputElement, totalAmountElement, maxQuantity = null, calculateTotalAmountCallback = null) {
    if (!quantityMinusBtn || !quantityPlusBtn || !quantityInputElement) return;

    const calcTotal = calculateTotalAmountCallback || (() => calculateTotalAmount(priceInputElement, quantityInputElement, totalAmountElement));

    quantityMinusBtn.addEventListener("click", function() {
        let currentQuantity = parseInt(quantityInputElement.value.replace(/,/g, "")) || 0;
        currentQuantity = Math.max(0, currentQuantity - 1);
        quantityInputElement.value = currentQuantity > 0 ? currentQuantity.toString() : "";
        calcTotal();
    });

    quantityPlusBtn.addEventListener("click", function() {
        let currentQuantity = parseInt(quantityInputElement.value.replace(/,/g, "")) || 0;
        let newQuantity = currentQuantity + 1;
        
        // 최대값 제한 (함수인 경우 호출하여 계산)
        let maxQty = maxQuantity;
        if (typeof maxQuantity === 'function') {
            maxQty = maxQuantity();
        }
        
        // maxQty가 null이거나 0보다 큰 경우에만 제한 적용
        // (시장가 모드에서 가격을 가져올 수 없는 경우를 위해)
        if (maxQty !== null && maxQty !== undefined && maxQty > 0 && newQuantity > maxQty) {
            newQuantity = maxQty;
        }
        
        quantityInputElement.value = newQuantity.toString();
        calcTotal();
    });
}

/**
 * 가격 입력 필드 이벤트 핸들러 설정
 * @param {HTMLElement} priceInputElement - 가격 입력 요소
 * @param {HTMLElement} quantityInputElement - 수량 입력 요소
 * @param {HTMLElement} totalAmountElement - 총 금액 표시 요소
 * @param {Function} calculateTotalAmountCallback - 총 금액 계산 콜백 함수 (선택적, 없으면 공통 함수 사용)
 */
export function setupPriceInput(priceInputElement, quantityInputElement, totalAmountElement, calculateTotalAmountCallback = null) {
    if (!priceInputElement) return;

    const calcTotal = calculateTotalAmountCallback || (() => calculateTotalAmount(priceInputElement, quantityInputElement, totalAmountElement));

    priceInputElement.addEventListener("input", function() {
        // 숫자만 입력 허용 (콤마 제거 후 다시 포맷팅)
        let value = this.value.replace(/[^0-9]/g, "");
        if (value) {
            this.value = formatNumber(parseInt(value));
        }
        calcTotal();
    });
}

/**
 * 수량 입력 필드 이벤트 핸들러 설정
 * @param {HTMLElement} quantityInputElement - 수량 입력 요소
 * @param {HTMLElement} priceInputElement - 가격 입력 요소
 * @param {HTMLElement} totalAmountElement - 총 금액 표시 요소
 * @param {number|Function} maxQuantity - 최대 수량 (구매: 예치금/주가, 판매: 보유 수량) 또는 최대값을 계산하는 함수
 * @param {Function} calculateTotalAmountCallback - 총 금액 계산 콜백 함수 (선택적, 없으면 공통 함수 사용)
 */
export function setupQuantityInput(quantityInputElement, priceInputElement, totalAmountElement, maxQuantity = null, calculateTotalAmountCallback = null) {
    if (!quantityInputElement) return;

    const calcTotal = calculateTotalAmountCallback || (() => calculateTotalAmount(priceInputElement, quantityInputElement, totalAmountElement));

    quantityInputElement.addEventListener("input", function() {
        // 숫자만 입력 허용
        let value = this.value.replace(/[^0-9]/g, "");
        if (value) {
            let num = parseInt(value);
            if (num < 0) {
                this.value = "";
            } else {
                // 최대값 제한 (함수인 경우 호출하여 계산)
                let maxQty = maxQuantity;
                if (typeof maxQuantity === 'function') {
                    maxQty = maxQuantity();
                }
                
                // maxQty가 null이거나 0보다 큰 경우에만 제한 적용
                // (시장가 모드에서 가격을 가져올 수 없는 경우를 위해)
                if (maxQty !== null && maxQty !== undefined && maxQty > 0 && num > maxQty) {
                    num = maxQty;
                }
                this.value = num.toString();
            }
        } else {
            this.value = "";
        }
        calcTotal();
    });
    
    // blur 이벤트로 최대값 재확인
    quantityInputElement.addEventListener("blur", function() {
        let value = this.value.replace(/[^0-9]/g, "");
        if (value) {
            let num = parseInt(value);
            
            // 최대값 제한 (함수인 경우 호출하여 계산)
            let maxQty = maxQuantity;
            if (typeof maxQuantity === 'function') {
                maxQty = maxQuantity();
            }
            
            if (maxQty !== null && maxQty !== undefined && num > maxQty) {
                num = maxQty;
                this.value = num.toString();
            }
        }
        calcTotal();
    });
}

/**
 * 주문 제출 버튼 이벤트 핸들러 설정
 * @param {HTMLElement} submitBtn - 제출 버튼
 * @param {HTMLElement} priceInputElement - 가격 입력 요소
 * @param {HTMLElement} quantityInputElement - 수량 입력 요소
 * @param {HTMLElement} orderTypeHidden - 주문 유형 hidden input
 */
export function setupSubmitButton(submitBtn, priceInputElement, quantityInputElement, orderTypeHidden) {
    if (!submitBtn || submitBtn.tagName !== "BUTTON") return;

    submitBtn.addEventListener("click", async function(e) {
        e.preventDefault();
        
        // 현재 활성화된 탭 확인 (구매/판매)
        const activeTab = document.querySelector(".order-card__trade-tab[aria-checked='true']");
        let side = "BUY"; // 기본값
        if (activeTab) {
            const tabText = activeTab.textContent.trim();
            if (tabText.includes("판매")) {
                side = "SELL";
            } else if (tabText.includes("구매")) {
                side = "BUY";
            } else {
                // 대기 탭이면 주문 제출 불가
                return;
            }
        }
        
        // 주문 데이터 수집 - 여러 소스에서 종목 ID 가져오기
        // 1. stock-header의 data-stock-id (stock-header가 항상 존재하므로 우선 확인)
        const stockHeader = document.querySelector(".stock-header");
        let stockId = stockHeader?.getAttribute("data-stock-id");
        
        if (stockId) {
            console.log("[주문 제출] 종목 ID를 stock-header에서 가져옴:", stockId);
        } else {
            // 2. order-card 섹션의 data-stock-id
            const orderCard = document.querySelector(".order-card");
            stockId = orderCard?.getAttribute("data-stock-id");
            
            if (stockId) {
                console.log("[주문 제출] 종목 ID를 order-card에서 가져옴:", stockId);
            } else {
                // 3. URL 파라미터 (fallback)
                stockId = new URLSearchParams(window.location.search).get("stockID");
                
                if (stockId) {
                    console.log("[주문 제출] 종목 ID를 URL 파라미터에서 가져옴:", stockId);
                }
            }
        }
        
        if (!stockId) {
            console.error("[주문 제출] 종목 ID를 찾을 수 없습니다.");
            console.error("  - stock-header:", stockHeader, "data-stock-id:", stockHeader?.getAttribute("data-stock-id"));
            console.error("  - order-card:", document.querySelector(".order-card"), "data-stock-id:", document.querySelector(".order-card")?.getAttribute("data-stock-id"));
            console.error("  - URL 파라미터 stockID:", new URLSearchParams(window.location.search).get("stockID"));
            alert("종목 정보를 찾을 수 없습니다. 페이지를 새로고침해주세요.");
            return;
        }
        
        // 주문 유형 확인 (일반 주문 / 조건 주문)
        const orderTypeDropdown = document.querySelector("[data-order-type-dropdown]");
        const orderTypeValue = orderTypeDropdown ? 
            (orderTypeDropdown.querySelector("[data-dropdown-input]")?.value || "general") : "general";
        const isConditionalOrder = orderTypeValue === "conditional";
        
        const orderType = orderTypeHidden ? orderTypeHidden.value : "LIMIT";
        let price = null;
        let watchPrice = null;
        let orderDuration = null;
        
        // 조건 주문인 경우 감시 가격 확인
        if (isConditionalOrder) {
            // 구매/판매 탭에 따라 올바른 감시 가격 필드 ID 선택
            let watchPriceInputId = "watch-price-input"; // 기본값 (구매용)
            if (side === "SELL") {
                watchPriceInputId = "conditional-sell-watch-price-input"; // 판매용
            }
            
            const watchPriceInput = document.getElementById(watchPriceInputId);
            if (!watchPriceInput || !watchPriceInput.value) {
                alert("감시 가격을 입력해주세요.");
                return;
            }
            const watchPriceValue = watchPriceInput.value.replace(/,/g, "");
            watchPrice = parseFloat(watchPriceValue);
            if (isNaN(watchPrice) || watchPrice <= 0) {
                alert("올바른 감시 가격을 입력해주세요.");
                return;
            }
            
            // 주문 유지 기간 가져오기 (구매/판매 공통)
            // 조건 주문 섹션 내에서 드롭다운 찾기
            let conditionalOrderSection = null;
            if (side === "BUY") {
                conditionalOrderSection = document.querySelector(".order-card__conditional-order:not(.order-card__conditional-order--sell)");
            } else {
                conditionalOrderSection = document.querySelector(".order-card__conditional-order--sell");
            }
            
            const orderDurationDropdown = conditionalOrderSection 
                ? conditionalOrderSection.querySelector(".order-card__order-duration [data-dropdown]")
                : document.querySelector(".order-card__order-duration [data-dropdown]");
                
            if (orderDurationDropdown) {
                const durationInput = orderDurationDropdown.querySelector("[data-dropdown-input]");
                if (durationInput) {
                    orderDuration = durationInput.value || "1month";
                    console.log("[주문 제출] 주문 유지 기간:", orderDuration);
                } else {
                    console.warn("[주문 제출] 주문 유지 기간 입력 필드를 찾을 수 없습니다.");
                }
            } else {
                console.warn("[주문 제출] 주문 유지 기간 드롭다운을 찾을 수 없습니다.");
            }
        }
        
        if (orderType === "LIMIT") {
            // 조건 주문일 경우 구매/판매 가격 필드 ID 찾기
            let targetPriceInput = priceInputElement;
            if (isConditionalOrder) {
                if (side === "BUY") {
                    targetPriceInput = document.getElementById("conditional-order-price-input");
                } else if (side === "SELL") {
                    targetPriceInput = document.getElementById("conditional-sell-price-input");
                }
            }
            
            if (!targetPriceInput) {
                console.error("[주문 제출] 가격 입력 필드를 찾을 수 없습니다.", {
                    side: side,
                    isConditionalOrder: isConditionalOrder,
                    orderType: orderType
                });
                alert("가격 입력 필드를 찾을 수 없습니다. 페이지를 새로고침해주세요.");
                return;
            }
            
            if (!targetPriceInput.value || targetPriceInput.value.trim() === "") {
                alert("가격을 입력해주세요.");
                return;
            }
            
            const priceValue = targetPriceInput.value.replace(/,/g, "");
            price = parseFloat(priceValue);
            if (isNaN(price) || price <= 0) {
                alert("올바른 가격을 입력해주세요. (0보다 큰 숫자)");
                return;
            }
            
            console.log("[주문 제출] 가격 검증 완료:", price);
        }
        
        // 수량 입력 필드 찾기 (조건 주문일 경우)
        let targetQuantityInput = quantityInputElement;
        if (isConditionalOrder) {
            if (side === "BUY") {
                targetQuantityInput = document.getElementById("conditional-order-quantity-input");
            } else if (side === "SELL") {
                targetQuantityInput = document.getElementById("conditional-sell-quantity-input");
            }
        }
        
        if (!targetQuantityInput) {
            console.error("[주문 제출] 수량 입력 필드를 찾을 수 없습니다.", {
                side: side,
                isConditionalOrder: isConditionalOrder
            });
            alert("수량 입력 필드를 찾을 수 없습니다. 페이지를 새로고침해주세요.");
            return;
        }
        
        if (!targetQuantityInput.value || targetQuantityInput.value.trim() === "") {
            alert("수량을 입력해주세요.");
            return;
        }
        
        const quantity = parseInt(targetQuantityInput.value.replace(/,/g, ""));
        if (isNaN(quantity) || quantity <= 0) {
            alert("올바른 수량을 입력해주세요. (1 이상의 숫자)");
            return;
        }
        
        console.log("[주문 제출] 수량 검증 완료:", quantity);
        
        // 주문 데이터 준비 - URLSearchParams 사용 (application/x-www-form-urlencoded)
        const params = new URLSearchParams();
        params.append("stockId", stockId.toString());
        params.append("side", side);
        params.append("orderType", orderType);
        if (price !== null) {
            params.append("price", price.toString());
        }
        params.append("quantity", quantity.toString());
        
        // 조건 주문인 경우 감시 가격과 주문 유지 기간 추가
        if (isConditionalOrder && watchPrice !== null) {
            params.append("watchPrice", watchPrice.toString());
            if (orderDuration) {
                params.append("orderDuration", orderDuration);
            }
        }
        
        try {
            console.log("[주문 제출] API 요청 시작:", {
                stockId: stockId,
                side: side,
                orderType: orderType,
                price: price,
                quantity: quantity,
                watchPrice: watchPrice,
                orderDuration: orderDuration
            });
            console.log("[주문 제출] 요청 파라미터:", params.toString());
            
            const response = await fetch("/api/order", {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
                },
                body: params.toString()
            });
            
            // HTTP 상태 코드 확인
            if (!response.ok) {
                // HTTP 오류 (4xx, 5xx)
                let errorMessage = "주문 생성에 실패했습니다.";
                try {
                    const errorResult = await response.json();
                    errorMessage = errorResult.message || errorMessage;
                } catch (e) {
                    // JSON 파싱 실패 시 기본 메시지 사용
                    errorMessage = `서버 오류가 발생했습니다. (${response.status})`;
                }
                console.error("[주문 제출] HTTP 오류:", response.status, errorMessage);
                alert(errorMessage);
                return;
            }
            
            // 응답 JSON 파싱
            let result;
            try {
                result = await response.json();
            } catch (e) {
                console.error("[주문 제출] JSON 파싱 오류:", e);
                alert("서버 응답을 처리할 수 없습니다. 페이지를 새로고침해주세요.");
                return;
            }
            
            if (result.success) {
                console.log("[주문 제출] 주문 생성 성공:", result.orderId);
                alert("주문이 성공적으로 생성되었습니다.");
                // 페이지 새로고침하여 대기 주문 목록 업데이트
                window.location.reload();
            } else {
                console.error("[주문 제출] 주문 생성 실패:", result.message);
                alert(result.message || "주문 생성에 실패했습니다.");
            }
        } catch (error) {
            // 네트워크 오류 또는 기타 예외
            console.error("[주문 제출] 네트워크 오류:", error);
            if (error instanceof TypeError && error.message.includes("fetch")) {
                alert("네트워크 연결을 확인해주세요. 인터넷 연결이 끊어졌을 수 있습니다.");
            } else {
                alert("주문 생성 중 오류가 발생했습니다: " + error.message);
            }
        }
    });
}

/**
 * 시장가/지정가 모드 전환 핸들러 설정
 * @param {NodeList} priceModePills - 가격 모드 pill 버튼들
 * @param {HTMLElement} priceInput - 가격 입력 요소
 * @param {HTMLElement} orderTypeHidden - 주문 유형 hidden input
 * @param {HTMLElement} unitText - 단위 텍스트 요소
 * @param {HTMLElement} priceMinusBtn - 가격 감소 버튼
 * @param {HTMLElement} pricePlusBtn - 가격 증가 버튼
 * @param {HTMLElement} priceInputElement - 가격 입력 요소 (data-current-price 속성용)
 * @param {Function} calculateTotalAmountCallback - 총 금액 계산 콜백 함수
 */
export function setupPriceModePills(priceModePills, priceInput, orderTypeHidden, unitText, priceMinusBtn, pricePlusBtn, priceInputElement, calculateTotalAmountCallback) {
    if (!priceModePills.length || !priceInput) return;

    // 초기화: 지정가를 기본값으로 설정
    const limitPill = Array.from(priceModePills).find(p => p.textContent.trim() === "지정가");
    const marketPill = Array.from(priceModePills).find(p => p.textContent.trim() === "시장가");
    
    // 조건 주문인지 확인 (조건 주문 섹션 내부에 있는지 확인)
    const isConditionalOrder = priceModePills[0]?.closest('.order-card__conditional-order') !== null;
    
    // 현재 상태 확인 (active pill 또는 orderTypeHidden 값)
    let currentActivePill = Array.from(priceModePills).find(p => p.classList.contains("order-card__pill--active"));
    
    // 조건 주문이거나, active pill이 없거나, orderTypeHidden 값과 불일치하면 지정가로 초기화
    if (isConditionalOrder || !currentActivePill || (orderTypeHidden && orderTypeHidden.value !== "LIMIT" && orderTypeHidden.value !== "MARKET")) {
        // 지정가로 초기화
        if (limitPill) {
            priceModePills.forEach(p => {
                p.classList.remove("order-card__pill--active");
                p.setAttribute("aria-checked", "false");
                p.setAttribute("data-state", "unchecked");
            });
            limitPill.classList.add("order-card__pill--active");
            limitPill.setAttribute("aria-checked", "true");
            limitPill.setAttribute("data-state", "checked");
            
            // 지정가 모드 설정
            priceInput.readOnly = false;
            priceInput.placeholder = "가격을 입력하세요";
            priceInput.classList.remove("order-card__input--readonly");
            
            if (orderTypeHidden) orderTypeHidden.value = "LIMIT";
            if (unitText) unitText.style.display = "";
            if (priceMinusBtn) priceMinusBtn.disabled = false;
            if (pricePlusBtn) pricePlusBtn.disabled = false;
            
            // 현재가가 없으면 기본값 설정
            if (!priceInput.value && priceInputElement) {
                const currentPrice = priceInputElement.getAttribute("data-current-price");
                if (currentPrice) {
                    priceInput.value = formatNumber(parseInt(currentPrice));
                    if (calculateTotalAmountCallback) {
                        calculateTotalAmountCallback();
                    }
                }
            }
        }
    } else if (currentActivePill === marketPill) {
        // 시장가가 활성화되어 있으면 시장가 모드 설정
        // 시장가가 활성화되어 있으면 시장가 모드 설정
        priceInput.value = "";
        priceInput.readOnly = true;
        priceInput.placeholder = "시장가로 주문됩니다";
        priceInput.classList.add("order-card__input--readonly");
        
        if (orderTypeHidden) orderTypeHidden.value = "MARKET";
        if (unitText) unitText.style.display = "none";
        if (priceMinusBtn) priceMinusBtn.disabled = true;
        if (pricePlusBtn) pricePlusBtn.disabled = true;
        
        if (calculateTotalAmountCallback) {
            calculateTotalAmountCallback();
        }
    } else if (currentActivePill === limitPill) {
        // 지정가가 활성화되어 있으면 지정가 모드 설정
        priceInput.readOnly = false;
        priceInput.placeholder = "가격을 입력하세요";
        priceInput.classList.remove("order-card__input--readonly");
        
        if (orderTypeHidden) orderTypeHidden.value = "LIMIT";
        if (unitText) unitText.style.display = "";
        if (priceMinusBtn) priceMinusBtn.disabled = false;
        if (pricePlusBtn) pricePlusBtn.disabled = false;
        
        // 현재가가 없으면 기본값 설정
        if (!priceInput.value && priceInputElement) {
            const currentPrice = priceInputElement.getAttribute("data-current-price");
            if (currentPrice) {
                priceInput.value = formatNumber(parseInt(currentPrice));
                if (calculateTotalAmountCallback) {
                    calculateTotalAmountCallback();
                }
            }
        }
    }

    priceModePills.forEach(function (pill) {
        pill.addEventListener("click", function () {
            // pill 활성화 변경
            priceModePills.forEach(p => {
                p.classList.remove("order-card__pill--active");
                p.setAttribute("aria-checked", "false");
                p.setAttribute("data-state", "unchecked");
            });
            pill.classList.add("order-card__pill--active");
            pill.setAttribute("aria-checked", "true");
            pill.setAttribute("data-state", "checked");

            const mode = pill.textContent.trim();

            if (mode === "시장가") {
                // 시장가 모드
                priceInput.value = "";
                priceInput.readOnly = true;
                priceInput.placeholder = "시장가로 주문됩니다";
                priceInput.classList.add("order-card__input--readonly");

                if (orderTypeHidden) orderTypeHidden.value = "MARKET";

                if (unitText) unitText.style.display = "none";

                // +/- 버튼 비활성화
                if (priceMinusBtn) priceMinusBtn.disabled = true;
                if (pricePlusBtn) pricePlusBtn.disabled = true;
                
                // 시장가로 전환 시 총 금액 계산 (현재가 사용)
                if (calculateTotalAmountCallback) {
                    calculateTotalAmountCallback();
                }

            } else {
                // 지정가 모드
                priceInput.readOnly = false;
                priceInput.placeholder = "가격을 입력하세요";
                priceInput.classList.remove("order-card__input--readonly");

                if (orderTypeHidden) orderTypeHidden.value = "LIMIT";

                if (unitText) unitText.style.display = "";

                // +/- 버튼 활성화
                if (priceMinusBtn) priceMinusBtn.disabled = false;
                if (pricePlusBtn) pricePlusBtn.disabled = false;

                // 현재가가 없으면 기본값 설정
                if (!priceInput.value && priceInputElement) {
                    const currentPrice = priceInputElement.getAttribute("data-current-price");
                    if (currentPrice) {
                        priceInput.value = formatNumber(parseInt(currentPrice));
                        if (calculateTotalAmountCallback) {
                            calculateTotalAmountCallback();
                        }
                    }
                }
            }
        });
    });
}

/**
 * 감시 가격 입력 필드 및 버튼 이벤트 핸들러 설정
 * @param {HTMLElement} watchPriceInput - 감시 가격 입력 요소
 * @param {HTMLElement} watchPriceInfo - 감시 가격 정보 표시 요소
 * @param {HTMLElement} watchPriceMinusBtn - 감시 가격 감소 버튼
 * @param {HTMLElement} watchPricePlusBtn - 감시 가격 증가 버튼
 */
export function setupWatchPriceInput(watchPriceInput, watchPriceInfo, watchPriceMinusBtn, watchPricePlusBtn) {
    if (!watchPriceInput || !watchPriceInfo) return;

    // 초기값 설정 (현재가)
    const currentPrice = watchPriceInput.getAttribute("data-current-price");
    if (currentPrice && !watchPriceInput.value) {
        watchPriceInput.value = formatNumber(parseInt(currentPrice));
        updateWatchPriceInfo(watchPriceInput, watchPriceInfo);
    }

    // 입력 시 실시간 계산
    watchPriceInput.addEventListener("input", function() {
        let value = this.value.replace(/[^0-9]/g, "");
        if (value) {
            this.value = formatNumber(parseInt(value));
        }
        updateWatchPriceInfo(watchPriceInput, watchPriceInfo);
    });

    // 감시 가격 +/- 버튼 이벤트
    if (watchPriceMinusBtn && watchPricePlusBtn) {
        watchPriceMinusBtn.addEventListener("click", function() {
            let currentPrice = parseFloat(watchPriceInput.value.replace(/,/g, "")) || 0;
            currentPrice = Math.max(0, currentPrice - 5);
            watchPriceInput.value = formatNumber(currentPrice);
            updateWatchPriceInfo(watchPriceInput, watchPriceInfo);
        });

        watchPricePlusBtn.addEventListener("click", function() {
            let currentPrice = parseFloat(watchPriceInput.value.replace(/,/g, "")) || 0;
            currentPrice = currentPrice + 5;
            watchPriceInput.value = formatNumber(currentPrice);
            updateWatchPriceInfo(watchPriceInput, watchPriceInfo);
        });
    }

    // 초기 감시 가격 정보 업데이트
    updateWatchPriceInfo(watchPriceInput, watchPriceInfo);
}

