

let draggedElement = null; // 드래그 중인 요소
let placeholder = null; // 자리 표시자
let isDragging = false; // 드래그 상태 플래그


function initInfiniteScroll(uuId, container) {
    let currentPage = 0; // 현재 페이지 번호
    let isLoading = false; // 데이터 로딩 중 상태
    const limit = 5; // 페이지당 데이터 개수

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
                console.log(`Observer triggered for page: ${currentPage}`);
                isLoading = true; // 로딩 중 상태 설정

                // 다음 데이터를 가져옴
                getTongLinkList(uuId, container, limit, currentPage, (nextPage, dataLength) => {
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
            root: null, // 기본 뷰포트
            rootMargin: "0px",
            threshold: 0.2, // 20% 보일 때 트리거
        }
    );

    // Observer 시작
    observer.observe(sentinel);

    // 첫 데이터 로드
    isLoading = true;
    getTongLinkList(uuId, container, limit, currentPage, (nextPage, dataLength) => {
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
 * @param {string} uuId - 사용자 UUID
 * @param {HTMLElement} container - 렌더링할 HTML 컨테이너
 * @param {number} limit - 한 페이지당 가져올 데이터 수
 * @param {number} page - 현재 페이지 번호
 * @param {Function} callback - 다음 페이지 번호와 가져온 데이터 길이를 전달하는 콜백 함수
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
            return response.json();
        })
        .then((data) => {
            // 첫 페이지 로딩 시 기존 콘텐츠 제거
            if (page === 0 && container.querySelector(".link-preview")) {
                container.querySelectorAll(".link-preview").forEach((el) => el.remove());
            }

            // 새로운 데이터 렌더링
            const content = data.data.content || [];
            content.forEach((link) => {
                const linkPreview = createLinkPreview(link);
                container.insertBefore(linkPreview, container.querySelector(".scroll-sentinel"));
            });

            console.log(`Current page loaded: ${page}`);
            callback(page + 1, content.length); // 데이터 길이를 반환
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
        <div class="link-preview-content">
        <button class="drag-handle">
            <img src="/images/drag.png" alt="드래그 아이콘">
        </button>
            <div class="title-container">
                
                <span class="d-day"></span>
                <h3 class="link-title">${link.title}</h3>
                <button class="update-button">
                        <img src="/images/update.png" alt="복사 아이콘">
                </button>
            </div>
            <button class="copy-button" style="padding: 0">
                <span class="link-url">${link.proxyUrl}</span>
            </button>
            <p class="link-description">만료일 - ${link.endDate}</p>
        </div>
        <!-- 오른쪽 컨텐츠 -->
        <div class="link-preview-thumbnail">
            <a href="${link.proxyUrl}/${link.id}" target="_blank" rel="noopener noreferrer">
                <img src="${link.thumbnailUrl}">
            </a>
        </div>
        
        <!-- 복사 완료 메시지 -->
        <div id="copy-toast" class="copy-toast">복사되었습니다!</div> 
    `;

    updateDday(wrapper, link.endDate);

    const updateButton = wrapper.querySelector(".update-button");
    updateButton.addEventListener("click", () => {
        openModal(link); // 모달 열기
    });

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
    // 기존 이벤트 핸들러 제거
    container.removeEventListener("mousedown", handleMouseDown);
    container.removeEventListener("touchstart", handleTouchStart);

    // 새로운 이벤트 핸들러 추가
    container.addEventListener("mousedown", handleMouseDown);
    container.addEventListener("touchstart", handleTouchStart, { passive: false });

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
        if (isDragging) return; // 이미 드래그 중이라면 무시

        draggedElement = target;
        const rect = draggedElement.getBoundingClientRect();
        offsetX = pageX - rect.left;
        offsetY = pageY - rect.top;
        isDragging = true; // 드래그 상태 활성화

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

        // Sentinel을 항상 컨테이너의 맨 마지막으로 이동
        const sentinel = container.querySelector(".scroll-sentinel");
        if (sentinel) {
            container.appendChild(sentinel);
        }
    }

    function endDrag() {
        if (!isDragging || !draggedElement || !placeholder) return;

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

        placeholder = null;
        draggedElement = null;
        isDragging = false; // 드래그 상태 해제

        updateOrder(container);
    }

    function handleMouseDown(e) {
        const handle = e.target.closest(".drag-handle");
        if (!handle) return;

        const target = e.target.closest(".link-preview");
        if (target && !target.classList.contains("scroll-sentinel")) {
            startDrag(e.pageX, e.pageY, target);

            function onMouseMove(e) {
                e.preventDefault();
                onDragMove(e.pageX, e.pageY);
            }

            function onMouseUp() {
                document.removeEventListener("mousemove", onMouseMove);
                document.removeEventListener("mouseup", onMouseUp);
                endDrag();
            }

            document.addEventListener("mousemove", onMouseMove);
            document.addEventListener("mouseup", onMouseUp);
        }
    }
    function handleTouchStart(e) {
        const handle = e.target.closest(".drag-handle");
        if (!handle) return;

        const target = e.target.closest(".link-preview");
        if (target && e.touches.length === 1) {
            const touch = e.touches[0];
            startDrag(touch.pageX, touch.pageY, target);

            function onTouchMove(e) {
                e.preventDefault();
                const touch = e.touches[0];
                onDragMove(touch.pageX, touch.pageY);
            }

            function onTouchEnd() {
                document.removeEventListener("touchmove", onTouchMove, { passive: false });
                document.removeEventListener("touchend", onTouchEnd);
                endDrag();
            }

            document.addEventListener("touchmove", onTouchMove, { passive: false });
            document.addEventListener("touchend", onTouchEnd);
        }
    }

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

/**
 * Calculate and update D-day
 * @param {HTMLElement} container - The container element for the link preview
 * @param {string} endDate - The expiration date string (e.g., "2024-12-22 01:13:12")
 */
function updateDday(container, endDate) {
    const dDayElement = container.querySelector(".d-day");

    // ISO 8601 형식으로 변환
    const parsedDate = endDate.replace(" ", "T"); // 공백을 'T'로 교체
    const expiryDate = new Date(`${parsedDate}+09:00`); // KST 타임존 추가
    const today = new Date(); // 현재 시간

    // 날짜가 유효하지 않은 경우 처리
    if (isNaN(expiryDate.getTime())) {
        console.error("Invalid date format:", endDate);
        dDayElement.textContent = "Invalid Date";
        dDayElement.style.color = "red";
        return;
    }

    // D-Day 계산
    const diffTime = expiryDate - today;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)); // 밀리초를 일 단위로 변환

    // D-Day 업데이트
    if (diffDays > 0) {
        dDayElement.textContent = `D-${diffDays}`;
        dDayElement.style.color = "#ea8080";
    } else if (diffDays === 0) {
        dDayElement.textContent = "D-Day";
        dDayElement.style.color = "#ea8080";
    } else {
        dDayElement.textContent = "Expired";
        dDayElement.style.color = "red";
        container.style.opacity = "0.5";
    }

    dDayElement.style.marginRight = "10px";
}

function initModal() {
    const modal = document.getElementById("update-modal");
    const deleteButton = document.getElementById("delete-modal");
    const form = modal.querySelector("#link-update-form");
    // 삭제 버튼 클릭 이벤트
    deleteButton.addEventListener("click", () => {
        const linkId = modal.getAttribute("data-id"); // 모달에서 저장된 ID 가져오기

        if (linkId) {
            // 삭제 요청 보내기
            if(confirm("정말 삭제하시겠습니까?")) {
                const body = {
                    id: linkId,
                    uuId: localStorage.getItem("userUUID")
                }

                console.log(body);
                showLoadingOverlay();

                const container = document.getElementById("tonglink-list");

                // 서버로 업데이트 요청 보내기
                deleteLink(body)
                    .then((updateLink) => {
                        modal.style.display = "none";
                        initInfiniteScroll(localStorage.getItem("userUUID"), container);
                    })
                    .catch((error) => {
                        console.error("링크 삭제 실패:", error);
                    })
                    .finally(() => {
                        hideLoadingOverlay();
                        form.reset();
                    });
                // 모달 닫기
                closeModal(form);
            }
            //deleteLink(linkId);
        } else {
            console.error("삭제할 ID를 찾을 수 없습니다.");
        }
    });


    // 모달 외부 클릭 시 닫기 이벤트 등록 (한 번만 실행)
    modal.addEventListener("click", (event) => {
        if (event.target === modal) {
            closeModal(); // 모달 닫기
        }
    });
}

function openModal(link) {
    const modal = document.getElementById("update-modal");
    const closeModalButton = document.getElementById("update-close-modal");
    const form = modal.querySelector("#link-update-form");
    const toggle = document.getElementById("update-toggle");
    // 모달을 보여줌
    modal.style.display = "flex";
    console.log("link.isExposure = " + link.isExposure);
    // 기존 값을 폼에 채워넣음
    form.title.value = link.title || ""; // 제목
    form.originUrl.value = link.originUrl || ""; // 원본 URL
    form.color.value = link.color || "#ff0000"; // 그래프 색상 기본값
    if(link.isExposure) {
        toggle.checked = true;
    }


    modal.setAttribute("data-id", link.id);

    // 저장 버튼에 대한 이벤트 리스너 추가
    form.onsubmit = (e) => {
        e.preventDefault();

        // 업데이트된 값 가져오기
        const updatedTitle = form.title.value;
        const updatedColor = form.color.value;

        const body = {
            id: link.id,
            uuId: localStorage.getItem("userUUID"),
            title: updatedTitle,
            color: updatedColor,
            isExposure: toggle.checked
        }

        console.log(body);
        showLoadingOverlay();

        const container = document.getElementById("tonglink-list");

        // 서버로 업데이트 요청 보내기
        updateLink(body)
            .then((updateLink) => {
                modal.style.display = "none";
                initInfiniteScroll(localStorage.getItem("userUUID"), container);
            })
            .catch((error) => {
                console.error("새 링크 추가 실패:", error);
            })
            .finally(() => {
                hideLoadingOverlay();
                form.reset();
            });
        // 모달 닫기
        closeModal(form);
    };

}

function closeModal() {
    const modal = document.getElementById("update-modal");
    modal.style.display = "none"; // 모달 숨김
}

function updateLink(body) {
    return fetch("/api/link", {
        method: "PATCH",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(body),
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error("링크 저장 실패");
            }
            return response.json(); // 서버에서 반환된 링크 데이터
        })
        .catch((error) => {
            console.error("서버 오류:", error);
        });
}

function deleteLink(body) {
    return fetch("/api/link", {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(body),
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error("링크 저장 실패");
            }
            return response.json(); // 서버에서 반환된 링크 데이터
        })
        .catch((error) => {
            console.error("서버 오류:", error);
        });
}

function setSentinelHeight(sentinel) {
    const viewportHeight = window.innerHeight;
    const sentinelHeight = Math.max(50, viewportHeight * 0.1); // 최소 50px, 뷰포트 높이의 10%
    sentinel.style.height = `${sentinelHeight}px`;
}

initModal();