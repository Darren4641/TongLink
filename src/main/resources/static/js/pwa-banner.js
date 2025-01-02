/**
 * PWA 설치 배너 초기화
 */
function initPwaBanner() {
    const banner = document.getElementById('pwa-install-banner');
    const installButton = document.getElementById('install-button');
    const iosGuideButton = document.getElementById('ios-guide-button');

    // 플랫폼별 배너 초기화
    if (shouldShowPwaBanner()) {
        if (isIosDevice()) {
            setupIosBanner(banner, iosGuideButton);
        } else if (isAndroidDevice()) {
            setupAndroidBanner(banner, installButton);
        }
    }
}

/**
 * PWA 배너를 보여줄지 여부 확인
 * @returns {boolean}
 */
function shouldShowPwaBanner() {
    return localStorage.getItem('pwa-banner-dismissed') !== 'true';
}

/**
 * iOS 디바이스 여부 확인
 * @returns {boolean}
 */
function isIosDevice() {
    const userAgent = navigator.userAgent.toLowerCase();
    return /iphone|ipad|ipod/.test(userAgent);
}

/**
 * Android 디바이스 여부 확인
 * @returns {boolean}
 */
function isAndroidDevice() {
    const userAgent = navigator.userAgent.toLowerCase();
    return /android/.test(userAgent);
}

/**
 * iOS 배너 설정
 * @param {HTMLElement} banner
 * @param {HTMLElement} iosGuideButton
 */
function setupIosBanner(banner, iosGuideButton) {
    document.body.classList.add('ios');
    banner.classList.remove('hidden');

    iosGuideButton.addEventListener('click', () => {
        alert('Safari에서 공유 버튼을 누르고 "홈 화면에 추가"를 선택하세요.');
        dismissPwaBanner(banner);
    });
}

/**
 * Android 배너 설정
 * @param {HTMLElement} banner
 * @param {HTMLElement} installButton
 */
function setupAndroidBanner(banner, installButton) {
    document.body.classList.add('android');
    banner.classList.remove('hidden');

    let deferredPrompt;
    window.addEventListener('beforeinstallprompt', (e) => {
        e.preventDefault();
        deferredPrompt = e;
        installButton.style.display = 'inline-block';
    });

    installButton.addEventListener('click', () => {
        if (deferredPrompt) {
            deferredPrompt.prompt();
            deferredPrompt.userChoice.then((choiceResult) => {
                if (choiceResult.outcome === 'accepted') {
                    console.log('User accepted the PWA installation');
                }
                deferredPrompt = null;
                dismissPwaBanner(banner);
            });
        }
    });
}

/**
 * PWA 배너 닫기
 * @param {HTMLElement} banner
 */
function dismissPwaBanner(banner) {
    localStorage.setItem('pwa-banner-dismissed', 'true');
    banner.classList.add('hidden');
}