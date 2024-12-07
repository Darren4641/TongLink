// 캐시 이름에 버전 추가
const CACHE_NAME = 'tonglink-v0.0.12'; // 매 배포 시 버전을 변경

const FILES_TO_CACHE = [

];

// 설치 이벤트
self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(CACHE_NAME).then((cache) => {
            console.log('Pre-caching offline page');
            return cache.addAll(FILES_TO_CACHE);
        })
    );
    self.skipWaiting(); // 새 버전 즉시 활성화
});

// 활성화 이벤트 - 이전 캐시 삭제
self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys().then((keyList) => {
            return Promise.all(
                keyList.map((key) => {
                    if (key !== CACHE_NAME) {
                        console.log('Removing old cache:', key);
                        return caches.delete(key);
                    }
                })
            );
        })
    );
    self.clients.claim(); // 활성화 후 클라이언트에 즉시 반영
});

// fetch 이벤트 - 네트워크 우선 로직
self.addEventListener('fetch', (event) => {
    event.respondWith(
        caches.match(event.request).then((response) => {
            return response || fetch(event.request);
        })
    );
});