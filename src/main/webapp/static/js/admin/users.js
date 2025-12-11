// Modal handler for admin user creation
(() => {
    const modal = document.getElementById('member-create-modal');
    const openButton = document.querySelector('[data-modal-open="member-create-modal"]');
    const closeTargets = modal ? modal.querySelectorAll('[data-modal-close]') : [];
    const form = document.getElementById('member-create-form');
    const privacyCheckbox = form ? form.querySelector('input[name="agreePrivacy"]') : null;

    if (!modal || !openButton || !form) {
        return;
    }

    const openModal = () => {
        modal.classList.add('is-open');
        modal.setAttribute('aria-hidden', 'false');
        form.reset();
        if (privacyCheckbox) {
            privacyCheckbox.checked = true;
        }
    };

    const closeModal = () => {
        modal.classList.remove('is-open');
        modal.setAttribute('aria-hidden', 'true');
    };

    openButton.addEventListener('click', openModal);
    closeTargets.forEach((target) => target.addEventListener('click', closeModal));
    modal.addEventListener('keydown', (event) => {
        if (event.key === 'Escape') {
            closeModal();
        }
    });
})();

