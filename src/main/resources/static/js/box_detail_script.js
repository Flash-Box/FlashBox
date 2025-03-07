document.addEventListener("DOMContentLoaded", function () {
    let galleryItems = document.querySelectorAll(".gallery-item");
    const selectAllBtn = document.getElementById("select-all-btn");
    const downloadBtn = document.getElementById("download-btn");
    const uploadBtn = document.getElementById("upload-btn");
    const deleteBtn = document.getElementById("delete-btn");

    const bid = uploadBtn ? uploadBtn.getAttribute("data-bid") : null;
    const token = sessionStorage.getItem("accessToken");

    let selectedImages = new Set();

    // ✅ 인증된 fetch 요청 (액세스 토큰만 사용)
    async function authenticatedFetch(url, options = {}) {
        if (!token) {
            alert("세션이 만료되었습니다. 다시 로그인해주세요.");
            window.location.href = "/login";
            return null;
        }

        const response = await fetch(url, {
            ...options,
            headers: {
                ...options.headers,
                "Authorization": `Bearer ${token}`
            }
        });

        if (response.status === 401) {
            alert("세션이 만료되었습니다. 다시 로그인해주세요.");
            window.location.href = "/login";
            return null;
        }

        return response;
    }

    // ✅ 개별 이미지 클릭 이벤트 (이미지 선택)
    function bindImageClickEvents() {
        galleryItems = document.querySelectorAll(".gallery-item");
        galleryItems.forEach(item => {
            item.addEventListener("click", function () {
                const imageId = this.getAttribute("data-id");

                if (this.classList.contains("selected")) {
                    this.classList.remove("selected");
                    selectedImages.delete(imageId);
                } else {
                    this.classList.add("selected");
                    selectedImages.add(imageId);
                }
                updateButtons();
            });
        });
    }
    bindImageClickEvents(); // 초기에 한 번 실행

    // ✅ 전체 선택 버튼 클릭 이벤트
    if (selectAllBtn) {
        selectAllBtn.addEventListener("click", function () {
            galleryItems = document.querySelectorAll(".gallery-item"); // ✅ 업로드 후 갤러리 최신화
            if (selectedImages.size === galleryItems.length) {
                galleryItems.forEach(item => item.classList.remove("selected"));
                selectedImages.clear();
            } else {
                galleryItems.forEach(item => {
                    const imageId = item.getAttribute("data-id");
                    item.classList.add("selected");
                    selectedImages.add(imageId);
                });
            }
            updateButtons();
        });
    }

    // ✅ 버튼 상태 업데이트 함수
    function updateButtons() {
        const hasSelection = selectedImages.size > 0;
        if (downloadBtn) downloadBtn.disabled = !hasSelection;
        if (deleteBtn) deleteBtn.disabled = !hasSelection;
    }

    // ✅ 업로드 버튼 클릭 이벤트 (파일 업로드 후 갤러리 갱신)
    if (uploadBtn) {
        uploadBtn.addEventListener("click", async function () {
            if (!bid) return;

            const input = document.createElement("input");
            input.type = "file";
            input.multiple = true;
            input.accept = "image/*";

            input.addEventListener("change", async function () {
                const files = input.files;
                if (files.length === 0) return;

                const formData = new FormData();
                for (let i = 0; i < files.length; i++) {
                    formData.append("files", files[i]);
                }

                const response = await authenticatedFetch(`/box/${bid}/picture`, {
                    method: "POST",
                    body: formData
                });

                if (!response) return;

                const data = await response.json();
                if (data.status === 201) {
                    alert("이미지 업로드가 완료되었습니다.");
                    refreshGallery(); // ✅ 갤러리 갱신 후 전체 선택 정상 작동
                } else {
                    alert(`업로드 실패: ${data.message}`);
                }
            });

            input.click();
        });
    }

    // ✅ 갤러리 새로고침 (업로드 후 호출됨)
    async function refreshGallery() {
        const response = await authenticatedFetch(`/box/${bid}/pictures`, {
            method: "GET"
        });

        if (!response) return;

        const data = await response.json();
        const galleryContainer = document.querySelector(".gallery-grid");

        // 기존 이미지 제거
        galleryContainer.innerHTML = "";

        // 새로운 이미지 추가
        data.forEach(image => {
            const imgElement = document.createElement("div");
            imgElement.classList.add("gallery-item");
            imgElement.setAttribute("data-id", image.pid);
            imgElement.innerHTML = `<img src="${image.imageUrl}" />`;
            galleryContainer.appendChild(imgElement);
        });

        bindImageClickEvents(); // ✅ 새로 추가된 이미지에도 이벤트 바인딩
    }

    // ✅ 다운로드 버튼 클릭 이벤트
    if (downloadBtn) {
        downloadBtn.addEventListener("click", function () {
            if (selectedImages.size === 0) {
                alert("다운로드할 이미지를 선택해주세요.");
                return;
            }
            window.location.href = `/box/${bid}/picture/download?pid=${Array.from(selectedImages).join(",")}`;
        });
    }

    // ✅ 이미지 삭제 버튼 클릭 이벤트
    if (deleteBtn) {
        deleteBtn.addEventListener("click", async function () {
            if (selectedImages.size === 0) {
                alert("삭제할 이미지를 선택해주세요.");
                return;
            }

            if (confirm("선택한 이미지를 삭제하시겠습니까?")) {
                for (let pid of selectedImages) {
                    const response = await authenticatedFetch(`/box/${bid}/picture/${pid}`, {
                        method: "DELETE"
                    });

                    if (response && response.ok) {
                        document.querySelector(`[data-id='${pid}']`).remove();
                        selectedImages.delete(pid);
                        updateButtons();
                    } else {
                        console.error("삭제 오류 발생");
                    }
                }
            }
        });
    }

    // ✅ 갤러리(박스) 삭제 버튼 클릭 이벤트
    if (deleteBtn) {
        deleteBtn.addEventListener("click", function () {
            if (!confirm("정말 삭제하시겠습니까?")) {
                return;
            }

            fetch(`/box`, {
                method: "DELETE",
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                },
                body: JSON.stringify([parseInt(bid)])
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
    }
});
