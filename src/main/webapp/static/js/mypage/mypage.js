document.addEventListener("DOMContentLoaded", function () {
    const textInputs = document.querySelectorAll(
        '.profile-card__item input[type="text"], input[type="password"], input[type="checkbox"]'
    );

    textInputs.forEach((e) => {
        e.addEventListener('input', updateSubmitButton);
        e.addEventListener('change', updateSubmitButton);
        e.addEventListener('keydown', (event) => {
            if (event.keyCode === 13) {
                event.preventDefault();
            }
        }, true);
    });

    function updateSubmitButton() {
        const submitButton = document.querySelector('.profile-card__submit input[type="submit"]');

        if (checkInputs(textInputs)) {
            submitButton.classList.add('active');
            submitButton.disabled = false;
        } else {
            submitButton.classList.remove('active');
            submitButton.disabled = true;
        }
    }

    updateSubmitButton();
});

function checkInputs(textInputs) {
    return Array.from(textInputs).some((e) => {
        if (e.type === 'text' || e.type === 'password') {
            return e.value.trim().length > 0;
        }
        else if (e.type === 'checkbox') {
            return e.checked;
        }
        return false;
    });
}