document.addEventListener("DOMContentLoaded", () => {

    const tbody = document.getElementById("assets-tbody");

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


    const rows = [
        { soldAt: "22.05.24", name: "기업A", profit: "+-000,000원", rate: "00%", qty: "00개" },
        { soldAt: "11.10.11", name: "기업B", profit: "+-000,000원", rate: "00%", qty: "00개" }
    ];

    function renderRows(data) {
        tbody.innerHTML = data.map(r => `
      <div class="assets__row">
        <div>${r.soldAt}</div>
        <div class="assets__stock">
          <span class="assets__dot"></span>
          <span>${r.name}</span>
        </div>
        <div></div>
        <div class="assets__cell--right">${r.profit}</div>
        <div class="assets__cell--right">${r.rate}</div>
        <div class="assets__cell--right">${r.qty}</div>
      </div>
    `).join("");
    }
    renderRows(rows);


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

        // 날짜 라벨 갱신
        if (unit === "month") {
            if (!monthInput.value) monthInput.value = new Date().toISOString().slice(0, 7);
            dateLabel.textContent = formatMonthLabel(monthInput.value);
        } else {
            // day
            if (!dateInput.value) {
                const base = monthInput.value || new Date().toISOString().slice(0, 7);
                dateInput.value = `${base}-01`;
            }
            dateLabel.textContent = formatDayLabel(dateInput.value);
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
    });

    // day 변경
    dateInput.addEventListener("change", () => {
        if (unit !== "day") return;
        dateLabel.textContent = formatDayLabel(dateInput.value);
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
