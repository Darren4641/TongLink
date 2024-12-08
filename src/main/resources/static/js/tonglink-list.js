
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

                    // 드래그 앤 드롭 활성화
                    enableDragAndDrop(container);
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

                // 드래그 앤 드롭 활성화
                enableDragAndDrop(container);
            });
        }
    });

    // 첫 데이터 로드
    isLoading = true; // 첫 로딩 상태 설정
    getTongLinkList(uuId, container, limit, 0, (nextPage) => {
        currentPage = nextPage; // 첫 번째 페이지 번호 설정
        isLoading = false; // 로딩 상태 해제

        // 드래그 앤 드롭 활성화
        enableDragAndDrop(container);
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

let uuId = localStorage.getItem("userUUID");
// 전역 변수로 order를 추적할 수도 있음
let currentMaxOrder = 0; // 현재까지 할당한 최대 order 값 추적용
function assignOrderIfNull(link) {
    if (link.order == null) {
        link.order = ++currentMaxOrder;
    } else {
        currentMaxOrder = Math.max(currentMaxOrder, link.order);
    }
}

/**
 * LinkDto 데이터를 기반으로 링크 프리뷰 HTML 요소 생성
 * @param {Object} link - LinkDto 객체 (썸네일 URL 포함)
 * @returns {HTMLElement} - 생성된 링크 프리뷰 요소
 */
function createLinkPreview(link) {
    assignOrderIfNull(link);

    const wrapper = document.createElement("div");
    wrapper.className = "link-preview";
    wrapper.id = `link-preview-${link.id}`;
    wrapper.setAttribute("draggable", "true"); // 처음부터 true
    wrapper.setAttribute("data-id", link.id);
    wrapper.setAttribute("data-order", link.order);

    wrapper.innerHTML = `
        <!-- 왼쪽 이미지 -->
        <div class="link-preview-thumbnail">
            <a href="${link.proxyUrl}/${link.id}" target="_blank" rel="noopener noreferrer">
                <img src="${link.thumbnailUrl}">
            </a>
        </div>
        <!-- 오른쪽 컨텐츠 -->
        <div class="link-preview-content">
            <div class="title-container">
                <h3 class="link-title">${link.title}</h3>
                <button class="copy-button">
                    <img src="/images/copy.png" alt="복사 아이콘">
                </button>
                <button class="drag-handle">
                    <img src="/images/drag.png" alt="드래그 아이콘">
                </button>
            </div>
            <a href="${link.originUrl}" target="_blank" class="link-url">${link.originUrl}</a>
            <p class="link-description">만료일 - ${link.endDate}</p>
        </div>
        <!-- 복사 완료 메시지 -->
        <div id="copy-toast" class="copy-toast">복사되었습니다!</div> 
    `;

    // 복사 버튼 클릭 이벤트 추가
    const copyButton = wrapper.querySelector(".copy-button");
    copyButton.addEventListener("click", () => {
        // 클립보드에 URL 복사
        navigator.clipboard.writeText(link.proxyUrl + "/" + link.id)
            .then(() => {
                showToast("URL이 클립보드에 복사되었습니다!"); // 성공 시 메시지 표시
            })
            .catch((error) => {
                console.error("클립보드 복사 실패:", error);
                showToast("클립보드 복사 실패. 다시 시도해주세요!"); // 실패 시 안내
            });
    });


    return wrapper; // DOM 요소 반환
}

// Toast 메시지 표시 함수
function showToast(message) {
    const toast = document.getElementById("copy-toast");
    toast.textContent = message; // 메시지 업데이트
    toast.classList.add("show"); // Toast 보이기

    // 2초 후 Toast 숨김
    setTimeout(() => {
        toast.classList.remove("show");
    }, 2000);
}


function enableDragAndDrop(container) {
    let draggedElement = null;
    let placeholder = null;
    let offsetX = 0;
    let offsetY = 0;
    let originalParent = null;
    let originalNextSibling = null;

    // 공통 함수: 위치 이동
    function moveAt(pageX, pageY) {
        if (!draggedElement) return;
        draggedElement.style.left = pageX - offsetX + "px";
        draggedElement.style.top = pageY - offsetY + "px";
    }

    function startDrag(pageX, pageY, target) {
        draggedElement = target;
        const rect = draggedElement.getBoundingClientRect();
        offsetX = pageX - rect.left;
        offsetY = pageY - rect.top;

        originalParent = draggedElement.parentNode;
        originalNextSibling = draggedElement.nextSibling;

        placeholder = document.createElement("div");
        placeholder.className = "drag-placeholder";
        placeholder.style.width = rect.width + "px";
        placeholder.style.height = rect.height + "px";
        originalParent.insertBefore(placeholder, draggedElement);

        draggedElement.style.position = "absolute";
        draggedElement.style.zIndex = "9999";
        draggedElement.style.width = rect.width + "px";
        draggedElement.style.height = rect.height + "px";
        draggedElement.style.boxSizing = "border-box";

        document.body.appendChild(draggedElement);
        moveAt(pageX, pageY);
    }

    function onDragMove(pageX, pageY) {
        moveAt(pageX, pageY);

        const linkPreviews = Array.from(container.querySelectorAll(".link-preview")).filter(el => el !== draggedElement);

        let inserted = false;
        for (let preview of linkPreviews) {
            const previewRect = preview.getBoundingClientRect();
            const centerY = previewRect.top + previewRect.height / 2;
            if (pageY < centerY) {
                container.insertBefore(placeholder, preview);
                inserted = true;
                break;
            }
        }

        if (!inserted) {
            container.appendChild(placeholder);
        }
    }

    function endDrag() {
        if (!draggedElement || !placeholder) return;

        placeholder.parentNode.insertBefore(draggedElement, placeholder);
        placeholder.remove();
        placeholder = null;

        // 스타일 복원
        draggedElement.style.position = "";
        draggedElement.style.zIndex = "";
        draggedElement.style.width = "";
        draggedElement.style.height = "";
        draggedElement.style.left = "";
        draggedElement.style.top = "";
        draggedElement = null;

        updateOrder(container);
    }

    // 마우스 이벤트
    container.addEventListener("mousedown", (e) => {
        const handle = e.target.closest(".drag-handle");
        if (!handle) return;
        e.preventDefault(); // PC 스크롤 방지

        const target = e.target.closest(".link-preview");
        if (target) {
            startDrag(e.pageX, e.pageY, target);

            function onMouseMove(e) {
                e.preventDefault();
                onDragMove(e.pageX, e.pageY);
            }

            function onMouseUp(e) {
                document.removeEventListener("mousemove", onMouseMove);
                document.removeEventListener("mouseup", onMouseUp);
                endDrag();
            }

            document.addEventListener("mousemove", onMouseMove);
            document.addEventListener("mouseup", onMouseUp);
        }
    });

    // 터치 이벤트
    container.addEventListener("touchstart", (e) => {
        const handle = e.target.closest(".drag-handle");
        if (!handle) return;
        e.preventDefault(); // 모바일 브라우저 기본 스크롤 방지

        const target = e.target.closest(".link-preview");
        if (target && e.touches.length === 1) {
            const touch = e.touches[0];
            startDrag(touch.pageX, touch.pageY, target);

            function onTouchMove(e) {
                e.preventDefault(); // 스크롤 방지
                const touch = e.touches[0];
                onDragMove(touch.pageX, touch.pageY);
            }

            function onTouchEnd(e) {
                document.removeEventListener("touchmove", onTouchMove, { passive: false });
                document.removeEventListener("touchend", onTouchEnd);
                endDrag();
            }

            document.addEventListener("touchmove", onTouchMove, { passive: false });
            document.addEventListener("touchend", onTouchEnd);
        }
    }, { passive: false }); // touchstart도 passive: false 추천
}

function updateOrder(container) {
    const items = container.querySelectorAll(".link-preview");
    const orderData = Array.from(items).map((item, index) => {
        const newOrder = index + 1;
        item.setAttribute("data-order", newOrder);
        return {
            id: item.getAttribute("data-id"),
            order: newOrder,
        };
    });

    fetch(`/api/link/${uuId}/update-order`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(orderData),
    })
        .then(response => {
            if (!response.ok) throw new Error("순서 업데이트 실패");
            return response.json();
        })
        .then(data => console.log("순서 업데이트 성공:", data))
        .catch(error => console.error("순서 업데이트 오류:", error));

    console.log("Updated order:", orderData);

    // fetch("/api/link/update-order", { ... }) 로 서버에 전송 가능
}