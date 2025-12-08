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

document.addEventListener("DOMContentLoaded", function handleStockPageInit() {

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

    // 가격/수량 증감 버튼 및 입력 필드 이벤트 설정
    setupPriceStepButtons(priceMinusBtn, pricePlusBtn, priceInputElement, quantityInputElement, totalAmountElement);
    setupQuantityStepButtons(quantityMinusBtn, quantityPlusBtn, quantityInputElement, priceInputElement, totalAmountElement);
    setupPriceInput(priceInputElement, quantityInputElement, totalAmountElement);
    setupQuantityInput(quantityInputElement, priceInputElement, totalAmountElement);
    setupSubmitButton(submitBtn, priceInputElement, quantityInputElement, orderTypeHidden);

    // 초기화
    initAvailableAmount(availableAmountElement);
    initCurrentPrice(priceInputElement, orderTypeHidden);
    initQuantity(quantityInputElement);
    
    // 현재가 설정 후 총 금액 계산
    if (priceInputElement && orderTypeHidden && orderTypeHidden.value === "LIMIT") {
        const currentPrice = priceInputElement.getAttribute("data-current-price");
        if (currentPrice && priceInputElement.value) {
            calculateTotalAmount();
        }
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
    const watchPriceInfo = document.getElementById("watch-price-info");
    const watchPriceMinusBtn = document.getElementById("watch-price-minus");
    const watchPricePlusBtn = document.getElementById("watch-price-plus");

    // 감시 가격 입력 필드 및 버튼 이벤트 설정
    setupWatchPriceInput(watchPriceInput, watchPriceInfo, watchPriceMinusBtn, watchPricePlusBtn);

});
