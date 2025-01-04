// 사용자를 식별하는 UUID 관리

document.querySelectorAll('input, textarea').forEach((element) => {
    element.addEventListener('focus', () => {
        document.body.style.zoom = '1'; // 확대 방지
    });
    element.addEventListener('blur', () => {
        document.body.style.zoom = ''; // 원래 상태로 복원
    });
});

function manageUserUUID() {
    // localStorage에서 UUID 확인
    let userUUID = localStorage.getItem("userUUID");

    if (!userUUID) {
        // UUID가 없으면 새로 생성
        userUUID = generateUUID();
        localStorage.setItem("userUUID", userUUID);
        console.log("새 UUID 생성:", userUUID);
    } else {
        console.log("기존 UUID 확인:", userUUID);
    }

    // 서버에 UUID 전달
    sendUUIDToServer(userUUID);
}

function generateUUID() {
    return ([1e7]+-1e3+-4e3+-8e3+-1e11).replace(/[018]/g, c =>
        (c ^ crypto.getRandomValues(new Uint8Array(1))[0] & 15 >> c / 4).toString(16)
    );
}

/**
 * 서버로 UUID 전달
 * @param {string} uuId - 사용자 UUID
 */
function sendUUIDToServer(uuId) {
    fetch("/api/user", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ uuId: uuId }),
    })
        .then((response) => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error("UUID 저장 실패");
            }
        })
        .catch((error) => {
            console.error("서버 통신 오류:", error);
        })
        .then((data) => {
            console.log("서버 응답 데이터:", data.data);
            localStorage.setItem("userUUID", data.data);
        });

}

function sendUUIDToServerPWA(uuId) {
    fetch("/api/user/pwa", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({ uuId: uuId }),
    })
        .then((response) => {
            if (response.ok) {
                return response.json();
            } else {
                throw new Error("UUID 저장 실패");
            }
        })
        .catch((error) => {
            console.error("서버 통신 오류:", error);
        })
        .then((data) => {
            console.log("서버 응답 데이터:", data.data);
            localStorage.setItem("userUUID", data.data);
        });

}

async function fetchUserInfo(uuId) {

    return fetch(`/api/user/info/${uuId}`, {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
        },
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('네트워크 응답에 문제가 있습니다.');
            }
            return response.json();
        })
        .then((result) => {
            return result.data;
        })
}