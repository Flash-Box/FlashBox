document.querySelectorAll('.btn-select').forEach(button => {
    button.addEventListener('click', function() {
        this.classList.toggle('btn-warning');
    });
});

document.addEventListener("DOMContentLoaded", function () {
    let selectedCard = null; // 현재 선택된 카드 저장
    let clickCount = 0; // 클릭 횟수 저장

    const actionButtonsContainer = document.getElementById("action-buttons-container"); // 다운로드/삭제 버튼 컨테이너

    document.querySelectorAll(".selectable-box").forEach(card => {
        const bid = card.getAttribute("data-bid");
        const deselectBtn = card.querySelector(".deselect-btn"); // 선택 해제 버튼 가져오기

        card.addEventListener("click", function () {
            // 이미 선택된 카드라면 클릭 횟수 증가 → 두 번 클릭하면 이동
            if (selectedCard === this) {
                clickCount++;
                if (clickCount === 2) {
                    window.location.href = `/box/${bid}`;
                }
                return;
            }

            // 이미 다른 카드가 선택된 경우 선택 불가
            if (selectedCard !== null) {
                console.log("이미 선택된 박스가 있음. 다른 박스 선택 불가");
                return;
            }

            // 새로운 카드 선택
            card.classList.add("selected-box");
            selectedCard = this; // 현재 선택된 카드 저장
            clickCount = 0; // 클릭 횟수 초기화
            deselectBtn.style.display = "block"; // 선택 해제 버튼 표시
            actionButtonsContainer.style.display = "block"; // 다운로드/삭제 버튼 보이기
            console.log("카드 선택됨:", bid);
        });

        // 선택 해제 버튼 클릭 시 선택 해제
        deselectBtn.addEventListener("click", function (event) {
            event.stopPropagation(); // 카드 클릭 이벤트 중복 실행 방지
            card.classList.remove("selected-box");
            selectedCard = null; // 선택된 카드 초기화
            clickCount = 0; // 클릭 횟수 초기화
            deselectBtn.style.display = "none"; // 선택 해제 버튼 숨김
            actionButtonsContainer.style.display = "none"; // 다운로드/삭제 버튼 숨김
            console.log("선택 해제됨:", bid);
        });
    });
});




