document.addEventListener('DOMContentLoaded', () => {
    const sidebarBtn = document.querySelector('.sidebar__arrow-icon');
    const sidebar = document.querySelector('.sidebar__work-area')
    const investment = document.querySelector('.sidebar__my-investment')
    const investmentBtn = document.querySelector('.sidebar__investment-btn')
    const favorite = document.querySelector('.sidebar__favorite')
    const favoriteBtn = document.querySelector('.sidebar__favorite-btn')
    let isOpened = false;
    let isInvestmentSelected = true;
    let isFavoriteSelected = !isInvestmentSelected

    sidebarBtn.addEventListener('click', () => {
        isOpened = toggleElement(sidebarBtn, 'activated');
        console.log(sidebarBtn)
        sidebar.classList.toggle('activated', isOpened)
    });

    investmentBtn.addEventListener('click', () => {
        isInvestmentSelected = true;
        isFavoriteSelected = !isInvestmentSelected;
        investment.classList.toggle('selected', isInvestmentSelected);
        favorite.classList.toggle('selected', isFavoriteSelected);
    });

    favoriteBtn.addEventListener('click', () => {
        isFavoriteSelected = true;
        isInvestmentSelected = !isFavoriteSelected;
        investment.classList.toggle('selected', isInvestmentSelected);
        favorite.classList.toggle('selected', isFavoriteSelected);
    });
});

function toggleElement(element, str) {
    element.classList.toggle(str)
    return element.classList.contains(str);
}