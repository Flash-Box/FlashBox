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

async function deleteBox(){
    try {
        console.log("deleteBox 실행됨"); // ✅ 확인
        const bidList = [parseInt(bid)];

        const token = sessionStorage.getItem("accessToken");

        const response = await fetch(`/box`, {
            method: "DELETE",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(bidList)
        })


        if (response.ok) {
            alert("✅박스 삭제 성공");
            window.location.href = "/main";

        } else {
            const responseData = await response.json();
            alert("❌박스 삭제 실패: " + responseData.message);

            if (responseData.message === JWT_ERROR_MSG) {
                alert("😭JWT 토큰 만료");
                try {
                    const newToken = await refreshToken();
                    if (newToken) {
                        await deleteBox(); // 새로운 토큰으로 재시도
                    }
                } catch (error) {
                    console.error("토큰 갱신 실패:", error);
                    alert("🔒재로그인이 필요합니다.");
                    window.location.href = "/login"
                }
            }
        }
    }catch (error) {
        console.error("Error:", error)
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
});

    // 🔹 삭제 요청 (DELETE)
    document.getElementById("deleteButton").addEventListener("click", async function handleDelete() {
        try {
            if (!confirm("정말 삭제하시겠습니까?")) {
                console.log("사용자가 취소를 눌렀습니다."); // ✅ 확인
                return;
            }

            console.log("사용자가 확인을 눌렀습니다. deleteBox 실행"); // ✅ 확인
            await deleteBox();
        }catch (error) {
            console.error("Error:", error)
        }

    });

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
