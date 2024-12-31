/**
 * Pull-to-Refresh 기능을 초기화
 * @param {Object} options - 설정 옵션
 * @param {Function} options.onRefresh - 새로고침 로직 실행 함수
 * @param {HTMLElement} options.refreshIcon - 새로고침 아이콘 요소
 * @param {number} options.refreshThreshold - 새로고침 트리거 임계값 (기본값: 800)
 */
function initializePullToRefresh({ onRefresh, refreshIcon, refreshThreshold = 800 }) {
    let isPulling = false; // Pull-to-Refresh 상태 확인
    let startY = 0; // 터치/스크롤 시작 위치
    let currentY = 0; // 터치/스크롤 중 현재 위치
    let accumulatedScroll = 0; // 스크롤 누적 거리

    // 공통 초기화 함수
    function resetPullState() {
        setTimeout(() => {
            refreshIcon.style.display = 'none';
        }, 800); // 1초 뒤 아이콘 숨김
        isPulling = false;
        startY = 0;
        currentY = 0;
        accumulatedScroll = 0;
    }

    // 터치 시작
    document.addEventListener('touchstart', (e) => {
        if (window.scrollY === 0) {
            isPulling = true;
            startY = e.touches[0].clientY;
        }
    });

    // 터치 이동
    document.addEventListener('touchmove', (e) => {
        if (!isPulling) return;

        currentY = e.touches[0].clientY;
        const diffY = currentY - startY;

        if (diffY > refreshThreshold) {
            refreshIcon.style.display = 'block';
        }
    });

    // 터치 종료
    document.addEventListener('touchend', () => {
        if (!isPulling) return;

        const diffY = currentY - startY;

        if (diffY > refreshThreshold) {
            // 새로고침 로직 실행
            refreshIcon.style.display = 'block';
            onRefresh();
        } else {
            refreshIcon.style.display = 'none';
        }

        resetPullState();
    });

    // 스크롤 감지 (트랙패드 포함)
    document.addEventListener('scroll', () => {
        if (window.scrollY === 0 && !isPulling) {
            isPulling = true;
            accumulatedScroll = 0; // 누적 스크롤 초기화
        } else if (window.scrollY > 0) {
            isPulling = false; // 스크롤이 아래로 이동하면 리셋
        }
    });

    // 휠 스크롤 감지 (트랙패드 포함)
    document.addEventListener('wheel', (e) => {
        if (isPulling && e.deltaY < 0) {
            accumulatedScroll += Math.abs(e.deltaY); // 위로 스크롤 시 누적

            if (accumulatedScroll > refreshThreshold) {
                refreshIcon.style.transform = `translateY(${Math.min(accumulatedScroll - refreshThreshold, 50)}px)`;
                refreshIcon.style.opacity = '1';
                onRefresh();
            }
        }
    });
}