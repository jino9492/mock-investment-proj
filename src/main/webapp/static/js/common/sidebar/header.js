document.addEventListener("DOMContentLoaded", function () {


    const profileBtn = document.querySelector(".header__profile-btn");
    const dropdown = document.getElementById("headerProfileDropdown");


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


    const searchButton = document.querySelector(".header__search-box");
    const overlay = document.getElementById("searchModalOverlay");
    const input = overlay ? overlay.querySelector(".search-modal__input") : null;

    if (searchButton && overlay) {
        function openSearchModal() {
            overlay.classList.add("search-modal-overlay--open");
            if (input) {
                setTimeout(() => input.focus(), 50);
            }
        }

        function closeSearchModal() {
            overlay.classList.remove("search-modal-overlay--open");
        }

        // 검색 버튼 클릭 시 모달 열기
        searchButton.addEventListener("click", function (e) {
            e.stopPropagation();
            openSearchModal();
        });

        // 오버레이 배경 클릭 시 닫기
        overlay.addEventListener("click", function () {
            closeSearchModal();
        });

        // 모달 내부 클릭은 닫히지 않도록
        const modalBox = overlay.querySelector(".search-modal");
        if (modalBox) {
            modalBox.addEventListener("click", function (e) {
                e.stopPropagation();
            });
        }

        // ESC 키로 닫기
        document.addEventListener("keydown", function (e) {
            if (e.key === "Escape") {
                closeSearchModal();
            }
        });
    }


    const STORAGE_KEY = "mock_recent_searches";
    const MAX_ITEMS = 10;

    const searchForm = document.querySelector(".search-modal__form");
    const searchInput = document.querySelector(".search-modal__input");
    const chipsContainer = document.getElementById("searchRecentChips");
    const clearBtn = document.querySelector(".search-recent__clear");

    if (!chipsContainer) {
        return;
    }

    // --- localStorage 헬퍼 ---
    function getHistory() {
        try {
            const raw = localStorage.getItem(STORAGE_KEY);
            const parsed = raw ? JSON.parse(raw) : [];
            return Array.isArray(parsed) ? parsed : [];
        } catch (e) {
            console.error("recent search parse error", e);
            return [];
        }
    }

    function setHistory(list) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(list));
    }

    function addHistory(keyword) {
        if (!keyword) return;
        let list = getHistory();

        // 중복 제거 후 맨 앞에 추가
        list = list.filter(item => item !== keyword);
        list.unshift(keyword);

        if (list.length > MAX_ITEMS) {
            list = list.slice(0, MAX_ITEMS);
        }

        setHistory(list);
        renderChips();
    }

    function clearHistory() {
        localStorage.removeItem(STORAGE_KEY);
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

        list.forEach(keyword => {
            const chipBtn = document.createElement("button");
            chipBtn.type = "button";
            chipBtn.className = "search-chip";
            chipBtn.textContent = keyword;


            chipBtn.addEventListener("click", function () {
                if (searchForm && searchInput) {
                    searchInput.value = keyword;
                    searchForm.submit(); // form action(/search) 그대로 사용
                } else {

                    const url = "/search?q=" + encodeURIComponent(keyword);
                    window.location.href = url;
                }
            });

            chipsContainer.appendChild(chipBtn);
        });
    }

    (function saveCurrentQueryFromUrl() {
        const params = new URLSearchParams(window.location.search);
        const q = params.get("q");
        if (q && q.trim() !== "") {
            addHistory(q.trim());
        }
    })();


    if (searchForm && searchInput) {
        searchForm.addEventListener("submit", function () {
            const keyword = searchInput.value.trim();
            if (keyword) {
                addHistory(keyword);
            }
        });
    }


    if (clearBtn) {
        clearBtn.addEventListener("click", clearHistory);
    }


    renderChips();
});
