import {
    formatNumber,
    calculateTotalAmount as calcTotalAmount,
    initializeAvailableAmount as initAvailableAmount,
    initializeCurrentPrice as initCurrentPrice,
    initializeQuantity as initQuantity,
    updateWatchPriceInfo
} from '../common/stock/stock_order_utils.js';

import {
    setupPriceStepButtons,
    setupQuantityStepButtons,
    setupPriceInput,
    setupQuantityInput,
    setupSubmitButton,
    setupPriceModePills,
    setupWatchPriceInput
} from '../common/stock/stock_order_card.js';
import {fetchStockChartData} from "../common/api/fetch_utils.js";

document.addEventListener("DOMContentLoaded", function handleStockPageInit() {


    /* 주문하기 탭 전환 */

    const tradeTabs = document.querySelectorAll(".order-card__trade-tab");

    // 구매가격 / 판매가격 라벨
    const priceLabel = Array.from(
        document.querySelectorAll(".order-card__label")
    ).find(function findPriceLabel(label) {
        const text = label.textContent.trim();
        return text.includes("구매가격") || text.includes("판매가격");
    });

    // 구매가능 금액 / 판매가능 금액 라벨
    const amountLabel = Array.from(
        document.querySelectorAll(".order-card__summary-label")
    ).find(function findAmountLabel(label) {
        const text = label.textContent.trim();
        return text.includes("구매가능 금액") || text.includes("판매가능 금액");
    });

    const submitButton = document.querySelector(".order-card__submit-btn");
    const orderCard = document.querySelector(".order-card");

    if (tradeTabs.length && priceLabel && amountLabel && submitButton) {
        tradeTabs.forEach(function attachTradeTab(tab) {
            tab.addEventListener("click", function onTradeTabClick() {

                tradeTabs.forEach(function removeActive(t) {
                    t.classList.remove("order-card__trade-tab--active");
                    t.setAttribute("aria-checked", "false");
                    t.setAttribute("data-state", "unchecked");
                });

                // order-card 섹션의 상태 클래스 제거
                if (orderCard) {
                    orderCard.classList.remove("order-card--buy");
                    orderCard.classList.remove("order-card--sell");
                    orderCard.classList.remove("order-card--wait");
                }

                const text = tab.textContent.trim();

                if (text.includes("판매")) {
                    // 판매 탭
                    tab.classList.add("order-card__trade-tab--active");
                    tab.setAttribute("aria-checked", "true");
                    tab.setAttribute("data-state", "checked");
                    if (orderCard) orderCard.classList.add("order-card--sell");
                    priceLabel.textContent = "판매가격";
                    amountLabel.textContent = "판매가능 금액";
                    // 로그인 상태 확인 (버튼이면 로그인, 링크면 비로그인)
                    if (submitButton.tagName === "BUTTON") {
                        submitButton.textContent = "판매하기";
                    } else {
                        submitButton.textContent = "로그인하고 판매하기";
                    }
                } else if (text.includes("구매")) {
                    // 구매 탭
                    tab.classList.add("order-card__trade-tab--active");
                    tab.setAttribute("aria-checked", "true");
                    tab.setAttribute("data-state", "checked");
                    if (orderCard) orderCard.classList.add("order-card--buy");
                    priceLabel.textContent = "구매가격";
                    amountLabel.textContent = "구매가능 금액";
                    // 로그인 상태 확인 (버튼이면 로그인, 링크면 비로그인)
                    if (submitButton.tagName === "BUTTON") {
                        submitButton.textContent = "구매하기";
                    } else {
                        submitButton.textContent = "로그인하고 구매하기";
                    }
                }
                else if (text.includes("대기")) {
                    // 대기 탭
                    tab.classList.add("order-card__trade-tab--active");
                    tab.setAttribute("aria-checked", "true");
                    tab.setAttribute("data-state", "checked");
                    if (orderCard) orderCard.classList.add("order-card--wait");
                    priceLabel.textContent = "대기가격";
                    amountLabel.textContent = "대기가능 금액";
                    submitButton.textContent = "대기하기";
                }
            });
        });
    }

    // 주문 관련 요소들 (먼저 정의)
    const priceInputElement = document.getElementById("order-price-input");
    const quantityInputElement = document.getElementById("order-quantity-input");
    const priceMinusBtn = document.getElementById("order-price-minus");
    const pricePlusBtn = document.getElementById("order-price-plus");
    const quantityMinusBtn = document.getElementById("order-quantity-minus");
    const quantityPlusBtn = document.getElementById("order-quantity-plus");
    const totalAmountElement = document.getElementById("order-total-amount");
    const availableAmountElement = document.getElementById("order-available-amount");
    const submitBtn = document.getElementById("order-submit-btn");

    // 시장가 / 지정가 모드 전환

    const priceModePills = document.querySelectorAll(
        ".order-card__field--split .order-card__pill"
    );
    // priceInputElement가 있으면 그것을 사용, 없으면 기존 방식으로 선택
    const priceInput = priceInputElement || document.querySelector(
        ".order-card__field--price .order-card__input"
    );
    const orderTypeHidden = document.querySelector(
        ".order-card__order-type-hidden"
    );

    const unitText = document.querySelector(".order-card__field--price .unit-text");

    // 총 주문 금액 계산 함수 (로컬 래퍼)
    function calculateTotalAmount() {
        // 요소가 없으면 다시 찾기
        const totalAmount = totalAmountElement || document.getElementById("order-total-amount");
        const quantityInput = quantityInputElement || document.getElementById("order-quantity-input") || document.querySelector(".order-card__field--qty .order-card__input");
        
        if (!totalAmount || !quantityInput) return;
        
        // 현재 활성화된 탭 확인
        const activeTab = document.querySelector(".order-card__trade-tab[aria-checked='true']");
        const isBuy = activeTab && activeTab.textContent.trim().includes("구매");
        const isSell = activeTab && activeTab.textContent.trim().includes("판매");
        
        // 주문 유형 확인 (일반/조건)
        const orderTypeDropdown = document.querySelector("[data-order-type-dropdown]");
        const orderTypeInput = orderTypeDropdown ? orderTypeDropdown.querySelector("[data-dropdown-input]") : null;
        const isConditional = orderTypeInput && orderTypeInput.value === "conditional";
        
        // 주문 모드 확인 (지정가/시장가)
        const orderTypeHidden = document.querySelector(".order-card__order-type-hidden");
        const isMarket = orderTypeHidden && orderTypeHidden.value === "MARKET";
        
        // 가격 및 수량 가져오기
        let price = 0;
        const quantity = parseInt(quantityInput.value.replace(/,/g, "") || "0");
        
        if (isConditional) {
            // 조건 주문일 경우
            let targetPriceInput = null;
            if (isBuy) {
                targetPriceInput = document.getElementById("conditional-order-price-input");
            } else if (isSell) {
                targetPriceInput = document.getElementById("conditional-sell-price-input");
            }
            
            if (targetPriceInput) {
                price = parseFloat(targetPriceInput.value.replace(/,/g, "")) || 0;
                // 조건 주문은 항상 지정가이므로 가격이 있어야 함
            }
        } else {
            // 일반 주문일 경우
            // priceInputElement가 없으면 다시 찾기
            const priceInput = priceInputElement || document.getElementById("order-price-input") || document.querySelector(".order-card__field--price .order-card__input");
            
            if (isMarket) {
                // 시장가: 현재가 사용 (여러 소스에서 찾기)
                if (priceInput) {
                    let currentPriceStr = priceInput.getAttribute("data-current-price");
                    // data-current-price가 없으면 다른 곳에서 찾기
                    if (!currentPriceStr || currentPriceStr === "0") {
                        // watch-price-info 요소에서 찾기
                        const watchPriceInfo = document.querySelector('.order-card__watch-price-info-wrapper [id$="-current"]') || 
                                               document.querySelector('.order-card__watch-price-info[id$="-current"]');
                        if (watchPriceInfo) {
                            currentPriceStr = watchPriceInfo.getAttribute("data-current-price");
                        }
                        // 여전히 없으면 stock-header에서 찾기
                        if (!currentPriceStr || currentPriceStr === "0") {
                            const stockHeaderPrice = document.querySelector('.stock-info__header-price, .stock-header__price');
                            if (stockHeaderPrice) {
                                const priceText = stockHeaderPrice.textContent.replace(/[^0-9]/g, "");
                                if (priceText) {
                                    currentPriceStr = priceText;
                                }
                            }
                        }
                    }
                    if (currentPriceStr) {
                        price = parseFloat(currentPriceStr) || 0;
                    }
                }
            } else {
                // 지정가: 입력된 가격 사용
                if (priceInput) {
                    price = parseFloat(priceInput.value.replace(/,/g, "")) || 0;
                    // 지정가인데 가격이 없으면 현재가 사용
                    if (price === 0) {
                        const currentPriceStr = priceInput.getAttribute("data-current-price");
                        if (currentPriceStr) {
                            price = parseFloat(currentPriceStr) || 0;
                        }
                    }
                }
            }
        }
        
        // 총 금액 계산 및 표시
        const total = price * quantity;
        totalAmount.textContent = formatNumber(total) + "원";
    }
    
    // 조건 주문 총 금액 계산 함수 (구매용)
    function calculateConditionalBuyTotalAmount() {
        const conditionalBuyQuantityInput = document.getElementById("conditional-order-quantity-input");
        const conditionalBuyPriceInput = document.getElementById("conditional-order-price-input");
        const conditionalBuyTotalAmount = document.querySelector(".order-card__conditional-order .order-card__summary-value:last-child");
        const conditionalBuyOrderTypeHidden = document.querySelector(".order-card__conditional-order .order-card__order-type-hidden");
        
        if (!conditionalBuyQuantityInput || !conditionalBuyPriceInput || !conditionalBuyTotalAmount) return;
        
        const quantity = parseInt(conditionalBuyQuantityInput.value.replace(/,/g, "") || "0");
        let price = 0;
        
        // 주문 모드 확인 (지정가/시장가)
        const isMarket = conditionalBuyOrderTypeHidden && conditionalBuyOrderTypeHidden.value === "MARKET";
        
        if (isMarket) {
            // 시장가: 현재가 사용 (여러 소스에서 찾기)
            let currentPriceStr = conditionalBuyPriceInput.getAttribute("data-current-price");
            if (!currentPriceStr || currentPriceStr === "0") {
                // watch-price-info 요소에서 찾기
                const watchPriceInfo = document.querySelector('.order-card__watch-price-info-wrapper [id$="-current"]') || 
                                       document.querySelector('.order-card__watch-price-info[id$="-current"]');
                if (watchPriceInfo) {
                    currentPriceStr = watchPriceInfo.getAttribute("data-current-price");
                }
                // 여전히 없으면 stock-header에서 찾기
                if (!currentPriceStr || currentPriceStr === "0") {
                    const stockHeaderPrice = document.querySelector('.stock-header__price, .stock-info__header-price');
                    if (stockHeaderPrice) {
                        const priceText = stockHeaderPrice.textContent.replace(/[^0-9]/g, "");
                        if (priceText) {
                            currentPriceStr = priceText;
                        }
                    }
                }
            }
            if (currentPriceStr) {
                price = parseFloat(currentPriceStr) || 0;
            }
        } else {
            // 지정가: 입력된 가격 사용
            price = parseFloat(conditionalBuyPriceInput.value.replace(/,/g, "")) || 0;
        }
        
        const total = price * quantity;
        conditionalBuyTotalAmount.textContent = formatNumber(total) + "원";
    }
    
    // 조건 주문 총 금액 계산 함수 (판매용)
    function calculateConditionalSellTotalAmount() {
        const conditionalSellQuantityInput = document.getElementById("conditional-sell-quantity-input");
        const conditionalSellPriceInput = document.getElementById("conditional-sell-price-input");
        const conditionalSellTotalAmount = document.querySelector(".order-card__conditional-order--sell .order-card__summary-value:last-child");
        const conditionalSellOrderTypeHidden = document.querySelector(".order-card__conditional-order--sell .order-card__order-type-hidden");
        
        if (!conditionalSellQuantityInput || !conditionalSellPriceInput || !conditionalSellTotalAmount) return;
        
        const quantity = parseInt(conditionalSellQuantityInput.value.replace(/,/g, "") || "0");
        let price = 0;
        
        // 주문 모드 확인 (지정가/시장가)
        const isMarket = conditionalSellOrderTypeHidden && conditionalSellOrderTypeHidden.value === "MARKET";
        
        if (isMarket) {
            // 시장가: 현재가 사용 (여러 소스에서 찾기)
            let currentPriceStr = conditionalSellPriceInput.getAttribute("data-current-price");
            if (!currentPriceStr || currentPriceStr === "0") {
                // watch-price-info 요소에서 찾기
                const watchPriceInfo = document.querySelector('.order-card__watch-price-info-wrapper [id$="-current"]') || 
                                       document.querySelector('.order-card__watch-price-info[id$="-current"]');
                if (watchPriceInfo) {
                    currentPriceStr = watchPriceInfo.getAttribute("data-current-price");
                }
                // 여전히 없으면 stock-header에서 찾기
                if (!currentPriceStr || currentPriceStr === "0") {
                    const stockHeaderPrice = document.querySelector('.stock-header__price, .stock-info__header-price');
                    if (stockHeaderPrice) {
                        const priceText = stockHeaderPrice.textContent.replace(/[^0-9]/g, "");
                        if (priceText) {
                            currentPriceStr = priceText;
                        }
                    }
                }
            }
            if (currentPriceStr) {
                price = parseFloat(currentPriceStr) || 0;
            }
        } else {
            // 지정가: 입력된 가격 사용
            price = parseFloat(conditionalSellPriceInput.value.replace(/,/g, "")) || 0;
        }
        
        const total = price * quantity;
        conditionalSellTotalAmount.textContent = formatNumber(total) + "원";
    }

    // 최대 수량 계산 함수
    function calculateMaxQuantity() {
        const orderCard = document.querySelector(".order-card");
        if (!orderCard) return null;
        
        const availableCash = parseInt(orderCard.getAttribute("data-available-cash") || "0");
        const holdingQuantity = parseInt(orderCard.getAttribute("data-holding-quantity") || "0");
        
        // 현재 활성화된 탭 확인
        const activeTab = document.querySelector(".order-card__trade-tab[aria-checked='true']");
        const isBuy = activeTab && activeTab.textContent.trim().includes("구매");
        
        // 주문 유형 확인 (일반/조건)
        const orderTypeDropdown = document.querySelector("[data-order-type-dropdown]");
        const orderTypeInput = orderTypeDropdown ? orderTypeDropdown.querySelector("[data-dropdown-input]") : null;
        const isConditional = orderTypeInput && orderTypeInput.value === "conditional";
        
        // 주문 모드 확인 (지정가/시장가)
        const orderTypeHidden = document.querySelector(".order-card__order-type-hidden");
        const isMarket = orderTypeHidden && orderTypeHidden.value === "MARKET";
        
        if (isBuy) {
            // 구매: 예치금 / 주가
            let price = 0;
            if (isConditional) {
                // 조건 주문일 경우 조건 주문 가격 필드 확인
                const conditionalPriceInput = document.getElementById("conditional-order-price-input");
                if (conditionalPriceInput) {
                    price = parseFloat(conditionalPriceInput.value.replace(/,/g, "")) || 0;
                }
            } else {
                // 일반 주문일 경우
                if (isMarket) {
                    // 시장가 모드: 현재가 사용
                    const currentPrice = priceInputElement ? priceInputElement.getAttribute("data-current-price") : null;
                    if (currentPrice) {
                        price = parseFloat(currentPrice) || 0;
                    }
                } else {
                    // 지정가 모드: 입력된 가격 사용
                    price = parseFloat(priceInputElement.value.replace(/,/g, "")) || 0;
                }
            }
            
            if (price > 0) {
                return Math.floor(availableCash / price);
            }
            // 가격이 없으면 예치금으로 최대 수량 계산 (시장가 모드에서 현재가를 가져올 수 없는 경우)
            // 이 경우는 매우 큰 값을 반환하여 사용자가 수량을 입력할 수 있도록 함
            return availableCash > 0 ? 999999 : 0;
        } else {
            // 판매: 보유 수량 - 대기 중인 판매 주문 수량
            const pendingSellQuantity = parseInt(orderCard.getAttribute("data-pending-sell-quantity") || "0");
            const availableQuantity = holdingQuantity - pendingSellQuantity;
            return Math.max(0, availableQuantity); // 음수가 되지 않도록
        }
    }
    
    // placeholder 업데이트 함수
    function updateQuantityPlaceholders() {
        const maxQty = calculateMaxQuantity();
        if (maxQty === null) return;
        
        const formattedMaxQty = maxQty.toLocaleString('ko-KR');
        
        // 일반 주문 수량 필드
        if (quantityInputElement) {
            quantityInputElement.placeholder = `최대 ${formattedMaxQty}주 가능`;
        }
        
        // 조건 주문 수량 필드 (구매)
        const conditionalBuyQuantityInput = document.getElementById("conditional-order-quantity-input");
        if (conditionalBuyQuantityInput) {
            conditionalBuyQuantityInput.placeholder = `최대 ${formattedMaxQty}주 가능`;
        }
        
        // 조건 주문 수량 필드 (판매)
        const conditionalSellQuantityInput = document.getElementById("conditional-sell-quantity-input");
        if (conditionalSellQuantityInput) {
            conditionalSellQuantityInput.placeholder = `최대 ${formattedMaxQty}주 가능`;
        }
    }
    
    // 가격/수량 증감 버튼 및 입력 필드 이벤트 설정
    setupPriceStepButtons(priceMinusBtn, pricePlusBtn, priceInputElement, quantityInputElement, totalAmountElement, calculateTotalAmount);
    
    // 수량 관련 이벤트 설정 (최대값을 동적으로 계산)
    // 일반 주문 수량 필드 - 최대값을 동적으로 계산하는 래퍼 함수 사용, 로컬 calculateTotalAmount 함수 사용
    setupQuantityStepButtons(quantityMinusBtn, quantityPlusBtn, quantityInputElement, priceInputElement, totalAmountElement, () => calculateMaxQuantity(), calculateTotalAmount);
    setupQuantityInput(quantityInputElement, priceInputElement, totalAmountElement, () => calculateMaxQuantity(), calculateTotalAmount);
    
    // 조건 주문 수량 필드 (구매)
    const conditionalBuyQuantityInput = document.getElementById("conditional-order-quantity-input");
    const conditionalBuyQuantityMinus = document.getElementById("conditional-order-quantity-minus");
    const conditionalBuyQuantityPlus = document.getElementById("conditional-order-quantity-plus");
    const conditionalBuyPriceInput = document.getElementById("conditional-order-price-input");
    const conditionalBuyPriceMinus = document.getElementById("conditional-order-price-minus");
    const conditionalBuyPricePlus = document.getElementById("conditional-order-price-plus");
    const conditionalBuyTotalAmount = document.querySelector(".order-card__conditional-order .order-card__summary-value:last-child");
    
    // 조건 주문 가격 모드 pill 설정 (구매)
    const conditionalBuyPriceModePills = document.querySelectorAll(".order-card__conditional-order .order-card__field--split .order-card__pill");
    const conditionalBuyOrderTypeHidden = document.querySelector(".order-card__conditional-order .order-card__order-type-hidden") || 
                                         (() => {
                                             // 조건 주문용 hidden input 생성 (없으면)
                                             const hidden = document.createElement("input");
                                             hidden.type = "hidden";
                                             hidden.className = "order-card__order-type-hidden";
                                             hidden.value = "LIMIT";
                                             const conditionalOrderSection = document.querySelector(".order-card__conditional-order");
                                             if (conditionalOrderSection) {
                                                 conditionalOrderSection.insertBefore(hidden, conditionalOrderSection.firstChild);
                                             }
                                             return hidden;
                                         })();
    // 가격 입력 필드와 같은 row에 있는 unit-text만 선택 (감시가격의 unit-text 제외)
    const conditionalBuyUnitText = conditionalBuyPriceInput ? conditionalBuyPriceInput.closest(".order-card__row")?.querySelector(".unit-text") : null;
    
    if (conditionalBuyPriceModePills.length > 0 && conditionalBuyPriceInput) {
        setupPriceModePills(conditionalBuyPriceModePills, conditionalBuyPriceInput, conditionalBuyOrderTypeHidden, conditionalBuyUnitText, conditionalBuyPriceMinus, conditionalBuyPricePlus, conditionalBuyPriceInput, calculateConditionalBuyTotalAmount);
    }
    
    // 조건 주문 가격 증감 버튼 설정 (구매)
    if (conditionalBuyPriceMinus && conditionalBuyPricePlus && conditionalBuyPriceInput) {
        setupPriceStepButtons(conditionalBuyPriceMinus, conditionalBuyPricePlus, conditionalBuyPriceInput, conditionalBuyQuantityInput, conditionalBuyTotalAmount, calculateConditionalBuyTotalAmount);
    }
    
    if (conditionalBuyQuantityInput && conditionalBuyQuantityMinus && conditionalBuyQuantityPlus) {
        setupQuantityStepButtons(conditionalBuyQuantityMinus, conditionalBuyQuantityPlus, conditionalBuyQuantityInput, conditionalBuyPriceInput, conditionalBuyTotalAmount, () => calculateMaxQuantity(), calculateConditionalBuyTotalAmount);
        setupQuantityInput(conditionalBuyQuantityInput, conditionalBuyPriceInput, conditionalBuyTotalAmount, () => calculateMaxQuantity(), calculateConditionalBuyTotalAmount);
    }
    
    // 조건 주문 수량 필드 (판매)
    const conditionalSellQuantityInput = document.getElementById("conditional-sell-quantity-input");
    const conditionalSellQuantityMinus = document.getElementById("conditional-sell-quantity-minus");
    const conditionalSellQuantityPlus = document.getElementById("conditional-sell-quantity-plus");
    const conditionalSellPriceInput = document.getElementById("conditional-sell-price-input");
    const conditionalSellPriceMinus = document.getElementById("conditional-sell-price-minus");
    const conditionalSellPricePlus = document.getElementById("conditional-sell-price-plus");
    const conditionalSellTotalAmount = document.querySelector(".order-card__conditional-order--sell .order-card__summary-value:last-child");
    
    // 조건 주문 가격 모드 pill 설정 (판매)
    const conditionalSellPriceModePills = document.querySelectorAll(".order-card__conditional-order--sell .order-card__field--split .order-card__pill");
    const conditionalSellOrderTypeHidden = document.querySelector(".order-card__conditional-order--sell .order-card__order-type-hidden") || 
                                          (() => {
                                              // 조건 주문 판매용 hidden input 생성 (없으면)
                                              const hidden = document.createElement("input");
                                              hidden.type = "hidden";
                                              hidden.className = "order-card__order-type-hidden";
                                              hidden.value = "LIMIT";
                                              const conditionalSellSection = document.querySelector(".order-card__conditional-order--sell");
                                              if (conditionalSellSection) {
                                                  conditionalSellSection.insertBefore(hidden, conditionalSellSection.firstChild);
                                              }
                                              return hidden;
                                          })();
    // 가격 입력 필드와 같은 row에 있는 unit-text만 선택 (감시가격의 unit-text 제외)
    const conditionalSellUnitText = conditionalSellPriceInput ? conditionalSellPriceInput.closest(".order-card__row")?.querySelector(".unit-text") : null;
    
    if (conditionalSellPriceModePills.length > 0 && conditionalSellPriceInput) {
        setupPriceModePills(conditionalSellPriceModePills, conditionalSellPriceInput, conditionalSellOrderTypeHidden, conditionalSellUnitText, conditionalSellPriceMinus, conditionalSellPricePlus, conditionalSellPriceInput, calculateConditionalSellTotalAmount);
    }
    
    // 조건 주문 가격 증감 버튼 설정 (판매)
    if (conditionalSellPriceMinus && conditionalSellPricePlus && conditionalSellPriceInput) {
        setupPriceStepButtons(conditionalSellPriceMinus, conditionalSellPricePlus, conditionalSellPriceInput, conditionalSellQuantityInput, conditionalSellTotalAmount, calculateConditionalSellTotalAmount);
    }
    
    if (conditionalSellQuantityInput && conditionalSellQuantityMinus && conditionalSellQuantityPlus) {
        setupQuantityStepButtons(conditionalSellQuantityMinus, conditionalSellQuantityPlus, conditionalSellQuantityInput, conditionalSellPriceInput, conditionalSellTotalAmount, () => calculateMaxQuantity(), calculateConditionalSellTotalAmount);
        setupQuantityInput(conditionalSellQuantityInput, conditionalSellPriceInput, conditionalSellTotalAmount, () => calculateMaxQuantity(), calculateConditionalSellTotalAmount);
    }
    
    // 가격 변경 시 최대값 재계산 및 placeholder 업데이트, 총 금액 계산
    const updateQuantityOnPriceChange = () => {
        // 가격이 변경되면 현재 수량이 최대값을 초과하는지 확인하고 조정
        const maxQty = calculateMaxQuantity();
        if (maxQty !== null && quantityInputElement.value) {
            const currentQty = parseInt(quantityInputElement.value.replace(/,/g, "")) || 0;
            if (currentQty > maxQty) {
                quantityInputElement.value = maxQty.toString();
            }
        }
        // 총 금액 계산 (항상 수행)
        calculateTotalAmount();
        // placeholder 업데이트
        updateQuantityPlaceholders();
    };
    
    priceInputElement.addEventListener("input", updateQuantityOnPriceChange);
    priceInputElement.addEventListener("change", updateQuantityOnPriceChange);
    
    if (conditionalBuyPriceInput) {
        // 가격 입력 필드 이벤트 설정 (포맷팅 및 총 금액 계산)
        setupPriceInput(conditionalBuyPriceInput, conditionalBuyQuantityInput, conditionalBuyTotalAmount, calculateConditionalBuyTotalAmount);
    }
    if (conditionalSellPriceInput) {
        // 가격 입력 필드 이벤트 설정 (포맷팅 및 총 금액 계산)
        setupPriceInput(conditionalSellPriceInput, conditionalSellQuantityInput, conditionalSellTotalAmount, calculateConditionalSellTotalAmount);
    }
    
    // 탭 전환 시 수량 필드 최대값 재확인 및 placeholder 업데이트, 총 금액 계산
    tradeTabs.forEach(tab => {
        tab.addEventListener("click", () => {
            setTimeout(() => {
                // 주문 유형 확인 (일반/조건)
                const orderTypeDropdown = document.querySelector("[data-order-type-dropdown]");
                const orderTypeInput = orderTypeDropdown ? orderTypeDropdown.querySelector("[data-dropdown-input]") : null;
                const isConditional = orderTypeInput && orderTypeInput.value === "conditional";
                
                // 현재 활성화된 탭 확인
                const activeTab = document.querySelector(".order-card__trade-tab[aria-checked='true']");
                const isBuy = activeTab && activeTab.textContent.trim().includes("구매");
                const isSell = activeTab && activeTab.textContent.trim().includes("판매");
                
                if (isConditional) {
                    // 조건 주문일 경우
                    if (isBuy) {
                        const maxQty = calculateMaxQuantity();
                        const conditionalBuyQuantityInput = document.getElementById("conditional-order-quantity-input");
                        if (maxQty !== null && conditionalBuyQuantityInput && conditionalBuyQuantityInput.value) {
                            const currentQty = parseInt(conditionalBuyQuantityInput.value.replace(/,/g, "")) || 0;
                            if (currentQty > maxQty) {
                                conditionalBuyQuantityInput.value = maxQty.toString();
                            }
                        }
                        calculateConditionalBuyTotalAmount();
                    } else if (isSell) {
                        const maxQty = calculateMaxQuantity();
                        const conditionalSellQuantityInput = document.getElementById("conditional-sell-quantity-input");
                        if (maxQty !== null && conditionalSellQuantityInput && conditionalSellQuantityInput.value) {
                            const currentQty = parseInt(conditionalSellQuantityInput.value.replace(/,/g, "")) || 0;
                            if (currentQty > maxQty) {
                                conditionalSellQuantityInput.value = maxQty.toString();
                            }
                        }
                        calculateConditionalSellTotalAmount();
                    }
                } else {
                    // 일반 주문일 경우
                    const maxQty = calculateMaxQuantity();
                    if (maxQty !== null && quantityInputElement.value) {
                        const currentQty = parseInt(quantityInputElement.value.replace(/,/g, "")) || 0;
                        if (currentQty > maxQty) {
                            quantityInputElement.value = maxQty.toString();
                        }
                    }
                    calculateTotalAmount();
                }
                // placeholder 업데이트
                updateQuantityPlaceholders();
            }, 100);
        });
    });
    
    // 초기화
    initAvailableAmount(availableAmountElement);
    initCurrentPrice(priceInputElement, orderTypeHidden);
    initQuantity(quantityInputElement);
    
    // 현재가 설정 후 총 금액 계산 및 placeholder 업데이트
    setTimeout(function() {
        // 주문 유형 확인 (일반/조건)
        const orderTypeDropdown = document.querySelector("[data-order-type-dropdown]");
        const orderTypeInput = orderTypeDropdown ? orderTypeDropdown.querySelector("[data-dropdown-input]") : null;
        const isConditional = orderTypeInput && orderTypeInput.value === "conditional";
        
        // 현재 활성화된 탭 확인
        const activeTab = document.querySelector(".order-card__trade-tab[aria-checked='true']");
        const isBuy = activeTab && activeTab.textContent.trim().includes("구매");
        const isSell = activeTab && activeTab.textContent.trim().includes("판매");
        
        if (isConditional) {
            // 조건 주문일 경우
            if (isBuy) {
                calculateConditionalBuyTotalAmount();
            } else if (isSell) {
                calculateConditionalSellTotalAmount();
            }
        } else {
            // 일반 주문일 경우
            calculateTotalAmount();
        }
        // placeholder 업데이트
        updateQuantityPlaceholders();
    }, 200);

    // 시장가/지정가 모드 전환 설정
    setupPriceModePills(priceModePills, priceInput, orderTypeHidden, unitText, priceMinusBtn, pricePlusBtn, priceInputElement, calculateTotalAmount);

    /* 커스텀 드롭다운 (주문유형 등) */

    const dropdowns = document.querySelectorAll("[data-dropdown]");

    function closeAllDropdowns(except) {
        dropdowns.forEach(function (dropdown) {
            if (dropdown !== except) {
                dropdown.classList.remove("is-open");
                dropdown.setAttribute("aria-expanded", "false");
            }
        });
    }

    dropdowns.forEach(function (dropdown) {
        const toggle = dropdown.querySelector("[data-dropdown-toggle]");
        const list = dropdown.querySelector("[data-dropdown-list]");
        const label = dropdown.querySelector("[data-dropdown-label]");
        const hiddenInput = dropdown.querySelector("[data-dropdown-input]");
        const options = dropdown.querySelectorAll(".order-card__dropdown-option");

        if (!toggle || !list || !label || !hiddenInput || !options.length) return;

        // 초기 aria-expanded 설정
        dropdown.setAttribute("aria-expanded", "false");

        function setActiveOption(option) {
            options.forEach(function (opt) {
                opt.setAttribute("aria-selected", opt === option ? "true" : "false");
            });
            label.textContent = option.querySelector(".order-card__dropdown-option-label")?.textContent || option.textContent;
            hiddenInput.value = option.getAttribute("data-value") || "";
        }

        // 초기 활성 옵션
        const initial = dropdown.querySelector(".order-card__dropdown-option[aria-selected=\"true\"]") || options[0];
        setActiveOption(initial);

        toggle.addEventListener("click", function (e) {
            e.stopPropagation();
            const isOpen = dropdown.classList.toggle("is-open");
            dropdown.setAttribute("aria-expanded", isOpen ? "true" : "false");
            if (isOpen) {
                closeAllDropdowns(dropdown);
                // 포커스를 첫 옵션으로
                options[0].focus({ preventScroll: true });
            }
        });

        options.forEach(function (option) {
            option.addEventListener("click", function (e) {
                e.stopPropagation();
                setActiveOption(option);
                dropdown.classList.remove("is-open");
                dropdown.setAttribute("aria-expanded", "false");
                toggle.focus({ preventScroll: true });
                
                // 주문 유형 드롭다운 변경 시 조건 주문 필드 초기화
                if (dropdown.hasAttribute("data-order-type-dropdown")) {
                    const orderType = option.getAttribute("data-value");
                    // 현재 활성화된 탭 확인
                    const activeTab = document.querySelector(".order-card__trade-tab[aria-checked='true']");
                    let activeTabType = "buy"; // 기본값
                    if (activeTab) {
                        const tabText = activeTab.textContent.trim();
                        if (tabText.includes("판매")) {
                            activeTabType = "sell";
                        } else if (tabText.includes("구매")) {
                            activeTabType = "buy";
                        } else {
                            activeTabType = "wait";
                        }
                    }
                    
                    if (orderType === "conditional") {
                        // 조건 주문 필드가 표시될 때 구매/판매 가격 초기화 및 총 금액 계산
                        setTimeout(() => {
                            initializeConditionalOrderPriceFields();
                            // 조건 주문 총 금액 계산
                            if (activeTabType === "buy") {
                                calculateConditionalBuyTotalAmount();
                            } else if (activeTabType === "sell") {
                                calculateConditionalSellTotalAmount();
                            }
                        }, 0);
                    } else {
                        // 일반 주문으로 변경 시 placeholder 업데이트 및 총 금액 계산
                        setTimeout(() => {
                            updateQuantityPlaceholders();
                            calculateTotalAmount();
                        }, 100);
                    }
                }
            });

            option.addEventListener("keydown", function (e) {
                const currentIndex = Array.from(options).indexOf(option);
                if (e.key === "ArrowDown") {
                    e.preventDefault();
                    const next = options[Math.min(options.length - 1, currentIndex + 1)];
                    next?.focus({ preventScroll: true });
                } else if (e.key === "ArrowUp") {
                    e.preventDefault();
                    const prev = options[Math.max(0, currentIndex - 1)];
                    prev?.focus({ preventScroll: true });
                } else if (e.key === "Enter" || e.key === " ") {
                    e.preventDefault();
                    option.click();
                } else if (e.key === "Escape") {
                    dropdown.classList.remove("is-open");
                    dropdown.setAttribute("aria-expanded", "false");
                    toggle.focus({ preventScroll: true });
                }
            });
        });
    });

    document.addEventListener("click", function () {
        closeAllDropdowns(null);
    });

    document.addEventListener("keydown", function (e) {
        if (e.key === "Escape") {
            closeAllDropdowns(null);
        }
    });

    // 조건 주문 - 감시 가격 정보 업데이트
    const watchPriceInput = document.getElementById("watch-price-input");
    // wrapper 요소를 찾거나, 없으면 기존 요소를 찾음
    const watchPriceInfoWrapper = document.querySelector(".order-card__watch-price-info-wrapper") || 
                                   document.getElementById("watch-price-info");
    const watchPriceInfo = watchPriceInfoWrapper || document.getElementById("watch-price-info-current") || 
                           document.getElementById("watch-price-info");
    const watchPriceMinusBtn = document.getElementById("watch-price-minus");
    const watchPricePlusBtn = document.getElementById("watch-price-plus");

    // 감시 가격 입력 필드 및 버튼 이벤트 설정 (구매 조건 주문용)
    setupWatchPriceInput(watchPriceInput, watchPriceInfoWrapper || watchPriceInfo, watchPriceMinusBtn, watchPricePlusBtn);
    
    // 판매 조건 주문 - 감시 가격 정보 업데이트
    const conditionalSellWatchPriceInput = document.getElementById("conditional-sell-watch-price-input");
    const conditionalSellWatchPriceInfoWrapper = document.querySelector(".order-card__conditional-order--sell .order-card__watch-price-info-wrapper");
    const conditionalSellWatchPriceInfo = conditionalSellWatchPriceInfoWrapper || 
                                         document.getElementById("conditional-sell-watch-price-info-current") || 
                                         document.getElementById("conditional-sell-watch-price-info");
    const conditionalSellWatchPriceMinusBtn = document.getElementById("conditional-sell-watch-price-minus");
    const conditionalSellWatchPricePlusBtn = document.getElementById("conditional-sell-watch-price-plus");

    // 판매 조건 주문 감시 가격 입력 필드 및 버튼 이벤트 설정
    if (conditionalSellWatchPriceInput) {
        setupWatchPriceInput(conditionalSellWatchPriceInput, conditionalSellWatchPriceInfoWrapper || conditionalSellWatchPriceInfo, conditionalSellWatchPriceMinusBtn, conditionalSellWatchPricePlusBtn);
    }

    /* 실시간 주가 업데이트 (감시 가격 정보용) */
    let watchPricePollingInterval = null;
    
    // 실시간 주가 업데이트 함수
    async function updateWatchPriceCurrentPrice() {
        // URL에서 종목 코드 가져오기
        const urlParams = new URLSearchParams(window.location.search);
        const code = urlParams.get('code');
        
        if (!code) {
            console.warn("[실시간 주가 업데이트] 종목 코드를 찾을 수 없습니다.");
            return;
        }
        
        try {
            // 차트 데이터에서 최신 주가 가져오기 (첫 번째 항목이 최신 데이터)
            const chartData = await fetchStockChartData(code);
            
            if (!chartData || chartData.length === 0) {
                console.warn("[실시간 주가 업데이트] 차트 데이터를 가져올 수 없습니다.");
                return;
            }
            
            // 최신 종가 (closePrice) 가져오기
            const latestPrice = chartData[0].closePrice;
            
            if (latestPrice === null || latestPrice === undefined) {
                console.warn("[실시간 주가 업데이트] 유효한 주가 데이터가 없습니다.");
                return;
            }
            
            // 절댓값으로 변환 (음수일 수 있으므로)
            const currentPrice = Math.abs(Math.round(latestPrice));
            
            // 모든 watch-price-info 요소 찾기 (구매용, 판매용 모두 포함)
            // wrapper 내부의 요소들도 포함
            const allWatchPriceInfoElements = document.querySelectorAll('.order-card__watch-price-info');
            const allWatchPriceInfoWrappers = document.querySelectorAll('.order-card__watch-price-info-wrapper');
            
            // wrapper가 있으면 wrapper 단위로, 없으면 개별 요소 단위로 처리
            if (allWatchPriceInfoWrappers.length > 0) {
                allWatchPriceInfoWrappers.forEach(function(wrapperEl) {
                    const infoElements = wrapperEl.querySelectorAll('.order-card__watch-price-info');
                    infoElements.forEach(function(infoEl) {
                        infoEl.setAttribute("data-current-price", currentPrice.toString());
                    });
                    
                    // wrapper와 연결된 watch-price-input 찾기
                    const wrapperId = wrapperEl.querySelector('.order-card__watch-price-info')?.id;
                    if (wrapperId) {
                        const baseId = wrapperId.replace(/-current$|-avg$/, '');
                        let watchPriceInputEl = null;
                        
                        if (baseId === 'watch-price-info') {
                            watchPriceInputEl = document.getElementById('watch-price-input');
                        } else if (baseId === 'conditional-sell-watch-price-info') {
                            watchPriceInputEl = document.getElementById('conditional-sell-watch-price-input');
                        } else {
                            const inputId = baseId.replace('-info', '-input');
                            watchPriceInputEl = document.getElementById(inputId);
                        }
                        
                        if (watchPriceInputEl) {
                            watchPriceInputEl.setAttribute("data-current-price", currentPrice.toString());
                            updateWatchPriceInfo(watchPriceInputEl, wrapperEl);
                        }
                    }
                    
                    // 가격 입력 필드의 data-current-price도 업데이트하고 총 금액 재계산
                    // priceInputElement 변수가 스코프에 있으면 사용, 없으면 다시 찾기
                    const priceInput = priceInputElement || document.getElementById("order-price-input") || document.querySelector(".order-card__field--price .order-card__input");
                    if (priceInput) {
                        priceInput.setAttribute("data-current-price", currentPrice.toString());
                        // 시장가 모드이거나 수량이 입력되어 있으면 총 금액 재계산
                        const qtyInput = quantityInputElement || document.getElementById("order-quantity-input") || document.querySelector(".order-card__field--qty .order-card__input");
                        if (qtyInput) {
                            const quantity = parseInt(qtyInput.value.replace(/,/g, "") || "0");
                            if (quantity > 0) {
                                // calculateTotalAmount 함수가 스코프에 있으면 호출
                                if (typeof calculateTotalAmount === 'function') {
                                    calculateTotalAmount();
                                }
                            }
                        }
                    }
                });
            } else {
                // 기존 구조 (개별 요소)
                allWatchPriceInfoElements.forEach(function(watchPriceInfoEl) {
                    const oldPrice = watchPriceInfoEl.getAttribute("data-current-price");
                    watchPriceInfoEl.setAttribute("data-current-price", currentPrice.toString());
                    
                    // 해당 watch-price-info에 연결된 watch-price-input 찾기
                    const watchPriceInfoId = watchPriceInfoEl.id;
                    let watchPriceInputEl = null;
                    
                    if (watchPriceInfoId === 'watch-price-info') {
                        watchPriceInputEl = document.getElementById('watch-price-input');
                    } else if (watchPriceInfoId === 'conditional-sell-watch-price-info') {
                        watchPriceInputEl = document.getElementById('conditional-sell-watch-price-input');
                    } else {
                        const inputId = watchPriceInfoId.replace('-info', '-input');
                        watchPriceInputEl = document.getElementById(inputId);
                    }
                    
                    if (watchPriceInputEl) {
                        watchPriceInputEl.setAttribute("data-current-price", currentPrice.toString());
                        if (oldPrice !== currentPrice.toString()) {
                            updateWatchPriceInfo(watchPriceInputEl, watchPriceInfoEl);
                        }
                    }
                });
                
                // 가격 입력 필드의 data-current-price도 업데이트하고 총 금액 재계산
                const priceInput = document.getElementById("order-price-input") || document.querySelector(".order-card__field--price .order-card__input");
                if (priceInput) {
                    priceInput.setAttribute("data-current-price", currentPrice.toString());
                    // 시장가 모드이거나 수량이 입력되어 있으면 총 금액 재계산
                    const quantityInput = document.getElementById("order-quantity-input") || document.querySelector(".order-card__field--qty .order-card__input");
                    if (quantityInput) {
                        const quantity = parseInt(quantityInput.value.replace(/,/g, "") || "0");
                        if (quantity > 0) {
                            // calculateTotalAmount 함수가 스코프에 있으면 호출
                            if (typeof calculateTotalAmount === 'function') {
                                calculateTotalAmount();
                            }
                        }
                    }
                }
            }
            
            // 가격 변경 로그 (한 번만 출력)
            if (allWatchPriceInfoElements.length > 0) {
                const firstElement = allWatchPriceInfoElements[0];
                const oldPrice = firstElement.getAttribute("data-current-price") || currentPrice.toString();
                if (oldPrice !== currentPrice.toString()) {
                    console.log(`[실시간 주가 업데이트] 주가 변경: ${oldPrice} → ${currentPrice}`);
                }
            }
        } catch (error) {
            console.error("[실시간 주가 업데이트] 오류:", error);
        }
    }

    // 실시간 주가 폴링 시작 (5초마다 체크)
    function startWatchPricePolling() {
        if (watchPricePollingInterval) {
            clearInterval(watchPricePollingInterval);
        }
        
        // 초기 로드 시 즉시 업데이트
        updateWatchPriceCurrentPrice();
        
        // 5초마다 업데이트
        watchPricePollingInterval = setInterval(updateWatchPriceCurrentPrice, 5000);
    }

    // 실시간 주가 폴링 중지
    function stopWatchPricePolling() {
        if (watchPricePollingInterval) {
            clearInterval(watchPricePollingInterval);
            watchPricePollingInterval = null;
        }
    }

    // 페이지가 보일 때만 폴링 시작
    document.addEventListener("visibilitychange", function() {
        const isPageVisible = !document.hidden;
        const hasWatchPriceInfo = document.querySelector('.order-card__watch-price-info');
        if (isPageVisible && hasWatchPriceInfo) {
            startWatchPricePolling();
        } else {
            stopWatchPricePolling();
        }
    });

    // watch-price-info 요소가 존재할 때만 폴링 시작
    const hasWatchPriceInfo = document.querySelector('.order-card__watch-price-info');
    if (hasWatchPriceInfo) {
        startWatchPricePolling();
    }
    
    // 페이지 언로드 시 폴링 중지
    window.addEventListener("beforeunload", function() {
        stopWatchPricePolling();
    });

    // 조건 주문 구매/판매 가격 필드 초기화 함수
    function initializeConditionalOrderPriceFields() {
        const conditionalOrderPriceInput = document.getElementById("conditional-order-price-input");
        const conditionalSellPriceInput = document.getElementById("conditional-sell-price-input");
        
        // 구매 조건 주문 가격 필드 초기화
        // 조건 주문은 항상 지정가 모드이므로 orderTypeHidden 체크 없이 초기화
        if (conditionalOrderPriceInput) {
            const currentPrice = conditionalOrderPriceInput.getAttribute("data-current-price");
            if (currentPrice && !conditionalOrderPriceInput.value) {
                // 감시 가격처럼 초기값 설정
                conditionalOrderPriceInput.value = formatNumber(parseInt(currentPrice));
            }
        }
        
        // 판매 조건 주문 가격 필드 초기화
        if (conditionalSellPriceInput) {
            const currentPrice = conditionalSellPriceInput.getAttribute("data-current-price");
            if (currentPrice && !conditionalSellPriceInput.value) {
                // 감시 가격처럼 초기값 설정
                conditionalSellPriceInput.value = formatNumber(parseInt(currentPrice));
            }
        }
        
        // 조건 주문 필드 초기화 후 placeholder 업데이트
        if (typeof updateQuantityPlaceholders === 'function') {
            updateQuantityPlaceholders();
        }
    }
    
    // 초기 로딩 시 조건 주문 가격 필드 초기화
    initializeConditionalOrderPriceFields();

    /* 구매 가능 금액 실시간 업데이트 (폴링) */
    let availableCashPollingInterval = null;
    
    // 구매 가능 금액 업데이트 함수
    async function updateAvailableCash() {
        try {
            // stockId 가져오기
            const orderCard = document.querySelector(".order-card");
            const stockId = orderCard ? orderCard.getAttribute("data-stock-id") : null;
            
            // API 호출 (stockId가 있으면 포함)
            let url = "/api/order";
            if (stockId) {
                url += "?stockId=" + stockId;
            }
            
            const response = await fetch(url);
            
            if (!response.ok) {
                return;
            }
            
            const result = await response.json();
            
            if (result.success && result.availableCash !== undefined) {
                const formattedAmount = result.availableCash.toLocaleString('ko-KR');
                
                // 일반 주문 구매가능 금액 업데이트
                const availableAmountElement = document.getElementById("order-available-amount");
                if (availableAmountElement) {
                    availableAmountElement.textContent = formattedAmount + "원";
                    availableAmountElement.setAttribute("data-available-cash", result.availableCash);
                }
                
                // 조건 주문 구매가능 금액 업데이트
                const conditionalAvailableAmountElement = document.getElementById("conditional-order-available-amount");
                if (conditionalAvailableAmountElement) {
                    conditionalAvailableAmountElement.textContent = formattedAmount + "원";
                }
                
                // order-card의 data-available-cash도 업데이트
                if (orderCard) {
                    orderCard.setAttribute("data-available-cash", result.availableCash);
                    
                    // 대기 중인 판매 주문 수량 업데이트
                    if (result.pendingSellQuantity !== undefined) {
                        orderCard.setAttribute("data-pending-sell-quantity", result.pendingSellQuantity);
                        // placeholder 업데이트 (판매 탭일 경우)
                        if (typeof updateQuantityPlaceholders === 'function') {
                            updateQuantityPlaceholders();
                        }
                    }
                }
            }
        } catch (error) {
            console.error("[구매 가능 금액 업데이트] 오류:", error);
        }
    }

    // 구매 가능 금액 폴링 시작 (5초마다 체크)
    function startAvailableCashPolling() {
        if (availableCashPollingInterval) {
            clearInterval(availableCashPollingInterval);
        }
        
        availableCashPollingInterval = setInterval(updateAvailableCash, 5000); // 5초마다 업데이트
    }

    // 구매 가능 금액 폴링 중지
    function stopAvailableCashPolling() {
        if (availableCashPollingInterval) {
            clearInterval(availableCashPollingInterval);
            availableCashPollingInterval = null;
        }
    }

    // 페이지가 보일 때만 폴링 시작
    document.addEventListener("visibilitychange", function() {
        const isPageVisible = !document.hidden;
        if (isPageVisible) {
            startAvailableCashPolling();
        } else {
            stopAvailableCashPolling();
        }
    });

    // 초기 로드 시 즉시 업데이트
    updateAvailableCash();
    
    // 초기 폴링 시작
    startAvailableCashPolling();
    
    // 페이지 언로드 시 폴링 중지
    window.addEventListener("beforeunload", function() {
        stopAvailableCashPolling();
    });

    /* 대기 주문 취소 버튼 이벤트 */
    function attachCancelButtonListeners() {
        const cancelOrderButtons = document.querySelectorAll(".order-card__wait-order-btn--cancel");
        
        cancelOrderButtons.forEach(function(button) {
            // 이미 이벤트 리스너가 연결되어 있는지 확인
            if (button.hasAttribute("data-listener-attached")) {
                return;
            }
            
            button.setAttribute("data-listener-attached", "true");
            button.addEventListener("click", async function() {
                const orderId = this.getAttribute("data-order-id");
                
                if (!orderId) {
                    alert("주문 ID를 찾을 수 없습니다.");
                    return;
                }
                
                // 확인 메시지
                const confirmed = window.confirm("정말로 이 주문을 취소하시겠습니까?");
                if (!confirmed) {
                    return;
                }
                
                try {
                    // DELETE 요청
                    const params = new URLSearchParams();
                    params.append("orderId", orderId);
                    
                    const response = await fetch("/api/order?" + params.toString(), {
                        method: "DELETE",
                        headers: {
                            "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
                        }
                    });
                    
                    // 응답 처리
                    let result;
                    try {
                        result = await response.json();
                    } catch (e) {
                        console.error("[주문 취소] JSON 파싱 오류:", e);
                        alert("서버 응답을 처리할 수 없습니다. 페이지를 새로고침해주세요.");
                        return;
                    }
                    
                    if (result.success) {
                        alert(result.message || "주문이 성공적으로 취소되었습니다.");
                        // 페이지 새로고침하여 대기 주문 목록 업데이트
                        window.location.reload();
                    } else {
                        alert(result.message || "주문 취소에 실패했습니다.");
                    }
                } catch (error) {
                    console.error("[주문 취소] 네트워크 오류:", error);
                    if (error instanceof TypeError && error.message.includes("fetch")) {
                        alert("네트워크 연결을 확인해주세요. 인터넷 연결이 끊어졌을 수 있습니다.");
                    } else {
                        alert("주문 취소 중 오류가 발생했습니다: " + error.message);
                    }
                }
            });
        });
    }

    // 초기 취소 버튼 이벤트 리스너 연결
    attachCancelButtonListeners();
    
    // 동적으로 추가된 취소 버튼을 위한 MutationObserver
    const waitOrdersContainer = document.querySelector(".order-card__wait-orders");
    if (waitOrdersContainer) {
        const observer = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                if (mutation.addedNodes.length > 0) {
                    attachCancelButtonListeners();
                }
            });
        });
        
        observer.observe(waitOrdersContainer, {
            childList: true,
            subtree: true
        });
    }

});
