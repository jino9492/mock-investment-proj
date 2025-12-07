document.addEventListener("DOMContentLoaded", function handleMyStockLoginButton() {
    const myStockLoginBtn = document.querySelector(".my-stock-card__login-btn");

    if (myStockLoginBtn) {
        myStockLoginBtn.addEventListener("click", function () {
            window.location.href = "/login";   // 로그인 URL에 맞게 수정
        });
    }
});
