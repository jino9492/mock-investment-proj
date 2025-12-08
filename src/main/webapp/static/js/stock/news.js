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

    if (tradeTabs.length && priceLabel && amountLabel && submitButton) {
        tradeTabs.forEach(function attachTradeTab(tab) {
            tab.addEventListener("click", function onTradeTabClick() {

                tradeTabs.forEach(function removeActive(t) {
                    t.classList.remove("order-card__trade-tab--active");
                });
                tab.classList.add("order-card__trade-tab--active");

                const text = tab.textContent.trim();

                if (text.includes("판매")) {
                    priceLabel.textContent = "판매가격";
                    amountLabel.textContent = "판매가능 금액";
                    submitButton.textContent = "판매하기";
                } else if (text.includes("구매")) {
                    priceLabel.textContent = "구매가격";
                    amountLabel.textContent = "구매가능 금액";
                    submitButton.textContent = "구매하기";
                }
                else if (text.includes("대기")) {
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

                priceModePills.forEach(p =>
                    p.classList.remove("order-card__pill--active")
                );
                pill.classList.add("order-card__pill--active");

                const mode = pill.textContent.trim();

                if (mode === "시장가") {
                    priceInput.value = "";
                    priceInput.readOnly = true;
                    priceInput.placeholder = "시장가로 주문됩니다";
                    priceInput.classList.add("order-card__input--readonly");

                    if (orderTypeHidden) orderTypeHidden.value = "MARKET";

                    if (unitText) unitText.style.display = "none";

                } else {
                    priceInput.readOnly = false;
                    priceInput.placeholder = "가격을 입력하세요";
                    priceInput.classList.remove("order-card__input--readonly");

                    if (orderTypeHidden) orderTypeHidden.value = "LIMIT";

                    if (unitText) unitText.style.display = "";
                }
            });
        });
    }



      /* STOCK NEWS 탭 전환 + 페이지네이션*/

    const stockNewsMain = document.querySelector(".stock-news__main");

    if (stockNewsMain) {
        const navItems = Array.from(
            stockNewsMain.querySelectorAll(".stock-news__nav-item")
        );
        const panels = Array.from(
            stockNewsMain.querySelectorAll(".stock-news__panel")
        );

        // --- 탭 전환 : index 기반 ---
        if (navItems.length && panels.length) {
            navItems.forEach(function (btn, index) {
                btn.addEventListener("click", function () {
                    // 왼쪽 탭 active
                    navItems.forEach(function (i) {
                        i.classList.remove("stock-news__nav-item--active");
                    });
                    btn.classList.add("stock-news__nav-item--active");

                    // 패널 전환 (버튼 순서 == 패널 순서)
                    panels.forEach(function (panel, panelIndex) {
                        if (panelIndex === index) {
                            panel.classList.add("stock-news__panel--active");
                        } else {
                            panel.classList.remove("stock-news__panel--active");
                        }
                    });
                });
            });
        }

        function initPagination(listSelector, paginationSelector, pageSize = 5) {

            const listEl       = document.querySelector(listSelector);
            const paginationEl = document.querySelector(paginationSelector);

            if (!listEl || !paginationEl) return;

            const items = Array.from(listEl.children);

            // li가 5개 이하 → 페이지네이션 숨김
            if (items.length <= pageSize) {
                paginationEl.innerHTML = "";
                items.forEach(li => li.style.display = "");
                return;
            }

            let currentPage = 1;
            const totalPages = Math.ceil(items.length / pageSize);

            function renderPage(page) {
                currentPage = page;

                const start = (page - 1) * pageSize;
                const end   = start + pageSize;

                items.forEach((li, index) => {
                    li.style.display = (index >= start && index < end) ? "" : "none";
                });

                // 페이지 버튼 렌더링
                paginationEl.innerHTML = "";

                for (let p = 1; p <= totalPages; p++) {
                    const btn = document.createElement("button");
                    btn.className = "stock-news__page-btn";
                    btn.textContent = p + "페이지";

                    if (p === currentPage) {
                        btn.classList.add("stock-news__page-btn--active");
                    }

                    btn.addEventListener("click", () => renderPage(p));

                    paginationEl.appendChild(btn);
                }
            }

            // 초기 1페이지 렌더링
            renderPage(1);
        }


        // 뉴스 페이지네이션
        initPagination(".stock-news__list", ".stock-news__pagination");

        // 공시 페이지네이션
        initPagination(".stock-disclosure__list", ".stock-disclosure__pagination");

        }

    });
