document.addEventListener("DOMContentLoaded", function () {
    const deleteTrigger = document.querySelector('.member-delete-trigger');
    const deleteForm = document.querySelector('.member-delete-form');
    if (deleteTrigger && deleteForm) {
        deleteTrigger.addEventListener('click', () => {
            const confirmDelete = window.confirm('정말로 회원을 탈퇴 처리하시겠습니까? 이 작업은 되돌릴 수 없습니다.');
            if (confirmDelete) {
                deleteForm.submit();
            }
        });
    }

    const textInputs = document.querySelectorAll(
        '.profile-card__item input[type="text"], input[type="password"]'
    );

    const checkInput = document.querySelector('.toggle__checkbox');
    console.log(checkInput)

    textInputs.forEach((e) => {
        e.addEventListener('input', updateSubmitButton);
        e.addEventListener('change', updateSubmitButton);
        e.addEventListener('keydown', (event) => {
            if (event.keyCode === 13) {
                event.preventDefault();
            }
        }, true);
    });

    if (checkInput) {
        checkInput.addEventListener('change', updateSubmitButton);
    }

    function updateSubmitButton() {
        const submitButton = document.querySelector('.profile-card__submit input[type="submit"]');

        const allInputs = Array.from(textInputs);
        if (checkInput) {
            allInputs.push(checkInput);
        }

        if (checkInputs(allInputs)) {
            submitButton.classList.add('active');
            submitButton.disabled = false;
        } else {
            submitButton.classList.remove('active');
            submitButton.disabled = true;
        }
    }

    updateSubmitButton();
});

function checkInputs(allInputs) {
    return Array.from(allInputs).find((e) => {

        if (e.type === 'text' || e.type === 'password') {
            return e.value.trim().length > 0;
        }

        else if (e.type === 'checkbox') {

            // ⭐ 이 부분이 요청하신 '기본 상태의 반대' 로직입니다. ⭐
            const isDefaultChecked = e.defaultChecked;
            return e.checked !== isDefaultChecked;
        }
        return false;
    });
}