const MOCK_PENDING_ORDERS = [
    {
        id: "P1",
        status: "대기",
        corpName: "A기업",
        typeLabel: "조건 구매",
        sharesLabel: "00주",
        priceText: "00,000원",
        sideLabel: "구매",
        createdAt: "10.3",
        condition: {
            profitCondition: "+0%일 때",
            orderPrice: "0원 예상",
            orderQty: "00주",
            validUntil: "10월 30일까지",
            trigger: "정규장일 때만"
        }
    },
    {
        id: "P2",
        status: "대기",
        corpName: "B기업",
        typeLabel: "구매",
        sharesLabel: "00주",
        priceText: "00,000원",
        sideLabel: "구매",
        createdAt: "11.3",
        condition: {
            profitCondition: "+3%일 때",
            orderPrice: "지정가 10,000원",
            orderQty: "00주",
            validUntil: "12월 10일까지",
            trigger: "장중 언제나"
        }
    },
    {
        id: "P3",
        status: "대기",
        corpName: "C기업",
        typeLabel: "판매",
        sharesLabel: "00주",
        priceText: "00,000원",
        sideLabel: "판매",
        createdAt: "11.3",
        condition: {
            profitCondition: "+5%일 때",
            orderPrice: "시장가",
            orderQty: "00주",
            validUntil: "11월 30일까지",
            trigger: "장마감 전"
        }
    }
];

const MOCK_COMPLETED_ORDERS = [
    {
        id: "C1",
        date: "10.3",
        corpName: "A기업",
        typeLabel: "구매체결",
        sharesLabel: "00주",
        priceText: "00,000원"
    },
    {
        id: "C2",
        date: "11.3",
        corpName: "A기업",
        typeLabel: "판매체결",
        sharesLabel: "00주",
        priceText: "00,000원"
    },
    {
        id: "C3",
        date: "11.2",
        corpName: "B기업",
        typeLabel: "구매취소",
        sharesLabel: "00주",
        priceText: "-"
    },
    {
        id: "C4",
        date: "11.2",
        corpName: "C기업",
        typeLabel: "판매취소",
        sharesLabel: "00주",
        priceText: "-"
    }
];


// ------ 오른쪽 조건구매 컴포넌트 ------
function renderConditionOrderComponent(container, order) {
    if (!container) return;

    if (!order || !order.condition) {
        container.innerHTML = `
      <div class="condition-order">
        <p class="condition-order__empty">선택된 조건 주문이 없습니다.</p>
      </div>
    `;
        return;
    }

    const { condition } = order;

    container.innerHTML = `
    <article class="condition-order">
      <header class="condition-order__header">
        <div>
          <div class="condition-order__corp">${order.corpName}</div>
          <div class="condition-order__title">조건 구매</div>
        </div>
        <div class="condition-order__thumbnail"></div>
      </header>

      <div class="condition-order__body">
        <div class="condition-order__row">
          <span class="condition-order__label">설정한 수익률일 때 현재가로 주문해요</span>
          <span class="condition-order__badge">진행중</span>
        </div>

        <div class="condition-order__row">
          <span class="condition-order__label">수익률 조건</span>
          <span class="condition-order__value">${condition.profitCondition}</span>
        </div>

        <div class="condition-order__row">
          <span class="condition-order__label">주문 가격</span>
          <span class="condition-order__value">${condition.orderPrice}</span>
        </div>

        <div class="condition-order__row">
          <span class="condition-order__label">주문 수량</span>
          <span class="condition-order__value">${condition.orderQty}</span>
        </div>

        <div class="condition-order__row">
          <span class="condition-order__label">주문 유지</span>
          <span class="condition-order__value">${condition.validUntil}</span>
        </div>

        <div class="condition-order__row">
          <span class="condition-order__label">주문 수행</span>
          <span class="condition-order__value">${condition.trigger}</span>
        </div>
      </div>

      <footer class="condition-order__footer">
        <button class="condition-order__btn" type="button">삭제하기</button>
        <button class="condition-order__btn condition-order__btn--primary" type="button">수정하기</button>
      </footer>
    </article>
  `;
}

// ------ 페이지 스크립트 ------
document.addEventListener("DOMContentLoaded", () => {
    const pendingListEl = document.getElementById("pending-list");
    const pendingCountEl = document.getElementById("pending-count");
    const completedGroupsEl = document.getElementById("completed-groups");
    const conditionPanel = document.getElementById("condition-order-panel");
    const tabButtons = document.querySelectorAll(".order-tabs__item");

    if (!pendingListEl || !pendingCountEl || !completedGroupsEl || !conditionPanel || tabButtons.length === 0) {
        return;
    }


    const completedSectionEl = completedGroupsEl.closest(".order-section");


    const pendingSectionEl = pendingCountEl.closest(".order-section") || pendingListEl.closest(".order-section");

    // 조건주문 모드 상태
    let isConditionMode = false;
    let conditionSubTab = "setting"; // 'setting' | 'ended'
    let stockFilter = "ALL";         // 'ALL' | 'A기업' | ...

    // ============ 기존 전체 화면 렌더 ============
    function renderPendingDefault() {
        pendingListEl.innerHTML = "";

        MOCK_PENDING_ORDERS.forEach((order, index) => {
            const li = document.createElement("li");
            li.className = "order-item";
            li.dataset.orderId = order.id;
            li.dataset.kind = "pending";

            li.innerHTML = `
        <div class="order-item__left">
          <span class="order-item__badge">${order.status}</span>
          <div class="order-item__info">
            <div class="order-item__title">${order.corpName} ${order.typeLabel}</div>
            <div class="order-item__meta">${order.sharesLabel}</div>
          </div>
        </div>
        <div class="order-item__right">
          <div class="order-item__price">${order.priceText}</div>
          <div class="order-item__status">${order.sideLabel}</div>
        </div>
      `;

            li.addEventListener("click", () => {
                setActiveItem(li);
                renderConditionOrderComponent(conditionPanel, order);
            });

            if (index === 0) {
                li.classList.add("order-item--active");
                renderConditionOrderComponent(conditionPanel, order);
            }

            pendingListEl.appendChild(li);
        });

        pendingCountEl.textContent = `${MOCK_PENDING_ORDERS.length}건`;
        updatePendingTitleCount(MOCK_PENDING_ORDERS.length);

    }

    function renderCompletedDefault() {
        const selectEl = document.getElementById("completed-month-label");
        const selectedMonth = selectEl?.value || "ALL";

        const filtered = (selectedMonth === "ALL")
            ? MOCK_COMPLETED_ORDERS
            : MOCK_COMPLETED_ORDERS.filter(o => normalizeMonthKey(o.date) === selectedMonth);

        completedGroupsEl.innerHTML = "";

        const groups = {};
        filtered.forEach(order => {
            if (!groups[order.date]) groups[order.date] = [];
            groups[order.date].push(order);
        });

        Object.keys(groups)
            .sort((a, b) => b.localeCompare(a))
            .forEach(date => {
                const groupEl = document.createElement("div");
                groupEl.className = "completed-group";

                const dateLabel = document.createElement("div");
                dateLabel.className = "completed-group__date";
                dateLabel.textContent = date;
                groupEl.appendChild(dateLabel);

                const ul = document.createElement("ul");
                ul.className = "order-list";

                groups[date].forEach(order => {
                    const li = document.createElement("li");
                    li.className = "order-item order-item--completed";
                    li.dataset.orderId = order.id;
                    li.dataset.kind = "completed";

                    const isBuy = order.typeLabel.includes("구매");
                    const isSell = order.typeLabel.includes("판매");
                    const isCanceled = order.typeLabel.includes("취소");

                    li.className = `
                      order-item
                      order-item--completed
                      ${isCanceled ? "order-item--canceled" : ""}
                    `;

                                        li.innerHTML = `
                      <div class="order-item__left">
                        <div class="order-item__info">
                          <div class="order-item__title">${order.corpName}</div>
                    
                          <div class="order-item__meta">
                            <span class="
                              ${isBuy ? "order-text--buy" : ""}
                              ${isSell ? "order-text--sell" : ""}
                            ">
                              ${order.typeLabel}
                            </span>
                            | ${order.sharesLabel}
                          </div>
                        </div>
                      </div>
                    
                      <div class="order-item__right">
                        <div class="order-item__price">${order.priceText}</div>
                      </div>
                    `;



                    li.addEventListener("click", () => {
                        setActiveItem(li);
                        renderConditionOrderComponent(conditionPanel, null);
                    });

                    ul.appendChild(li);
                });

                groupEl.appendChild(ul);
                completedGroupsEl.appendChild(groupEl);
            });
    }

    function setActiveItem(selectedLi) {
        document.querySelectorAll(".order-item--active").forEach(li => li.classList.remove("order-item--active"));
        selectedLi.classList.add("order-item--active");
    }

    // ============ 조건주문 모드 UI ============
    function ensureConditionControls() {
        if (!pendingSectionEl) return;

        let controls = pendingSectionEl.querySelector(".condition-controls");
        if (controls) return controls;

        controls = document.createElement("div");
        controls.className = "condition-controls";
        controls.style.marginBottom = "12px";

        controls.innerHTML = `
      <div class="condition-subtabs" style="display:flex; gap:8px; margin-bottom:10px;">
        <button type="button" class="order-tabs__item order-tabs__item--active" data-condtab="setting">설정 내역</button>
        <button type="button" class="order-tabs__item" data-condtab="ended">종료 내역</button>
      </div>

      <div class="condition-filter" style="display:flex; align-items:center; gap:8px;">
        <label style="font-size:13px; color:#111827;">모든 주식</label>
        <select id="condition-stock-select" style="height:32px; border:1px solid #e5e7eb; border-radius:8px; padding:0 10px;">
          <option value="ALL">모든 주식</option>
        </select>
      </div>
    `;

        const listParent = pendingListEl.parentElement;
        if (listParent) {
            listParent.insertBefore(controls, pendingListEl);
        } else {
            pendingSectionEl.insertAdjacentElement("afterbegin", controls);
        }

        // 서브탭 이벤트
        const subBtns = controls.querySelectorAll("[data-condtab]");
        subBtns.forEach(btn => {
            btn.addEventListener("click", () => {
                subBtns.forEach(b => b.classList.remove("order-tabs__item--active"));
                btn.classList.add("order-tabs__item--active");
                conditionSubTab = btn.dataset.condtab;
                renderPendingCondition();
            });
        });

        // 드롭다운 옵션 채우기
        const selectEl = controls.querySelector("#condition-stock-select");
        if (selectEl) {
            // 기업명 유니크 추출
            const corpSet = new Set(MOCK_PENDING_ORDERS.map(o => o.corpName));
            [...corpSet].forEach(name => {
                const opt = document.createElement("option");
                opt.value = name;
                opt.textContent = name;
                selectEl.appendChild(opt);
            });

            selectEl.addEventListener("change", () => {
                stockFilter = selectEl.value;
                renderPendingCondition();
            });
        }

        return controls;
    }

    // 조건주문 모드에서 pending 섹션 내용 렌더링
    function renderPendingCondition() {
        if (completedSectionEl) completedSectionEl.style.display = "none";
        ensureConditionControls();

        if (conditionSubTab === "ended") {
            updatePendingSectionTitle("종료된 주문");
        } else {
            updatePendingSectionTitle("대기중인 주문");
        }


        // 설정내역: 조건주문(대기) / 종료내역: 완료된 주문
        let data = [];

        if (conditionSubTab === "setting") {
            const conditionOrders = MOCK_PENDING_ORDERS.filter(o => (o.typeLabel || "").includes("조건"));
            data = conditionOrders.map(o => {
                const isBuy = o.sideLabel === "구매";
                const isSell = o.sideLabel === "판매";

                return {
                    id: o.id,
                    corpName: o.corpName,

                    meta: `
      ${o.sharesLabel}
      <span class="${isBuy ? "order-text--buy" : ""}${isSell ? " order-text--sell" : ""}">
        ${o.sideLabel}
      </span>
      | ${o.condition?.profitCondition ?? ""}
    `,

                    raw: o,
                    isCompleted: false
                };
            });

        } else {
            // 종료내역에서는 "완료된 주문" 보여주기
            data = MOCK_COMPLETED_ORDERS.map(o => {
                const isBuy = o.typeLabel === "구매체결" || o.typeLabel === "구매취소";
                const isSell = o.typeLabel === "판매체결" || o.typeLabel === "판매취소";

                return {
                    id: o.id,
                    corpName: o.corpName,

                    meta: `
      <span class="${isBuy ? "order-text--buy" : ""}${isSell ? " order-text--sell" : ""}">
        ${o.typeLabel}
      </span>
      | ${o.sharesLabel}
    `,

                    raw: o,
                    isCompleted: true
                };
            });
        }

        // 모든 주식 필터 적용
        if (stockFilter !== "ALL") {
            data = data.filter(x => x.corpName === stockFilter);
        }

        // 렌더링
        pendingListEl.innerHTML = "";

        data.forEach((item, index) => {
            const li = document.createElement("li");
            li.className = `order-item order-item--completed`;

            li.innerHTML = `
      <div class="order-item__left">
        <div class="order-item__info">
          <div class="order-item__title">${item.corpName}</div>
          <div class="order-item__meta">${item.meta}</div>
        </div>
      </div>
      <div class="order-item__right" style="flex:0 0 auto; width:auto;">
        <div class="order-item__status">${item.statusText}</div>
      </div>
    `;

            li.addEventListener("click", () => {
                setActiveItem(li);

                // 종료내역(완료 주문)은 오른쪽 패널을 비우거나 안내만 띄우기
                if (item.isCompleted) {
                    renderConditionOrderComponent(conditionPanel, null);
                } else {
                    // 설정내역(조건 주문)은 오른쪽에 상세 표시
                    renderConditionOrderComponent(conditionPanel, item.raw);
                }
            });

            if (index === 0) {
                li.classList.add("order-item--active");
                if (item.isCompleted) renderConditionOrderComponent(conditionPanel, null);
                else renderConditionOrderComponent(conditionPanel, item.raw);
            }

            pendingListEl.appendChild(li);
        });

        pendingCountEl.textContent = `${data.length}건`;
        updatePendingTitleCount(data.length);

    }

    function updatePendingSectionTitle(text) {
        const titleEl = pendingSectionEl?.querySelector(".order-section__title");
        if (!titleEl) return;

        // 기존 배지(span)는 유지하고, 제목 텍스트만 교체
        const badge = titleEl.querySelector(".order-section__count-badge");

        titleEl.textContent = text;
        if (badge) titleEl.appendChild(badge);
    }



    // 전체 모드 복귀
    function renderAllMode() {
        // 완료 섹션 다시 보이기
        if (completedSectionEl) completedSectionEl.style.display = "";

        // 조건 컨트롤 숨김
        if (pendingSectionEl) {
            const controls = pendingSectionEl.querySelector(".condition-controls");
            if (controls) controls.style.display = "none";
        }

        updatePendingSectionTitle("대기중인 주문");


        renderPendingDefault();
        renderCompletedDefault();
    }

    // 조건 모드 진입
    function renderConditionMode() {
        // 컨트롤 보이기
        const controls = ensureConditionControls();
        if (controls) controls.style.display = "";

        renderPendingCondition();

    }

    // ============ 상단 전체/조건주문 클릭 ============
    tabButtons.forEach(btn => {
        btn.addEventListener("click", () => {
            tabButtons.forEach(b => b.classList.remove("order-tabs__item--active"));
            btn.classList.add("order-tabs__item--active");

            const label = btn.textContent.trim();
            isConditionMode = label.includes("조건");

            if (isConditionMode) renderConditionMode();
            else renderAllMode();
        });
    });

    function normalizeMonthKey(dateStr) {

        if (!dateStr) return "";
        const [m] = String(dateStr).split(".");
        return m?.padStart(2, "0") || "";
    }

    function buildCompletedMonthOptions() {
        const selectEl = document.getElementById("completed-month-label");
        if (!selectEl) return;

        selectEl.innerHTML = `<option value="ALL">전체</option>`;

        const months = new Set(
            MOCK_COMPLETED_ORDERS.map(o => normalizeMonthKey(o.date)).filter(Boolean)
        );

        // 최신 월이 위로 오게 정렬
        [...months].sort((a, b) => Number(b) - Number(a)).forEach(mm => {
            const opt = document.createElement("option");
            opt.value = mm;
            opt.textContent = `${Number(mm)}월`;
            selectEl.appendChild(opt);
        });
    }

    function updatePendingTitleCount(count) {
        // pending 섹션 안의 제목만 정확히 찾기
        const titleEl = pendingSectionEl?.querySelector(".order-section__title");
        if (!titleEl) return;

        // 이미 붙어있으면 재사용
        let badge = titleEl.querySelector(".order-section__count-badge");
        if (!badge) {
            badge = document.createElement("span");
            badge.className = "order-section__count-badge";
            badge.style.marginLeft = "8px";
            badge.style.fontSize = "13px";
            badge.style.color = "#9ca3af";
            badge.style.fontWeight = "500";
            titleEl.appendChild(badge);
        }

        badge.textContent = `${count}건`;
    }





    // 초기 렌더(전체)
    renderPendingDefault();
    renderCompletedDefault();

    buildCompletedMonthOptions();

    const completedMonthSelect = document.getElementById("completed-month-label");
    completedMonthSelect?.addEventListener("change", () => {
        renderCompletedDefault();
    });

});
