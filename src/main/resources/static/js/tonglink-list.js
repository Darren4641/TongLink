
/**
 * 무한 스크롤 초기화
 * @param {string} uuId - 사용자 UUID
 * @param {HTMLElement} container - 렌더링할 HTML 컨테이너
 */
function initInfiniteScroll(uuId, container) {
    let currentPage = 0; // 현재 페이지 번호
    let isLoading = false; // 데이터 로딩 중 상태
    const limit = 5; // 페이지당 데이터 개수

    // Sentinel 요소 생성 및 추가
    const sentinel = document.createElement("div");
    sentinel.className = "scroll-sentinel";
    container.appendChild(sentinel);

    // IntersectionObserver로 Sentinel 감지
    const observer = new IntersectionObserver(
        (entries) => {
            const target = entries[0];
            if (target.isIntersecting && !isLoading) {
                isLoading = true;
                console.log(`Observer triggered for page: ${currentPage}`); // Observer 호출 디버깅

                getTongLinkList(uuId, container, limit, currentPage, (nextPage) => {
                    currentPage = nextPage; // 다음 페이지 번호 업데이트
                    isLoading = false; // 로딩 상태 해제
                });
            }
        },
        {
            root: null, // 기본 뷰포트
            rootMargin: "0px",
            threshold: 0.2, // 20% 보일 때 트리거
        }
    );

    // Sentinel 감지 시작
    observer.observe(sentinel);

    // Fallback: Scroll Event 추가
    window.addEventListener("scroll", () => {
        const scrollPosition = window.innerHeight + window.scrollY;
        const bottomPosition = document.documentElement.offsetHeight - 50; // 하단 여유 50px
        if (scrollPosition >= bottomPosition && !isLoading) {
            console.log(`Scroll fallback triggered for page: ${currentPage}`);
            isLoading = true;
            getTongLinkList(uuId, container, limit, currentPage, (nextPage) => {
                currentPage = nextPage;
                isLoading = false;
            });
        }
    });

    // 첫 데이터 로드
    isLoading = true; // 첫 로딩 상태 설정
    getTongLinkList(uuId, container, limit, 0, (nextPage) => {
        currentPage = nextPage; // 첫 번째 페이지 번호 설정
        isLoading = false; // 로딩 상태 해제
    });
}

/**
 * 서버에서 사용자 TongLink 데이터를 가져와 동적으로 렌더링
 * @param {string} uuId - 사용자 UUID
 * @param {HTMLElement} container - 렌더링할 HTML 컨테이너
 * @param {number} limit - 한 페이지당 가져올 데이터 수
 * @param {number} page - 현재 페이지 번호
 * @param {Function} callback - 다음 페이지를 업데이트할 콜백 함수
 */
function getTongLinkList(uuId, container, limit = 5, page = 0, callback) {
    const queryParams = new URLSearchParams({
        limit: limit.toString(),
        page: page.toString(),
    });

    fetch(`/api/link/${uuId}?${queryParams.toString()}`, {
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
        .then((data) => {
            // 처음 로딩 시, 기존 컨텐츠는 유지하고 초기화하지 않음
            if (page === 0 && container.querySelector(".link-preview")) {
                container.querySelectorAll(".link-preview").forEach((el) => el.remove());
            }

            // API 응답 데이터에서 바로 렌더링
            data.data.content.forEach((link) => {
                const linkPreview = createLinkPreview(link); // DOM 요소 생성
                container.insertBefore(linkPreview, container.querySelector(".scroll-sentinel")); // Sentinel 앞에 추가
            });

            console.log(`Current page loaded: ${page}`); // 현재 페이지 출력

            // 다음 페이지가 있는 경우, 콜백 함수 실행
            if (!data.data.last) {
                callback(page + 1);
            } else {
                // 마지막 페이지일 경우 Sentinel 제거
                const sentinel = container.querySelector(".scroll-sentinel");
                if (sentinel) sentinel.remove();
            }

            // 렌더링 후, 전체 데이터가 없으면 "아직 링크가 없습니다" 메시지 추가
            if (data.data.totalElements === 0) {
                if (!container.querySelector(".empty-message")) {
                    const emptyMessage = document.createElement("pre");
                    emptyMessage.className = "empty-message";
                    emptyMessage.textContent = "아직 링크가 없습니다.\n링크를 추가하고 조회 통계를 확인하세요!";
                    container.appendChild(emptyMessage);
                }
            } else {
                // 데이터가 있을 경우 메시지 제거
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

    wrapper.innerHTML = `
        <!-- 왼쪽 이미지 -->
        <div class="link-preview-thumbnail">
            <img src="${link.thumbnailUrl}" alt="${link.title} 썸네일">
        </div>
        <!-- 오른쪽 컨텐츠 -->
        <div class="link-preview-content">
            <h3 class="link-title">${link.title}</h3>
            <a href="${link.originUrl}" target="_blank" class="link-url">${link.originUrl}</a>
            <p class="link-description">만료일 - ${link.endDate}</p>
        </div>
    `;

    return wrapper; // DOM 요소 반환
}

