
/**
 * 메인 초기화 함수: 통계 데이터를 가져와 차트를 초기화
 */
let chartInstance = null; // Chart 인스턴스를 저장할 변수

async function initializeStatisticsChart(uuId, ctx) {
    try {
        const labels = getLastFiveDays(); // 오늘부터 5일 전까지의 날짜 계산
        const statisticsData = await fetchStatistics(uuId); // 서버에서 통계 데이터 가져오기
        const processedData = processStatisticsData(statisticsData, labels); // 데이터 처리

        // 기존 차트가 있으면 삭제
        if (chartInstance) {
            chartInstance.destroy(); // 기존 차트 파괴
        }

        // 새로운 차트 생성 및 저장
        chartInstance = renderChart(ctx, labels, processedData);
    } catch (error) {
        console.error("Error initializing statistics chart:", error);
    }
}

/**
 * 오늘부터 5일 전까지의 날짜를 계산하여 반환
 * @returns {string[]} 날짜 배열 (YYYY-MM-DD 형식)
 */
function getLastFiveDays() {
    const today = new Date();
    const dates = [];
    for (let i = 4; i >= 0; i--) {
        const date = new Date();
        date.setDate(today.getDate() - i);

        // MM/DD 형식으로 변환
        const formattedDate = `${String(date.getMonth() + 1).padStart(2, '0')}/${String(date.getDate()).padStart(2, '0')}`;
        dates.push(formattedDate);
    }
    console.log("Generated labels (MM/DD):", dates); // 디버깅용
    return dates;
}

/**
 * 서버에서 통계 데이터를 가져오는 함수
 * @returns {Promise<Object[]>} 통계 데이터 배열
 */
async function fetchStatistics(uuId) {
    const response = await fetch(`/api/link/${uuId}/statistics`, {
        method: "GET",
        headers: {
            "Content-Type": "application/json"
        }
    });
    if (!response.ok) {
        throw new Error("Failed to fetch statistics data");
    }
    const result = await response.json();
    return result.data;
}

/**
 * 통계 데이터를 날짜별로 그룹화하고 누락된 날짜를 0으로 채움
 * @param {Object[]} data 서버에서 가져온 통계 데이터
 * @param {string[]} labels 날짜 레이블 배열
 * @returns {Object[]} 링크별 데이터 배열
 */
function processStatisticsData(data, labels) {
    const linkDataMap = new Map();

    data.forEach(item => {

        const visitDateFormatted = item.visitDate.slice(5).replace("-", "/");
        if (!linkDataMap.has(item.linkId)) {
            linkDataMap.set(item.linkId, {
                title: item.title,
                color: item.color,
                data: Array(labels.length).fill(0) // 0으로 초기화된 데이터 배열
            });
        }
        const linkData = linkDataMap.get(item.linkId);
        const dateIndex = labels.indexOf(visitDateFormatted);
        if (dateIndex !== -1) {
            linkData.data[dateIndex] = item.visitCount;
        }
    });

    return Array.from(linkDataMap.values()); // Map을 배열로 변환
}

/**
 * Chart.js를 사용하여 막대 차트를 렌더링
 * @param {string[]} labels 날짜 레이블
 * @param {Object[]} datasets Chart.js에 사용할 데이터셋
 */
// function renderChart(labels, datasets) {
//     const ctx = document.getElementById("stats-chart").getContext("2d");
//
//     const chartData = datasets.map(link => ({
//         label: link.title,
//         backgroundColor: link.color,
//         data: link.data
//     }));
//
//     new Chart(ctx, {
//         type: "bar",
//         data: {
//             labels: labels, // x축 레이블 (MM/DD 형식)
//             datasets: chartData // 데이터셋
//         },
//         options: {
//             responsive: true,
//             plugins: {
//                 legend: {
//                     position: "top",
//                     labels: {
//                         font: { size: 14 }
//                     }
//                 },
//                 tooltip: {
//                     callbacks: {
//                         label: function (context) {
//                             return `${context.dataset.label}: ${context.raw}명`;
//                         }
//                     }
//                 }
//             },
//             scales: {
//                 x: {
//                     grid: { display: false },
//                     ticks: { color: "#cdcccc" }
//                 },
//                 y: {
//                     beginAtZero: true,
//                     grid: { color: "#cdcccc" },
//                     ticks: {
//                         stepSize: 1,
//                         color: "#cdcccc"
//                     }
//                 }
//             }
//         }
//     });
// }

/**
 * Chart.js를 사용하여 꺾은선 그래프를 렌더링
 * @param {string[]} labels 날짜 레이블
 * @param {Object[]} datasets Chart.js에 사용할 데이터셋
 */
function renderChart(ctx, labels, datasets) {
    const chartData = datasets.map((link) => ({
        label: link.title,
        borderColor: link.color,
        backgroundColor: link.color + "33", // 투명도 추가
        data: link.data,
        borderWidth: 1,
        fill: false,
        tension: 0.5,
        pointRadius: 4,
        pointHoverRadius: 7,
    }));

    // 새로운 차트 생성 및 반환
    return new Chart(ctx, {
        type: "line",
        data: {
            labels: labels,
            datasets: chartData,
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: "top",
                    labels: {
                        generateLabels: (chart) => {
                            return chart.data.datasets.map((dataset, i) => {
                                const shortenedLabel =
                                    dataset.label.length > 8
                                        ? `${dataset.label.substring(0, 8)}...`
                                        : dataset.label;

                                return {
                                    text: shortenedLabel,
                                    fillStyle: dataset.borderColor,
                                    strokeStyle: dataset.borderColor,
                                    lineWidth: dataset.borderWidth,
                                    hidden: !chart.isDatasetVisible(i),
                                    lineCap: dataset.borderCapStyle,
                                    lineDash: dataset.borderDash,
                                    lineDashOffset: dataset.borderDashOffset,
                                    lineJoin: dataset.borderJoinStyle,
                                    pointStyle: dataset.pointStyle,
                                    rotation: dataset.rotation,
                                    fontColor: "#cdcccc",
                                    font: {
                                        size: 14,
                                    },
                                    datasetIndex: i, // 데이터셋 인덱스 추가
                                };
                            });
                        },
                        usePointStyle: true,
                        boxWidth: 10,
                        boxHeight: 10,
                    },
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return `${context.dataset.label}: ${context.raw}`;
                        },
                    },
                },
            },
            scales: {
                x: {
                    grid: { display: false },
                    ticks: { color: "#cdcccc" },
                },
                y: {
                    beginAtZero: true,
                    grid: { color: "#cdcccc" },
                    ticks: {
                        stepSize: 1,
                        color: "#cdcccc",
                    },
                },
            },
            elements: {
                point: {
                    pointStyle: "circle",
                },
            },
        },
    });
}