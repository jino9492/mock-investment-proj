// Modal + delete flows for member detail page
(() => {
    const modal = document.getElementById('member-edit-modal');
    const openBtn = document.querySelector('[data-detail-modal-open="member-edit-modal"]');
    const closeBtns = modal ? modal.querySelectorAll('[data-modal-close]') : [];
    if (modal && openBtn) {
        const openModal = () => {
            modal.classList.add('is-open');
            modal.setAttribute('aria-hidden', 'false');
        };
        const closeModal = () => {
            modal.classList.remove('is-open');
            modal.setAttribute('aria-hidden', 'true');
        };
        openBtn.addEventListener('click', openModal);
        closeBtns.forEach((btn) => btn.addEventListener('click', closeModal));
        modal.addEventListener('keydown', (event) => {
            if (event.key === 'Escape') {
                closeModal();
            }
        });
    }

    const deleteTrigger = document.getElementById('member-delete-trigger');
    const deleteForm = document.getElementById('member-delete-form');
    if (deleteTrigger && deleteForm) {
        deleteTrigger.addEventListener('click', () => {
            const confirmDelete = window.confirm('정말로 회원을 탈퇴 처리하시겠습니까? 이 작업은 되돌릴 수 없습니다.');
            if (confirmDelete) {
                deleteForm.submit();
            }
        });
    }
})();

