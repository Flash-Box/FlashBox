const JWT_ERROR_MSG = "jwt 토큰 인증 실패"

document.addEventListener("DOMContentLoaded", async function () {
    const token = sessionStorage.getItem("accessToken");
    if (!token) {
        alert("로그인이 필요합니다.");
        window.location.href = "/login";
        return;
    }

    try {
        // 🔹 로그인된 유저의 닉네임 가져오기
        const nickname = sessionStorage.getItem("nickname") || "사용자";
        document.querySelector(".nickname").textContent = nickname;

        // 🔹 박스 리스트 가져오기
        const boxResponse = await fetch("/api/boxes", {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Accept": "application/json"
            }
        });

        if (!boxResponse.ok) throw new Error("박스 데이터를 불러오는 데 실패했습니다.");

        const boxes = await boxResponse.json();
        console.log("📦 박스 리스트:", boxes);

        await renderBoxes(boxes); // 박스 렌더링 실행
    } catch (error) {
        console.error("🚨 오류 발생:", error);
        alert("데이터를 불러오는 중 문제가 발생했습니다.");
    }
});

// 🔹 박스 리스트 렌더링 함수
async function renderBoxes(boxes) {
    const container = document.getElementById("box-container");
    container.innerHTML = ""; // 기존 내용 삭제

    boxes.data.forEach(box => {
        // 🔹 썸네일 설정: `box.images` 배열이 있으면 첫 번째 이미지를 사용, 없으면 기본 이미지
        const images = box.images || [];
        const thumbnailSrc = images.length > 0 ? images[0] : "/images/default-thumbnail.jpg";

        const boxHTML = `
            <div class="selectable-box" data-bid="${box.bid}">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">${box.name}</h5>
                        <p>📅 모임 날짜: ${box.eventStartDate}</p>
                        <p>⏳ 최종 업로드: ${box.modifiedDate}</p>
                        <p style="color: red;">🔥 폭파 날짜: ${box.boomDate}</p>
                       
                        <!-- 🔹 썸네일 이미지 추가 -->
                        <div class="thumbnail-container">
                            <img src="${thumbnailSrc}" alt="썸네일 이미지" class="thumbnail-img">
                        </div>
                        
                        <!-- 🔹 선택 해제 버튼 추가 -->
                        <button class="btn btn-danger deselect-btn" style="display: none;">선택 해제</button>
                    </div>
                </div>
            </div>
        `;
        container.innerHTML += boxHTML;
    });

    addEventListeners(); // 박스 이벤트 리스너 등록
}


// 🔹 박스 선택/해제 및 다운로드/삭제 이벤트 리스너 추가
function addEventListeners() {
    let selectedCard = null;
    let clickCount = 0;
    const actionButtonsContainer = document.getElementById("action-buttons-container");
    const downloadBtn = document.getElementById("download-btn");
    let deleteBtn = document.getElementById("delete-btn");

    document.querySelectorAll(".selectable-box").forEach(card => {
        const bid = card.getAttribute("data-bid");
        const deselectBtn = card.querySelector(".deselect-btn"); // 선택 해제 버튼 가져오기

        card.addEventListener("click", function () {
            if (selectedCard === this) {
                clickCount++;
                if (clickCount === 2) {
                    window.location.href = `/box/${bid}`;
                }
                return;
            }

            if (selectedCard !== null) {
                console.log("이미 선택된 박스가 있음. 다른 박스 선택 불가");
                return;
            }

            card.classList.add("selected-box");
            selectedCard = this;
            clickCount = 0;
            deselectBtn.style.display = "block"; // 🔹 선택 해제 버튼 보이기
            actionButtonsContainer.style.display = "flex"; // 다운로드/삭제 버튼 보이기
            console.log("카드 선택됨:", bid);
        });


         // 🔹 선택 해제 버튼 클릭 시 선택 해제
        deselectBtn.addEventListener("click", function (event) {
            event.stopPropagation();
            card.classList.remove("selected-box");
            selectedCard = null;
            clickCount = 0;
            deselectBtn.style.display = "none"; // 선택 해제 버튼 숨김
            actionButtonsContainer.style.display = "none"; // 다운로드/삭제 버튼 숨김
            console.log("선택 해제됨:", bid);
        });

        // 🔹 다운로드 버튼 클릭 시 API 요청
        downloadBtn.addEventListener("click", async function () {
            if (!selectedCard) {
                alert("다운로드할 박스를 선택하세요!");
                return;
            }

            try {
                const token = sessionStorage.getItem("accessToken");
                const response = await fetch(`/box/${selectedCard.getAttribute("data-bid")}/download`, {
                    method: "GET",
                    headers: {
                        "Authorization": `Bearer ${token}`,
                        "Content-Type": "application/json"
                    }
                });

                if (!response.ok) {
                    throw new Error("다운로드 실패!");
                }

                // JSON 응답에서 downloadUrl 추출
                const data = await response.json();
                const downloadUrl = data.downloadUrl;

                // 브라우저를 해당 URL로 리다이렉트하여 다운로드 실행
                window.location.href = downloadUrl;
            } catch (error) {
                console.error("다운로드 오류:", error);
                alert("다운로드 중 오류가 발생했습니다.");
            }
        });


        // 기존 이벤트 리스너 제거 후 다시 등록 (중복 방지)
        const newDeleteBtn = deleteBtn.cloneNode(true);
        deleteBtn.replaceWith(newDeleteBtn);
        deleteBtn = newDeleteBtn; // 새로운 버튼으로 교체

        deleteBtn.addEventListener("click", async function() {
            console.log("삭제 버튼 클릭")

            if (!selectedCard) {
                alert("삭제할 박스를 선택하세요!");
                return;
            }

            if (!confirm("정말 삭제하시겠습니까?")) {
                console.log("취소 버튼 클릭")
                return;
            }

            console.log("확인 버튼 클릭")
            await deleteBox()

        });


    });

    async function deleteBox(){
        try {
            deleteBtn.disabled = true; // 중복 클릭 방지

            const token = sessionStorage.getItem("accessToken");
            const bid = selectedCard.getAttribute("data-bid");

            console.log("삭제 요청 보냄, bid:", bid);

            const response = await fetch(`/box`, {
                method: "DELETE",
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify([bid]) // bid를 JSON 배열로 전송
            });

            const responseData = await response.json();
            if (response.ok) {
                alert("✅ 박스 삭제 성공");

                // UI에서 삭제된 박스 제거
                selectedCard.parentElement.remove();
                actionButtonsContainer.style.display = "none"; // 버튼 숨기기
                selectedCard = null;

                window.location.href="/main"

            } else {
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
                        window.location.href="/login"
                    }
                }
            }

        } catch (error) {
            console.error("삭제 오류:", error);
            alert("삭제 중 오류가 발생했습니다.");
        } finally {
            deleteBtn.disabled = false; // 버튼 다시 활성화
        }

    }


}