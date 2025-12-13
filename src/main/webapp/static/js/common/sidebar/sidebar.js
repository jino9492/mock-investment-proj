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
                    const profitSign = profitLoss > 0 ? "+" : "";
                    const rateSign = profitRate > 0 ? "+" : "";
                    
                    holdingsHTML += `
                        <li class="stock-box__container" data-stock-name="${holding.stockName}" data-stock-code="${holding.stockSymbol || ''}">
                            <div class="stock-box__company-profile"></div>
                            <div class="stock-box__company-wrapper">
                                <div class="stock-box__detail-wrapper">
                                    <div class="stock-box__company-name">${holding.stockName}</div>
                                    <div class="stock-box__stock-price">
                                        ${Math.abs(Number(holding.evaluationAmount) || 0).toLocaleString('ko-KR')}원
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

    // 관심종목 목록 업데이트 함수 (전역으로 노출)
    window.updateSidebarWatchlistFromAPI = async function() {
        const watchlistList = document.getElementById("sidebar-watchlist-list");
        if (!watchlistList) {
            console.warn("[사이드바 관심종목] sidebar-watchlist-list 요소를 찾을 수 없습니다.");
            return;
        }
        
        try {
            const response = await fetch("/api/watchlist");
            
            if (!response.ok) {
                console.warn("[사이드바 관심종목 업데이트] 서버 응답 오류:", response.status);
                return;
            }
            
            const result = await response.json();
            
            if (!result.success || !result.watchlistStocks) {
                console.warn("[사이드바 관심종목 업데이트] 응답 데이터 오류:", result);
                return;
            }
            
            const watchlistStocks = result.watchlistStocks;
            
            // 관심종목 목록 HTML 업데이트
            if (watchlistStocks.length === 0) {
                watchlistList.innerHTML = `
                    <li class="stock-box__empty">
                        <div style="text-align: center; padding: 20px; color: #999; font-size: 14px;">
                            관심종목이 없습니다.
                        </div>
                    </li>
                `;
            } else {
                let watchlistHTML = "";
                watchlistStocks.forEach(function(stock) {
                    const changePrice = stock.changePrice || 0;
                    const changeRate = stock.changeRate || 0;
                    const profitClass = changePrice > 0 ? "stock-box__comparison--positive" : 
                                      changePrice < 0 ? "stock-box__comparison--negative" : 
                                      "stock-box__comparison--neutral";
                    const priceSign = changePrice > 0 ? "+" : "";
                    const rateSign = changeRate > 0 ? "+" : "";
                    const currentPrice = stock.currentPrice || 0;
                    
                    watchlistHTML += `
                        <li class="stock-box__container" data-stock-name="${stock.stockName}" data-stock-code="${stock.stockSymbol || ''}" data-stock-id="${stock.stockId}">
                            <div class="stock-box__company-profile"></div>
                            <div class="stock-box__company-wrapper">
                                <div class="stock-box__detail-wrapper">
                                    <div class="stock-box__company-name">${stock.stockName}</div>
                                    <div class="stock-box__stock-price">
                                        ${currentPrice.toLocaleString('ko-KR')}원
                                    </div>
                                </div>
                                <div class="stock-box__detail-wrapper">
                                    <div class="stock-box__comparison ${profitClass}">
                                        ${priceSign}${Math.abs(changePrice).toLocaleString('ko-KR')}원
                                        (${rateSign}${Math.abs(changeRate).toFixed(2)}%)
                                    </div>
                                </div>
                            </div>
                        </li>
                    `;
                });
                watchlistList.innerHTML = watchlistHTML;
            }
            
        } catch (error) {
            console.error("[사이드바 관심종목 업데이트] 오류:", error);
        }
    };
    
    // 내부에서 사용하는 함수 (폴링용)
    async function updateSidebarWatchlist() {
        await window.updateSidebarWatchlistFromAPI();
    }

    // 폴링 시작 (5초마다 체크)
    function startSidebarPolling() {
        if (sidebarHoldingsPollingInterval) {
            clearInterval(sidebarHoldingsPollingInterval);
        }
        
        sidebarHoldingsPollingInterval = setInterval(() => {
            updateSidebarHoldings();
            updateSidebarAccount();
            updateSidebarWatchlist();
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

    // stock-box__container 클릭 이벤트 처리 (이벤트 위임 사용)
    document.addEventListener('click', function(e) {
        const stockContainer = e.target.closest('.stock-box__container');
        if (stockContainer) {
            const stockCode = stockContainer.getAttribute('data-stock-code');
            if (stockCode) {
                // 종목 코드로 주식 페이지로 이동
                window.location.href = `/stock?code=${stockCode}`;
            } else {
                // 종목 코드가 없으면 종목명으로 시도
                const stockName = stockContainer.getAttribute('data-stock-name');
                if (stockName) {
                    window.location.href = `/stock?name=${encodeURIComponent(stockName)}`;
                }
            }
        }
    });

    // 초기 로드 시 즉시 업데이트
    updateSidebarHoldings();
    updateSidebarAccount();
    updateSidebarWatchlist();
    
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