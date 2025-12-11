
function initSignupAgreements() {

    const allRequiredCheckbox = document.querySelector(
        '.signup__agreement-all .signup__agreement-checkbox'
    );

    const agreementCheckboxes = document.querySelectorAll(
        '.signup__agreement-list .signup__agreement-checkbox'
    );

    if (!allRequiredCheckbox || agreementCheckboxes.length === 0) {
        return; // 요소를 찾지 못한 경우 종료
    }

    const requiredCheckboxes = Array.from(agreementCheckboxes).slice(0, 2);

    function toggleRequiredCheckboxes(checked) {
        requiredCheckboxes.forEach(function (checkbox) {
            checkbox.checked = checked;
        });
    }

    function syncAllRequiredCheckbox() {
        const allChecked = requiredCheckboxes.every(function (checkbox) {
            return checkbox.checked;
        });
        allRequiredCheckbox.checked = allChecked;
    }

    allRequiredCheckbox.addEventListener('change', function () {
        toggleRequiredCheckboxes(allRequiredCheckbox.checked);
    });

    requiredCheckboxes.forEach(function (checkbox) {
        checkbox.addEventListener('change', function () {
            syncAllRequiredCheckbox();
        });
    });
}

document.addEventListener('DOMContentLoaded', function () {
    initSignupAgreements();
});
