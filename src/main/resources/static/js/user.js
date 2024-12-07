// 사용자를 식별하는 UUID 관리
function manageUserUUID() {
    // localStorage에서 UUID 확인
    let userUUID = localStorage.getItem("userUUID");

    if (!userUUID) {
        // UUID가 없으면 새로 생성
        userUUID = crypto.randomUUID();
        localStorage.setItem("userUUID", userUUID);
        console.log("새 UUID 생성:", userUUID);
    } else {
        console.log("기존 UUID 확인:", userUUID);
    }

    // 서버에 UUID 전달
    sendUUIDToServer(userUUID);
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