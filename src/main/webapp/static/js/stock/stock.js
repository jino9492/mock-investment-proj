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
    const orderCard = document.querySelector(".order-card");

    // 각 탭별 주문 유형 상태 저장
    const orderTypeByTab = {
        buy: "general",
        sell: "general",
        wait: "general"
    };

    // 숫자 포맷팅 헬퍼 함수
    function formatNumber(num) {
        return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    }

    // 조건 주문 가격 필드 초기화 함수
    function initializeConditionalOrderPriceFields() {
        const conditionalOrderPriceInput = document.getElementById("conditional-order-price-input");
        const conditionalSellPriceInput = document.getElementById("conditional-sell-price-input");
        
        // 구매 조건 주문 가격 필드 초기화
        if (conditionalOrderPriceInput) {
            const currentPrice = conditionalOrderPriceInput.getAttribute("data-current-price");
            if (currentPrice && !conditionalOrderPriceInput.value) {
                conditionalOrderPriceInput.value = formatNumber(parseInt(currentPrice));
            }
        }
        
        // 판매 조건 주문 가격 필드 초기화
        if (conditionalSellPriceInput) {
            const currentPrice = conditionalSellPriceInput.getAttribute("data-current-price");
            if (currentPrice && !conditionalSellPriceInput.value) {
                conditionalSellPriceInput.value = formatNumber(parseInt(currentPrice));
            }
        }
    }

    // 주문 섹션 표시/숨김 헬퍼 함수
    function updateOrderSections(activeTab, orderType) {
        const generalOrder = document.querySelector(".order-card__general-order");
        const buyConditionalOrder = document.querySelector(".order-card__conditional-order");
        const sellConditionalOrder = document.querySelector(".order-card__conditional-order--sell");
        
        if (orderType === "conditional") {
            // 조건 주문 선택 시
            if (generalOrder) generalOrder.style.display = "none";
            
            if (activeTab === "sell") {
                // 판매 탭 + 조건 주문
                if (buyConditionalOrder) buyConditionalOrder.style.display = "none";
                if (sellConditionalOrder) sellConditionalOrder.style.display = "block";
                // 조건 주문 필드가 표시될 때 가격 필드 초기화
                setTimeout(() => initializeConditionalOrderPriceFields(), 0);
            } else if (activeTab === "buy") {
                // 구매 탭 + 조건 주문
                if (buyConditionalOrder) buyConditionalOrder.style.display = "block";
                if (sellConditionalOrder) sellConditionalOrder.style.display = "none";
                // 조건 주문 필드가 표시될 때 가격 필드 초기화
                setTimeout(() => initializeConditionalOrderPriceFields(), 0);
            } else {
                // 대기 탭 등
                if (buyConditionalOrder) buyConditionalOrder.style.display = "none";
                if (sellConditionalOrder) sellConditionalOrder.style.display = "none";
            }
        } else {
            // 일반 주문 선택 시
            if (generalOrder) generalOrder.style.display = "block";
            if (buyConditionalOrder) buyConditionalOrder.style.display = "none";
            if (sellConditionalOrder) sellConditionalOrder.style.display = "none";
        }
    }

    // 주문 유형 드롭다운 UI 업데이트 함수
    function updateOrderTypeDropdownUI(orderType) {
        const orderTypeDropdown = orderCard?.querySelector("[data-order-type-dropdown]");
        if (!orderTypeDropdown) return;

        const hiddenInput = orderTypeDropdown.querySelector("[data-dropdown-input]");
        const label = orderTypeDropdown.querySelector("[data-dropdown-label]");
        const options = orderTypeDropdown.querySelectorAll(".order-card__dropdown-option");

        if (hiddenInput) {
            hiddenInput.value = orderType;
        }

        // 해당 옵션 찾아서 활성화
        options.forEach(function(option) {
            const value = option.getAttribute("data-value");
            if (value === orderType) {
                option.setAttribute("aria-selected", "true");
                if (label) {
                    label.textContent = option.querySelector(".order-card__dropdown-option-label")?.textContent || option.textContent;
                }
            } else {
                option.setAttribute("aria-selected", "false");
            }
        });
    }

    // 탭 전환 로직을 함수로 추출
    function switchToTab(tab) {
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
            if (orderCard) {
                orderCard.classList.add("order-card--sell");
                // 판매 탭의 주문 유형 복원 (기본값: 일반 주문)
                const orderType = orderTypeByTab.sell;
                const orderTypeRow = document.querySelector(".order-card__order-type-row");
                const waitOrders = document.querySelector(".order-card__wait-orders");
                
                if (orderTypeRow) {
                    orderTypeRow.style.display = "flex";
                    orderTypeRow.classList.remove("order-card__order-type-hidden");
                }
                if (waitOrders) {
                    waitOrders.style.display = "none";
                    waitOrders.classList.add("order-card__wait-orders--hidden");
                }
                updateOrderTypeDropdownUI(orderType);
                updateOrderSections("sell", orderType);
            }
            priceLabel.textContent = "판매가격";
            amountLabel.textContent = "판매가능 금액";
            if (submitButton) {
                submitButton.style.display = "flex";
                submitButton.textContent = "판매하기";
            }
        } else if (text.includes("구매")) {
            // 구매 탭
            tab.classList.add("order-card__trade-tab--active");
            tab.setAttribute("aria-checked", "true");
            tab.setAttribute("data-state", "checked");
            if (orderCard) {
                orderCard.classList.add("order-card--buy");
                // 구매 탭의 주문 유형 복원
                const orderType = orderTypeByTab.buy;
                const orderTypeRow = document.querySelector(".order-card__order-type-row");
                const waitOrders = document.querySelector(".order-card__wait-orders");
                
                if (orderTypeRow) {
                    orderTypeRow.style.display = "flex";
                    orderTypeRow.classList.remove("order-card__order-type-hidden");
                }
                if (waitOrders) {
                    waitOrders.style.display = "none";
                    waitOrders.classList.add("order-card__wait-orders--hidden");
                }
                updateOrderTypeDropdownUI(orderType);
                updateOrderSections("buy", orderType);
            }
            priceLabel.textContent = "구매가격";
            amountLabel.textContent = "구매가능 금액";
            if (submitButton) {
                submitButton.style.display = "flex";
                submitButton.textContent = "구매하기";
            }
        }
        else if (text.includes("대기")) {
            // 대기 탭
            tab.classList.add("order-card__trade-tab--active");
            tab.setAttribute("aria-checked", "true");
            tab.setAttribute("data-state", "checked");
            if (orderCard) {
                orderCard.classList.add("order-card--wait");
                // 대기 탭에서는 주문 유형 드롭다운과 일반/조건 주문 섹션 숨김
                const orderTypeRow = document.querySelector(".order-card__order-type-row");
                const generalOrder = document.querySelector(".order-card__general-order");
                const buyConditionalOrder = document.querySelector(".order-card__conditional-order");
                const sellConditionalOrder = document.querySelector(".order-card__conditional-order--sell");
                const waitOrders = document.querySelector(".order-card__wait-orders");
                
                if (orderTypeRow) {
                    orderTypeRow.style.display = "none";
                    orderTypeRow.classList.add("order-card__order-type-hidden");
                }
                if (generalOrder) {
                    generalOrder.style.display = "none";
                    generalOrder.classList.add("order-card__conditional-order--hidden");
                }
                if (buyConditionalOrder) {
                    buyConditionalOrder.style.display = "none";
                    buyConditionalOrder.classList.add("order-card__conditional-order--hidden");
                }
                if (sellConditionalOrder) {
                    sellConditionalOrder.style.display = "none";
                    sellConditionalOrder.classList.add("order-card__conditional-order--hidden");
                }
                if (waitOrders) {
                    waitOrders.style.display = "block";
                    waitOrders.classList.remove("order-card__wait-orders--hidden");
                }
            }
            if (submitButton) submitButton.style.display = "none";
        }
    }

    if (tradeTabs.length && priceLabel && amountLabel && submitButton) {
        // 초기 로드 시 활성화된 탭에 맞게 섹션 초기화
        const activeTab = Array.from(tradeTabs).find(tab => 
            tab.classList.contains("order-card__trade-tab--active") || 
            tab.getAttribute("aria-checked") === "true"
        );
        
        if (activeTab) {
            switchToTab(activeTab);
            // 초기 로드 시 placeholder 업데이트 (약간의 지연을 두어 DOM이 완전히 렌더링되고 가격이 설정된 후 실행)
            setTimeout(function() {
                if (typeof updateQuantityPlaceholders === 'function') {
                    updateQuantityPlaceholders();
                }
            }, 300);
        }

        // 탭 클릭 이벤트 핸들러
        tradeTabs.forEach(function attachTradeTab(tab) {
            tab.addEventListener("click", function onTradeTabClick() {
                switchToTab(tab);
                // 탭 전환 후 placeholder 업데이트
                setTimeout(function() {
                    if (typeof updateQuantityPlaceholders === 'function') {
                        updateQuantityPlaceholders();
                    }
                }, 100);
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

        if (!toggle || !list || !label || !hiddenInput || !options.length) {
            return;
        }

        dropdown.setAttribute("aria-expanded", "false");

        function setActiveOption(option) {
            options.forEach(function (opt) {
                opt.setAttribute("aria-selected", opt === option ? "true" : "false");
            });
            label.textContent = option.querySelector(".order-card__dropdown-option-label")?.textContent || option.textContent;
            hiddenInput.value = option.getAttribute("data-value") || "";
        }

        const initial = dropdown.querySelector(".order-card__dropdown-option[aria-selected=\"true\"]") || options[0];
        setActiveOption(initial);

        // 초기 상태에서 주문 유형에 따라 display 설정
        if (dropdown.hasAttribute("data-order-type-dropdown")) {
            const initialOrderType = initial.getAttribute("data-value");
            // 현재 활성화된 탭 확인
            const activeTab = document.querySelector(".order-card__trade-tab[aria-checked='true']");
            let activeTabType = "buy"; // 기본값
            if (activeTab) {
                const tabText = activeTab.textContent.trim();
                if (tabText.includes("판매")) {
                    activeTabType = "sell";
                } else if (tabText.includes("구매")) {
                    activeTabType = "buy";
                } else {
                    activeTabType = "wait";
                }
            }
            // 초기 주문 유형을 현재 탭의 상태로 설정
            orderTypeByTab[activeTabType] = initialOrderType;
            updateOrderSections(activeTabType, initialOrderType);
        }

        toggle.addEventListener("click", function (e) {
            e.stopPropagation();
            const isOpen = dropdown.classList.toggle("is-open");
            dropdown.setAttribute("aria-expanded", isOpen ? "true" : "false");
            if (isOpen) {
                closeAllDropdowns(dropdown);
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

                // 주문 유형 드롭다운 변경 시 조건 주문 필드 표시/숨김 처리
                if (dropdown.hasAttribute("data-order-type-dropdown")) {
                    const orderType = option.getAttribute("data-value");
                    // 현재 활성화된 탭 확인
                    const activeTab = document.querySelector(".order-card__trade-tab[aria-checked='true']");
                    let activeTabType = "buy"; // 기본값
                    if (activeTab) {
                        const tabText = activeTab.textContent.trim();
                        if (tabText.includes("판매")) {
                            activeTabType = "sell";
                        } else if (tabText.includes("구매")) {
                            activeTabType = "buy";
                        } else {
                            activeTabType = "wait";
                        }
                    }
                    // 현재 활성 탭의 주문 유형 상태 업데이트
                    orderTypeByTab[activeTabType] = orderType;
                    updateOrderSections(activeTabType, orderType);
                    // 조건 주문 선택 시 가격 필드 초기화 (updateOrderSections에서도 호출되지만 명시적으로 호출)
                    if (orderType === "conditional") {
                        setTimeout(() => initializeConditionalOrderPriceFields(), 0);
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

    /* 대기 주문 취소 버튼 이벤트 */
    function attachCancelButtonListeners() {
        const cancelOrderButtons = document.querySelectorAll(".order-card__wait-order-btn--cancel");
        
        cancelOrderButtons.forEach(function(button) {
            // 이미 이벤트 리스너가 연결되어 있는지 확인
            if (button.hasAttribute("data-listener-attached")) {
                return;
            }
            
            button.setAttribute("data-listener-attached", "true");
            button.addEventListener("click", async function() {
                const orderId = this.getAttribute("data-order-id");
                
                if (!orderId) {
                    alert("주문 ID를 찾을 수 없습니다.");
                    return;
                }
                
                // 확인 메시지
                const confirmed = window.confirm("정말로 이 주문을 취소하시겠습니까?");
                if (!confirmed) {
                    return;
                }
                
                try {
                    // DELETE 요청
                    const params = new URLSearchParams();
                    params.append("orderId", orderId);
                    
                    const response = await fetch("/api/order?" + params.toString(), {
                        method: "DELETE",
                        headers: {
                            "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
                        }
                    });
                    
                    // 응답 처리
                    let result;
                    try {
                        result = await response.json();
                    } catch (e) {
                        console.error("[주문 취소] JSON 파싱 오류:", e);
                        alert("서버 응답을 처리할 수 없습니다. 페이지를 새로고침해주세요.");
                        return;
                    }
                    
                    if (result.success) {
                        alert(result.message || "주문이 성공적으로 취소되었습니다.");
                        // 주문 목록 다시 업데이트 (페이지 새로고침 대신)
                        await updatePendingOrders();
                    } else {
                        alert(result.message || "주문 취소에 실패했습니다.");
                    }
                } catch (error) {
                    console.error("[주문 취소] 네트워크 오류:", error);
                    if (error instanceof TypeError && error.message.includes("fetch")) {
                        alert("네트워크 연결을 확인해주세요. 인터넷 연결이 끊어졌을 수 있습니다.");
                    } else {
                        alert("주문 취소 중 오류가 발생했습니다: " + error.message);
                    }
                }
            });
        });
    }

    // 초기 취소 버튼 이벤트 리스너 연결
    attachCancelButtonListeners();

    /* 대기 주문 실시간 업데이트 (폴링) */
    let orderPollingInterval = null;
    let lastOrderIds = new Set();

    // 현재 대기 주문 ID 수집
    function collectCurrentOrderIds() {
        const orderItems = document.querySelectorAll(".order-card__wait-order-item");
        const orderIds = new Set();
        orderItems.forEach(function(item) {
            const cancelBtn = item.querySelector(".order-card__wait-order-btn--cancel");
            if (cancelBtn) {
                const orderId = cancelBtn.getAttribute("data-order-id");
                if (orderId) {
                    orderIds.add(orderId);
                }
            }
        });
        return orderIds;
    }

    // 초기 주문 ID 수집
    lastOrderIds = collectCurrentOrderIds();

    // 주문 목록 업데이트 함수
    async function updatePendingOrders() {
        try {
            const response = await fetch("/api/order");
            
            if (!response.ok) {
                console.warn("[주문 업데이트] 서버 응답 오류:", response.status);
                return;
            }
            
            const result = await response.json();
            
            if (!result.success || !result.orders) {
                console.warn("[주문 업데이트] 응답 데이터 오류:", result);
                return;
            }
            
            // 구매 가능 금액 업데이트
            if (result.availableCash !== undefined) {
                const formattedAmount = result.availableCash.toLocaleString('ko-KR');
                
                // 일반 주문 구매가능 금액 업데이트
                const availableAmountElement = document.getElementById("order-available-amount");
                if (availableAmountElement) {
                    availableAmountElement.textContent = formattedAmount + "원";
                    availableAmountElement.setAttribute("data-available-cash", result.availableCash);
                }
                
                // 조건 주문 구매가능 금액 업데이트
                const conditionalAvailableAmountElement = document.getElementById("conditional-order-available-amount");
                if (conditionalAvailableAmountElement) {
                    conditionalAvailableAmountElement.textContent = formattedAmount + "원";
                }
                
                // order-card의 data-available-cash도 업데이트
                const orderCard = document.querySelector(".order-card");
                if (orderCard) {
                    orderCard.setAttribute("data-available-cash", result.availableCash);
                }
            }
            
            // 대기 중인 판매 주문 수량 업데이트
            if (result.pendingSellQuantity !== undefined) {
                const orderCard = document.querySelector(".order-card");
                if (orderCard) {
                    orderCard.setAttribute("data-pending-sell-quantity", result.pendingSellQuantity);
                    // placeholder 업데이트 (판매 탭일 경우)
                    if (typeof updateQuantityPlaceholders === 'function') {
                        updateQuantityPlaceholders();
                    }
                }
            }
            
            const currentOrderIds = new Set(result.orders.map(order => order.orderId.toString()));
            const lastOrderIdsArray = Array.from(lastOrderIds);
            
            // 체결된 주문이 있는지 확인 (이전에 있던 주문이 현재 목록에 없으면 체결됨)
            const filledOrders = lastOrderIdsArray.filter(id => !currentOrderIds.has(id));
            
            if (filledOrders.length > 0) {
                console.log("[주문 업데이트] 체결된 주문 발견:", filledOrders);
                
                // 체결 알림 표시
                const tradeType = filledOrders.length === 1 ? "주문" : "주문들";
                alert(`${filledOrders.length}개의 ${tradeType}이(가) 체결되었습니다.`);
                
                // 대기 주문 목록 영역 찾기
                const waitOrdersContainer = document.querySelector(".order-card__wait-orders");
                
                if (waitOrdersContainer) {
                    // 현재 대기 탭이 활성화되어 있는지 확인
                    const waitTab = document.querySelector(".order-card__trade-tab--wait");
                    const isWaitTabActive = waitTab && waitTab.classList.contains("order-card__trade-tab--active");
                    
                    // 대기 주문 목록 HTML 업데이트
                    if (result.orders.length === 0) {
                        // 대기 주문이 없으면 빈 상태 표시
                        waitOrdersContainer.innerHTML = `
                            <div class="order-card__wait-empty">
                                <p class="order-card__wait-empty-text">대기중인 주문이 없어요</p>
                            </div>
                        `;
                    } else {
                        // 대기 주문 목록 재구성
                        let ordersHTML = "";
                        result.orders.forEach(function(order) {
                            const tradeTypeText = order.tradeType === "buy" ? "구매" : "판매";
                            const quantity = order.quantity.toLocaleString();
                            const price = order.price ? order.price.toLocaleString() : "시장가";
                            
                            ordersHTML += `
                                <div class="order-card__wait-order-item">
                                    <div class="order-card__wait-order-info">
                                        <span class="order-card__wait-order-type order-card__wait-order-type--${order.tradeType}">
                                            ${tradeTypeText} ${quantity}주
                                        </span>
                                        <span class="order-card__wait-order-price">
                                            주당 ${price}원
                                        </span>
                                    </div>
                                    <div class="order-card__wait-order-actions">
                                        <button type="button" 
                                                class="order-card__wait-order-btn order-card__wait-order-btn--edit"
                                                data-order-id="${order.orderId}">수정</button>
                                        <button type="button" 
                                                class="order-card__wait-order-btn order-card__wait-order-btn--cancel"
                                                data-order-id="${order.orderId}">취소</button>
                                    </div>
                                </div>
                            `;
                        });
                        waitOrdersContainer.innerHTML = ordersHTML;
                        
                        // 취소 버튼 이벤트 리스너 재등록
                        attachCancelButtonListeners();
                    }
                }
            }
            
            // 주문 ID 목록 업데이트
            lastOrderIds = currentOrderIds;
            
        } catch (error) {
            console.error("[주문 업데이트] 오류:", error);
        }
    }

    // 폴링 시작 (5초마다 체크)
    function startOrderPolling() {
        if (orderPollingInterval) {
            clearInterval(orderPollingInterval);
        }
        
        orderPollingInterval = setInterval(updatePendingOrders, 5000); // 5초마다 업데이트
    }

    // 폴링 중지
    function stopOrderPolling() {
        if (orderPollingInterval) {
            clearInterval(orderPollingInterval);
            orderPollingInterval = null;
        }
    }

    // 페이지가 보일 때만 폴링 시작
    let isPageVisible = true;
    
    document.addEventListener("visibilitychange", function() {
        isPageVisible = !document.hidden;
        if (isPageVisible) {
            startOrderPolling();
        } else {
            stopOrderPolling();
        }
    });

    // 초기 폴링 시작
    startOrderPolling();
    
    // 페이지 언로드 시 폴링 중지
    window.addEventListener("beforeunload", function() {
        stopOrderPolling();
    });
});
