
document.addEventListener("DOMContentLoaded", function () {


    const profileBtn = document.querySelector(".header__profile-btn");
    const dropdown = document.getElementById("headerProfileDropdown");
    const profileMenuUser = document.getElementById("profileMenuUser");


    if (profileBtn && dropdown) {
        // 버튼 클릭하면 열기/닫기
        profileBtn.addEventListener("click", function (e) {
            e.stopPropagation();
            dropdown.classList.toggle("header__profile-dropdown--open");
        });

        // 드롭다운 안 클릭 시 버블링 막기
        dropdown.addEventListener("click", function (e) {
            e.stopPropagation();
        });

        // 문서 아무 데나 클릭하면 닫기
        document.addEventListener("click", function () {
            dropdown.classList.remove("header__profile-dropdown--open");
        });
    }

    // 프로필 메뉴 유저 항목 클릭 시 마이페이지로 이동 (뱃지 링크가 아닌 전체 영역 클릭 시)
    if (profileMenuUser) {
        profileMenuUser.addEventListener("click", function (e) {
            // 뱃지 링크를 클릭한 경우는 기본 동작(링크 이동)을 허용
            if (e.target.classList.contains("profile-menu__badge") || e.target.closest(".profile-menu__badge")) {
                return;
            }
            // 그 외의 영역을 클릭한 경우 마이페이지로 이동
            e.preventDefault();
            e.stopPropagation();
            window.location.href = "/mypage";
        });
    }


    // 검색 모달 팝업
    const searchButton = document.querySelector(".header__search-box");
    const overlay = document.getElementById("searchModalOverlay");
    const modalBox = overlay ? overlay.querySelector(".search-modal") : null;
    const input = overlay ? overlay.querySelector(".search-modal__input") : null;
    const searchClearBtn = document.getElementById("searchModalClearBtn");

    // 섹션 표시/숨김 함수들 (먼저 정의)
    const searchResultsSection = document.getElementById("searchResultsSection");
    const searchRecentSection = document.getElementById("searchRecentSection");
    const searchPopularSection = document.getElementById("searchPopularSection");

    function showDefaultSections() {
        if (searchResultsSection) searchResultsSection.style.display = "none";
        if (searchRecentSection) searchRecentSection.style.display = "block";
        if (searchPopularSection) searchPopularSection.style.display = "block";
    }

    function showSearchResultsSection() {
        if (searchResultsSection) searchResultsSection.style.display = "block";
        if (searchRecentSection) searchRecentSection.style.display = "none";
        if (searchPopularSection) searchPopularSection.style.display = "none";
    }

    if (searchButton && overlay) {
        // 모달 열기 함수
        function openSearchModal() {
            overlay.classList.add("search-modal-overlay--open");
            document.body.style.overflow = "hidden"; // 스크롤 방지
            if (input) {
                input.value = "";
                input.setAttribute("aria-expanded", "false");
                setTimeout(() => {
                    input.focus();
                }, 100);
            }
            // 지우기 버튼 숨김
            if (searchClearBtn) {
                searchClearBtn.style.display = "none";
            }
            // 기본 섹션 표시 (최근 검색, 인기 종목)
            showDefaultSections();
        }

        // 모달 닫기 함수
        function closeSearchModal() {
            overlay.classList.remove("search-modal-overlay--open");
            document.body.style.overflow = ""; // 스크롤 복원
            if (input) {
                input.value = "";
                input.blur();
            }
            // 지우기 버튼 숨김
            if (searchClearBtn) {
                searchClearBtn.style.display = "none";
            }
            // 기본 섹션 표시로 복원
            showDefaultSections();
        }

        // 검색 버튼 클릭 시 모달 열기
        searchButton.addEventListener("click", function (e) {
            e.preventDefault();
            e.stopPropagation();
            openSearchModal();
        });

        // 오버레이 배경 클릭 시 닫기
        overlay.addEventListener("click", function (e) {
            if (e.target === overlay) {
                closeSearchModal();
            }
        });

        // 모달 내부 클릭은 닫히지 않도록
        if (modalBox) {
            modalBox.addEventListener("click", function (e) {
                e.stopPropagation();
            });
        }

        // ESC 키로 닫기
        document.addEventListener("keydown", function (e) {
            if (e.key === "Escape" && overlay.classList.contains("search-modal-overlay--open")) {
                closeSearchModal();
            }
        });
    }


    const STORAGE_KEY = "mock_recent_searches";
    const MAX_ITEMS = 10;

    const searchForm = document.querySelector(".search-modal__form");
    const searchInput = document.querySelector(".search-modal__input");
    const chipsContainer = document.getElementById("searchRecentChips");
    const searchResultsContainer = document.getElementById("searchResults");

    if (!chipsContainer) {
        return;
    }

    // --- localStorage 헬퍼 (종목명과 종목코드를 객체로 저장) ---
    function getHistory() {
        try {
            const raw = localStorage.getItem(STORAGE_KEY);
            if (!raw) return [];
            const parsed = JSON.parse(raw);
            
            // 기존 문자열 배열을 객체 배열로 변환 (마이그레이션)
            if (Array.isArray(parsed) && parsed.length > 0) {
                if (typeof parsed[0] === 'string') {
                    // 기존 형식이면 빈 배열 반환 (새 형식으로 전환)
                    return [];
                }
                return parsed;
            }
            return [];
        } catch (e) {
            return [];
        }
    }

    function setHistory(list) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
    }

    function addHistory(stockName, stockSymbol) {
        if (!stockName || !stockSymbol) return;
        let list = getHistory();

        // 중복 제거 (종목코드 기준)
        list = list.filter(item => item.stockSymbol !== stockSymbol);
        
        // 맨 앞에 추가
        list.unshift({ stockName: stockName, stockSymbol: stockSymbol });

        if (list.length > MAX_ITEMS) {
            list = list.slice(0, MAX_ITEMS);
        }

        setHistory(list);
        renderChips();
    }

    function removeHistory(stockSymbol) {
        if (!stockSymbol) return;
        let list = getHistory();
        
        // 종목코드 기준으로 제거
        list = list.filter(item => item.stockSymbol !== stockSymbol);
        
        setHistory(list);
        renderChips();
    }

    function renderChips() {
        const list = getHistory();
        chipsContainer.innerHTML = "";

        if (!list.length) {
            const span = document.createElement("span");
            span.style.fontSize = "12px";
            span.style.color = "#aaa";
            span.textContent = "최근 검색어가 없습니다.";
            chipsContainer.appendChild(span);
            return;
        }

        list.forEach(item => {
            const chipBtn = document.createElement("button");
            chipBtn.type = "button";
            chipBtn.className = "search-chip";
            chipBtn.style.display = "flex";
            chipBtn.style.alignItems = "center";
            
            // 종목명 텍스트
            const chipText = document.createElement("span");
            chipText.textContent = item.stockName || item;
            
            // 삭제 버튼
            const deleteBtn = document.createElement("img");
            deleteBtn.src = "/static/assets/x.png";
            deleteBtn.alt = "삭제";
            deleteBtn.className = "search-chip__delete";
            deleteBtn.style.cursor = "pointer";
            deleteBtn.style.marginLeft = "6px";
            deleteBtn.style.width = "14px";
            deleteBtn.style.height = "14px";
            
            // 삭제 버튼 클릭 시
            deleteBtn.addEventListener("click", function (e) {
                e.stopPropagation(); // 버블링 방지
                const symbol = item.stockSymbol || item;
                removeHistory(symbol);
            });
            
            chipBtn.appendChild(chipText);
            chipBtn.appendChild(deleteBtn);

            chipBtn.addEventListener("click", function (e) {
                // 삭제 버튼 클릭이 아닐 때만 페이지 이동
                if (e.target !== deleteBtn && !deleteBtn.contains(e.target)) {
                    const symbol = item.stockSymbol || item;
                    window.location.href = "/stock?code=" + encodeURIComponent(symbol);
                }
            });

            chipsContainer.appendChild(chipBtn);
        });
    }

    // --- 실시간 검색 기능 ---
    let searchTimeout = null;
    let selectedResultIndex = -1; // 현재 선택된 검색 결과 인덱스

    if (searchInput) {
        // 지우기 버튼 표시/숨김 함수
        function updateClearButton() {
            if (searchClearBtn) {
                if (searchInput.value.trim() !== "") {
                    searchClearBtn.style.display = "flex";
                } else {
                    searchClearBtn.style.display = "none";
                }
            }
        }

        // 지우기 버튼 클릭 이벤트
        if (searchClearBtn) {
            searchClearBtn.addEventListener("click", function (e) {
                e.preventDefault();
                e.stopPropagation();
                searchInput.value = "";
                searchInput.focus();
                updateClearButton();
                showDefaultSections();
                
                // 기존 타이머 취소
                if (searchTimeout) {
                    clearTimeout(searchTimeout);
                }
            });
        }

        searchInput.addEventListener("input", function (e) {
            const query = e.target.value.trim();

            // 지우기 버튼 업데이트
            updateClearButton();

            // 기존 타이머 취소
            if (searchTimeout) {
                clearTimeout(searchTimeout);
            }

            if (query === "") {
                // 입력이 비어있으면 최근 검색과 인기 종목 표시
                showDefaultSections();
                if (searchInput) {
                    searchInput.setAttribute("aria-expanded", "false");
                }
            } else {
                // 300ms debounce 후 검색 실행
                searchTimeout = setTimeout(() => {
                    performSearch(query);
                }, 300);
            }
        });

        // 폼 제출 방지 (실시간 검색 사용)
        if (searchForm) {
            searchForm.addEventListener("submit", function (e) {
                e.preventDefault();
                const query = searchInput.value.trim();
                if (query) {
                    performSearch(query);
                }
            });
        }

        // 키보드 네비게이션 (Arrow Up/Down, Enter)
        searchInput.addEventListener("keydown", function (e) {
            const resultItems = searchResultsContainer.querySelectorAll(".search-modal__result-item");
            
            if (resultItems.length === 0) {
                return;
            }

            if (e.key === "ArrowDown") {
                e.preventDefault();
                selectedResultIndex = Math.min(selectedResultIndex + 1, resultItems.length - 1);
                updateSelectedResult(resultItems);
            } else if (e.key === "ArrowUp") {
                e.preventDefault();
                selectedResultIndex = Math.max(selectedResultIndex - 1, -1);
                if (selectedResultIndex === -1) {
                    // 검색 입력 필드로 포커스 복귀
                    searchInput.focus();
                    updateSelectedResult(resultItems);
                } else {
                    updateSelectedResult(resultItems);
                }
            } else if (e.key === "Enter" && selectedResultIndex >= 0) {
                e.preventDefault();
                const selectedItem = resultItems[selectedResultIndex];
                if (selectedItem) {
                    selectedItem.click();
                }
            } else if (e.key === "Escape") {
                // ESC 키는 모달 닫기로 처리 (이미 구현됨)
            }
        });
    }

    // 선택된 검색 결과 업데이트
    function updateSelectedResult(resultItems) {
        resultItems.forEach((item, index) => {
            if (index === selectedResultIndex) {
                item.setAttribute("aria-selected", "true");
                item.focus();
                item.scrollIntoView({ block: "nearest", behavior: "smooth" });
            } else {
                item.setAttribute("aria-selected", "false");
            }
        });
    }

    function performSearch(query) {
        if (!query || !searchResultsContainer) return;

        showSearchResultsSection();
        
        // aria-expanded 업데이트
        if (searchInput) {
            searchInput.setAttribute("aria-expanded", "true");
        }

        // 로딩 표시
        const loadingLi = document.createElement("li");
        loadingLi.className = "search-modal__status-message";
        loadingLi.textContent = "검색 중...";
        searchResultsContainer.innerHTML = "";
        searchResultsContainer.appendChild(loadingLi);

        fetch("/api/search?q=" + encodeURIComponent(query))
            .then(response => {
                if (!response.ok) {
                    throw new Error("Network response was not ok");
                }
                return response.json();
            })
            .then(data => {
                renderSearchResults(data.results || []);
            })
            .catch(error => {
                const errorLi = document.createElement("li");
                errorLi.className = "search-modal__status-message";
                errorLi.textContent = "검색 중 오류가 발생했습니다.";
                searchResultsContainer.innerHTML = "";
                searchResultsContainer.appendChild(errorLi);
            });
    }

    function renderSearchResults(results) {
        searchResultsContainer.innerHTML = "";
        selectedResultIndex = -1; // 검색 결과 렌더링 시 선택 인덱스 초기화

        if (!results || results.length === 0) {
            const li = document.createElement("li");
            li.className = "search-modal__status-message";
            li.textContent = "검색 결과가 없습니다.";
            searchResultsContainer.appendChild(li);
            return;
        }

        results.forEach((stock, index) => {
            const li = document.createElement("li");
            li.className = "search-modal__result-item";
            li.setAttribute("role", "option");
            li.setAttribute("tabindex", "0");
            li.setAttribute("aria-selected", "false");
            
            // aria-label: 종목명, 종목코드, 현재가, 등락률 정보 포함
            const priceValue = stock.currentPrice || stock.current_price || "-";
            const changeRateStr = stock.changeRate || stock.change_rate || "0.00%";
            const ariaLabel = `${stock.stockName} ${stock.stockSymbol}, 현재가 ${priceValue}, 등락률 ${changeRateStr}`;
            li.setAttribute("aria-label", ariaLabel);

            // 클릭 시 종목 상세 페이지로 이동 및 최근 검색에 추가
            li.addEventListener("click", function () {
                addHistory(stock.stockName, stock.stockSymbol);
                window.location.href = "/stock?code=" + encodeURIComponent(stock.stockSymbol);
            });

            // Enter 키로도 선택 가능
            li.addEventListener("keydown", function (e) {
                if (e.key === "Enter" || e.key === " ") {
                    e.preventDefault();
                    li.click();
                }
            });

            // 회사 로고
            const logo = document.createElement("div");
            logo.className = "search-modal__result-logo";

            // 종목 정보
            const info = document.createElement("div");
            info.className = "search-modal__result-info";

            const name = document.createElement("div");
            name.className = "search-modal__result-name";
            name.textContent = stock.stockName || "";

            const code = document.createElement("div");
            code.className = "search-modal__result-code";
            code.textContent = stock.stockSymbol || "";

            info.appendChild(name);
            info.appendChild(code);

            // 가격 정보
            const price = document.createElement("div");
            price.className = "search-modal__result-price";

            const currentPrice = document.createElement("div");
            currentPrice.className = "search-modal__result-current-price";
            currentPrice.textContent = priceValue;

            const changeRate = document.createElement("div");
            changeRate.className = "search-modal__result-change-rate";
            
            // 서버에서 이미 포맷팅된 문자열을 받음 (예: "+2.70%", "-3.26%", "0.00%")
            // Gson이 필드명을 그대로 유지하므로 currentPrice, changeRate로 접근
            // changeRateStr은 위에서 이미 선언됨
            
            // 서버에서 이미 포맷팅된 값을 그대로 사용
            changeRate.textContent = changeRateStr;
            
            // % 기호 제거 후 숫자 파싱하여 색상 클래스 결정
            const rateValue = parseFloat(changeRateStr.replace("%", "").replace("+", "")) || 0;
            
            if (rateValue > 0) {
                changeRate.classList.add("search-modal__result-change-rate--up");
            } else if (rateValue < 0) {
                changeRate.classList.add("search-modal__result-change-rate--down");
            } else {
                changeRate.classList.add("search-modal__result-change-rate--neutral");
            }

            price.appendChild(currentPrice);
            price.appendChild(changeRate);

            li.appendChild(logo);
            li.appendChild(info);
            li.appendChild(price);
            searchResultsContainer.appendChild(li);
        });

        // 검색 결과 컨테이너에 role="listbox" 추가 (이미 JSP에서 설정됨)
        // aria-label은 이미 JSP에서 설정됨
    }


    renderChips();

    // 인기 종목 (실시간 차트 상위 3개) 로드
    const popularStocksContainer = document.getElementById("searchPopularStocks");
    if (popularStocksContainer) {
        loadPopularStocks();
    }

    function loadPopularStocks() {
        fetch("/api/rank/items")
            .then(response => {
                if (!response.ok) {
                    throw new Error("Network response was not ok");
                }
                return response.json();
            })
            .then(data => {
                // 상위 3개만 가져오기
                const top3 = data.slice(0, 3);
                renderPopularStocks(top3);
            })
            .catch(error => {
                // 에러 시 빈 메시지 표시
                const errorLi = document.createElement("li");
                errorLi.className = "search-modal__status-message--small";
                errorLi.textContent = "데이터를 불러올 수 없습니다.";
                popularStocksContainer.innerHTML = "";
                popularStocksContainer.appendChild(errorLi);
            });
    }

    function renderPopularStocks(stocks) {
        popularStocksContainer.innerHTML = "";

        if (!stocks || stocks.length === 0) {
            const li = document.createElement("li");
            li.className = "search-modal__status-message--small";
            li.textContent = "인기 종목이 없습니다.";
            popularStocksContainer.appendChild(li);
            return;
        }

        stocks.forEach((stock, index) => {
            const li = document.createElement("li");
            li.className = "search-modal__list-item";
            li.style.cursor = "pointer";

            // 클릭 시 종목 상세 페이지로 이동
            li.addEventListener("click", function() {
                window.location.href = "/stock?code=" + encodeURIComponent(stock.stkCode);
            });

            const rank = document.createElement("span");
            rank.className = "search-modal__rank";
            rank.textContent = stock.nowRank || (index + 1);

            const name = document.createElement("span");
            name.className = "search-modal__name";
            name.textContent = stock.stkName || "";

            const change = document.createElement("span");
            change.className = "search-modal__change";
            const fluRt = stock.fluRt || 0;
            const fluRtValue = parseFloat(fluRt);
            
            // 등락률에 따라 색상 및 부호 설정
            if (fluRtValue > 0) {
                change.textContent = "+" + fluRtValue.toFixed(2) + "%";
                change.style.color = "#E74C3C"; // 빨간색 (상승)
            } else if (fluRtValue < 0) {
                change.textContent = fluRtValue.toFixed(2) + "%";
                change.style.color = "#4A90E2"; // 파란색 (하락)
            } else {
                change.textContent = "0.00%";
                change.style.color = "#999"; // 회색 (보합)
            }

            li.appendChild(rank);
            li.appendChild(name);
            li.appendChild(change);
            popularStocksContainer.appendChild(li);
        });
    }
});
