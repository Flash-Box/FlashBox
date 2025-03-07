document.addEventListener("DOMContentLoaded", function () {
    const galleryItems = document.querySelectorAll(".gallery-item");
    const selectAllBtn = document.getElementById("select-all-btn");
    const uploadBtn = document.getElementById("upload-btn");
    const downloadBtn = document.getElementById("download-btn");
    const deleteBtn = document.getElementById("delete-btn");

    let selectedImages = new Set();

    // ✅ Access Token 갱신 함수 (리프레시 토큰 사용)
	async function refreshToken() {
	    try {
	        const response = await fetch("/token/refresh", {
	            method: "POST",
	            credentials: "include", // 리프레시 토큰을 쿠키에서 가져옴
	        });
	
	        if (!response.ok) {
	            console.error("리프레시 토큰이 만료됨");
	            alert("세션이 만료되었습니다. 다시 로그인해주세요.");
	            window.location.href = "/login";
	            return null;
	        }
	
	        const data = await response.json();
	        if (data.success) {
	            console.log("✅ 새 액세스 토큰 발급:", data.data.accessToken);
	            localStorage.setItem("token", data.data.accessToken);
	            return data.data.accessToken;
	        } else {
	            console.error("🚨 리프레시 토큰 만료됨, 다시 로그인 필요");
	            alert("세션이 만료되었습니다. 다시 로그인해주세요.");
	            window.location.href = "/login";
	            return null;
	        }
	    } catch (error) {
	        console.error("🚨 토큰 갱신 오류:", error);
	        alert("세션이 만료되었습니다. 다시 로그인해주세요.");
	        window.location.href = "/login";
	        return null;
	    }
	}


    // ✅ 인증된 fetch 요청 (401 처리)
    async function authenticatedFetch(url, options = {}) {
        let token = localStorage.getItem("token");

        if (!token) {
            token = await refreshToken();
            if (!token) return null;
        }

        const response = await fetch(url, {
            ...options,
            headers: {
                ...options.headers,
                "Authorization": `Bearer ${token}`
            }
        });

        // ✅ 401 Unauthorized 발생 시 자동으로 토큰 갱신 후 재시도
        if (response.status === 401) {
            token = await refreshToken();
            if (!token) return null;

            return fetch(url, {
                ...options,
                headers: {
                    ...options.headers,
                    "Authorization": `Bearer ${token}`
                }
            });
        }

        return response;
    }

    // ✅ 개별 이미지 클릭 이벤트
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

    // ✅ 전체 선택 버튼 클릭 이벤트
    selectAllBtn.addEventListener("click", function () {
        if (selectedImages.size === galleryItems.length) {
            galleryItems.forEach(item => {
                item.classList.remove("selected");
            });
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

    // ✅ 버튼 상태 업데이트 함수
    function updateButtons() {
        const hasSelection = selectedImages.size > 0;
        downloadBtn.disabled = !hasSelection;
        deleteBtn.disabled = !hasSelection;
    }

    // ✅ 업로드 버튼 클릭 이벤트 (파일 업로드 처리 + 토큰 갱신)
    uploadBtn.addEventListener("click", async function () {
        const bid = uploadBtn.getAttribute("data-bid");
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
                location.reload();
            } else {
                alert(`업로드 실패: ${data.message}`);
            }
        });

        input.click();
    });

    // ✅ 다운로드 버튼 클릭 이벤트
    downloadBtn.addEventListener("click", function () {
        if (selectedImages.size === 0) {
            alert("다운로드할 이미지를 선택해주세요.");
            return;
        }
        const bid = downloadBtn.getAttribute("data-bid");
        window.location.href = `/box/${bid}/picture/download?pid=${Array.from(selectedImages).join(",")}`;
    });

    // ✅ 삭제 버튼 클릭 이벤트
    deleteBtn.addEventListener("click", async function () {
        if (selectedImages.size === 0) {
            alert("삭제할 이미지를 선택해주세요.");
            return;
        }
        if (confirm("선택한 이미지를 삭제하시겠습니까?")) {
            const bid = deleteBtn.getAttribute("data-bid");

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
});