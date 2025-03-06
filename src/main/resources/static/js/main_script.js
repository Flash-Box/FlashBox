document.addEventListener("DOMContentLoaded", async function () {
    const token = sessionStorage.getItem("accessToken");
    if (!token) {
        alert("로그인이 필요합니다.");
        window.location.href = "/login";
        return;
    }

    try {
        const nickname = sessionStorage.getItem("nickname");
        document.querySelector(".nickname").innerHTML = nickname || "OOO";

        const boxResponse = await fetch("/api/boxes", {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Accept": "application/json"
            }
        });

        if (!boxResponse.ok) throw new Error("Failed to fetch boxes");

        const boxes = await boxResponse.json();
        renderBoxes(boxes);
    } catch (error) {
        console.error("Error:", error);
    }
});

async function renderBoxes(boxes) {
    const container = document.getElementById("box-container");
    container.innerHTML = "";

    boxes.data.forEach(box => {
        const boxHTML = `
            <div class="selectable-box" data-bid="${box.bid}">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">${box.name}</h5>
                        <p>📅 모임 날짜: ${box.eventStartDate}</p>
                        <p>⏳ 최종 업로드: ${box.modifiedDate}</p>
                        <p style="color: red;">🔥 폭파 날짜: ${box.boomDate}</p>
                        <button class="btn btn-secondary btn-select">썸네일</button>
                    </div>
                </div>
            </div>
        `;
        container.innerHTML += boxHTML;
    });

    addEventListeners();
}


// 이벤트 리스너 추가 함수
function addEventListeners() {
    let selectedCard = null;
    let clickCount = 0;
    const actionButtonsContainer = document.getElementById("action-buttons-container");

    document.querySelectorAll(".selectable-box").forEach(card => {
        const bid = card.getAttribute("data-bid");
        const deselectBtn = document.createElement("button"); // 선택 해제 버튼 생성
        deselectBtn.textContent = "선택 해제";
        deselectBtn.classList.add("btn", "btn-danger", "deselect-btn");
        deselectBtn.style.display = "none"; // 처음에는 숨김
        card.appendChild(deselectBtn); // 카드에 버튼 추가

        const selectButton = card.querySelector(".btn-select");

        // 박스 클릭 이벤트
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
            if (selectButton) {
                selectButton.classList.add("btn-warning");
                selectButton.textContent = "상세 보기"; // 선택 버튼의 텍스트 변경
            }

            selectedCard = this;
            clickCount = 0;
            deselectBtn.style.display = "block"; // 선택 해제 버튼 보이기
            actionButtonsContainer.style.display = "flex"; // 다운로드/삭제 버튼 보이기
            console.log("카드 선택됨:", bid);
        });

        // 선택 해제 버튼 클릭 시 선택 해제
        deselectBtn.addEventListener("click", function (event) {
            event.stopPropagation();
            card.classList.remove("selected-box");
            if (selectButton) {
                selectButton.classList.remove("btn-warning");
                selectButton.textContent = "선택"; // 다시 "선택"으로 변경
            }

            selectedCard = null;
            clickCount = 0;
            deselectBtn.style.display = "none"; // 선택 해제 버튼 숨김
            actionButtonsContainer.style.display = "none"; // 다운로드/삭제 버튼 숨김
            console.log("선택 해제됨:", bid);
        });

        // "선택" 버튼 클릭 시 상세 페이지 이동
        if (selectButton) {
            selectButton.addEventListener("click", function (event) {
                event.stopPropagation();
                if (selectedCard === card) {
                    window.location.href = `/box/${bid}`;
                } else {
                    this.classList.toggle("btn-warning");
                }
            });
        }
    });
}
