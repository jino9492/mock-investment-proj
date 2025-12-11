import { loadIndexSparklineData } from '../api/fetch_utils.js';

document.addEventListener('DOMContentLoaded', () => {
    const sparklines = document.querySelectorAll('.sparkline-container');
    const loadPromises = [];
    const indexNamesMap = new Map();

    sparklines.forEach((e, i) => {
        const indexName = e.id;

        if (indexName) {
            loadPromises.push(loadIndexSparklineData(indexName));
            indexNamesMap.set(i, indexName);
        }
    });

    Promise.all(loadPromises)
        .then(results => {
            console.log("모든 스파크라인 데이터 로드 완료 :", results);

            results.forEach((dataObject, i) => {
                const indexName = indexNamesMap.get(i);

                const prices = dataObject.prices || [];
                const overnights = dataObject.overnights || [];

                const selector = '#' + indexName;

                drawSparkline(selector, prices, overnights);
            });
        })
        .catch(error => {
            const failedIndex = error.indexName || "알 수 없는 지수";
            console.error(`데이터 로드 실패 (${failedIndex}):`, error.message);

            if (failedIndex !== "알 수 없는 지수") {
                d3.select('#' + failedIndex).html("데이터 로드 실패");
            }
        });
});

function drawSparkline(selector, prices, overnights) {
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

    if (!prices || prices.length < 2) return;

    const elementId = selector.substring(1);
    const gradientId = `left-area-gradient-${elementId}`;
    const clipPathId = `left-clip-path-${elementId}`;

    const isUp = overnights[overnights.length - 1] > 0;
    const baseFillColor = isUp ? "#FFCDD2" : "#BBDEFB";

    // 스케일 정의
    const x = d3.scaleLinear()
        .domain([0, prices.length - 1])
        .range([0, width]);

    const y = d3.scaleLinear()
        .domain(d3.extent(prices))
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
        .datum(prices)
        .attr("d", area)
        .attr("fill", `url(#${gradientId})`) // 그라데이션 적용
        .attr("clip-path", `url(#${clipPathId})`) // 클립 경로 적용
        .attr("stroke-width", 0); // 면적 차트이므로 stroke 없음

    // 라인 Path
    svg.append("path")
        .datum(prices)
        .attr("fill", "none")
        .attr("stroke", isUp ? "#FF5252" : "#42A5F5")
        .attr("stroke-width", 1.5)
        .attr("d", line);
}