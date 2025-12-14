import {fetchStockChartData} from "../api/fetch_utils.js";

// ✨ 1. (수정) 차트 인스턴스를 함수 바깥(모듈 스코프)에 선언하여 상태를 유지합니다.
let candlestickChartInstance = null;

export function loadChartByUrlCode() {

    const urlParams = new URLSearchParams(window.location.search);
    const code = urlParams.get('code');

    // 코드가 없거나 캔버스 컨텍스트를 얻지 못하면 종료
    const ctx = document.getElementById('candlestickChart')?.getContext('2d');
    if (!code || !ctx) {
        if (candlestickChartInstance) { candlestickChartInstance.destroy(); candlestickChartInstance = null; }
        console.warn("차트를 로드할 코드 또는 캔버스가 유효하지 않습니다.");
        return;
    }

    fetchStockChartData(code).then(data => {

        // ✨ 2. (수정) destroy 로직은 외부 변수를 참조하며 실행
        if (candlestickChartInstance) {
            candlestickChartInstance.destroy();
        }

        // { x: new Date('2025-12-12T02:35:00').getTime(), o: 7450, h: 7650, l: 7400, c: 7500 },
        const chartData = data.map((e, i) => {
            let effectiveOpenPrice = e.openPrice; // 기본 시가

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

        // 2. 전체 데이터 중 절대적인 최소값과 최대값 찾기
        const absoluteMin = Math.min(...allLowPrices);
        const absoluteMax = Math.max(...allHighPrices);

        // 3. 여유 공간 (Padding) 계산
        const priceRange = absoluteMax - absoluteMin;
        const padding = priceRange * 0.05; // 5% 여백

        // 4. 최종 Y축 Min/Max 설정
        const finalYMin = Math.floor(absoluteMin - padding);
        const finalYMax = Math.ceil(absoluteMax + padding);

        const DateTime = luxon.DateTime;

        // ✨ 3. (수정) 0번째 인덱스가 가장 최근 데이터입니다. (사용자 요청 반영)
        const latestTimestamp = chartData[0].x;

        const latestTime = DateTime.fromMillis(latestTimestamp);
        // 2시간 전 시간 계산 (4시간 범위 유지)
        const timeTwoHoursBeforeLatestData = latestTime.minus({hours: 4});

        const finalXMax = latestTime.toMillis();
        const finalXMin = timeTwoHoursBeforeLatestData.toMillis();


        const maxVolume = Math.max(...volumeData.map(v => v.y));

        // ✨ 4. (수정) VOLUME_MULTIPLIER를 안전 범위인 1.25로 수정 (막대 침범 방지)
        const VOLUME_MULTIPLIER = 0.5;

        const finalVolumeMax = maxVolume * VOLUME_MULTIPLIER;

        // const ctx = document.getElementById('candlestickChart').getContext('2d'); // 위에서 이미 정의됨

        const UP_COLOR = '#3664EB';
        const DOWN_COLOR = '#FF140A';
        const UNCHANGED_COLOR = '#808080';

        // ✨ 5. (수정) 새로 생성된 인스턴스를 외부 변수에 저장
        candlestickChartInstance = new Chart(ctx, {
            data: {
                datasets: [{
                    type: 'candlestick', // Chart.js v4+ 호환성 위해 'financial' 타입 사용 권장
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
                                if (context.dataset.type === 'financial') {
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