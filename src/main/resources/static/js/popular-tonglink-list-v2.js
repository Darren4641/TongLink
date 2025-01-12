/**
 * Top 100 데이터를 가져와 렌더링
 * @param {HTMLElement} container - 렌더링할 HTML 컨테이너
 */
function loadTop100Links(container) {
    // 서버에서 Top 100 데이터 가져오기
    fetch('/api/link/popular?ignoreCache=false', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    })
        .then((response) => {
            if (!response.ok) {
                throw new Error(`HTTP error! Status: ${response.status}`);
            }
            return response.json();
        })
        .then((result) => {
            // 기존 데이터를 초기화
            container.innerHTML = '';

            // 가져온 데이터를 순서대로 렌더링
            result.data.forEach((link) => {
                const linkPreview = createLinkPreview(link);
                container.appendChild(linkPreview);
            });

            // 메달 추가
            addMedalsToList(container);

            console.log('Top 100 데이터 로드 완료');
        })
        .catch((error) => {
            console.error('링크 목록 가져오기 실패:', error);
        });
}

/**
 * LinkDto 데이터를 기반으로 링크 프리뷰 HTML 요소 생성
 * @param {Object} link - LinkDto 객체 (썸네일 URL 포함)
 * @returns {HTMLElement} - 생성된 링크 프리뷰 요소
 */
function createLinkPreview(link) {
    const wrapper = document.createElement('div');
    wrapper.className = 'link-preview';
    wrapper.id = `link-preview-${link.id}`;
    wrapper.setAttribute('data-id', link.id);
    wrapper.setAttribute('data-order', link.order);

    wrapper.innerHTML = `
        <div class="link-preview-content" style="margin-left: 7px; margin-top: 1px;">
            <div class="title-container" style="align-items: center;">
                <h3 class="link-title" style="max-width: 275px;">${link.title}</h3>
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

    const copyButton = wrapper.querySelector('.copy-button');
    copyButton.addEventListener('click', () => {
        navigator.clipboard
            .writeText(link.proxyUrl + '/' + link.id)
            .then(() => {
                showToast('URL이 클립보드에 복사되었습니다!');
            })
            .catch((error) => {
                console.error('클립보드 복사 실패:', error);
                showToast('클립보드 복사 실패. 다시 시도해주세요!');
            });
    });

    return wrapper;
}

/**
 * Toast 메시지 표시 함수
 * @param {string} message - 표시할 메시지
 */
function showToast(message) {
    const toast = document.getElementById('copy-toast');
    toast.textContent = message;
    toast.classList.add('show');

    setTimeout(() => {
        toast.classList.remove('show');
    }, 2000);
}

/**
 * 메달 표시를 위한 함수
 * @param {HTMLElement} container - 리스트 컨테이너
 */
function addMedalsToList(container) {
    const listItems = container.querySelectorAll('.link-preview');
    listItems.forEach((item, index) => {
        const medal = document.createElement('span');
        medal.className = 'medal';

        if (index === 0) medal.classList.add('gold');
        else if (index === 1) medal.classList.add('silver');
        else if (index === 2) medal.classList.add('bronze');
        else return;

        const titleContainer = item.querySelector('.title-container');
        if (titleContainer) {
            titleContainer.insertBefore(medal, titleContainer.firstChild);
        }
    });
}

document.addEventListener('DOMContentLoaded', () => {
    const listContainer = document.getElementById('tonglink-list');
    loadTop100Links(listContainer);
});