const JWT_ERROR_MSG = "jwt 토큰 인증 실패"

function validateDates() {
    var startDate = document.getElementById("eventStartDate");
    var endDate = document.getElementById("eventEndDate");

    if (startDate.value) {
        endDate.min = startDate.value;
    }
    if (endDate.value) {
        startDate.max = endDate.value;
    }
    
    if (startDate.value && endDate.value) {
        var start = new Date(startDate.value);
        var end = new Date(endDate.value);

        if (start > end) {
            alert("시작 날짜가 종료 날짜보다 클 수 없습니다.");
            endDate.value = "";
        }
    }
}

async function createBox(event) {
    try {
        event.preventDefault();  // 폼이 제출되지 않도록 막음

        const token = sessionStorage.getItem("accessToken"); // 저장된 토큰 가져오기

        const name = document.getElementById("name").value;
        const startDate = document.getElementById("eventStartDate").value;
        const endDate = document.getElementById("eventEndDate").value;

        const data = {
            name: name,
            eventStartDate: startDate,
            eventEndDate: endDate
        };

        const response = await fetch("/box", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(data)  // 데이터 전송
        })

        const responseData = await response.json();
        console.log("서버 응답 데이터:", responseData);

        if (responseData.success) {
            alert("✅박스 생성 성공!");
            window.location.href = '/main';
          
        } else {
            alert("❌박스 생성 실패");
            if (responseData.message === JWT_ERROR_MSG) {
                alert("😭JWT 토큰 만료");
                try {
                    const newToken = await refreshToken();
                    if (newToken) {
                        await createBox(event); // 새로운 토큰으로 재시도
                    }
                } catch (error) {
                    console.error("토큰 갱신 실패:", error);
                    alert("🔒재로그인이 필요합니다.");
                    window.location.href="/login"
                }
            }

        }
    }catch (error) {
        console.error("Error:", error);
    }
}

// 입력칸 클릭 시 달력 열기 기능 추가
    	document.addEventListener("DOMContentLoaded", function () {
        	const startDateInput = document.getElementById("eventStartDate");
        	const endDateInput = document.getElementById("eventEndDate");

        	startDateInput.addEventListener("click", function () {
            	this.showPicker();
        	});

        	endDateInput.addEventListener("click", function () {
            	this.showPicker();
        	});
    	});