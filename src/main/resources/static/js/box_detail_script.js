document.addEventListener("DOMContentLoaded", function () {
    const galleryItems = document.querySelectorAll(".gallery-item");
    const selectAllCheckbox = document.getElementById("select-all");
    const downloadBtn = document.getElementById("download-btn");
    const deleteBtn = document.getElementById("delete-btn");
    const memberList = document.querySelector(".member-list ul"); // ✅ 유저 리스트를 표시할 요소

    let selectedImages = new Set();

    // 개별 이미지 클릭 이벤트
    galleryItems.forEach(item => {
        item.addEventListener("click", function () {
            const checkbox = this.querySelector(".image-checkbox");
            const imageId = this.getAttribute("data-id");

            if (this.classList.contains("selected")) {
                this.classList.remove("selected");
                checkbox.checked = false;
                selectedImages.delete(imageId);
            } else {
                this.classList.add("selected");
                checkbox.checked = true;
                selectedImages.add(imageId);
            }

            updateButtons();
        });
    });

    // 전체 선택 기능
    selectAllCheckbox.addEventListener("change", function () {
        galleryItems.forEach(item => {
            const checkbox = item.querySelector(".image-checkbox");
            const imageId = item.getAttribute("data-id");

            if (selectAllCheckbox.checked) {
                item.classList.add("selected");
                checkbox.checked = true;
                selectedImages.add(imageId);
            } else {
                item.classList.remove("selected");
                checkbox.checked = false;
                selectedImages.delete(imageId);
            }
        });

        updateButtons();
    });

    // 버튼 상태 업데이트 함수
    function updateButtons() {
        if (selectedImages.size > 0) {
            downloadBtn.disabled = false;
            deleteBtn.disabled = false;
        } else {
            downloadBtn.disabled = true;
            deleteBtn.disabled = true;
        }
    }

    // 다운로드 버튼 클릭 이벤트
    downloadBtn.addEventListener("click", function () {
        if (selectedImages.size === 0) return;
        alert(`다운로드할 이미지 ID: ${Array.from(selectedImages).join(", ")}`);
    });

    // 삭제 버튼 클릭 이벤트
    deleteBtn.addEventListener("click", function () {
        if (selectedImages.size === 0) return;
        if (confirm("선택한 이미지를 삭제하시겠습니까?")) {
            alert(`삭제할 이미지 ID: ${Array.from(selectedImages).join(", ")}`);
        }
    });

    // ✅ **박스 참여 유저 리스트 가져오기**
    function fetchBoxUsers() {
        const boxId = window.location.pathname.split("/").pop(); // URL에서 boxId 추출

        fetch(`/box/${boxId}/members`)
            .then(response => response.json())
            .then(users => {
                memberList.innerHTML = ""; // 기존 리스트 초기화

                users.forEach(user => {
                    const li = document.createElement("li");
                    li.textContent = user.name;
                    memberList.appendChild(li);
                });
            })
            .catch(error => console.error("유저 리스트 불러오기 실패:", error));
    }

    // ✅ 페이지 로드 시 유저 리스트 가져오기
    fetchBoxUsers();
});
