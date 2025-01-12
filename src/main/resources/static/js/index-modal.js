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
                // 상태 코드가 400일 경우 처리
                if (response.status === 400) {
                    return response.json().then((errorData) => {
                        alert(`에러 발생: ${errorData.message || "잘못된 요청입니다."}`);
                        throw new Error(`400 에러: ${errorData.message || "잘못된 요청"}`);
                    });
                } else {
                    throw new Error(`HTTP 에러: ${response.status}`);
                }
            }
            return response.json(); // 서버에서 반환된 링크 데이터
        })
        .catch((error) => {
            console.error("서버 오류:", error);
        });
}

