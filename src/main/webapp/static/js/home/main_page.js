document.addEventListener('DOMContentLoaded', function () {
    const body = document.body;
    const headerLoginBtn = document.querySelector('.main__btn-login');

    const popupLoginBtn = document.querySelector('.login__modal-login');
    const popupCloseBtn = document.querySelector('.login__modal-signup');

 
    body.classList.add('logged-out');

    headerLoginBtn?.addEventListener('click', function () {
      body.classList.add('logged-out');
    });

    popupLoginBtn?.addEventListener('click', function () {
      body.classList.remove('logged-out');
      body.classList.add('logged-in');
    });

    popupCloseBtn?.addEventListener('click', function () {
      body.classList.remove('logged-out');
    });
});
