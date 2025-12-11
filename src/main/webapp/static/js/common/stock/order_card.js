document.addEventListener("DOMContentLoaded", function handleStockPageInit() {

    const orderCard = document.querySelector(".order-card");
    const isLoggedIn =
        !!orderCard && orderCard.dataset.isLoggedIn === "true";

    /* =====  일/주/월/년 버튼 활성화 ===== */

    const rangeButtons = document.querySelectorAll(".stock-range-tabs__item");

    rangeButtons.forEach(function attachRangeClick(button) {
        button.addEventListener("click", function onRangeClick() {
            rangeButtons.forEach(function removeActiveClass(btn) {
                btn.classList.remove("stock-range-tabs__item--active");
            });
            button.classList.add("stock-range-tabs__item--active");
        });
    });


    /* =====  주문하기 탭 전환 (구매/판매/대기) ===== */

    const tradeTabs = document.querySelectorAll(".order-card__trade-tab");


    const priceLabel = Array.from(
        document.querySelectorAll(".order-card__label")
    ).find(function findPriceLabel(label) {
        const text = label.textContent.trim();
        return (
            text.includes("구매가격") ||
            text.includes("판매가격") ||
            text.includes("대기가격")
        );
    });


    const amountLabel = Array.from(
        document.querySelectorAll(".order-card__summary-label")
    ).find(function findAmountLabel(label) {
        const text = label.textContent.trim();
        return (
            text.includes("구매가능 금액") ||
            text.includes("판매가능 금액") ||
            text.includes("대기가능 금액")
        );
    });

    const submitButton = document.querySelector(".order-card__submit-btn");

    if (submitButton) {
        if (!isLoggedIn) {
            submitButton.classList.add("order-card__submit-btn--logged-out");
        } else {
            submitButton.classList.remove("order-card__submit-btn--logged-out");
        }
    }

    function updateTradeUIByTabText(tabText) {
        if (!priceLabel || !amountLabel || !submitButton) return;

        let baseText = "";

        if (tabText.includes("판매")) {
            priceLabel.textContent = "판매가격";
            amountLabel.textContent = "판매가능 금액";
            baseText = "판매하기";
        } else if (tabText.includes("대기")) {
            priceLabel.textContent = "대기가격";
            amountLabel.textContent = "대기가능 금액";
            baseText = "대기하기";
        } else {
            // 기본: 구매
            priceLabel.textContent = "구매가격";
            amountLabel.textContent = "구매가능 금액";
            baseText = "구매하기";
        }


        if (isLoggedIn) {
            submitButton.textContent = baseText;
        } else {
            submitButton.textContent = "로그인하고 " + baseText;
        }
    }

    if (tradeTabs.length && priceLabel && amountLabel && submitButton) {
        tradeTabs.forEach(function attachTradeTab(tab) {
            tab.addEventListener("click", function onTradeTabClick() {

                tradeTabs.forEach(function removeActive(t) {
                    t.classList.remove("order-card__trade-tab--active");
                });
                tab.classList.add("order-card__trade-tab--active");

                const text = tab.textContent.trim();
                updateTradeUIByTabText(text);
            });
        });

        // 초기 활성 탭 기준 세팅
        const initialActiveTab =
            document.querySelector(".order-card__trade-tab--active") ||
            tradeTabs[0];
        if (initialActiveTab) {
            updateTradeUIByTabText(initialActiveTab.textContent.trim());
        }
    }


    /* ===== 시장가 / 지정가 모드 전환 ===== */

    const priceModePills = document.querySelectorAll(
        ".order-card__field--split .order-card__pill"
    );
    const priceInput = document.querySelector(
        ".order-card__field--price .order-card__input"
    );
    const orderTypeHidden = document.querySelector(
        ".order-card__order-type-hidden"
    );
    const unitText = document.querySelector(
        ".order-card__field--price .unit-text"
    );

    // 수량 관련 요소 (로그인 여부에 따라 on/off)
    const qtyInput = document.querySelector(
        ".order-card__field--qty .order-card__input"
    );
    const qtyStepBtn = document.querySelector(
        ".order-card__field--qty .order-card__step-btn"
    );

    function applyLimitMode() {
        if (!priceInput) return;

        if (orderTypeHidden) orderTypeHidden.value = "LIMIT";

        if (isLoggedIn) {

            priceInput.value = "";
            priceInput.readOnly = false;
            priceInput.placeholder = "가격을 입력하세요";
            priceInput.classList.remove("order-card__input--readonly");

            if (unitText) unitText.style.display = "";
        } else {

            priceInput.value = "";
            priceInput.readOnly = true;
            priceInput.placeholder = "로그인하고 입력";
            priceInput.classList.add("order-card__input--readonly");

            if (unitText) unitText.style.display = "none";
        }
    }

    function applyMarketMode() {
        if (!priceInput) return;

        if (orderTypeHidden) orderTypeHidden.value = "MARKET";

        priceInput.value = "";
        priceInput.readOnly = true;
        priceInput.classList.add("order-card__input--readonly");

        if (isLoggedIn) {
            priceInput.placeholder = "시장가로 주문됩니다";
        } else {
            priceInput.placeholder = "로그인하고 입력";
        }

        if (unitText) unitText.style.display = "none";
    }

    if (priceModePills.length && priceInput) {
        priceModePills.forEach(function (pill) {
            pill.addEventListener("click", function () {

                // pill 활성화 변경
                priceModePills.forEach(function (p) {
                    p.classList.remove("order-card__pill--active");
                });
                pill.classList.add("order-card__pill--active");

                const mode = pill.textContent.trim();

                if (mode === "시장가") {
                    applyMarketMode();
                } else {
                    applyLimitMode();
                }
            });
        });

        // 초기 활성 pill 기준 세팅
        const initialActivePill =
            document.querySelector(".order-card__pill--active") ||
            priceModePills[0];
        if (initialActivePill) {
            const mode = initialActivePill.textContent.trim();
            if (mode === "시장가") {
                applyMarketMode();
            } else {
                applyLimitMode();
            }
        } else {
            // 아무 것도 활성화 안되어 있으면 기본 지정가
            applyLimitMode();
        }
    }


    /* ===== 수량 입력/버튼  ===== */

    if (qtyInput) {
        if (isLoggedIn) {
            qtyInput.readOnly = false;
            qtyInput.placeholder = "수량을 입력하세요";
            qtyInput.classList.remove("order-card__input--readonly");
        } else {
            qtyInput.value = "";
            qtyInput.readOnly = true;
            qtyInput.placeholder = "로그인하고 입력";
            qtyInput.classList.add("order-card__input--readonly");
        }
    }

    if (qtyStepBtn) {
        if (isLoggedIn) {
            qtyStepBtn.disabled = false;
            qtyStepBtn.classList.remove("order-card__step-btn--disabled");
        } else {
            qtyStepBtn.disabled = true;
            qtyStepBtn.classList.add("order-card__step-btn--disabled");
        }
    }

    // 수량 스텝 버튼 클릭 시 간단한 증가 로직
    if (qtyInput && qtyStepBtn) {
        qtyStepBtn.addEventListener("click", function handleQtyStep() {
            if (qtyStepBtn.disabled) return;

            const raw = qtyInput.value.replace(/,/g, "").trim();
            let current = parseInt(raw || "0", 10);
            if (Number.isNaN(current)) current = 0;

            // 기본: 1주씩 증가
            current = current + 1;

            qtyInput.value = current.toString();
            // 여기서 총 주문 금액 계산 로직 연동
        });
    }


});
