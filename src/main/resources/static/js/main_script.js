// script.js
document.querySelectorAll('.btn-select').forEach(button => {
    button.addEventListener('click', function() {
        this.classList.toggle('btn-warning');
    });
});

document.addEventListener("DOMContentLoaded", function () {
    let selectedCard = null; // 현재 선택된 카드 저장 변수

    document.querySelectorAll(".selectable-box").forEach(card => {
        card.addEventListener("click", function () {
            // 이미 선택된 카드 클릭 시 선택 해제
            if (selectedCard === this) {
                this.classList.remove("selected-box");
                selectedCard = null; // 선택 해제
                console.log("선택 해제됨:", this);
                return; // 함수 종료
            }

            // 이미 다른 카드가 선택된 경우 새 선택 불가
            if (selectedCard !== null) {
                console.log("이미 선택된 박스가 있음, 다른 박스 선택 불가");
                return;
            }

            // 현재 카드 선택
            this.classList.add("selected-box");
            selectedCard = this; // 선택된 카드 저장
            console.log("카드 선택됨:", this);
        });
    });
});

