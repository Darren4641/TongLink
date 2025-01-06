// 캐시 이름에 버전 추가
const CACHE_NAME = 'tonglink-v0.2.26'; // 매 배포 시 버전을 변경

const FILES_TO_CACHE = [
    "/", // HTML
    "/css/common.css",
    "/js/user.js",
    "/images/tonglink-logo-white.png",
    "/images/tonglink-logo-white.png",
    "/images/home.png",
    "/images/star.png",
    "/images/user.png",
    "/images/icon_profile.png",
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
                    return caches.delete(key);
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
        caches.match(event.request).then((cachedResponse) => {
            const fetchPromise = fetch(event.request).then((networkResponse) => {
                if (networkResponse.ok) {
                    // 새 요청 결과를 캐시에 업데이트
                    caches.open(CACHE_NAME).then((cache) => {
                        cache.put(event.request, networkResponse.clone());
                    });
                }
                return networkResponse;
            });

            // 캐시된 응답 또는 네트워크 요청 결과 반환
            return cachedResponse || fetchPromise;
        })
    );
});

// 푸시 이벤트 처리
self.addEventListener('push', (event) => {
    let data = { title: '새로운 알림', body: '알림 내용입니다.', icon: '/icon.png' };

    if (event.data) {
        data = event.data.json();
    }

    const options = {
        body: data.body,
        icon: data.icon,
        data: data.data || {}, // 추가 데이터 (예: URL)
    };

    event.waitUntil(
        self.registration.showNotification(data.title, options)
    );
});

// 알림 클릭 이벤트 처리
self.addEventListener('notificationclick', (event) => {
    event.notification.close();

    const targetUrl = event.notification.data.url || '/';

    event.waitUntil(
        clients.matchAll({ type: 'window', includeUncontrolled: true }).then((clientsArr) => {
            const client = clientsArr.find((c) => c.url === targetUrl);
            if (client) {
                return client.focus();
            } else {
                return clients.openWindow(targetUrl);
            }
        })
    );
});