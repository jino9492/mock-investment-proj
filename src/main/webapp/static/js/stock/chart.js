import {
    formatNumber,
    calculateTotalAmount as calcTotalAmount,
    initializeAvailableAmount as initAvailableAmount,
    initializeCurrentPrice as initCurrentPrice,
    initializeQuantity as initQuantity,
    updateWatchPriceInfo
} from '../common/stock/stock_order_utils.js';
import {getPriceColorClass} from '../common/price_color_utils.js';

import {
    setupPriceStepButtons,
    setupQuantityStepButtons,
    setupPriceInput,
    setupQuantityInput,
    setupSubmitButton,
    setupPriceModePills,
    setupWatchPriceInput
} from '../common/stock/stock_order_card.js';
import {loadChartByUrlCode} from "../common/chart/candle_chart.js";
import {fetchStockChartData} from "../common/api/fetch_utils.js";

document.addEventListener("DOMContentLoaded", function handleStockPageInit() {
    loadChartByUrlCode();
    /* 일/주/월/년 버튼 활성화*/

    const rangeButtons = document.querySelectorAll(".stock-range-tabs__item");

    rangeButtons.forEach(function attachRangeClick(button) {
        button.addEventListener("click", function onRangeClick() {
            rangeButtons.forEach(function removeActiveClass(btn) {
                btn.classList.remove("stock-range-tabs__item--active");
            });
            button.classList.add("stock-range-tabs__item--active");
        });
    });


    /* 주문하기 탭 전환 */

    const tradeTabs = document.querySelectorAll(".order-card__trade-tab");
    console.log(tradeTabs);

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

    // 각 탭별 주문 유형 상태 저장
    const orderTypeByTab = {
        buy: "general",
        sell: "general",
        wait: "general"
    };

    // 숫자 포맷팅 헬퍼 함수
    function formatNumber(num) {
        return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    }

    // 조건 주문 가격 필드 초기화 함수
    function initializeConditionalOrderPriceFields() {
        const conditionalOrderPriceInput = document.getElementById("conditional-order-price-input");
        const conditionalSellPriceInput = document.getElementById("conditional-sell-price-input");
        
        // 구매 조건 주문 가격 필드 초기화
        if (conditionalOrderPriceInput) {
            const currentPrice = conditionalOrderPriceInput.getAttribute("data-current-price");
            if (currentPrice && !conditionalOrderPriceInput.value) {
                conditionalOrderPriceInput.value = formatNumber(parseInt(currentPrice));
            }
        }
        
        // 판매 조건 주문 가격 필드 초기화
        if (conditionalSellPriceInput) {
            const currentPrice = conditionalSellPriceInput.getAttribute("data-current-price");
            if (currentPrice && !conditionalSellPriceInput.value) {
                conditionalSellPriceInput.value = formatNumber(parseInt(currentPrice));
            }
        }
        
        // 조건 주문 필드 초기화 후 placeholder 업데이트
        if (typeof updateQuantityPlaceholders === 'function') {
            updateQuantityPlaceholders();
        }
    }

    // 주문 섹션 표시/숨김 헬퍼 함수
    function updateOrderSections(activeTab, orderType) {
        const generalOrder = document.querySelector(".order-card__general-order");
        const buyConditionalOrder = document.querySelector(".order-card__conditional-order");
        const sellConditionalOrder = document.querySelector(".order-card__conditional-order--sell");
        
        if (orderType === "conditional") {
            // 조건 주문 선택 시
            if (generalOrder) generalOrder.style.display = "none";
            
            if (activeTab === "sell") {
                // 판매 탭 + 조건 주문
                if (buyConditionalOrder) buyConditionalOrder.style.display = "none";
                if (sellConditionalOrder) sellConditionalOrder.style.display = "block";
                // 조건 주문 필드가 표시될 때 가격 필드 초기화
                setTimeout(() => initializeConditionalOrderPriceFields(), 0);
            } else if (activeTab === "buy") {
                // 구매 탭 + 조건 주문
                if (buyConditionalOrder) buyConditionalOrder.style.display = "block";
                if (sellConditionalOrder) sellConditionalOrder.style.display = "none";
                // 조건 주문 필드가 표시될 때 가격 필드 초기화
                setTimeout(() => initializeConditionalOrderPriceFields(), 0);
            } else {
                // 대기 탭 등
                if (buyConditionalOrder) buyConditionalOrder.style.display = "none";
                if (sellConditionalOrder) sellConditionalOrder.style.display = "none";
            }
        } else {
            // 일반 주문 선택 시
            if (generalOrder) generalOrder.style.display = "block";
            if (buyConditionalOrder) buyConditionalOrder.style.display = "none";
            if (sellConditionalOrder) sellConditionalOrder.style.display = "none";
        }
    }

    // 주문 유형 드롭다운 UI 업데이트 함수
    function updateOrderTypeDropdownUI(orderType) {
        const orderTypeDropdown = orderCard?.querySelector("[data-order-type-dropdown]");
        if (!orderTypeDropdown) return;

        const hiddenInput = orderTypeDropdown.querySelector("[data-dropdown-input]");
        const label = orderTypeDropdown.querySelector("[data-dropdown-label]");
        const options = orderTypeDropdown.querySelectorAll(".order-card__dropdown-option");

        if (hiddenInput) {
            hiddenInput.value = orderType;
        }

        // 해당 옵션 찾아서 활성화
        options.forEach(function(option) {
            const value = option.getAttribute("data-value");
            if (value === orderType) {
                option.setAttribute("aria-selected", "true");
                if (label) {
                    label.textContent = option.querySelector(".order-card__dropdown-option-label")?.textContent || option.textContent;
                }
            } else {
                option.setAttribute("aria-selected", "false");
            }
        });
    }

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
                    if (orderCard) {
                        orderCard.classList.add("order-card--sell");
                        // 판매 탭의 주문 유형 복원 (기본값: 일반 주문)
                        const orderType = orderTypeByTab.sell;
                        const orderTypeRow = document.querySelector(".order-card__order-type-row");
                        const waitOrders = document.querySelector(".order-card__wait-orders");
                        
                        if (orderTypeRow) orderTypeRow.style.display = "flex";
                        if (waitOrders) waitOrders.style.display = "none";
                        updateOrderTypeDropdownUI(orderType);
                        updateOrderSections("sell", orderType);
                    }
                    priceLabel.textContent = "판매가격";
                    amountLabel.textContent = "판매가능 금액";
                    if (submitButton) {
                        submitButton.style.display = "flex";
                        // 로그인 상태 확인 (버튼이면 로그인, 링크면 비로그인)
                        if (submitButton.tagName === "BUTTON") {
                            submitButton.textContent = "판매하기";
                        } else {
                            submitButton.textContent = "로그인하고 판매하기";
                        }
                    }
                } else if (text.includes("구매")) {
                    // 구매 탭
                    tab.classList.add("order-card__trade-tab--active");
                    tab.setAttribute("aria-checked", "true");
                    tab.setAttribute("data-state", "checked");
                    if (orderCard) {
                        orderCard.classList.add("order-card--buy");
                        // 구매 탭의 주문 유형 복원
                        const orderType = orderTypeByTab.buy;
                        const orderTypeRow = document.querySelector(".order-card__order-type-row");
                        const waitOrders = document.querySelector(".order-card__wait-orders");
                        
                        if (orderTypeRow) orderTypeRow.style.display = "flex";
                        if (waitOrders) waitOrders.style.display = "none";
                        updateOrderTypeDropdownUI(orderType);
                        updateOrderSections("buy", orderType);
                    }
                    priceLabel.textContent = "구매가격";
                    amountLabel.textContent = "구매가능 금액";
                    if (submitButton) {
                        submitButton.style.display = "flex";
                        // 로그인 상태 확인 (버튼이면 로그인, 링크면 비로그인)
                        if (submitButton.tagName === "BUTTON") {
                            submitButton.textContent = "구매하기";
                        } else {
                            submitButton.textContent = "로그인하고 구매하기";
                        }
                    }
                }
                else if (text.includes("대기")) {
                    // 대기 탭
                    tab.classList.add("order-card__trade-tab--active");
                    tab.setAttribute("aria-checked", "true");
                    tab.setAttribute("data-state", "checked");
                    if (orderCard) {
                        orderCard.classList.add("order-card--wait");
                        // 대기 탭에서는 주문 유형 드롭다운과 일반/조건 주문 섹션 숨김
                        const orderTypeRow = document.querySelector(".order-card__order-type-row");
                        const generalOrder = document.querySelector(".order-card__general-order");
                        const buyConditionalOrder = document.querySelector(".order-card__conditional-order");
                        const sellConditionalOrder = document.querySelector(".order-card__conditional-order--sell");
                        const waitOrders = document.querySelector(".order-card__wait-orders");
                        
                        if (orderTypeRow) orderTypeRow.style.display = "none";
                        if (generalOrder) generalOrder.style.display = "none";
                        if (buyConditionalOrder) buyConditionalOrder.style.display = "none";
                        if (sellConditionalOrder) sellConditionalOrder.style.display = "none";
                        if (waitOrders) waitOrders.style.display = "block";
                    }
                    if (submitButton) submitButton.style.display = "none";
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
        calcTotalAmount(priceInputElement, quantityInputElement, totalAmountElement);
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
    setupPriceStepButtons(priceMinusBtn, pricePlusBtn, priceInputElement, quantityInputElement, totalAmountElement);
    
    // 수량 관련 이벤트 설정 (최대값을 동적으로 계산)
    // 일반 주문 수량 필드 - 최대값을 동적으로 계산하는 함수 전달
    setupQuantityStepButtons(quantityMinusBtn, quantityPlusBtn, quantityInputElement, priceInputElement, totalAmountElement, () => calculateMaxQuantity());
    setupQuantityInput(quantityInputElement, priceInputElement, totalAmountElement, () => calculateMaxQuantity());
    
    // 조건 주문 수량 필드 (구매)
    const conditionalBuyQuantityInput = document.getElementById("conditional-order-quantity-input");
    const conditionalBuyQuantityMinus = document.getElementById("conditional-order-quantity-minus");
    const conditionalBuyQuantityPlus = document.getElementById("conditional-order-quantity-plus");
    const conditionalBuyPriceInput = document.getElementById("conditional-order-price-input");
    const conditionalBuyTotalAmount = document.querySelector(".order-card__conditional-order .order-card__summary-value:last-child");
    
    if (conditionalBuyQuantityInput && conditionalBuyQuantityMinus && conditionalBuyQuantityPlus) {
        setupQuantityStepButtons(conditionalBuyQuantityMinus, conditionalBuyQuantityPlus, conditionalBuyQuantityInput, conditionalBuyPriceInput, conditionalBuyTotalAmount, () => calculateMaxQuantity());
        setupQuantityInput(conditionalBuyQuantityInput, conditionalBuyPriceInput, conditionalBuyTotalAmount, () => calculateMaxQuantity());
    }
    
    // 조건 주문 수량 필드 (판매)
    const conditionalSellQuantityInput = document.getElementById("conditional-sell-quantity-input");
    const conditionalSellQuantityMinus = document.getElementById("conditional-sell-quantity-minus");
    const conditionalSellQuantityPlus = document.getElementById("conditional-sell-quantity-plus");
    const conditionalSellPriceInput = document.getElementById("conditional-sell-price-input");
    const conditionalSellTotalAmount = document.querySelector(".order-card__conditional-order--sell .order-card__summary-value:last-child");
    
    if (conditionalSellQuantityInput && conditionalSellQuantityMinus && conditionalSellQuantityPlus) {
        setupQuantityStepButtons(conditionalSellQuantityMinus, conditionalSellQuantityPlus, conditionalSellQuantityInput, conditionalSellPriceInput, conditionalSellTotalAmount, () => calculateMaxQuantity());
        setupQuantityInput(conditionalSellQuantityInput, conditionalSellPriceInput, conditionalSellTotalAmount, () => calculateMaxQuantity());
    }
    
    // 가격 변경 시 수량 필드 최대값 재확인 및 placeholder 업데이트
    const updateQuantityOnPriceChange = () => {
        const maxQty = calculateMaxQuantity();
        if (maxQty !== null && quantityInputElement.value) {
            const currentQty = parseInt(quantityInputElement.value.replace(/,/g, "")) || 0;
            if (currentQty > maxQty) {
                quantityInputElement.value = maxQty.toString();
                calculateTotalAmount();
            }
        }
        // placeholder 업데이트
        updateQuantityPlaceholders();
    };
    
    priceInputElement.addEventListener("input", updateQuantityOnPriceChange);
    priceInputElement.addEventListener("change", updateQuantityOnPriceChange);
    
    if (conditionalBuyPriceInput) {
        conditionalBuyPriceInput.addEventListener("input", updateQuantityOnPriceChange);
        conditionalBuyPriceInput.addEventListener("change", updateQuantityOnPriceChange);
    }
    if (conditionalSellPriceInput) {
        conditionalSellPriceInput.addEventListener("input", updateQuantityOnPriceChange);
        conditionalSellPriceInput.addEventListener("change", updateQuantityOnPriceChange);
    }
    
    // 탭 전환 시 수량 필드 최대값 재확인 및 placeholder 업데이트
    tradeTabs.forEach(tab => {
        tab.addEventListener("click", () => {
            setTimeout(() => {
                const maxQty = calculateMaxQuantity();
                if (maxQty !== null && quantityInputElement.value) {
                    const currentQty = parseInt(quantityInputElement.value.replace(/,/g, "")) || 0;
                    if (currentQty > maxQty) {
                        quantityInputElement.value = maxQty.toString();
                        calculateTotalAmount();
                    }
                }
                // placeholder 업데이트
                updateQuantityPlaceholders();
            }, 100);
        });
    });
    
    // 초기 placeholder 설정
    updateQuantityPlaceholders();
    
    setupPriceInput(priceInputElement, quantityInputElement, totalAmountElement);
    setupSubmitButton(submitBtn, priceInputElement, quantityInputElement, orderTypeHidden);

    // 초기화
    initAvailableAmount(availableAmountElement);
    initCurrentPrice(priceInputElement, orderTypeHidden);
    initQuantity(quantityInputElement);
    
    // 현재가 설정 후 총 금액 계산 및 placeholder 업데이트
    if (priceInputElement && orderTypeHidden && orderTypeHidden.value === "LIMIT") {
        const currentPrice = priceInputElement.getAttribute("data-current-price");
        if (currentPrice && priceInputElement.value) {
            calculateTotalAmount();
        }
        // 현재가가 설정된 후 placeholder 업데이트
        setTimeout(function() {
            updateQuantityPlaceholders();
        }, 200);
    } else {
        // 시장가 모드이거나 가격이 없는 경우에도 placeholder 업데이트 (판매 탭일 수 있음)
        setTimeout(function() {
            updateQuantityPlaceholders();
        }, 200);
    }

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

        if (!toggle || !list || !label || !hiddenInput || !options.length) {
            return;
        }

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

        // 초기 상태에서 주문 유형에 따라 display 설정
        if (dropdown.hasAttribute("data-order-type-dropdown")) {
            const initialOrderType = initial.getAttribute("data-value");
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
            // 초기 주문 유형을 현재 탭의 상태로 설정
            orderTypeByTab[activeTabType] = initialOrderType;
            updateOrderSections(activeTabType, initialOrderType);
        }

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

                // 주문 유형 드롭다운 변경 시 조건 주문 필드 표시/숨김 처리
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
                    // 현재 활성 탭의 주문 유형 상태 업데이트
                    orderTypeByTab[activeTabType] = orderType;
                    updateOrderSections(activeTabType, orderType);
                    // 조건 주문 선택 시 가격 필드 초기화 (updateOrderSections에서도 호출되지만 명시적으로 호출)
                    if (orderType === "conditional") {
                        setTimeout(() => initializeConditionalOrderPriceFields(), 0);
                    } else {
                        // 일반 주문으로 변경 시 placeholder 업데이트
                        setTimeout(updateQuantityPlaceholders, 100);
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

    /* my-stock-card 실시간 업데이트 (폴링) */
    let holdingPollingInterval = null;
    
    // 보유 정보 업데이트 함수
    async function updateMyStockCard() {
        const myStockCard = document.querySelector(".my-stock-card");
        if (!myStockCard) return;
        
        const stockId = myStockCard.getAttribute("data-stock-id");
        if (!stockId) return;
        
        try {
            const response = await fetch(`/api/holding?stockId=${stockId}`);
            
            if (!response.ok) {
                if (response.status === 401) {
                    // 401 Unauthorized: 로그인이 필요함 - 폴링 중지
                    console.warn("[보유 정보 업데이트] 로그인이 필요합니다. 폴링을 중지합니다.");
                    stopHoldingPolling();
                    return;
                }
                console.warn("[보유 정보 업데이트] 서버 응답 오류:", response.status);
                return;
            }
            
            const result = await response.json();
            
            if (!result.success) {
                console.warn("[보유 정보 업데이트] 응답 데이터 오류:", result);
                return;
            }
            
            const holding = result.holding;
            
            if (holding) {
                // 총 수익 및 수익률 업데이트
                const profitElement = myStockCard.querySelector(".my-stock-card__profit");
                const profitRateElement = myStockCard.querySelector(".my-stock-card__profit-rate");
                
                if (profitElement) {
                    const profitLoss = holding.profitLoss || 0;
                    const priceColorClass = getPriceColorClass(profitLoss);
                    profitElement.className = "my-stock-card__profit " + priceColorClass;
                    profitElement.textContent = (profitLoss > 0 ? "+" : profitLoss < 0 ? "-" : "") + 
                        Math.abs(profitLoss).toLocaleString('ko-KR');
                }
                
                if (profitRateElement) {
                    const profitRate = holding.profitRate || 0;
                    const rateColorClass = getPriceColorClass(profitRate);
                    profitRateElement.className = "my-stock-card__profit-rate " + rateColorClass;
                    profitRateElement.textContent = "(" + (profitRate > 0 ? "+" : profitRate < 0 ? "-" : "") + Math.abs(profitRate).toFixed(2) + "%)";
                }
                
                // 총 금액 업데이트
                const evaluationAmountElement = document.getElementById("my-stock-evaluation-amount");
                if (evaluationAmountElement) {
                    evaluationAmountElement.textContent = (holding.evaluationAmount || 0).toLocaleString('ko-KR') + "원";
                }
                
                // 수량 업데이트
                const quantityElement = document.getElementById("my-stock-quantity");
                if (quantityElement) {
                    quantityElement.textContent = (holding.quantity || 0).toLocaleString('ko-KR') + "주";
                }
                
                // 평균 단가 업데이트
                const averagePriceElement = document.getElementById("my-stock-average-price");
                if (averagePriceElement) {
                    averagePriceElement.textContent = (holding.averagePrice || 0).toLocaleString('ko-KR') + "원";
                }
                
                // watch-price-info 요소들의 평균 단가 정보 업데이트
                const allWatchPriceInfoWrappers = document.querySelectorAll('.order-card__watch-price-info-wrapper');
                allWatchPriceInfoWrappers.forEach(function(wrapperEl) {
                    const infoElements = wrapperEl.querySelectorAll('.order-card__watch-price-info');
                    infoElements.forEach(function(infoEl) {
                        if (holding.averagePrice) {
                            infoEl.setAttribute("data-avg-price", holding.averagePrice.toString());
                        }
                    });
                    
                    // 연결된 watch-price-input 찾아서 업데이트 호출
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
                            updateWatchPriceInfo(watchPriceInputEl, wrapperEl);
                        }
                    }
                });
                
                // 기존 구조 지원 (wrapper 없는 경우)
                const allWatchPriceInfoElements = document.querySelectorAll('.order-card__watch-price-info:not(.order-card__watch-price-info-wrapper .order-card__watch-price-info)');
                allWatchPriceInfoElements.forEach(function(infoEl) {
                    if (holding.averagePrice && infoEl.closest('.order-card__watch-price-info-wrapper') === null) {
                        infoEl.setAttribute("data-avg-price", holding.averagePrice.toString());
                        const infoId = infoEl.id;
                        if (infoId) {
                            let watchPriceInputEl = null;
                            if (infoId === 'watch-price-info') {
                                watchPriceInputEl = document.getElementById('watch-price-input');
                            } else if (infoId === 'conditional-sell-watch-price-info') {
                                watchPriceInputEl = document.getElementById('conditional-sell-watch-price-input');
                            } else {
                                const inputId = infoId.replace('-info', '-input');
                                watchPriceInputEl = document.getElementById(inputId);
                            }
                            if (watchPriceInputEl) {
                                updateWatchPriceInfo(watchPriceInputEl, infoEl);
                            }
                        }
                    }
                });
            } else {
                // 보유 정보가 없으면 0으로 표시
                const profitElement = myStockCard.querySelector(".my-stock-card__profit");
                const profitRateElement = myStockCard.querySelector(".my-stock-card__profit-rate");
                
                if (profitElement) {
                    profitElement.className = "my-stock-card__profit price-color--neutral";
                    profitElement.textContent = "0";
                }
                
                if (profitRateElement) {
                    profitRateElement.className = "my-stock-card__profit-rate price-color--neutral";
                    profitRateElement.textContent = "(0.00%)";
                }
                
                const evaluationAmountElement = document.getElementById("my-stock-evaluation-amount");
                if (evaluationAmountElement) evaluationAmountElement.textContent = "0원";
                
                const quantityElement = document.getElementById("my-stock-quantity");
                if (quantityElement) quantityElement.textContent = "0주";
                
                const averagePriceElement = document.getElementById("my-stock-average-price");
                if (averagePriceElement) averagePriceElement.textContent = "0원";
            }
            
        } catch (error) {
            console.error("[보유 정보 업데이트] 오류:", error);
        }
    }

    // 폴링 시작 (5초마다 체크)
    function startHoldingPolling() {
        if (holdingPollingInterval) {
            clearInterval(holdingPollingInterval);
        }
        
        holdingPollingInterval = setInterval(updateMyStockCard, 5000); // 5초마다 업데이트
    }

    // 폴링 중지
    function stopHoldingPolling() {
        if (holdingPollingInterval) {
            clearInterval(holdingPollingInterval);
            holdingPollingInterval = null;
        }
    }

    // 페이지가 보일 때만 폴링 시작
    let isPageVisible = true;
    
    document.addEventListener("visibilitychange", function() {
        isPageVisible = !document.hidden;
        const myStockCard = document.querySelector(".my-stock-card");
        if (isPageVisible && myStockCard) {
            startHoldingPolling();
        } else {
            stopHoldingPolling();
        }
    });

    // 로그인 상태 확인 후 폴링 시작
    // my-stock-card가 존재하면 로그인한 것으로 간주
    const myStockCard = document.querySelector(".my-stock-card");
    if (myStockCard) {
        startHoldingPolling();
        
        // 주가 변경 시 평균 단가 실시간 업데이트
        let lastKnownPrice = null;
        const priceInputElement = document.getElementById("order-price-input");
        let priceCheckInterval = null;
        
        // 주가 변경 감지 함수
        function checkPriceChange() {
            if (!priceInputElement) return;
            
            const currentPriceAttr = priceInputElement.getAttribute("data-current-price");
            const currentPrice = currentPriceAttr ? parseFloat(currentPriceAttr) : null;
            
            // 주가가 변경되었고 유효한 값인 경우
            if (currentPrice !== null && !isNaN(currentPrice)) {
                if (lastKnownPrice !== null && currentPrice !== lastKnownPrice) {
                    // 주가가 변경되었으므로 평균 단가 즉시 업데이트
                    console.log(`[평균 단가 실시간 업데이트] 주가 변경 감지: ${lastKnownPrice} → ${currentPrice}`);
                    updateMyStockCard();
                }
                lastKnownPrice = currentPrice;
            }
        }
        
        // 주가 변경 감지를 위한 MutationObserver 설정
        if (priceInputElement) {
            const priceObserver = new MutationObserver(function(mutations) {
                mutations.forEach(function(mutation) {
                    if (mutation.type === 'attributes' && mutation.attributeName === 'data-current-price') {
                        checkPriceChange();
                    }
                });
            });
            
            priceObserver.observe(priceInputElement, {
                attributes: true,
                attributeFilter: ['data-current-price']
            });
            
            // 초기 가격 설정
            const initialPrice = priceInputElement.getAttribute("data-current-price");
            if (initialPrice) {
                const parsedPrice = parseFloat(initialPrice);
                if (!isNaN(parsedPrice)) {
                    lastKnownPrice = parsedPrice;
                }
            }
            
            // 주기적으로도 체크 (폴링과 함께 사용하여 놓친 변경사항 감지)
            // 장이 열려있을 때만 더 자주 체크 (1초마다)
            priceCheckInterval = setInterval(checkPriceChange, 1000);
        }
        
        // 페이지 언로드 시 인터벌 정리
        window.addEventListener("beforeunload", function() {
            if (priceCheckInterval) {
                clearInterval(priceCheckInterval);
            }
        });
    }
    
    // 페이지 언로드 시 폴링 중지
    window.addEventListener("beforeunload", function() {
        stopHoldingPolling();
    });

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
                if (response.status === 401) {
                    // 401 Unauthorized: 로그인이 필요함 - 폴링 중지
                    console.warn("[구매 가능 금액 업데이트] 로그인이 필요합니다. 폴링을 중지합니다.");
                    stopAvailableCashPolling();
                    return;
                }
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
        if (isPageVisible && orderCard) {
            startAvailableCashPolling();
        } else {
            stopAvailableCashPolling();
        }
    });

    // 로그인 상태 확인 후 폴링 시작
    // order-card가 존재하면 로그인한 것으로 간주
    if (orderCard) {
        // 초기 로드 시 즉시 업데이트
        updateAvailableCash();
        // 초기 폴링 시작
        startAvailableCashPolling();
    }
    
    // 페이지 언로드 시 폴링 중지
    window.addEventListener("beforeunload", function() {
        stopAvailableCashPolling();
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

    // 감시 가격 입력 필드 및 버튼 이벤트 설정
    setupWatchPriceInput(watchPriceInput, watchPriceInfoWrapper || watchPriceInfo, watchPriceMinusBtn, watchPricePlusBtn);

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
