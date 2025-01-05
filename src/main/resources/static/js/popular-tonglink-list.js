let isLoading = false;

/**
 * 무한 스크롤 초기화
 * @param {HTMLElement} container - 렌더링할 HTML 컨테이너
 */
function initPopularInfiniteScroll(container) {
    let currentPage = 0; // 현재 페이지 번호
    const limit = 10; // 페이지당 데이터 개수
    let isLoading = false; // 로딩 상태 플래그

    // Sentinel 요소 생성 및 추가
    const sentinel = document.createElement("div");
    sentinel.className = "scroll-sentinel";
    sentinel.draggable = false;
    container.appendChild(sentinel);
    setSentinelHeight(sentinel);

    // IntersectionObserver로 Sentinel 감지
    const observer = new IntersectionObserver(
        (entries) => {
            const target = entries[0];
            if (target.isIntersecting && !isLoading) {
                isLoading = true;
                console.log(`Observer triggered for page: ${currentPage}`);

                getPopularTongLink(container, limit, currentPage, (nextPage, dataLength) => {
                    currentPage = nextPage; // 다음 페이지 번호 업데이트

                    if (dataLength < limit) {
                        console.log("No more data to load. Disconnecting observer.");
                        observer.unobserve(sentinel); // Sentinel 감시 중지
                    }

                    isLoading = false; // 로딩 상태 해제
                });
            }
        },
        {
            root: null,
            rootMargin: "0px",
            threshold: 0.2, // 20% 보일 때 트리거
        }
    );

    observer.observe(sentinel);

    // 첫 데이터 로드
    isLoading = true;
    getPopularTongLink(container, limit, 0, (nextPage, dataLength) => {
        currentPage = nextPage;

        if (dataLength < limit) {
            console.log("No more data to load. Disconnecting observer.");
            observer.unobserve(sentinel);
        }

        isLoading = false;
    });
}

/**
 * 서버에서 사용자 TongLink 데이터를 가져와 동적으로 렌더링
 * @param {HTMLElement} container - 렌더링할 HTML 컨테이너
 * @param {number} limit - 한 페이지당 가져올 데이터 수
 * @param {number} page - 현재 페이지 번호
 * @param {Function} callback - 다음 페이지를 업데이트할 콜백 함수
 */
function getPopularTongLink(container, limit = 10, page = 0, callback) {
    const queryParams = new URLSearchParams({
        limit: limit.toString(),
        page: page.toString(),
    });

    fetch(`/api/link/popular?${queryParams.toString()}`, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
        },
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then((data) => {
            if (page === 0 && container.querySelector(".link-preview")) {
                container.querySelectorAll(".link-preview").forEach((el) => el.remove());
            }

            data.data.content.forEach((link) => {
                const linkPreview = createLinkPreview(link);
                container.insertBefore(linkPreview, container.querySelector(".scroll-sentinel"));
            });

            if (page === 0) {
                addMedalsToList(container);
            }

            console.log(`Current page loaded: ${page}`);

            callback(page + 1, content.length); // 데이터 길이를 반환

            if (data.data.totalElements === 0) {
                if (!container.querySelector(".empty-message")) {
                    const emptyMessage = document.createElement("pre");
                    emptyMessage.className = "empty-message";
                    emptyMessage.textContent = "아직 링크가 없습니다.\n링크를 추가하고 조회 통계를 확인하세요!";
                    container.appendChild(emptyMessage);
                }
            } else {
                const existingEmptyMessage = container.querySelector(".empty-message");
                if (existingEmptyMessage) existingEmptyMessage.remove();
            }
        })
        .catch((error) => {
            console.error("링크 목록 가져오기 실패:", error);
        });
}

/**
 * LinkDto 데이터를 기반으로 링크 프리뷰 HTML 요소 생성
 * @param {Object} link - LinkDto 객체 (썸네일 URL 포함)
 * @returns {HTMLElement} - 생성된 링크 프리뷰 요소
 */
function createLinkPreview(link) {
    const wrapper = document.createElement("div");
    wrapper.className = "link-preview";
    wrapper.id = `link-preview-${link.id}`;
    wrapper.setAttribute("data-id", link.id);
    wrapper.setAttribute("data-order", link.order);

    wrapper.innerHTML = `
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
        <div id="copy-toast" class="copy-toast">복사되었습니다!</div>
    `;

    const copyButton = wrapper.querySelector(".copy-button");
    copyButton.addEventListener("click", () => {
        navigator.clipboard.writeText(link.proxyUrl + "/" + link.id)
            .then(() => {
                showToast("URL이 클립보드에 복사되었습니다!");
            })
            .catch((error) => {
                console.error("클립보드 복사 실패:", error);
                showToast("클립보드 복사 실패. 다시 시도해주세요!");
            });
    });

    return wrapper;
}

/**
 * Toast 메시지 표시 함수
 * @param {string} message - 표시할 메시지
 */
function showToast(message) {
    const toast = document.getElementById("copy-toast");
    toast.textContent = message;
    toast.classList.add("show");

    setTimeout(() => {
        toast.classList.remove("show");
    }, 2000);
}

/**
 * 메달 표시를 위한 함수
 * @param {HTMLElement} container - 리스트 컨테이너
 */
function addMedalsToList(container) {
    const listItems = container.querySelectorAll(".link-preview");
    listItems.forEach((item, index) => {
        const medal = document.createElement("span");
        medal.className = "medal";

        if (index === 0) medal.classList.add("gold");
        else if (index === 1) medal.classList.add("silver");
        else if (index === 2) medal.classList.add("bronze");
        else return;

        const titleContainer = item.querySelector(".title-container");
        if (titleContainer) {
            titleContainer.insertBefore(medal, titleContainer.firstChild);
        }
    });
}

/**
 * Sentinel의 높이를 설정
 * @param {HTMLElement} sentinel - Sentinel 요소
 */
function setSentinelHeight(sentinel) {
    const viewportHeight = window.innerHeight;
    const sentinelHeight = Math.max(50, viewportHeight * 0.1);
    sentinel.style.height = `${sentinelHeight}px`;
}

document.addEventListener("DOMContentLoaded", () => {
    const listContainer = document.getElementById("tonglink-list");
    initPopularInfiniteScroll(listContainer);
});