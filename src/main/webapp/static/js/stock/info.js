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
                    submitButton.textContent = "판매하기";
                } else if (text.includes("구매")) {
                    // 구매 탭
                    tab.classList.add("order-card__trade-tab--active");
                    tab.setAttribute("aria-checked", "true");
                    tab.setAttribute("data-state", "checked");
                    if (orderCard) orderCard.classList.add("order-card--buy");
                    priceLabel.textContent = "구매가격";
                    amountLabel.textContent = "구매가능 금액";
                    submitButton.textContent = "구매하기";
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

    // 시장가 / 지정가 모드 전환

    const priceModePills = document.querySelectorAll(
        ".order-card__field--split .order-card__pill"
    );
    const priceInput = document.querySelector(
        ".order-card__field--price .order-card__input"
    );
    const orderTypeHidden = document.querySelector(
        ".order-card__order-type-hidden"
    );

    const unitText = document.querySelector(".order-card__field--price .unit-text");

    if (priceModePills.length && priceInput) {
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
                    priceInput.placeholder = "최대한 빠른가격";
                    priceInput.classList.add("order-card__input--readonly");

                    if (orderTypeHidden) orderTypeHidden.value = "MARKET";

                    if (unitText) unitText.style.display = "none";


                } else {
                    // 지정가 모드
                    priceInput.readOnly = false;
                    priceInput.placeholder = "가격을 입력하세요";
                    priceInput.classList.remove("order-card__input--readonly");

                    if (orderTypeHidden) orderTypeHidden.value = "LIMIT";

                    if (unitText) unitText.style.display = "";
                }
            });
        });
    }

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


});
