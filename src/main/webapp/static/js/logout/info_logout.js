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

    if (tradeTabs.length && priceLabel && amountLabel && submitButton) {
        tradeTabs.forEach(function attachTradeTab(tab) {
            tab.addEventListener("click", function onTradeTabClick() {

                tradeTabs.forEach(function removeActive(t) {
                    t.classList.remove("order-card__trade-tab--active");
                });
                tab.classList.add("order-card__trade-tab--active");

                const text = tab.textContent.trim();

                if (text.includes("판매")) {
                    // 판매 탭
                    priceLabel.textContent = "판매가격";
                    amountLabel.textContent = "판매가능 금액";
                    submitButton.textContent = "로그인하고 판매하기";
                } else if (text.includes("구매")) {
                    // 구매 탭
                    priceLabel.textContent = "구매가격";
                    amountLabel.textContent = "구매가능 금액";
                    submitButton.textContent = "로그인하고 구매하기";
                }
                else if (text.includes("대기")) {
                    // 대기 탭
                    priceLabel.textContent = "대기가격";
                    amountLabel.textContent = "대기가능 금액";
                    submitButton.textContent = "로그인하고 대기하기";
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

    const qtyInput = document.querySelector(
        ".order-card__field--qty .order-card__input"
    );

    const qtyStepBtn = document.querySelector(
        ".order-card__field--qty .order-card__step-btn"
    );


    const unitText = document.querySelector(".order-card__field--price .unit-text");

    if (priceModePills.length && priceInput) {
        priceModePills.forEach(function (pill) {
            pill.addEventListener("click", function () {

                // pill 활성화 변경
                priceModePills.forEach(p =>
                    p.classList.remove("order-card__pill--active")
                );
                pill.classList.add("order-card__pill--active");

                const mode = pill.textContent.trim();

                if (mode === "시장가") {
                    // 시장가 모드
                    priceInput.value = "";
                    priceInput.readOnly = true;
                    priceInput.placeholder = "시장가로 주문됩니다";
                    priceInput.classList.add("order-card__input--readonly");

                    if (orderTypeHidden) orderTypeHidden.value = "MARKET";

                    if (unitText) unitText.style.display = "none";


                } else {
                    // 지정가 모드
                    priceInput.value = "";
                    priceInput.readOnly = true;
                    priceInput.placeholder = "로그인하고 입력";
                    priceInput.classList.add("order-card__input--readonly");

                    if (orderTypeHidden) orderTypeHidden.value = "LIMIT";

                    if (unitText) unitText.style.display = "none";
                }
            });
        });
    }


    if (priceInput) {
        priceInput.value = "";
        priceInput.readOnly = true;
        priceInput.placeholder = "로그인하고 입력";
        priceInput.classList.add("order-card__input--readonly");

        if (orderTypeHidden) orderTypeHidden.value = "LIMIT";

        if (unitText) unitText.style.display = "none";
    }


    if (qtyInput) {
        qtyInput.value = "";
        qtyInput.readOnly = true;
        qtyInput.placeholder = "로그인하고 입력";
        qtyInput.classList.add("order-card__input--readonly");
    }

    if (qtyStepBtn) {
        qtyStepBtn.disabled = true;      
        qtyStepBtn.classList.add("order-card__step-btn--disabled");
    }







});
