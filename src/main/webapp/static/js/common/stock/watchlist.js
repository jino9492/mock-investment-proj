/**
 * 관심종목 체크박스 이벤트 처리
 */

// sidebar 관심종목 목록 업데이트 함수
function updateSidebarWatchlist(watchlistStocks) {
    console.log("[관심종목] updateSidebarWatchlist 호출됨, 데이터:", watchlistStocks);
    
    // 요소를 찾을 때까지 최대 5번 시도 (DOM 로딩 대기)
    function tryUpdate() {
        const watchlistList = document.getElementById("sidebar-watchlist-list");
        if (!watchlistList) {
            console.warn("[관심종목] sidebar-watchlist-list 요소를 찾을 수 없습니다. 재시도 중...");
            return false;
        }
            if (!watchlistStocks || watchlistStocks.length === 0) {
            watchlistList.innerHTML = `
                <li class="stock-box__empty">
                    <div style="text-align: center; padding: 20px; color: #999; font-size: 14px;">
                        관심종목이 없습니다.
                    </div>
                </li>
            `;
            return true;
        }
        
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
        return true;
    }
    
    // 최대 5번 시도
    let attempts = 0;
    const maxAttempts = 5;
    const interval = setInterval(() => {
        attempts++;
        if (tryUpdate() || attempts >= maxAttempts) {
            clearInterval(interval);
            if (attempts >= maxAttempts && !document.getElementById("sidebar-watchlist-list")) {
                console.error("[관심종목] sidebar-watchlist-list 요소를 찾을 수 없습니다.");
            }
        }
    }, 100);
    
    // 즉시 한 번 시도
    if (tryUpdate()) {
        clearInterval(interval);
    }
}

document.addEventListener("DOMContentLoaded", function() {
    const checkbox = document.getElementById("stock-header-checkbox");
    const stockHeader = document.querySelector(".stock-header");
    
    if (!checkbox || !stockHeader) {
        return;
    }
    
    const stockId = stockHeader.getAttribute("data-stock-id");
    if (!stockId) {
        // stockId가 없으면 체크박스 비활성화
        checkbox.disabled = true;
        return;
    }
    
    // 체크박스 클릭 이벤트
    checkbox.addEventListener("change", async function() {
        const isChecked = this.checked;
        
        try {
            const params = new URLSearchParams();
            params.append("stockId", stockId);
            
            let response;
            if (isChecked) {
                // 관심종목 추가
                response = await fetch("/api/watchlist", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
                    },
                    body: params.toString()
                });
            } else {
                // 관심종목 삭제
                response = await fetch("/api/watchlist?" + params.toString(), {
                    method: "DELETE",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
                    }
                });
            }
            
            const result = await response.json();
            console.log("[관심종목] 응답:", result);
            
            if (!result.success) {
                // 실패 시 체크박스 상태 되돌리기
                this.checked = !isChecked;
                
                if (response.status === 401) {
                    alert("로그인이 필요합니다.");
                } else {
                    alert(result.message || "오류가 발생했습니다.");
                }
            } else {
                // 성공 시 sidebar 관심종목 목록 업데이트
                console.log("[관심종목] 업데이트할 목록:", result.watchlistStocks);
                if (result.watchlistStocks) {
                    // watchlist.js의 로컬 함수로 직접 업데이트 시도
                    updateSidebarWatchlist(result.watchlistStocks);
                    // sidebar.js의 API 호출 함수도 호출 (폴백)
                    if (typeof window.updateSidebarWatchlistFromAPI === 'function') {
                        setTimeout(() => {
                            window.updateSidebarWatchlistFromAPI();
                        }, 100);
                    }
                } else {
                    console.warn("[관심종목] watchlistStocks가 응답에 없습니다.");
                    // API를 통해 다시 가져오기
                    if (typeof window.updateSidebarWatchlistFromAPI === 'function') {
                        window.updateSidebarWatchlistFromAPI();
                    }
                }
            }
        } catch (error) {
            console.error("[관심종목] 오류:", error);
            // 오류 발생 시 체크박스 상태 되돌리기
            this.checked = !isChecked;
            alert("네트워크 오류가 발생했습니다. 다시 시도해주세요.");
        }
    });
});
