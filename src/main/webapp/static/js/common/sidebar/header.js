
    document.addEventListener("DOMContentLoaded", function () {
    const profileBtn = document.querySelector(".header__profile-btn");
    const dropdown   = document.getElementById("headerProfileDropdown");

    if (!profileBtn || !dropdown) return;

    // 버튼 클릭하면 열기/닫기
    profileBtn.addEventListener("click", function (e) {
    e.stopPropagation();
    dropdown.classList.toggle("header__profile-dropdown--open");
});

    // 드롭다운 안 클릭 시 버블링 막기 (안 하면 내부 클릭해도 닫힘)
    dropdown.addEventListener("click", function (e) {
    e.stopPropagation();
});

    // 문서 아무 데나 클릭하면 닫기
    document.addEventListener("click", function () {
    dropdown.classList.remove("header__profile-dropdown--open");
});
});
