let deferredPrompt = null; // 전역 변수 선언

// beforeinstallprompt 이벤트를 감지하여 설치 프로세스 준비(Android)
window.addEventListener('beforeinstallprompt', (e) => {
    console.log('beforeinstallprompt 이벤트 발생');
    e.preventDefault(); // 기본 배너 표시 방지
    deferredPrompt = e; // 이벤트 저장
});

function initPwaBanner(alwaysShow) {
    const overlay = document.querySelector('.pwa-overlay');
    const installButton = document.getElementById('install-button');
    const closeButton = document.getElementById('close-banner');
    const installContainer = document.getElementById('install-container');

    // 배너 표시 조건 확인
    if (!shouldShowBanner(alwaysShow)) {
        console.log('배너 표시 조건이 만족되지 않음');
        return; // 조건을 만족하지 않으면 배너를 표시하지 않음
    }

    // iOS일 경우 버튼 숨기고 설치 가이드 표시
    if (isIosDevice()) {
        if (installButton != null) {
            installButton.style.display = 'none'; // 버튼 숨기기
        }
        showIosInstallationGuide(installContainer); // iOS 설치 가이드 표시
    }

    // 배너 표시
    overlay.classList.remove('hidden');

    // 닫기 버튼 동작
    closeButton.removeEventListener('click', () => dismissPwaBanner(overlay));
    closeButton.addEventListener('click', () => dismissPwaBanner(overlay));

    // 앱 설치 버튼 동작 (Android 전용)
    installButton.addEventListener('click', () => {
        if (deferredPrompt) {
            installPwa(deferredPrompt, overlay);
        } else {
            alert('이 브라우저는 앱 설치를 지원하지 않습니다.');
        }
    });


}

/**
 * 배너 표시 조건 확인
 * @param {boolean} alwaysShow - true이면 항상 배너를 표시, false이면 첫 방문 시만 표시
 * @returns {boolean}
 */
function shouldShowBanner(alwaysShow) {
    if (alwaysShow) {
        return true; // 항상 표시
    }
    const isVisited = localStorage.getItem('pwa-banner-shown');
    if (isVisited) {
        return false; // 이미 방문한 경우 표시하지 않음
    }
    localStorage.setItem('pwa-banner-shown', 'true'); // 방문 기록 저장
    return true; // 첫 방문 시 표시
}

/**
 * PWA 배너 닫기
 * @param {HTMLElement} overlay
 */
function dismissPwaBanner(overlay) {
    overlay.classList.add('hidden');
}

/**
 * Android PWA 설치 프로세스
 * @param {Event} deferredPrompt
 * @param {HTMLElement} overlay
 */
function installPwa(deferredPrompt, overlay) {
    deferredPrompt.prompt();
    deferredPrompt.userChoice.then((choiceResult) => {
        if (choiceResult.outcome === 'accepted') {
            console.log('User accepted the PWA installation');
        } else {
            console.log('User dismissed the PWA installation');
        }
        deferredPrompt = null; // 초기화
        dismissPwaBanner(overlay); // 배너 닫기
    });
}

/**
 * iOS 디바이스 설치 안내를 버튼 대신 표시
 * @param {HTMLElement} installContainer
 */
function showIosInstallationGuide(installContainer) {
    installContainer.innerHTML = `
        <div class="ios-install-guide">
            <p>iOS에서 PWA를 설치하려면:</p>
            <ol>
                <li>Safari에서 브라우저 하단의 <strong>공유 버튼</strong>(사각형 + 화살표)을 클릭하세요.</li>
                <li><strong>"홈 화면에 추가"</strong>를 선택하세요.</li>
                <li>추가 화면에서 <strong>"추가"</strong> 버튼을 눌러주세요.</li>
            </ol>
        </div>
    `;
}

/**
 * iOS 디바이스 여부 확인
 * @returns {boolean}
 */
function isIosDevice() {
    const userAgent = navigator.userAgent.toLowerCase();
    return /iphone|ipad|ipod/.test(userAgent);
}