document.addEventListener("DOMContentLoaded", () => {

    const tbody = document.getElementById("analysis-tbody");
    const totalProfitEl = document.getElementById("analysis-totalProfit");
    const sellProfitEl = document.getElementById("analysis-sellProfit");

    const viewWrap  = document.getElementById("analysis-viewType");
    const viewBtn   = document.getElementById("analysis-viewTypeBtn");
    const viewLabel = document.getElementById("analysis-viewTypeLabel");
    const viewMenu  = document.getElementById("analysis-viewTypeMenu");

    const dateBtn   = document.getElementById("analysis-dateBtn");
    const dateLabel = document.getElementById("analysis-dateLabel");

    const monthInput = document.getElementById("analysis-monthInput");   // type="month"
    const dateInput  = document.getElementById("analysis-dateInput");    // type="date"

    // 필수 요소 없으면 종료
    if (!tbody || !viewWrap || !viewBtn || !viewLabel || !viewMenu || !dateBtn || !dateLabel || !monthInput || !dateInput) {
        console.error("[analysis.js] 필요한 DOM 요소를 찾지 못했습니다.", {
            tbody, viewWrap, viewBtn, viewLabel, viewMenu, dateBtn, dateLabel, monthInput, dateInput
        });
        return;
    }

    // 금액 포맷팅 함수 (수익/손실 표시용 - 빨강/파랑)
    function formatCurrency(amount) {
        const sign = amount >= 0 ? "+" : "";
        return sign + amount.toLocaleString("ko-KR") + "원";
    }

    // 일반 금액 포맷팅 함수 (총 판매금액, 총 구매금액용)
    function formatAmount(amount) {
        return amount.toLocaleString("ko-KR") + "원";
    }

    // 수익률 포맷팅 함수
    function formatRate(rate) {
        const sign = rate >= 0 ? "+" : "";
        return sign + rate.toFixed(2) + "%";
    }

    // 수량 포맷팅 함수
    function formatQuantity(qty) {
        return qty.toLocaleString("ko-KR") + "주";
    }

    // 실현수익 데이터 가져오기
    async function fetchRealizedProfits(viewType, date) {
        try {
            const params = new URLSearchParams({
                viewType: viewType,
                date: date
            });
            
            const response = await fetch(`/api/mypage/analysis?${params}`);
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
                <div class="analysis__row" style="text-align: center; padding: 40px; color: #999; grid-column: 1 / -1;">
                    거래 내역이 없습니다.
                </div>
            `;
            return;
        }
        
        tbody.innerHTML = profits.map(profit => {
            const profitFormatted = formatCurrency(profit.totalProfit || 0);
            const rateFormatted = formatRate(profit.profitRate || 0);
            const salesAmountFormatted = formatAmount(profit.totalSalesAmount || 0);
            const purchaseAmountFormatted = formatAmount(profit.totalPurchaseAmount || 0);
            const qtyFormatted = formatQuantity(profit.quantity || 0);
            
            // 수익/손실에 따른 색상 클래스
            const profitClass = (profit.totalProfit || 0) >= 0 ? "analysis__profit--positive" : "analysis__profit--negative";
            const rateClass = (profit.profitRate || 0) >= 0 ? "analysis__profit--positive" : "analysis__profit--negative";
            
            return `
                <div class="analysis__row" data-stock-symbol="${profit.stockSymbol || ""}">
                    <div>${profit.soldAt || ""}</div>
                    <div class="analysis__stock">
                        <span class="analysis__dot"></span>
                        <span>${profit.stockName || ""}</span>
                    </div>
                    <div class="analysis__cell--right ${profitClass}">${profitFormatted}</div>
                    <div class="analysis__cell--right ${rateClass}">${rateFormatted}</div>
                    <div class="analysis__cell--right">${salesAmountFormatted}</div>
                    <div class="analysis__cell--right">${purchaseAmountFormatted}</div>
                    <div class="analysis__cell--right">${qtyFormatted}</div>
                </div>
            `;
        }).join("");
        
        // 각 행에 클릭 이벤트 추가
        const rows = tbody.querySelectorAll(".analysis__row");
        rows.forEach(row => {
            row.addEventListener("click", function() {
                const stockSymbol = this.getAttribute("data-stock-symbol");
                if (stockSymbol) {
                    window.location.href = `/stock?code=${encodeURIComponent(stockSymbol)}`;
                }
            });
        });
    }

    // 요약 정보 업데이트
    function updateSummary(totalProfit, sellProfit) {
        if (totalProfitEl) {
            totalProfitEl.textContent = formatCurrency(totalProfit);
            // 기존 색상 클래스 제거
            totalProfitEl.classList.remove("analysis__profit--positive", "analysis__profit--negative");
            // 값에 따라 색상 클래스 추가
            if (totalProfit > 0) {
                totalProfitEl.classList.add("analysis__profit--positive");
            } else if (totalProfit < 0) {
                totalProfitEl.classList.add("analysis__profit--negative");
            }
        }
        if (sellProfitEl) {
            sellProfitEl.textContent = formatCurrency(sellProfit);
            // 기존 색상 클래스 제거
            sellProfitEl.classList.remove("analysis__profit--positive", "analysis__profit--negative");
            // 값에 따라 색상 클래스 추가
            if (sellProfit > 0) {
                sellProfitEl.classList.add("analysis__profit--positive");
            } else if (sellProfit < 0) {
                sellProfitEl.classList.add("analysis__profit--negative");
            }
        }
    }

    let unit = "month"; // 'day' | 'week' | 'month' | 'year' | 'all'

    function formatDayLabel(yyyyMMdd) {
        const [y, m, d] = yyyyMMdd.split("-");
        return `${y}년 ${Number(m)}월 ${Number(d)}일`;
    }

    function formatWeekLabel(yyyyMMdd) {
        const date = new Date(yyyyMMdd);
        const year = date.getFullYear();
        const month = date.getMonth() + 1;
        const day = date.getDate();
        // 주의 시작일 계산 (월요일 기준)
        const dayOfWeek = date.getDay();
        const diff = date.getDate() - dayOfWeek + (dayOfWeek === 0 ? -6 : 1); // 월요일로 조정
        const startDate = new Date(date.setDate(diff));
        const endDate = new Date(startDate);
        endDate.setDate(startDate.getDate() + 6);
        
        const startMonth = startDate.getMonth() + 1;
        const startDay = startDate.getDate();
        const endMonth = endDate.getMonth() + 1;
        const endDay = endDate.getDate();
        
        if (startMonth === endMonth) {
            return `${year}년 ${startMonth}월 ${startDay}일 ~ ${endDay}일`;
        } else {
            return `${year}년 ${startMonth}월 ${startDay}일 ~ ${endMonth}월 ${endDay}일`;
        }
    }

    function formatMonthLabel(yyyyMM) {
        const [y, m] = yyyyMM.split("-");
        return `${y}년 ${Number(m)}월`;
    }

    function formatYearLabel(yyyy) {
        return `${yyyy}년`;
    }

    function setUnit(nextUnit) {
        unit = nextUnit;

        // 상단 드롭다운 라벨
        const labels = {
            "day": "일",
            "week": "주",
            "month": "월",
            "year": "년",
            "all": "전체"
        };
        viewLabel.textContent = labels[unit] || "월";

        // 날짜 라벨 갱신 및 데이터 로드
        if (unit === "day") {
            if (!dateInput.value) {
                dateInput.value = new Date().toISOString().slice(0, 10);
            }
            dateLabel.textContent = formatDayLabel(dateInput.value);
            fetchRealizedProfits("day", dateInput.value);
        } else if (unit === "week") {
            if (!dateInput.value) {
                dateInput.value = new Date().toISOString().slice(0, 10);
            }
            dateLabel.textContent = formatWeekLabel(dateInput.value);
            fetchRealizedProfits("week", dateInput.value);
        } else if (unit === "month") {
            if (!monthInput.value) monthInput.value = new Date().toISOString().slice(0, 7);
            dateLabel.textContent = formatMonthLabel(monthInput.value);
            fetchRealizedProfits("month", monthInput.value);
        } else if (unit === "year") {
            if (!monthInput.value) monthInput.value = new Date().toISOString().slice(0, 7);
            const year = monthInput.value.split("-")[0];
            dateLabel.textContent = formatYearLabel(year);
            fetchRealizedProfits("year", year);
        } else if (unit === "all") {
            dateLabel.textContent = "전체";
            fetchRealizedProfits("all", "");
        }
    }

    // 날짜 버튼 클릭 → 현재 unit에 맞게 picker 열기
    dateBtn.addEventListener("click", () => {
        if (unit === "all") {
            // 전체는 날짜 선택 불필요
            return;
        } else if (unit === "day" || unit === "week") {
            dateInput.focus();
            if (dateInput.showPicker) dateInput.showPicker();
            else dateInput.click();
        } else if (unit === "month" || unit === "year") {
            monthInput.focus();
            if (monthInput.showPicker) monthInput.showPicker();
            else monthInput.click();
        }
    });

    // month 변경
    monthInput.addEventListener("change", () => {
        if (unit === "month") {
            dateLabel.textContent = formatMonthLabel(monthInput.value);
            fetchRealizedProfits("month", monthInput.value);
        } else if (unit === "year") {
            const year = monthInput.value.split("-")[0];
            dateLabel.textContent = formatYearLabel(year);
            fetchRealizedProfits("year", year);
        }
    });

    // day 변경
    dateInput.addEventListener("change", () => {
        if (unit === "day") {
            dateLabel.textContent = formatDayLabel(dateInput.value);
            fetchRealizedProfits("day", dateInput.value);
        } else if (unit === "week") {
            dateLabel.textContent = formatWeekLabel(dateInput.value);
            fetchRealizedProfits("week", dateInput.value);
        }
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
        const item = e.target.closest(".analysis__pill-item");
        if (!item) return;

        viewMenu.querySelectorAll(".analysis__pill-item").forEach(x => x.classList.remove("is-active"));
        item.classList.add("is-active");

        setUnit(item.dataset.view); // month | day
        closeMenu();
    });



    // 바깥 클릭 시 닫기
    document.addEventListener("click", closeMenu);

    // 초기 세팅
    setUnit("month");
});

