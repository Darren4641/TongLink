// 모달 관리


function saveLink(body) {
    // 서버로 데이터 전송
    fetch("/api/link", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(body),
    })
        .then((response) => {
            if (response.ok) {
                console.log("링크 저장 성공");
            } else {
                console.error("링크 저장 실패");
            }
        })
        .catch((error) => {
            console.error("서버 오류:", error);
        });
}

