/**
 * 서버에서 인기 TongLink 데이터를 가져와 동적으로 렌더링
 * @param {HTMLElement} container - 렌더링할 HTML 컨테이너
 */
function initPopularInfiniteScroll(container) {
    fetch(`/api/link/popular`, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
        },
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json(); // JSON 데이터를 반환
        })
        .then((result) => {
            // 기존 컨텐츠 초기화
            container.innerHTML = "";

            // 받아온 데이터를 렌더링
            renderLinkPreviews(result.data, container);

            // 데이터가 없으면 메시지 표시
            if (result.data.size === 0) {
                showEmptyMessage(container);
            } else {
                removeEmptyMessage(container);
            }

        })
        .catch((error) => {
            console.error("링크 목록 가져오기 실패:", error);
        });
}


function renderLinkPreviews(links, container) {
    if (!(container instanceof HTMLElement)) {
        return;
    }

    // `map`을 사용해 각 링크를 HTML 문자열로 변환
    const html = links
        .map(
            (link) => `
        <!-- 복사 완료 메시지 -->
        <div class="link-preview" id="link-preview-${link.id}" data-id="${link.id}">
            <div class="link-preview-content" style="margin-left: 7px; margin-top: 1px;">
                <div class="title-container" style="align-items: center;">
                    <h3 class="link-title">${link.title}</h3>
                </div>
                <button class="copy-button" style="padding: 0">
                    <span class="link-url">${link.proxyUrl}</span>
                </button>
                <p class="link-description">조회수 ${link.count.toLocaleString()}</p>
            </div>
            <div class="link-preview-thumbnail">
                <a href="${link.proxyUrl}/${link.id}" target="_blank" rel="noopener noreferrer">
                    <img src="${link.thumbnailUrl}">
                </a>
            </div>
        </div>
    `
        )
        .join(""); // 문자열로 결합

    container.innerHTML = html; // 컨테이너에 HTML 삽입

    addMedalsToList(container);

    // 복사 버튼 클릭 이벤트 추가
    container.querySelectorAll(".copy-button").forEach((button, index) => {
        const link = links[index];
        button.addEventListener("click", () => {
            navigator.clipboard
                .writeText(`${link.proxyUrl}/${link.id}`)
                .then(() => {
                    showToast("URL이 클립보드에 복사되었습니다!");
                })
                .catch((error) => {
                    console.error("클립보드 복사 실패:", error);
                    showToast("클립보드 복사 실패. 다시 시도해주세요!");
                });
        });
    });
}


/**
 * Toast 메시지 표시 함수
 * @param {string} message - 표시할 메시지
 */
function showToast(message) {
    const toast = document.getElementById("copy-toast");
    toast.textContent = message;
    toast.classList.add("show");
    setTimeout(() => toast.classList.remove("show"), 2000);
}

/**
 * 메달 표시를 위한 함수
 * @param {HTMLElement} container - 리스트 컨테이너
 */
function addMedalsToList(container) {
    const listItems = container.querySelectorAll('.link-preview'); // `.link-preview` 클래스 요소를 모두 선택
    listItems.forEach((item, index) => {
        // 기존에 추가된 메달이 있으면 제거
        const existingMedal = item.querySelector('.medal');
        if (existingMedal) {
            existingMedal.remove();
        }

        // 메달 생성
        const medal = document.createElement('span');
        medal.className = 'medal';
        if (index === 0) {
            medal.classList.add('gold'); // 금메달
        } else if (index === 1) {
            medal.classList.add('silver'); // 은메달
        } else if (index === 2) {
            medal.classList.add('bronze'); // 동메달
        } else {
            return; // 4위 이하에는 메달 추가하지 않음
        }

        // 제목 앞에 메달 추가
        const titleContainer = item.querySelector('.title-container');
        if (titleContainer) {
            titleContainer.insertBefore(medal, titleContainer.firstChild); // 제목 앞에 메달 추가
        }
    });
}

/**
 * "데이터 없음" 메시지를 추가
 * @param {HTMLElement} container - 데이터를 렌더링할 HTML 컨테이너
 */
function showEmptyMessage(container) {
    const emptyMessage = document.createElement("pre");
    emptyMessage.className = "empty-message";
    emptyMessage.textContent = "아직 링크가 없습니다.\n링크를 추가하고 조회 통계를 확인하세요!";
    container.appendChild(emptyMessage);
}

/**
 * "데이터 없음" 메시지를 제거
 * @param {HTMLElement} container - 데이터를 렌더링할 HTML 컨테이너
 */
function removeEmptyMessage(container) {
    const emptyMessageDiv = container.querySelector(".empty-message");
    if (emptyMessageDiv) emptyMessageDiv.remove();
}


// 초기화
document.addEventListener('DOMContentLoaded', () => {
    const listContainer = document.getElementById('tonglink-list');
    initPopularInfiniteScroll(listContainer);
});