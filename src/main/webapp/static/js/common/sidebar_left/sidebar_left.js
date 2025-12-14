document.addEventListener("DOMContentLoaded", () => {
    // 사이드바 왼쪽의 모든 링크에 대해 클릭 이벤트 리스너 추가
    const sidebarLinks = document.querySelectorAll(".sidebar-left__btn");
    
    sidebarLinks.forEach(link => {
        link.addEventListener("click", function(e) {
            const href = this.getAttribute("href");
            
            // 절대 경로인 경우 쿼리 파라미터 없이 이동
            if (href && href.startsWith("/")) {
                // href에서 쿼리 파라미터 제거 (이미 없는 경우도 처리)
                const urlWithoutParams = href.split("?")[0];
                
                // 기본 동작을 막고 이벤트 전파도 막음
                e.preventDefault();
                e.stopPropagation();
                e.stopImmediatePropagation();
                
                // 쿼리 파라미터 없이 이동
                window.location.href = urlWithoutParams;
                return false;
            }
            // 상대 경로나 다른 형태의 링크는 기본 동작 유지
        });
    });
});
