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
        
        if (isBuy) {
            // 구매: 예치금 / 주가
            const price = parseFloat(priceInputElement.value.replace(/,/g, "")) || 0;
            if (price > 0) {
                return Math.floor(availableCash / price);
            }
            return 0;
        } else {
            // 판매: 보유 수량
            return holdingQuantity;
        }
    }
    
    // 가격/수량 증감 버튼 및 입력 필드 이벤트 설정
    setupPriceStepButtons(priceMinusBtn, pricePlusBtn, priceInputElement, quantityInputElement, totalAmountElement);
    
    // 수량 관련 이벤트는 최대값을 동적으로 계산
    const updateQuantityMax = () => {
        const maxQty = calculateMaxQuantity();
        
        // 일반 주문 수량 필드
        setupQuantityStepButtons(quantityMinusBtn, quantityPlusBtn, quantityInputElement, priceInputElement, totalAmountElement, maxQty);
        setupQuantityInput(quantityInputElement, priceInputElement, totalAmountElement, maxQty);
        
        // 조건 주문 수량 필드 (구매)
        const conditionalBuyQuantityInput = document.getElementById("conditional-order-quantity-input");
        const conditionalBuyQuantityMinus = document.getElementById("conditional-order-quantity-minus");
        const conditionalBuyQuantityPlus = document.getElementById("conditional-order-quantity-plus");
        const conditionalBuyPriceInput = document.getElementById("conditional-order-price-input");
        const conditionalBuyTotalAmount = document.querySelector(".order-card__conditional-order .order-card__summary-value:last-child");
        
        if (conditionalBuyQuantityInput && conditionalBuyQuantityMinus && conditionalBuyQuantityPlus) {
            setupQuantityStepButtons(conditionalBuyQuantityMinus, conditionalBuyQuantityPlus, conditionalBuyQuantityInput, conditionalBuyPriceInput, conditionalBuyTotalAmount, maxQty);
            setupQuantityInput(conditionalBuyQuantityInput, conditionalBuyPriceInput, conditionalBuyTotalAmount, maxQty);
        }
        
        // 조건 주문 수량 필드 (판매)
        const conditionalSellQuantityInput = document.getElementById("conditional-sell-quantity-input");
        const conditionalSellQuantityMinus = document.getElementById("conditional-sell-quantity-minus");
        const conditionalSellQuantityPlus = document.getElementById("conditional-sell-quantity-plus");
        const conditionalSellPriceInput = document.getElementById("conditional-sell-price-input");
        const conditionalSellTotalAmount = document.querySelector(".order-card__conditional-order--sell .order-card__summary-value:last-child");
        
        if (conditionalSellQuantityInput && conditionalSellQuantityMinus && conditionalSellQuantityPlus) {
            setupQuantityStepButtons(conditionalSellQuantityMinus, conditionalSellQuantityPlus, conditionalSellQuantityInput, conditionalSellPriceInput, conditionalSellTotalAmount, maxQty);
            setupQuantityInput(conditionalSellQuantityInput, conditionalSellPriceInput, conditionalSellTotalAmount, maxQty);
        }
    };
    
    // 초기 설정
    updateQuantityMax();
    
    // 가격 변경 시 최대값 재계산
    priceInputElement.addEventListener("input", updateQuantityMax);
    priceInputElement.addEventListener("change", updateQuantityMax);
    
    // 조건 주문 가격 필드 변경 시 최대값 재계산
    const conditionalBuyPriceInput = document.getElementById("conditional-order-price-input");
    const conditionalSellPriceInput = document.getElementById("conditional-sell-price-input");
    if (conditionalBuyPriceInput) {
        conditionalBuyPriceInput.addEventListener("input", updateQuantityMax);
        conditionalBuyPriceInput.addEventListener("change", updateQuantityMax);
    }
    if (conditionalSellPriceInput) {
        conditionalSellPriceInput.addEventListener("input", updateQuantityMax);
        conditionalSellPriceInput.addEventListener("change", updateQuantityMax);
    }
    
    // 탭 전환 시 최대값 재계산
    const tradeTabs = document.querySelectorAll(".order-card__trade-tab");
    tradeTabs.forEach(tab => {
        tab.addEventListener("click", () => {
            setTimeout(updateQuantityMax, 100); // 탭 전환 후 DOM 업데이트 대기
        });
    });
    
    setupPriceInput(priceInputElement, quantityInputElement, totalAmountElement);
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
                    if (orderType === "conditional") {
                        // 조건 주문 필드가 표시될 때 구매/판매 가격 초기화
                        initializeConditionalOrderPriceFields();
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
        
        // 조건 주문 필드 초기화 후 최대값 재계산
        updateQuantityMax();
    }
    
    // 초기 로딩 시 조건 주문 가격 필드 초기화
    initializeConditionalOrderPriceFields();

});
