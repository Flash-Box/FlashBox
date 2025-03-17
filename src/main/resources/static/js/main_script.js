const JWT_ERROR_MSG = "jwt 토큰 인증 실패"

document.addEventListener("DOMContentLoaded", async function getBoxes() {
    const token = sessionStorage.getItem("accessToken");
    if (!token) {
        alert("로그인이 필요합니다.");
        window.location.href = "/login";
        return;
    }

    try {
// 닉네임 제거 - main에서만 반영되고 있음. 모든 페이지의 js에 아래 코드를 각각 반영하면 관리하기 어려우므로 통일성을 위해 제거함
        // 🔹 로그인된 유저의 닉네임 가져오기
//        const nickname = sessionStorage.getItem("nickname") || "사용자";
//        document.querySelector(".nickname").textContent = nickname;

        // 🔹 박스 리스트 가져오기
        const boxResponse = await fetch("/api/boxes", {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Accept": "application/json"
            }
        });

        const boxes = await boxResponse.json();
        if (boxResponse.ok) {
            console.log("📦 박스 리스트:", boxes);
            await renderBoxes(boxes); // 박스 렌더링 실행

        }else {
            alert("❌박스 조회 실패");
            if (boxes.message === JWT_ERROR_MSG) {
                alert("😭JWT 토큰 만료");
                try {
                    const newToken = await refreshToken();
                    if (newToken) {
                        await getBoxes(); // 새로운 토큰으로 재시도
                    }
                } catch (error) {
                    console.error("토큰 갱신 실패:", error);
                    alert("🔒재로그인이 필요합니다.");
                    window.location.href = "/login"
                }
            }
        }
    } catch (error) {
        console.error("🚨 오류 발생:", error);
        alert("데이터를 불러오는 중 문제가 발생했습니다.");
    }
});

// 🔹 박스 리스트 렌더링 함수
async function renderBoxes(boxes) {
    const container = document.getElementById("box-container");
    container.innerHTML = "";

    boxes.data.forEach(box => {
        const images = box.images || [];
        const thumbnailSrc = images.length > 0 ? `${images[0]}?nocache=${Math.random()}` : "/images/default-thumbnail.jpg";

        console.log("📸 박스 이미지 리스트:", images);
        console.log("🔹 최종 썸네일 경로:", thumbnailSrc);

        const boxHTML = `
            <div class="selectable-box" data-bid="${box.bid}">
                <div class="card">                        
                    <div class="thumbnail-container">
                        <img src="${thumbnailSrc}" alt="썸네일 이미지" class="thumbnail-img" data-bid="${box.bid}"
                             onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                        <div class="thumbnail-fallback" style="display: none;">썸네일 이미지</div> 	 
                        <div class="thumbnail-overlay">상세보기</div> 
                    </div>
                    <h4 class="card-title">${box.name}</h4>
                    <h5 class="box-info">📅 모임 날짜: ${box.eventStartDate}</h5>
                    <h5 class="box-info">⏳ 최종 업로드: ${box.modifiedDate}</h5>
                    <h5 class="box-info boom-date">🔥 폭파 날짜: ${box.boomDate}</h5>
                </div>
            </div>
        `;
        container.innerHTML += boxHTML;
    });

    addEventListeners();
}



// 🔹 박스 선택/해제 및 다운로드/삭제 이벤트 리스너 추가
function addEventListeners() {
    const actionButtonsContainer = document.getElementById("action-buttons-container");
    const downloadBtn = document.getElementById("download-btn");
    const deleteBtn = document.getElementById("delete-btn");
    const selectedCard = new Set();

    document.querySelectorAll(".selectable-box").forEach(card => {
        const bid = card.getAttribute("data-bid");

        // 📌 박스 선택 기능 (클릭하면 선택됨)
        card.addEventListener("click", function (event) {
            // 썸네일 상세보기를 클릭했을 때는 선택 기능이 실행되지 않도록 함
            if (event.target.classList.contains("thumbnail-container")) {
                return;
            }

            if (selectedCard.has(bid)) {
                selectedCard.delete(bid);
                card.classList.remove("selected-box");
            } else {
                selectedCard.add(bid);
                card.classList.add("selected-box");
            }
            actionButtonsContainer.style.display = selectedCard.size > 0 ? "flex" : "none";
        });
    });

/*    // 📌 상세 보기 버튼을 클릭하면 상세 페이지로 이동
    document.querySelectorAll(".detail-btn").forEach(button => {
        button.addEventListener("click", function (event) {
            event.stopPropagation(); // 부모 div 클릭 이벤트 방지
            const bid = button.getAttribute("data-bid");
            window.location.href = `/box/${bid}`;
        });
    });
*/

    // 썸네일 컨테이너 클릭 시 상세 페이지로 이동
    document.querySelectorAll(".thumbnail-container").forEach(container => {
        container.addEventListener("click", function (event) {
            event.stopPropagation(); // 박스 선택 이벤트 방지
            const bid = container.querySelector(".thumbnail-img").getAttribute("data-bid");
            window.location.href = `/box/${bid}`;
        });
    });

    // 🔹 다운로드 버튼 클릭 시 API 요청
    downloadBtn.addEventListener("click", async function () {
        if (!selectedCard) {
            alert("다운로드할 박스를 선택하세요!");
            return;
            }
			
			if(selectedCard.size > 1) {
				alert("박스 전체 다운은 1개의 박스만 선택 가능합니다!");
				return;
			}
			
            try {
                const token = sessionStorage.getItem("accessToken");
                const bid = Array.from(selectedCard)[0]; // ✅ 첫 번째 선택된 박스의 bid 값
                const response = await fetch(`/box/${bid}/download`, {
                method: "GET",
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                    }
                });

                // 응답 본문을 한 번만 파싱
                const result = await response.json();

                // 상태 코드가 200이 아닌 경우 에러 처리
                if (!response.ok) {
                    alert("다운로드 오류: " + result.message);
                    selectedCard.classList.remove("selected-box");
                    actionButtonsContainer.style.display = "none";
                    selectedCard = null;
                    return;
                }

                // JSON 응답에서 downloadUrl 추출
                const downloadUrl = result.downloadUrl;

                // 브라우저를 해당 URL로 리다이렉트하여 다운로드 실행
                window.location.href = downloadUrl;
            } catch (error) {
                console.error("다운로드 오류:", error);
                alert("다운로드 중 오류가 발생했습니다.");
            }
    });

	// 🔹 삭제 버튼 클릭 시 `deleteBox` 실행
    deleteBtn.addEventListener("click", async function () {
        await deleteBox(); // 삭제 함수 실행
    });

		async function deleteBox() {
	    try {
	        const deleteBtn = document.getElementById("delete-btn");
	        deleteBtn.disabled = true; // 중복 클릭 방지
	
	        const token = sessionStorage.getItem("accessToken");
	
	        if (selectedCard.size === 0) {
	            alert("삭제할 박스를 선택하세요!");
	            deleteBtn.disabled = false;
	            return;
	        }
	
	        // 선택된 모든 박스의 bid 배열을 가져옴
	        const bidList = Array.from(selectedCard);
	
	        console.log("🛠 삭제 요청 시작 - 선택된 박스 bid:", bidList);
	
	        const response = await fetch(`/box`, {
	            method: "DELETE",
	            headers: {
	                "Authorization": `Bearer ${token}`,
	                "Content-Type": "application/json"
	            },
	            body: JSON.stringify(bidList) // bid를 JSON 배열로 전송
	        });
	
	        const responseData = await response.json();
	        if (response.ok) {
	            alert("✅ 박스 삭제 성공");
	
	            // UI에서 삭제된 박스 제거
	            bidList.forEach(bid => {
	                const selectedElement = document.querySelector(`[data-bid="${bid}"]`);
	                if (selectedElement) {
	                    selectedElement.remove();
	                }
	            });
	
	            actionButtonsContainer.style.display = "none"; // 버튼 숨기기
	            selectedCard.clear(); // 선택 목록 초기화
	
	            window.location.href = "/main";
	        } else {
	            alert("❌ 박스 삭제 실패: " + responseData.message);
	
	            if (responseData.message === JWT_ERROR_MSG) {
	                alert("😭 JWT 토큰 만료");
	                try {
	                    const newToken = await refreshToken();
	                    if (newToken) {
	                        await deleteBox(); // 새로운 토큰으로 재시도
	                    }
	                } catch (error) {
	                    console.error("토큰 갱신 실패:", error);
	                    alert("🔒 재로그인이 필요합니다.");
	                    window.location.href = "/login";
	                }
	            }
	        }
	    } catch (error) {
	        console.error("🚨 삭제 오류 발생:", error);
	        alert("삭제 중 오류가 발생했습니다.");
	    } finally {
	        deleteBtn.disabled = false;
	    }
	}
}


    
    