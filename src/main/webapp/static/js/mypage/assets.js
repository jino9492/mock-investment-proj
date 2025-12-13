document.addEventListener("DOMContentLoaded", () => {

    const tbody = document.getElementById("assets-tbody");
    const totalProfitEl = document.getElementById("assets-totalProfit");
    const sellProfitEl = document.getElementById("assets-sellProfit");

    const viewWrap  = document.getElementById("assets-viewType");
    const viewBtn   = document.getElementById("assets-viewTypeBtn");
    const viewLabel = document.getElementById("assets-viewTypeLabel");
    const viewMenu  = document.getElementById("assets-viewTypeMenu");

    const dateBtn   = document.getElementById("assets-dateBtn");
    const dateLabel = document.getElementById("assets-dateLabel");

    const monthInput = document.getElementById("assets-monthInput");   // type="month"
    const dateInput  = document.getElementById("assets-dateInput");    // type="date"

    // 필수 요소 없으면 종료
    if (!tbody || !viewWrap || !viewBtn || !viewLabel || !viewMenu || !dateBtn || !dateLabel || !monthInput || !dateInput) {
        console.error("[assets.js] 필요한 DOM 요소를 찾지 못했습니다.", {
            tbody, viewWrap, viewBtn, viewLabel, viewMenu, dateBtn, dateLabel, monthInput, dateInput
        });
        return;
    }

    // 금액 포맷팅 함수
    function formatCurrency(amount) {
        const sign = amount >= 0 ? "+" : "";
        return sign + amount.toLocaleString("ko-KR") + "원";
    }

    // 수익률 포맷팅 함수
    function formatRate(rate) {
        const sign = rate >= 0 ? "+" : "";
        return sign + rate.toFixed(2) + "%";
    }

    // 수량 포맷팅 함수
    function formatQuantity(qty) {
        return qty.toLocaleString("ko-KR") + "개";
    }

    // 실현수익 데이터 가져오기
    async function fetchRealizedProfits(viewType, date) {
        try {
            const params = new URLSearchParams({
                viewType: viewType,
                date: date
            });
            
            const response = await fetch(`/api/mypage/assets?${params}`);
            const data = await response.json();
            
            if (!data.success) {
                console.error("실현수익 조회 실패:", data.message);
                renderRows([]);
                updateSummary(0, 0);
                return;
            }
            
            renderRows(data.profits || []);
            updateSummary(data.totalProfit || 0, data.sellProfit || 0);
            
        } catch (error) {
            console.error("실현수익 조회 중 오류:", error);
            renderRows([]);
            updateSummary(0, 0);
        }
    }

    // 테이블 행 렌더링
    function renderRows(profits) {
        if (!profits || profits.length === 0) {
            tbody.innerHTML = `
                <div class="assets__row" style="text-align: center; padding: 40px; color: #999;">
                    거래 내역이 없습니다.
                </div>
            `;
            return;
        }
        
        tbody.innerHTML = profits.map(profit => {
            const profitFormatted = formatCurrency(profit.totalProfit);
            const rateFormatted = formatRate(profit.profitRate || 0);
            const qtyFormatted = formatQuantity(profit.quantity);
            
            return `
                <div class="assets__row">
                    <div>${profit.soldAt}</div>
                    <div class="assets__stock">
                        <span class="assets__dot"></span>
                        <span>${profit.stockName}</span>
                    </div>
                    <div></div>
                    <div class="assets__cell--right">${profitFormatted}</div>
                    <div class="assets__cell--right">${rateFormatted}</div>
                    <div class="assets__cell--right">${qtyFormatted}</div>
                </div>
            `;
        }).join("");
    }

    // 요약 정보 업데이트
    function updateSummary(totalProfit, sellProfit) {
        if (totalProfitEl) {
            totalProfitEl.textContent = formatCurrency(totalProfit);
        }
        if (sellProfitEl) {
            sellProfitEl.textContent = formatCurrency(sellProfit);
        }
    }


    document.querySelectorAll(".assets__tab").forEach(tab => {
        tab.addEventListener("click", () => {
            document.querySelectorAll(".assets__tab").forEach(t => t.classList.remove("is-active"));
            tab.classList.add("is-active");
            // const type = tab.dataset.tab; // realized | group
        });
    });


    let unit = "month"; // 'month' | 'day'

    function formatMonthLabel(yyyyMM) {
        const [y, m] = yyyyMM.split("-");
        return `${y}년 ${Number(m)}월`;
    }

    function formatDayLabel(yyyyMMdd) {
        const [y, m, d] = yyyyMMdd.split("-");
        return `${y}년 ${Number(m)}월 ${Number(d)}일`;
    }

    function setUnit(nextUnit) {
        unit = nextUnit;

        // 상단 드롭다운 라벨
        viewLabel.textContent = unit === "month" ? "월" : "일";

        // 날짜 라벨 갱신 및 데이터 로드
        if (unit === "month") {
            if (!monthInput.value) monthInput.value = new Date().toISOString().slice(0, 7);
            dateLabel.textContent = formatMonthLabel(monthInput.value);
            fetchRealizedProfits("month", monthInput.value);
        } else {
            // day
            if (!dateInput.value) {
                const base = monthInput.value || new Date().toISOString().slice(0, 7);
                dateInput.value = `${base}-01`;
            }
            dateLabel.textContent = formatDayLabel(dateInput.value);
            fetchRealizedProfits("day", dateInput.value);
        }
    }

    // 날짜 버튼 클릭 → 현재 unit에 맞게 picker 열기
    dateBtn.addEventListener("click", () => {;

        if (unit === "month") {
            monthInput.focus();
            if (monthInput.showPicker) monthInput.showPicker();
            else monthInput.click();
        } else {
            dateInput.focus();
            if (dateInput.showPicker) dateInput.showPicker();
            else dateInput.click();
        }
    });


    // month 변경
    monthInput.addEventListener("change", () => {
        if (unit !== "month") return;
        dateLabel.textContent = formatMonthLabel(monthInput.value);
        fetchRealizedProfits("month", monthInput.value);
    });

    // day 변경
    dateInput.addEventListener("change", () => {
        if (unit !== "day") return;
        dateLabel.textContent = formatDayLabel(dateInput.value);
        fetchRealizedProfits("day", dateInput.value);
    });


    function closeMenu() {
        viewWrap.classList.remove("is-open");
        viewMenu.setAttribute("aria-hidden", "true");
    }

    viewBtn.addEventListener("click", (e) => {
        e.stopPropagation();
        const isOpen = viewWrap.classList.toggle("is-open");
        viewMenu.setAttribute("aria-hidden", String(!isOpen));
    });

    // 메뉴 클릭(월/일 선택)
    viewMenu.addEventListener("click", (e) => {
        const item = e.target.closest(".assets__pill-item");
        if (!item) return;

        viewMenu.querySelectorAll(".assets__pill-item").forEach(x => x.classList.remove("is-active"));
        item.classList.add("is-active");

        setUnit(item.dataset.view); // month | day
        closeMenu();
    });



    // 바깥 클릭 시 닫기
    document.addEventListener("click", closeMenu);

    // 초기 세팅
    setUnit("month");
});
