
document.addEventListener("DOMContentLoaded", function handleStockPageInit() {

     /* 기능 2. 일/주/월/년 버튼 활성화*/

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
                    submitButton.textContent = "판매하기";
                } else if (text.includes("구매")) {
                    // 구매 탭
                    priceLabel.textContent = "구매가격";
                    amountLabel.textContent = "구매가능 금액";
                    submitButton.textContent = "구매하기";
                }
                else if (text.includes("대기")) {
                    // 대기 탭
                    priceLabel.textContent = "대기가격";
                    amountLabel.textContent = "대기가능 금액";
                    submitButton.textContent = "대기하기";
                }
            });
        });
    }
});
