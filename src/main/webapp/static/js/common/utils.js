export const STORAGE_KEY = 'realtimeStockData';
export function saveRealtimeData(stkCode, price, rate, changePrice=null) {
    let storedData = sessionStorage.getItem(STORAGE_KEY);
    storedData = storedData ? JSON.parse(storedData) : {};

    storedData[stkCode] = {
        price: price,
        rate: rate,
        changePrice: changePrice,
        timestamp: new Date().getTime()
    };

    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(storedData));
}