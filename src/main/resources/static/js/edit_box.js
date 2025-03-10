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

document.addEventListener("DOMContentLoaded", function () {
	
    const form = document.getElementById("updateForm");
    const bid = form.getAttribute("data-bid");


    // 🔹 수정 요청 (PUT)
    form.addEventListener("submit", async function updateBox(event) {
        try {
            event.preventDefault();

            const formData = {
                name: document.getElementById("name").value,
                eventStartDate: document.getElementById("eventStartDate").value,
                eventEndDate: document.getElementById("eventEndDate").value
            };

            const token = sessionStorage.getItem("accessToken");

            const response = await fetch(`/box/${bid}`, {
                method: "PUT",
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(formData)
            });

            const responseData = await response.json();
            if (response.ok) {
                alert("✅박스 수정 성공");
                window.location.href = "/main";

            } else {
                alert("❌박스 수정 실패: " + responseData.message);

                if (responseData.message === JWT_ERROR_MSG) {
                    alert("😭JWT 토큰 만료");
                    try {
                        const newToken = await refreshToken();
                        if (newToken) {
                            await updateBox(event); // 새로운 토큰으로 재시도
                        }
                    } catch (error) {
                        console.error("토큰 갱신 실패:", error);
                        alert("🔒재로그인이 필요합니다.");
                        window.location.href="/login"
                    }
                }
            }
        }catch(error){
            console.error("Error:", error)
        }
    });

    // 🔹 삭제 요청 (DELETE)
    document.getElementById("deleteButton").addEventListener("click", function () {
        if (!confirm("정말 삭제하시겠습니까?")) {
            return;
        }

        const bidList = [parseInt(bid)]; 

        fetch(`/box`, {
            method: "DELETE",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
        	},
        	body: JSON.stringify(bidList)
        })
        .then(response => {
            if (response.ok) {
		        return response.text().then(msg => {
		            alert(msg);
		            window.location.href = "/main";
		        });
		    } else {
		        return response.text().then(errMsg => {
		            alert("삭제 실패: " + errMsg);
		        });
		    }
        })
        .catch(error => console.error("Error:", error));
    });
});