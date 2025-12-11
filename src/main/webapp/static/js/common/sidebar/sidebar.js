document.addEventListener('DOMContentLoaded', () => {
    const sidebarBtn = document.querySelector('.sidebar__arrow-icon');
    const sidebar = document.querySelector('.sidebar__work-area')
    const investment = document.querySelector('.sidebar__my-investment')
    const investmentBtn = document.querySelector('.sidebar__investment-btn')
    const favorite = document.querySelector('.sidebar__favorite')
    const favoriteBtn = document.querySelector('.sidebar__favorite-btn')
    let isOpened = false;
    let isInvestmentSelected = true;
    let isFavoriteSelected = !isInvestmentSelected

    sidebarBtn.addEventListener('click', () => {
        isOpened = toggleElement(sidebarBtn, 'activated');
        console.log(sidebarBtn)
        sidebar.classList.toggle('activated', isOpened)
    });

    investmentBtn.addEventListener('click', () => {
        isInvestmentSelected = true;
        isFavoriteSelected = !isInvestmentSelected;
        investment.classList.toggle('selected', isInvestmentSelected);
        favorite.classList.toggle('selected', isFavoriteSelected);
    });

    favoriteBtn.addEventListener('click', () => {
        isFavoriteSelected = true;
        isInvestmentSelected = !isFavoriteSelected;
        investment.classList.toggle('selected', isInvestmentSelected);
        favorite.classList.toggle('selected', isFavoriteSelected);
    });

    /* sidebar 보유 종목 실시간 업데이트 (폴링) */
    let sidebarHoldingsPollingInterval = null;
    
    // 보유 종목 목록 업데이트 함수
    async function updateSidebarHoldings() {
        const holdingsList = document.getElementById("sidebar-holdings-list");
        if (!holdingsList) return;
        
        try {
            const response = await fetch("/api/holding");
            
            if (!response.ok) {
                console.warn("[사이드바 보유 종목 업데이트] 서버 응답 오류:", response.status);
                return;
            }
            
            const result = await response.json();
            
            if (!result.success || !result.holdings) {
                console.warn("[사이드바 보유 종목 업데이트] 응답 데이터 오류:", result);
                return;
            }
            
            const holdings = result.holdings;
            
            // 보유 종목 목록 HTML 업데이트
            if (holdings.length === 0) {
                holdingsList.innerHTML = `
                    <li class="stock-box__empty">
                        <div style="text-align: center; padding: 20px; color: #999;">
                            보유 종목이 없습니다.
                        </div>
                    </li>
                `;
            } else {
                let holdingsHTML = "";
                holdings.forEach(function(holding) {
                    const profitLoss = holding.profitLoss || 0;
                    const profitRate = holding.profitRate || 0;
                    const profitClass = profitLoss > 0 ? "stock-box__comparison--positive" : 
                                      profitLoss < 0 ? "stock-box__comparison--negative" : 
                                      "stock-box__comparison--neutral";
                    const profitSign = profitLoss > 0 ? "+" : profitLoss < 0 ? "-" : "";
                    const rateSign = profitRate > 0 ? "+" : profitRate < 0 ? "-" : "";
                    
                    holdingsHTML += `
                        <li class="stock-box__container" data-stock-name="${holding.stockName}">
                            <div class="stock-box__company-profile"></div>
                            <div class="stock-box__company-wrapper">
                                <div class="stock-box__detail-wrapper">
                                    <div class="stock-box__company-name">${holding.stockName}</div>
                                    <div class="stock-box__stock-price">
                                        ${(holding.currentPrice || 0).toLocaleString('ko-KR')}원
                                    </div>
                                </div>
                                <div class="stock-box__detail-wrapper">
                                    <div class="stock-box__stock-count">
                                        ${(holding.quantity || 0).toLocaleString('ko-KR')}주
                                    </div>
                                    <div class="stock-box__comparison ${profitClass}">
                                        ${profitSign}${Math.abs(profitLoss).toLocaleString('ko-KR')}원
                                        (${rateSign}${Math.abs(profitRate).toFixed(2)}%)
                                    </div>
                                </div>
                            </div>
                        </li>
                    `;
                });
                holdingsList.innerHTML = holdingsHTML;
            }
            
        } catch (error) {
            console.error("[사이드바 보유 종목 업데이트] 오류:", error);
        }
    }

    // 계좌 잔액 업데이트 함수
    async function updateSidebarAccount() {
        const accountBox = document.getElementById("sidebar-account-amount");
        if (!accountBox) return;
        
        try {
            const response = await fetch("/api/order");
            
            if (!response.ok) {
                return;
            }
            
            const result = await response.json();
            
            if (result.success && result.availableCash !== undefined) {
                accountBox.textContent = result.availableCash.toLocaleString('ko-KR') + "원";
            }
        } catch (error) {
            console.error("[사이드바 계좌 업데이트] 오류:", error);
        }
    }

    // 폴링 시작 (5초마다 체크)
    function startSidebarPolling() {
        if (sidebarHoldingsPollingInterval) {
            clearInterval(sidebarHoldingsPollingInterval);
        }
        
        sidebarHoldingsPollingInterval = setInterval(() => {
            updateSidebarHoldings();
            updateSidebarAccount();
        }, 5000); // 5초마다 업데이트
    }

    // 폴링 중지
    function stopSidebarPolling() {
        if (sidebarHoldingsPollingInterval) {
            clearInterval(sidebarHoldingsPollingInterval);
            sidebarHoldingsPollingInterval = null;
        }
    }

    // 페이지가 보일 때만 폴링 시작
    let isPageVisible = true;
    
    document.addEventListener("visibilitychange", function() {
        isPageVisible = !document.hidden;
        if (isPageVisible) {
            startSidebarPolling();
        } else {
            stopSidebarPolling();
        }
    });

    // 초기 로드 시 즉시 업데이트
    updateSidebarHoldings();
    updateSidebarAccount();
    
    // 초기 폴링 시작
    startSidebarPolling();
    
    // 페이지 언로드 시 폴링 중지
    window.addEventListener("beforeunload", function() {
        stopSidebarPolling();
    });
});

function toggleElement(element, str) {
    element.classList.toggle(str)
    return element.classList.contains(str);
}