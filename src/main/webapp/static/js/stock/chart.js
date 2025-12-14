import {
    formatNumber,
    calculateTotalAmount as calcTotalAmount,
    initializeAvailableAmount as initAvailableAmount,
    initializeCurrentPrice as initCurrentPrice,
    initializeQuantity as initQuantity
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
import {loadChartByUrlCode} from "../common/chart/candle_chart.js";

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
                
                // 모달 내부 드롭다운인 경우 위치 계산 (fixed position)
                const isModalDropdown = dropdown.closest(".order-modal__form-content");
                if (isModalDropdown) {
                    const dropdownList = dropdown.querySelector(".order-card__dropdown-list");
                    if (dropdownList) {
                        const toggleRect = toggle.getBoundingClientRect();
                        dropdownList.style.top = `${toggleRect.bottom + 4}px`;
                        dropdownList.style.left = `${toggleRect.left}px`;
                        dropdownList.style.width = `${toggleRect.width}px`;
                    }
                }
                
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
                // 총 수익 업데이트
                const profitElement = myStockCard.querySelector(".my-stock-card__profit");
                if (profitElement) {
                    const profitLoss = Number(holding.profitLoss) || 0;
                    const profitRate = Number(holding.profitRate) || 0;
                    const profitClass = profitLoss > 0 ? "my-stock-card__profit--positive" : 
                                      profitLoss < 0 ? "my-stock-card__profit--negative" : 
                                      "my-stock-card__profit--neutral";
                    profitElement.className = "my-stock-card__profit " + profitClass;
                    
                    const absProfitLoss = Math.abs(profitLoss);
                    let profitText = absProfitLoss.toLocaleString('ko-KR') + "원";
                    
                    if (profitRate !== 0) {
                        const absProfitRate = Math.abs(profitRate);
                        profitText += " (" + absProfitRate.toFixed(2) + "%)";
                    }
                    
                    profitElement.textContent = profitText;
                }
                
                // 총 금액 업데이트
                const evaluationAmountElement = document.getElementById("my-stock-evaluation-amount");
                if (evaluationAmountElement) {
                    const evaluationAmount = Number(holding.evaluationAmount) || 0;
                    const absEvaluationAmount = Math.abs(evaluationAmount);
                    evaluationAmountElement.textContent = absEvaluationAmount.toLocaleString('ko-KR') + "원";
                }
                
                // 수량 업데이트
                const quantityElement = document.getElementById("my-stock-quantity");
                if (quantityElement) {
                    quantityElement.textContent = (holding.quantity || 0).toLocaleString('ko-KR') + "주";
                }
                
                // 평균 단가 업데이트 (평균가 - 현재가) × 수량
                const averagePriceElement = document.getElementById("my-stock-average-price");
                if (averagePriceElement) {
                    const averagePrice = Number(holding.averagePrice) || 0;
                    const currentPrice = Number(holding.currentPrice) || 0;
                    const quantity = Number(holding.quantity) || 0;
                    const averageAmount = (averagePrice - currentPrice) * quantity;
                    const absAverageAmount = Math.abs(averageAmount);
                    averagePriceElement.textContent = absAverageAmount.toLocaleString('ko-KR') + "원";
                }
            } else {
                // 보유 정보가 없으면 0으로 표시
                const profitElement = myStockCard.querySelector(".my-stock-card__profit");
                if (profitElement) {
                    profitElement.className = "my-stock-card__profit my-stock-card__profit--neutral";
                    profitElement.textContent = "0";
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
    const watchPriceInfo = document.getElementById("watch-price-info");
    const watchPriceMinusBtn = document.getElementById("watch-price-minus");
    const watchPricePlusBtn = document.getElementById("watch-price-plus");

    // 감시 가격 입력 필드 및 버튼 이벤트 설정
    setupWatchPriceInput(watchPriceInput, watchPriceInfo, watchPriceMinusBtn, watchPricePlusBtn);

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
                    attachConditionalOrderClickListeners();
                }
            });
        });
        
        observer.observe(waitOrdersContainer, {
            childList: true,
            subtree: true
        });
    }

    /* 조건 주문 편집 모달 */
    const conditionalOrderModal = document.getElementById("conditional-order-edit-modal");
    const conditionalOrderModalOverlay = conditionalOrderModal?.querySelector(".order-modal__overlay");
    const conditionalOrderModalBackBtn = conditionalOrderModal?.querySelector(".order-modal__back-btn");
    const conditionalOrderModalToggle = conditionalOrderModal?.querySelector("#conditional-order-edit-toggle");
    const conditionalOrderModalDeleteBtn = conditionalOrderModal?.querySelector("#conditional-order-edit-delete-btn");
    const conditionalOrderModalModifyBtn = conditionalOrderModal?.querySelector("#conditional-order-edit-modify-btn");

    // 모달 열기
    function openConditionalOrderModal(orderData) {
        if (!conditionalOrderModal) return;
        
        // 모달이 이미 열려있으면 위치 재계산하지 않음
        const isAlreadyOpen = conditionalOrderModal.getAttribute("aria-hidden") === "false";
        
        // order-card의 위치를 기준으로 모달 위치 계산 (처음 열 때만)
        if (!isAlreadyOpen) {
            const orderCard = document.querySelector(".order-card");
            if (orderCard) {
                const orderCardRect = orderCard.getBoundingClientRect();
                // order-card의 왼쪽에 모달 배치 (모달 너비 300px)
                conditionalOrderModal.style.left = `${orderCardRect.left - 300}px`;
                conditionalOrderModal.style.top = `${orderCardRect.top + 120}px`;
            }
        }
        
        // 모달 데이터 채우기
        if (orderData) {
            // 모달 제목 설정
            const stockNameElement = document.getElementById("conditional-order-modal-stock-name");
            const tradeTypeElement = document.getElementById("conditional-order-modal-trade-type");
            if (stockNameElement && tradeTypeElement) {
                // 종목명은 주문 카드에서 가져오거나 기본값 사용
                const stockName = document.querySelector(".order-card")?.getAttribute("data-stock-name") || "종목";
                const tradeTypeText = orderData.tradeType === "buy" ? "조건 구매" : "조건 판매";
                stockNameElement.textContent = stockName + " ";
                tradeTypeElement.textContent = tradeTypeText;
            }
            
            // 주문 켜기 토글
            if (conditionalOrderModalToggle) {
                const isActive = orderData.isActive === true || orderData.isActive === "true";
                conditionalOrderModalToggle.setAttribute("data-is-active", isActive);
            }
            
            // 감시 가격 (유저가 설정한 감시가격을 기본값으로 설정)
            const watchPriceInput = document.getElementById("conditional-order-edit-watch-price");
            if (watchPriceInput) {
                // orderData.watchPrice가 유효한 숫자인지 확인
                if (orderData.watchPrice != null && !isNaN(orderData.watchPrice) && orderData.watchPrice > 0) {
                    watchPriceInput.value = formatNumber(orderData.watchPrice);
                } else {
                    watchPriceInput.value = ""; // 값이 없거나 유효하지 않으면 빈 문자열
                }
            }
            
            // 주문 가격
            const priceInput = document.getElementById("conditional-order-edit-price");
            if (priceInput && orderData.price) {
                priceInput.value = formatNumber(orderData.price);
            }
            
            // 주문 수량
            const quantityInput = document.getElementById("conditional-order-edit-quantity");
            if (quantityInput && orderData.quantity) {
                quantityInput.value = orderData.quantity;
            }
            
            // 주문 유형 (지정가/시장가)
            const priceModePills = conditionalOrderModal?.querySelectorAll("[data-price-mode]");
            if (priceModePills && orderData.orderType) {
                priceModePills.forEach(pill => {
                    const isLimit = orderData.orderType === "LIMIT";
                    if (pill.getAttribute("data-price-mode") === (isLimit ? "limit" : "market")) {
                        pill.classList.add("order-card__pill--active");
                        pill.setAttribute("aria-checked", "true");
                        pill.setAttribute("data-state", "checked");
                    } else {
                        pill.classList.remove("order-card__pill--active");
                        pill.setAttribute("aria-checked", "false");
                        pill.setAttribute("data-state", "unchecked");
                    }
                });
            }
            
            // 주문 ID 저장
            conditionalOrderModal.setAttribute("data-order-id", orderData.orderId || "");
        }
        
        conditionalOrderModal.setAttribute("aria-hidden", "false");
        document.body.style.overflow = "hidden";
    }

    // 모달 닫기
    function closeConditionalOrderModal() {
        if (!conditionalOrderModal) return;
        conditionalOrderModal.setAttribute("aria-hidden", "true");
        document.body.style.overflow = "";
    }

    // 모달 닫기 이벤트
    // overlay는 제거되었으므로 클릭 이벤트 제거
    // if (conditionalOrderModalOverlay) {
    //     conditionalOrderModalOverlay.addEventListener("click", closeConditionalOrderModal);
    // }
    if (conditionalOrderModalBackBtn) {
        conditionalOrderModalBackBtn.addEventListener("click", closeConditionalOrderModal);
    }
    
    // 모달 다이얼로그 클릭 시 이벤트 전파 중지 (다이얼로그 내부 클릭은 모달이 닫히지 않도록)
    const modalDialog = conditionalOrderModal?.querySelector(".order-modal__dialog");
    if (modalDialog) {
        modalDialog.addEventListener("click", function(e) {
            e.stopPropagation();
        });
    }
    
    // 모달 외부 클릭 시 닫히도록 처리 (document 레벨에서 감지)
    // capture phase에서 실행하여 다른 이벤트보다 먼저 처리
    document.addEventListener("click", function(e) {
        // 모달이 열려있을 때만 처리
        if (conditionalOrderModal && conditionalOrderModal.getAttribute("aria-hidden") === "false") {
            // 모달 다이얼로그 내부 클릭이 아닌 경우에만 닫기
            const clickedInsideDialog = e.target.closest(".order-modal__dialog");
            // 조건 주문 아이템 클릭도 제외 (모달을 여는 클릭)
            const clickedConditionalOrderItem = e.target.closest(".order-card__wait-order-item--conditional");
            if (!clickedInsideDialog && !clickedConditionalOrderItem) {
                closeConditionalOrderModal();
            }
        }
    }, true); // capture phase 사용

    // ESC 키로 모달 닫기
    document.addEventListener("keydown", function(e) {
        if (e.key === "Escape" && conditionalOrderModal?.getAttribute("aria-hidden") === "false") {
            closeConditionalOrderModal();
        }
    });

    // 주문 켜기 토글 이벤트
    if (conditionalOrderModalToggle) {
        conditionalOrderModalToggle.addEventListener("click", async function() {
            const orderId = conditionalOrderModal?.getAttribute("data-order-id");
            if (!orderId) return;
            
            const currentState = this.getAttribute("data-is-active") === "true";
            const newState = !currentState;
            
            try {
                const params = new URLSearchParams();
                params.append("orderId", orderId);
                params.append("action", "toggle");
                params.append("isActive", newState);
                
                const response = await fetch("/api/order?" + params.toString(), {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
                    }
                });
                
                let result;
                try {
                    result = await response.json();
                } catch (e) {
                    console.error("[조건 주문 토글] JSON 파싱 오류:", e);
                    alert("서버 응답을 처리할 수 없습니다.");
                    return;
                }
                
                if (result.success) {
                    this.setAttribute("data-is-active", newState);
                    // 모달이 열려있으면 아이템의 상태도 업데이트
                    const item = document.querySelector(`[data-order-id="${orderId}"].order-card__wait-order-item--conditional`);
                    if (item) {
                        item.setAttribute("data-is-active", newState);
                        const itemToggle = item.querySelector(".order-card__wait-order-toggle");
                        if (itemToggle) {
                            itemToggle.setAttribute("data-is-active", newState);
                        }
                    }
                } else {
                    alert(result.message || "조건 주문 활성화 상태 변경에 실패했습니다.");
                }
            } catch (error) {
                console.error("[조건 주문 토글] 네트워크 오류:", error);
                alert("조건 주문 활성화 상태 변경 중 오류가 발생했습니다.");
            }
        });
    }

    // 삭제하기 버튼 이벤트
    if (conditionalOrderModalDeleteBtn) {
        conditionalOrderModalDeleteBtn.addEventListener("click", async function() {
            const orderId = conditionalOrderModal?.getAttribute("data-order-id");
            if (!orderId) return;
            
            if (!window.confirm("정말로 이 조건 주문을 삭제하시겠습니까?")) {
                return;
            }
            
            try {
                const params = new URLSearchParams();
                params.append("orderId", orderId);
                
                const response = await fetch("/api/order?" + params.toString(), {
                    method: "DELETE",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
                    }
                });
                
                let result;
                try {
                    result = await response.json();
                } catch (e) {
                    console.error("[조건 주문 삭제] JSON 파싱 오류:", e);
                    alert("서버 응답을 처리할 수 없습니다.");
                    return;
                }
                
                if (result.success) {
                    alert(result.message || "조건 주문이 성공적으로 삭제되었습니다.");
                    closeConditionalOrderModal();
                    // 페이지 새로고침하여 대기 주문 목록 업데이트
                    window.location.reload();
                } else {
                    alert(result.message || "조건 주문 삭제에 실패했습니다.");
                }
            } catch (error) {
                console.error("[조건 주문 삭제] 네트워크 오류:", error);
                if (error instanceof TypeError && error.message.includes("fetch")) {
                    alert("네트워크 연결을 확인해주세요.");
                } else {
                    alert("조건 주문 삭제 중 오류가 발생했습니다: " + error.message);
                }
            }
        });
    }

    // 수정하기 버튼 이벤트
    if (conditionalOrderModalModifyBtn) {
        // form submit 이벤트 방지
        const conditionalOrderForm = conditionalOrderModal?.querySelector(".order-modal__form");
        if (conditionalOrderForm) {
            conditionalOrderForm.addEventListener("submit", function(e) {
                e.preventDefault();
                return false;
            });
        }
        
        conditionalOrderModalModifyBtn.addEventListener("click", async function(e) {
            e.preventDefault();
            const orderId = conditionalOrderModal?.getAttribute("data-order-id");
            if (!orderId) return;
            
            const watchPriceInput = document.getElementById("conditional-order-edit-watch-price");
            const priceInput = document.getElementById("conditional-order-edit-price");
            const quantityInput = document.getElementById("conditional-order-edit-quantity");
            const isActive = conditionalOrderModalToggle?.getAttribute("data-is-active") === "true";
            
            const watchPrice = watchPriceInput?.value?.replace(/,/g, "");
            const price = priceInput?.value?.replace(/,/g, "");
            const quantity = quantityInput?.value;
            
            // 유효성 검증
            if (watchPrice && (isNaN(parseFloat(watchPrice)) || parseFloat(watchPrice) <= 0)) {
                alert("올바른 감시 가격을 입력해주세요.");
                return;
            }
            
            if (price && (isNaN(parseFloat(price)) || parseFloat(price) <= 0)) {
                alert("올바른 가격을 입력해주세요.");
                return;
            }
            
            if (quantity && (isNaN(parseInt(quantity)) || parseInt(quantity) <= 0)) {
                alert("올바른 수량을 입력해주세요.");
                return;
            }
            
            try {
                const params = new URLSearchParams();
                params.append("orderId", orderId);
                if (watchPrice) params.append("watchPrice", watchPrice);
                if (price) params.append("price", price);
                if (quantity) params.append("quantity", quantity);
                params.append("isActive", isActive);
                
                const response = await fetch("/api/order?" + params.toString(), {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
                    }
                });
                
                let result;
                try {
                    result = await response.json();
                } catch (e) {
                    console.error("[조건 주문 수정] JSON 파싱 오류:", e);
                    alert("서버 응답을 처리할 수 없습니다.");
                    return;
                }
                
                if (result.success) {
                    alert(result.message || "조건 주문이 성공적으로 수정되었습니다.");
                    closeConditionalOrderModal();
                    // 페이지 새로고침하여 대기 주문 목록 업데이트
                    window.location.reload();
                } else {
                    alert(result.message || "조건 주문 수정에 실패했습니다.");
                }
            } catch (error) {
                console.error("[조건 주문 수정] 네트워크 오류:", error);
                if (error instanceof TypeError && error.message.includes("fetch")) {
                    alert("네트워크 연결을 확인해주세요.");
                } else {
                    alert("조건 주문 수정 중 오류가 발생했습니다: " + error.message);
                }
            }
        });
    }

    // 조건 주문 아이템 클릭 이벤트
    function attachConditionalOrderClickListeners() {
        const conditionalOrderItems = document.querySelectorAll(".order-card__wait-order-item--conditional");
        
        conditionalOrderItems.forEach(function(item) {
            // 이미 이벤트 리스너가 연결되어 있는지 확인
            if (item.hasAttribute("data-listener-attached")) {
                return;
            }
            
            item.setAttribute("data-listener-attached", "true");
            
            // 토글 버튼은 클릭 이벤트 전파 방지
            const toggleBtn = item.querySelector(".order-card__wait-order-toggle");
            if (toggleBtn) {
                toggleBtn.addEventListener("click", async function(e) {
                    e.stopPropagation();
                    const orderId = item.getAttribute("data-order-id");
                    if (!orderId) return;
                    
                    const currentState = this.getAttribute("data-is-active") === "true";
                    const newState = !currentState;
                    
                    try {
                        const params = new URLSearchParams();
                        params.append("orderId", orderId);
                        params.append("action", "toggle");
                        params.append("isActive", newState);
                        
                        const response = await fetch("/api/order?" + params.toString(), {
                            method: "PUT",
                            headers: {
                                "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
                            }
                        });
                        
                        let result;
                        try {
                            result = await response.json();
                        } catch (e) {
                            console.error("[조건 주문 토글] JSON 파싱 오류:", e);
                            alert("서버 응답을 처리할 수 없습니다.");
                            return;
                        }
                        
                        if (result.success) {
                            this.setAttribute("data-is-active", newState);
                            item.setAttribute("data-is-active", newState);
                            // 모달이 열려있으면 모달의 토글도 업데이트
                            const modalOrderId = conditionalOrderModal?.getAttribute("data-order-id");
                            if (modalOrderId === orderId && conditionalOrderModalToggle) {
                                conditionalOrderModalToggle.setAttribute("data-is-active", newState);
                            }
                        } else {
                            alert(result.message || "조건 주문 활성화 상태 변경에 실패했습니다.");
                        }
                    } catch (error) {
                        console.error("[조건 주문 토글] 네트워크 오류:", error);
                        alert("조건 주문 활성화 상태 변경 중 오류가 발생했습니다.");
                    }
                });
            }
            
            // 아이템 클릭 시 모달 열기
            item.addEventListener("click", function(e) {
                // 토글 버튼 클릭이 아닌 경우에만 모달 열기
                if (e.target.closest(".order-card__wait-order-toggle")) {
                    return;
                }
                
                // 이벤트 전파 중지 (document 클릭 이벤트와 충돌 방지)
                e.stopPropagation();
                
                const orderId = this.getAttribute("data-order-id");
                const tradeType = this.getAttribute("data-trade-type");
                const isActive = this.getAttribute("data-is-active") === "true";
                
                // 주문 데이터 수집
                const watchPriceText = this.querySelector(".order-card__wait-order-watch-price")?.textContent;
                const detailsText = this.querySelector(".order-card__wait-order-details")?.textContent;
                
                // 감시 가격 추출 (텍스트에서 숫자만 추출)
                let watchPrice = null;
                if (watchPriceText) {
                    const match = watchPriceText.match(/[\d,]+/);
                    if (match) {
                        const parsed = parseInt(match[0].replace(/,/g, ""));
                        // NaN 체크 추가
                        if (!isNaN(parsed)) {
                            watchPrice = parsed;
                        }
                    }
                }
                
                // 주문 가격과 수량 추출
                let price = null;
                let quantity = null;
                if (detailsText) {
                    const quantityMatch = detailsText.match(/([\d,]+)주/);
                    const priceMatch = detailsText.match(/주문가\s*([\d,]+)원/);
                    if (quantityMatch) {
                        const parsed = parseInt(quantityMatch[1].replace(/,/g, ""));
                        if (!isNaN(parsed)) {
                            quantity = parsed;
                        }
                    }
                    if (priceMatch) {
                        const parsed = parseInt(priceMatch[1].replace(/,/g, ""));
                        if (!isNaN(parsed)) {
                            price = parsed;
                        }
                    }
                }
                
                const orderData = {
                    orderId: orderId,
                    tradeType: tradeType,
                    watchPrice: watchPrice,
                    price: price,
                    quantity: quantity,
                    orderType: "LIMIT", // 기본값 (실제로는 DB에서 가져와야 함)
                    isActive: isActive
                };
                
                openConditionalOrderModal(orderData);
            });
        });
    }

    // 초기 조건 주문 클릭 이벤트 리스너 연결
    attachConditionalOrderClickListeners();

    /* 일반 주문 편집 모달 */
    const generalOrderModal = document.getElementById("general-order-edit-modal");
    const generalOrderModalOverlay = generalOrderModal?.querySelector(".order-modal__overlay");
    const generalOrderModalCloseBtn = generalOrderModal?.querySelector(".order-modal__close-btn");
    const generalOrderModalModifyBtn = generalOrderModal?.querySelector("#general-order-edit-modify-btn");
    const generalOrderPriceInput = document.getElementById("general-order-edit-price");
    const generalOrderQuantityInput = document.getElementById("general-order-edit-quantity");
    const generalOrderTotalAmount = document.getElementById("general-order-edit-total-amount");
    const generalOrderPricePills = generalOrderModal?.querySelectorAll(".order-card__pill[data-price-mode]");
    const generalOrderPriceMinusBtn = document.getElementById("general-order-edit-price-minus");
    const generalOrderPricePlusBtn = document.getElementById("general-order-edit-price-plus");
    const generalOrderQuantityMinusBtn = document.getElementById("general-order-edit-quantity-minus");
    const generalOrderQuantityPlusBtn = document.getElementById("general-order-edit-quantity-plus");

    // 모달 열기
    function openGeneralOrderModal(orderData) {
        if (!generalOrderModal) return;
        
        // 모달 데이터 채우기
        if (orderData) {
            // 모달 제목 설정
            const tradeTypeElement = document.getElementById("general-order-modal-trade-type");
            const quantityElement = document.getElementById("general-order-modal-quantity");
            const pricePerShareElement = document.getElementById("general-order-modal-price-per-share");
            
            if (tradeTypeElement && quantityElement) {
                const tradeTypeText = orderData.tradeType === "buy" ? "구매" : "판매";
                tradeTypeElement.textContent = tradeTypeText;
                quantityElement.textContent = formatNumber(orderData.quantity) + "주";
            }
            
            if (pricePerShareElement) {
                pricePerShareElement.textContent = "주당 " + formatNumber(orderData.price) + "원";
            }
            
            // 모달에 거래 타입 설정
            generalOrderModal.setAttribute("data-trade-type", orderData.tradeType);
            generalOrderModal.setAttribute("data-order-id", orderData.orderId);
            
            // 가격 라벨 동적 변경
            const priceLabel = generalOrderModal?.querySelector("#general-order-edit-price-mode-label");
            if (priceLabel) {
                priceLabel.textContent = orderData.tradeType === "buy" ? "구매가격" : "판매가격";
            }
            
            // 가격 입력 필드 설정
            if (generalOrderPriceInput) {
                generalOrderPriceInput.value = formatNumber(orderData.price);
            }
            
            // 수량 입력 필드 설정
            if (generalOrderQuantityInput) {
                generalOrderQuantityInput.value = orderData.quantity;
            }
            
            // 주문 타입 설정 (LIMIT / MARKET)
            if (orderData.orderType === "MARKET") {
                // 시장가 선택
                const marketPill = generalOrderModal?.querySelector('.order-card__pill[data-price-mode="market"]');
                const limitPill = generalOrderModal?.querySelector('.order-card__pill[data-price-mode="limit"]');
                if (marketPill && limitPill) {
                    limitPill.classList.remove("order-card__pill--active");
                    limitPill.setAttribute("aria-checked", "false");
                    limitPill.setAttribute("data-state", "unchecked");
                    marketPill.classList.add("order-card__pill--active");
                    marketPill.setAttribute("aria-checked", "true");
                    marketPill.setAttribute("data-state", "checked");
                    if (generalOrderPriceInput) {
                        generalOrderPriceInput.disabled = true;
                        generalOrderPriceInput.value = "";
                    }
                }
            } else {
                // 지정가 선택
                const marketPill = generalOrderModal?.querySelector('.order-card__pill[data-price-mode="market"]');
                const limitPill = generalOrderModal?.querySelector('.order-card__pill[data-price-mode="limit"]');
                if (marketPill && limitPill) {
                    marketPill.classList.remove("order-card__pill--active");
                    marketPill.setAttribute("aria-checked", "false");
                    marketPill.setAttribute("data-state", "unchecked");
                    limitPill.classList.add("order-card__pill--active");
                    limitPill.setAttribute("aria-checked", "true");
                    limitPill.setAttribute("data-state", "checked");
                    if (generalOrderPriceInput) {
                        generalOrderPriceInput.disabled = false;
                    }
                }
            }
            
            // 총 주문 금액 계산
            updateGeneralOrderTotalAmount();
            
            // 버튼 텍스트 업데이트
            updateGeneralOrderModalButtonText();
        }
        
        // 모달 표시
        generalOrderModal.setAttribute("aria-hidden", "false");
        document.body.style.overflow = "hidden";
    }

    // 모달 닫기
    function closeGeneralOrderModal() {
        if (!generalOrderModal) return;
        generalOrderModal.setAttribute("aria-hidden", "true");
        document.body.style.overflow = "";
    }

    // 총 주문 금액 업데이트
    function updateGeneralOrderTotalAmount() {
        if (!generalOrderTotalAmount || !generalOrderPriceInput || !generalOrderQuantityInput) return;
        
        const priceText = generalOrderPriceInput.value.replace(/,/g, "");
        const quantityText = generalOrderQuantityInput.value.replace(/,/g, "");
        
        const price = parseFloat(priceText) || 0;
        const quantity = parseInt(quantityText) || 0;
        
        const total = price * quantity;
        generalOrderTotalAmount.textContent = formatNumber(total) + "원";
    }

    // 모달 닫기 이벤트
    if (generalOrderModalOverlay) {
        generalOrderModalOverlay.addEventListener("click", closeGeneralOrderModal);
    }
    if (generalOrderModalCloseBtn) {
        generalOrderModalCloseBtn.addEventListener("click", closeGeneralOrderModal);
    }

    // ESC 키로 모달 닫기
    document.addEventListener("keydown", function(e) {
        if (e.key === "Escape" && generalOrderModal?.getAttribute("aria-hidden") === "false") {
            closeGeneralOrderModal();
        }
    });

    // 가격 pill 버튼 클릭 이벤트
    if (generalOrderPricePills) {
        generalOrderPricePills.forEach(function(pill) {
            pill.addEventListener("click", function() {
                const priceMode = this.getAttribute("data-price-mode");
                const allPills = generalOrderModal.querySelectorAll(".order-card__pill[data-price-mode]");
                
                allPills.forEach(function(p) {
                    p.classList.remove("order-card__pill--active");
                    p.setAttribute("aria-checked", "false");
                    p.setAttribute("data-state", "unchecked");
                });
                
                this.classList.add("order-card__pill--active");
                this.setAttribute("aria-checked", "true");
                this.setAttribute("data-state", "checked");
                
                if (priceMode === "market") {
                    if (generalOrderPriceInput) {
                        generalOrderPriceInput.disabled = true;
                        generalOrderPriceInput.value = "";
                    }
                } else {
                    if (generalOrderPriceInput) {
                        generalOrderPriceInput.disabled = false;
                        // 현재가로 설정
                        const currentPrice = generalOrderPriceInput.getAttribute("data-current-price");
                        if (currentPrice) {
                            generalOrderPriceInput.value = formatNumber(parseInt(currentPrice));
                        }
                    }
                }
                
                updateGeneralOrderTotalAmount();
            });
        });
    }

    // 가격 입력 필드 이벤트
    if (generalOrderPriceInput) {
        generalOrderPriceInput.addEventListener("input", function() {
            let value = this.value.replace(/,/g, "");
            if (value && !isNaN(value)) {
                this.value = formatNumber(parseInt(value));
            }
            updateGeneralOrderTotalAmount();
        });
        
        generalOrderPriceInput.addEventListener("blur", function() {
            let value = this.value.replace(/,/g, "");
            if (value && !isNaN(value)) {
                this.value = formatNumber(parseInt(value));
            }
        });
    }

    // 가격 +/- 버튼 이벤트
    if (generalOrderPriceMinusBtn && generalOrderPriceInput) {
        generalOrderPriceMinusBtn.addEventListener("click", function() {
            if (generalOrderPriceInput.disabled) return;
            let value = parseInt(generalOrderPriceInput.value.replace(/,/g, "")) || 0;
            if (value > 0) {
                value = Math.max(0, value - 1);
                generalOrderPriceInput.value = formatNumber(value);
                updateGeneralOrderTotalAmount();
            }
        });
    }

    if (generalOrderPricePlusBtn && generalOrderPriceInput) {
        generalOrderPricePlusBtn.addEventListener("click", function() {
            if (generalOrderPriceInput.disabled) return;
            let value = parseInt(generalOrderPriceInput.value.replace(/,/g, "")) || 0;
            value += 1;
            generalOrderPriceInput.value = formatNumber(value);
            updateGeneralOrderTotalAmount();
        });
    }

    // 수량 입력 필드 이벤트
    if (generalOrderQuantityInput) {
        generalOrderQuantityInput.addEventListener("input", function() {
            let value = this.value.replace(/,/g, "");
            if (value && !isNaN(value)) {
                const numValue = parseInt(value);
                const maxQuantity = parseInt(this.getAttribute("data-max-quantity")) || 0;
                if (maxQuantity > 0 && numValue > maxQuantity) {
                    this.value = maxQuantity;
                } else {
                    this.value = numValue;
                }
            }
            updateGeneralOrderTotalAmount();
        });
    }

    // 수량 +/- 버튼 이벤트
    if (generalOrderQuantityMinusBtn && generalOrderQuantityInput) {
        generalOrderQuantityMinusBtn.addEventListener("click", function() {
            let value = parseInt(generalOrderQuantityInput.value.replace(/,/g, "")) || 0;
            if (value > 1) {
                value -= 1;
                generalOrderQuantityInput.value = value;
                updateGeneralOrderTotalAmount();
            }
        });
    }

    if (generalOrderQuantityPlusBtn && generalOrderQuantityInput) {
        generalOrderQuantityPlusBtn.addEventListener("click", function() {
            let value = parseInt(generalOrderQuantityInput.value.replace(/,/g, "")) || 0;
            const maxQuantity = parseInt(generalOrderQuantityInput.getAttribute("data-max-quantity")) || 0;
            if (maxQuantity > 0 && value >= maxQuantity) {
                return;
            }
            value += 1;
            generalOrderQuantityInput.value = value;
            updateGeneralOrderTotalAmount();
        });
    }

    // 수정하기 버튼 텍스트 동적 변경
    function updateGeneralOrderModalButtonText() {
        if (!generalOrderModalModifyBtn) return;
        const tradeType = generalOrderModal?.getAttribute("data-trade-type");
        if (tradeType === "buy") {
            generalOrderModalModifyBtn.textContent = "구매 수정하기";
        } else if (tradeType === "sell") {
            generalOrderModalModifyBtn.textContent = "판매 수정하기";
        } else {
            generalOrderModalModifyBtn.textContent = "수정하기";
        }
    }

    // 수정하기 버튼 이벤트
    if (generalOrderModalModifyBtn) {
        // form submit 이벤트 방지
        const generalOrderForm = generalOrderModal?.querySelector(".order-modal__form");
        if (generalOrderForm) {
            generalOrderForm.addEventListener("submit", function(e) {
                e.preventDefault();
                return false;
            });
        }
        
        generalOrderModalModifyBtn.addEventListener("click", async function(e) {
            e.preventDefault();
            const orderId = generalOrderModal?.getAttribute("data-order-id");
            if (!orderId) return;
            
            const priceMode = generalOrderModal?.querySelector('.order-card__pill[data-state="checked"]')?.getAttribute("data-price-mode");
            const priceText = generalOrderPriceInput?.value?.replace(/,/g, "") || "";
            const quantityText = generalOrderQuantityInput?.value?.replace(/,/g, "") || "";
            
            // 유효성 검증
            if (priceMode === "limit" && (!priceText || isNaN(parseFloat(priceText)) || parseFloat(priceText) <= 0)) {
                alert("올바른 가격을 입력해주세요.");
                return;
            }
            
            if (!quantityText || isNaN(parseInt(quantityText)) || parseInt(quantityText) <= 0) {
                alert("올바른 수량을 입력해주세요.");
                return;
            }
            
            try {
                const params = new URLSearchParams();
                params.append("orderId", orderId);
                params.append("orderType", priceMode === "market" ? "MARKET" : "LIMIT");
                if (priceMode === "limit" && priceText) {
                    params.append("price", priceText);
                }
                params.append("quantity", quantityText);
                
                const response = await fetch("/api/order?" + params.toString(), {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
                    }
                });
                
                let result;
                try {
                    result = await response.json();
                } catch (e) {
                    console.error("[일반 주문 수정] JSON 파싱 오류:", e);
                    alert("서버 응답을 처리할 수 없습니다.");
                    return;
                }
                
                if (result.success) {
                    alert(result.message || "주문이 성공적으로 수정되었습니다.");
                    closeGeneralOrderModal();
                    // 페이지 새로고침하여 대기 주문 목록 업데이트
                    window.location.reload();
                } else {
                    alert(result.message || "주문 수정에 실패했습니다.");
                }
            } catch (error) {
                console.error("[일반 주문 수정] 네트워크 오류:", error);
                if (error instanceof TypeError && error.message.includes("fetch")) {
                    alert("네트워크 연결을 확인해주세요.");
                } else {
                    alert("주문 수정 중 오류가 발생했습니다: " + error.message);
                }
            }
        });
    }

    // 일반 주문 수정 버튼 클릭 이벤트
    function attachGeneralOrderEditButtonListeners() {
        const editOrderButtons = document.querySelectorAll(".order-card__wait-order-btn--edit");
        
        editOrderButtons.forEach(function(button) {
            // 이미 이벤트 리스너가 연결되어 있는지 확인
            if (button.hasAttribute("data-listener-attached")) {
                return;
            }
            
            button.setAttribute("data-listener-attached", "true");
            button.addEventListener("click", function(e) {
                e.stopPropagation();
                
                const orderId = this.getAttribute("data-order-id");
                if (!orderId) {
                    alert("주문 ID를 찾을 수 없습니다.");
                    return;
                }
                
                // 주문 아이템 찾기
                const orderItem = this.closest(".order-card__wait-order-item");
                if (!orderItem) {
                    alert("주문 정보를 찾을 수 없습니다.");
                    return;
                }
                
                // 주문 정보 추출
                const tradeTypeElement = orderItem.querySelector(".order-card__wait-order-type");
                const priceElement = orderItem.querySelector(".order-card__wait-order-price");
                
                let tradeType = "buy";
                if (tradeTypeElement) {
                    const typeText = tradeTypeElement.textContent.trim();
                    tradeType = typeText.includes("판매") ? "sell" : "buy";
                }
                
                let quantity = 0;
                let price = 0;
                
                if (tradeTypeElement) {
                    const quantityMatch = tradeTypeElement.textContent.match(/([\d,]+)주/);
                    if (quantityMatch) {
                        quantity = parseInt(quantityMatch[1].replace(/,/g, "")) || 0;
                    }
                }
                
                if (priceElement) {
                    const priceMatch = priceElement.textContent.match(/주당\s*([\d,]+)원/);
                    if (priceMatch) {
                        price = parseInt(priceMatch[1].replace(/,/g, "")) || 0;
                    }
                }
                
                // 주문 타입 확인 (일반 주문은 LIMIT 또는 MARKET)
                // 기본값은 LIMIT로 설정 (실제로는 DB에서 가져와야 함)
                let orderType = "LIMIT";
                
                const orderData = {
                    orderId: orderId,
                    tradeType: tradeType,
                    price: price,
                    quantity: quantity,
                    orderType: orderType
                };
                
                openGeneralOrderModal(orderData);
            });
        });
    }

    // 초기 일반 주문 수정 버튼 이벤트 리스너 연결
    attachGeneralOrderEditButtonListeners();
    
    // 동적으로 추가된 수정 버튼을 위한 MutationObserver
    if (waitOrdersContainer) {
        const observer = new MutationObserver(function(mutations) {
            mutations.forEach(function(mutation) {
                if (mutation.addedNodes.length > 0) {
                    attachGeneralOrderEditButtonListeners();
                }
            });
        });
        
        observer.observe(waitOrdersContainer, {
            childList: true,
            subtree: true
        });
    }

});
