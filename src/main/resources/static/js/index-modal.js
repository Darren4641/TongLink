// 모달 관리


function saveLink(body) {
    // 서버로 데이터 전송
    return fetch("/api/link", {
        method: "POST",
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

