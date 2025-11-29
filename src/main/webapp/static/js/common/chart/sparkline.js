document.addEventListener('DOMContentLoaded', ()=>{
    const sparklines = document.querySelectorAll('.sparkline-container');
    sparklines.forEach((e, i) => {
        loadSparkline(e.id);
    });
});

function drawSparkline(selector, data) {
    const margin = { top: 3, right: 3, bottom: 3, left: 3 };
    const width = 80 - margin.left - margin.right;
    const height = 40 - margin.top - margin.bottom;
    const chartHeight = height;

    d3.select(selector).select("svg").remove();
    const svg = d3.select(selector)
        .append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", `translate(${margin.left}, ${margin.top})`);

    if (!data || data.length < 2) return;

    const elementId = selector.substring(1);
    const gradientId = `left-area-gradient-${elementId}`;
    const clipPathId = `left-clip-path-${elementId}`;

    const isUp = data[data.length - 1] >= data[0];
    const baseFillColor = isUp ? "#FFCDD2" : "#BBDEFB";

    // 스케일 정의
    const x = d3.scaleLinear()
        .domain([0, data.length - 1])
        .range([0, width]);

    const y = d3.scaleLinear()
        .domain(d3.extent(data))
        .range([chartHeight, 0]);

    // Area 및 Line 생성 함수 정의
    const area = d3.area()
        .x((d, i) => x(i))
        .y0(chartHeight)
        .y1(d => y(d))
        .curve(d3.curveMonotoneX);

    const line = d3.line()
        .x((d, i) => x(i))
        .y(d => y(d))
        .curve(d3.curveMonotoneX);

    // <defs> (그라데이션 및 클립 경로)
    const defs = svg.append("defs");

    // 그라데이션 정의 (수직 Fade-out)
    const linearGradient = defs.append("linearGradient")
        .attr("id", gradientId)
        .attr("x1", "0%").attr("y1", "0%")
        .attr("x2", "0%").attr("y2", "100%");

    linearGradient.append("stop").attr("offset", "0%").attr("stop-color", baseFillColor).attr("stop-opacity", 0.7);
    linearGradient.append("stop").attr("offset", "100%").attr("stop-color", baseFillColor).attr("stop-opacity", 0.1);

    // 클립 경로 정의 (차트 경계 외의 영역 자르기)
    defs.append("clipPath")
        .attr("id", clipPathId)
        .append("rect")
        .attr("x", 0)
        .attr("y", 0)
        .attr("width", width)
        .attr("height", chartHeight);


    // SVG Path 그리기

    // 면적 Path
    svg.append("path")
        .attr("class", "visx-area-closed") // 클래스명 일치
        .datum(data)
        .attr("d", area)
        .attr("fill", `url(#${gradientId})`) // 그라데이션 적용
        .attr("clip-path", `url(#${clipPathId})`) // 클립 경로 적용
        .attr("stroke-width", 0); // 면적 차트이므로 stroke 없음

    // 라인 Path
    svg.append("path")
        .datum(data)
        .attr("fill", "none")
        .attr("stroke", isUp ? "#FF5252" : "#42A5F5")
        .attr("stroke-width", 1.5)
        .attr("d", line);
}

function loadSparkline(indexCode) {
    // 1. URL과 쿼리 파라미터 생성
    const url = `/api/index/sparkline?code=${encodeURIComponent(indexCode)}`;

    // 2. fetch API를 사용한 비동기 요청
    fetch(url, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP Error! Status: ${response.status} - Could not load ${indexCode}`);
            }
            return response.json();
        })
        .then(prices => {
            // 3. 성공적으로 JSON 데이터(prices 배열)를 받은 후 처리
            console.log(`순수 JS로 로드된 ${indexCode} 종가 데이터:`, prices);

            // =========================================================
            // ⭐ 이 부분을 수정합니다: D3.js 함수로 데이터를 전달합니다.
            // =========================================================
            var selector = '#' + indexCode; // HTML ID 선택자 생성 (예: #KOSPI)

            // *주의*: 서버에서 받은 데이터가 D3 함수가 요구하는 형식인지 확인해야 합니다.
            // 현재 drawSparkline은 [number, number, ...] 배열을 기대합니다.
            drawSparkline(selector, prices);

            // 추가: 현재가 표시 등 다른 UI 업데이트 로직도 여기에 추가
            // displayCurrentPrice(indexCode, prices);

        })
        .catch(error => {
            // 4. 요청 실패 또는 파싱 오류 시 처리
            console.error(`데이터 로드 중 오류 발생 (${indexCode}):`, error.message);
            // 실패 시 빈 차트를 그리거나 오류 메시지를 표시할 수 있습니다.
            const selector = '#' + indexCode;
            d3.select(selector).html("데이터 로드 실패");
        });
}