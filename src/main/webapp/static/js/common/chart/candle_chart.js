import {fetchStockChartData} from "../api/fetch_utils.js";

let candlestickChartInstance = null;

export function loadChartByUrlCode() {

    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');

    const ctx = document.getElementById('candlestickChart')?.getContext('2d');
    if (!code || !ctx) {
        if (candlestickChartInstance) { candlestickChartInstance.destroy(); candlestickChartInstance = null; }
        console.warn("차트를 로드할 코드 또는 캔버스가 유효하지 않습니다.");
        return;
    }

    fetchStockChartData(code).then(data => {
        if (candlestickChartInstance) {
            candlestickChartInstance.destroy();
        }

        const chartData = data.map((e, i) => {
            let effectiveOpenPrice = e.openPrice;

            if (i > 0) {
                effectiveOpenPrice = data[i - 1].closePrice;
            }

            return {
                x: new Date(e.chartTime).getTime(),
                o: effectiveOpenPrice,
                h: e.highPrice,
                l: e.lowPrice,
                c: e.closePrice,
                r: e.changeRate,
            }
        });

        const volumeData = data.map(item => ({
            x: new Date(item.chartTime).getTime(),
            y: item.volume
        }));

        const allLowPrices = chartData.map(d => d.l);
        const allHighPrices = chartData.map(d => d.h);

        const absoluteMin = Math.min(...allLowPrices);
        const absoluteMax = Math.max(...allHighPrices);

        const priceRange = absoluteMax - absoluteMin;
        const padding = priceRange * 0.05; // 5% 여백

        const finalYMin = Math.floor(absoluteMin - padding);
        const finalYMax = Math.ceil(absoluteMax + padding);

        const DateTime = luxon.DateTime;

        const latestTimestamp = chartData[0].x;

        const latestTime = DateTime.fromMillis(latestTimestamp);
        const timeTwoHoursBeforeLatestData = latestTime.minus({hours: 4});

        const finalXMax = latestTime.toMillis();
        const finalXMin = timeTwoHoursBeforeLatestData.toMillis();


        const maxVolume = Math.max(...volumeData.map(v => v.y));

        const VOLUME_MULTIPLIER = 0.5;

        const finalVolumeMax = maxVolume * VOLUME_MULTIPLIER;

        const UP_COLOR = '#3664EB';
        const DOWN_COLOR = '#FF140A';
        const UNCHANGED_COLOR = '#808080';

        candlestickChartInstance = new Chart(ctx, {
            data: {
                datasets: [{
                    type: 'candlestick',
                    label: '주식 가격',
                    data: chartData,
                    color: { up: UP_COLOR, down: DOWN_COLOR, unchanged: UNCHANGED_COLOR },
                    borderColor: { up: UP_COLOR, down: DOWN_COLOR, unchanged: UNCHANGED_COLOR },
                },
                    {
                        type: 'bar',
                        label: '거래량',
                        data: volumeData,
                        yAxisID: 'volume',
                        backgroundColor: 'rgba(200, 200, 200, 1)',
                        borderColor: 'transparent',
                        barPercentage: 0.8,
                        categoryPercentage: 0.8,
                    }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                interaction: { mode: 'index', intersect: false },
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                if (context.dataset.type === 'candlestick') {
                                    const data = context.raw;
                                    return [
                                        '시가: ' + data.o.toLocaleString() + '원', '최고: ' + data.h.toLocaleString() + '원',
                                        '최저: ' + data.l.toLocaleString() + '원', '종가: ' + data.c.toLocaleString() + '원',
                                        '등락율: ' + data.r.toLocaleString() + '%'
                                    ];
                                } else if (context.dataset.type === 'bar') {
                                    const volumeValue = context.parsed.y;
                                    return '거래량: '+ (typeof volumeValue === 'number' && !isNaN(volumeValue) ? volumeValue.toLocaleString() : 'N/A');
                                }
                                return '';
                            }
                        }
                    }
                },
                scales: {
                    x: {
                        type: 'time',
                        time: { unit: 'minute', displayFormats: { minute: 'HH:mm' }, tooltipFormat: 'yyyy.MM.dd(EEE) HH:mm' },
                        ticks: { source: 'auto', maxRotation: 0, autoSkipPadding: 10 },
                        grid: { display: false },
                        min: finalXMin,
                        max: finalXMax,
                    },
                    y: {
                        title: { display: false }, position: 'right', grid: { display: true },
                        min: finalYMin, max: finalYMax,
                        ticks: { display: false }
                    },
                    volume: {
                        type: 'linear', position: 'left', display: false,
                        grid: { drawOnChartArea: false },
                        min: 0,
                        max: finalVolumeMax
                    }
                }
            }
        });
    });
}