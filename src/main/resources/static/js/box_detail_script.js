document.addEventListener("DOMContentLoaded", function () {
    let galleryItems = document.querySelectorAll(".gallery-item");
    const selectAllBtn = document.getElementById("select-all-btn");
    const downloadBtn = document.getElementById("download-btn");
    const uploadBtn = document.getElementById("upload-btn");
    const deleteBtn = document.getElementById("delete-btn");

    const bid = uploadBtn ? uploadBtn.getAttribute("data-bid") : null;
    const token = sessionStorage.getItem("accessToken");

    let selectedImages = new Set();
	let currentImageId = null;
	
    // ✅ 인증된 fetch 요청 (토큰 인증 포함)
    async function authenticatedFetch(url, options = {}) {
		console.log("🔍 보낼 URL:", url);
    	console.log("🔍 보낼 토큰:", token);
				
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

    // ✅ 갤러리 새로고침 (업로드 후 자동 반영)
	async function refreshGallery() {
	    console.log("📸 갤러리 새로고침 시작!");
	
	    try {
	        const response = await authenticatedFetch(`/box/${bid}/pictures`);
	        if (!response) throw new Error("갤러리 데이터를 불러올 수 없음");
	
	        const data = await response.json();
	        console.log("✅ 새 이미지 목록:", data);
	
	        // galleryContainer 변경: id가 아니라 class 사용
	        const galleryContainer = document.querySelector(".gallery-grid");
	        if (!galleryContainer) {
	            console.error("🚨 갤러리 컨테이너를 찾을 수 없습니다! (gallery-grid)");
	            return;
	        }
	
	        console.log("📌 기존 갤러리 초기화...");
	        galleryContainer.innerHTML = ""; // 기존 이미지 삭제
	        console.log("✅ 갤러리 초기화 완료!");
	
	        console.log(`📸 새로 불러온 이미지 개수: ${data.length}`);
	        if (data.length === 0) {
	            console.warn("⚠ 새로 불러온 이미지가 없습니다.");
	            return;
	        }
	
	        console.log("📌 새 이미지 추가 시작...");
	        data.forEach(picture => {
	            console.log("🖼 추가되는 이미지:", picture.imageUrl);
	
	            const imgWrapper = document.createElement("div");
	            imgWrapper.classList.add("gallery-item");
	            imgWrapper.setAttribute("data-id", picture.pid);
	
	            const imgElement = document.createElement("img");
	            imgElement.src = `${picture.imageUrl}?nocache=${Math.random()}`; // 캐싱 방지
	            imgElement.classList.add("gallery-image");
	
	            imgWrapper.appendChild(imgElement);
	            galleryContainer.appendChild(imgWrapper);
	            console.log(`✅ 이미지 추가됨: ${imgElement.src}`);
	        });
	
	        console.log("🎉 갤러리 업데이트 완료!");
	
	    } catch (error) {
	        console.error("❌ 갤러리 갱신 실패:", error);
	    }
	}




    // ✅ 개별 이미지 클릭 이벤트 바인딩
    function bindImageClickEvents() {
        galleryItems = document.querySelectorAll(".gallery-image");
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
            
            // 더블클릭 시 모달 띄우기
            item.addEventListener("dblclick", function () {
                const imageId = this.getAttribute("data-id");
                const imageUrl = this.querySelector("img").src;

                // currentImageId에 클릭한 이미지 ID 저장
                currentImageId = imageId;

                // 모달에 이미지 표시
                const modalImage = document.getElementById("modalImage");
                modalImage.src = imageUrl;

                // 다운로드 버튼 활성화
                const downloadModalBtn = document.getElementById("downloadModalBtn");
                downloadModalBtn.disabled = false;

                // 모달 열기
                const modal = new bootstrap.Modal(document.getElementById("imageModal"));
                modal.show();
            });
        });
    }

    bindImageClickEvents(); // 초기에 한 번 실행

    // ✅ 전체 선택 버튼
    if (selectAllBtn) {
        selectAllBtn.addEventListener("click", function () {
            galleryItems = document.querySelectorAll(".gallery-image"); // 최신화
            if (selectedImages.size === galleryItems.length) {
                galleryItems.forEach(item => item.classList.remove("selected"));
                selectedImages.clear();
            } else {
                galleryItems.forEach(item => {
                    item.classList.add("selected");
                    selectedImages.add(item.getAttribute("data-id"));
                });
            }
            updateButtons();
        });
    }

    // ✅ 버튼 상태 업데이트
    function updateButtons() {
        const hasSelection = selectedImages.size > 0;
        if (downloadBtn) downloadBtn.disabled = !hasSelection;
        if (deleteBtn) deleteBtn.disabled = !hasSelection;
    }

    // ✅ 업로드 버튼 (이미지 업로드 후 즉시 반영)
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
	
	            try {
	                const response = await authenticatedFetch(`/box/${bid}/picture`, {
	                    method: "POST",
	                    body: formData
	                });
	
	                if (!response) throw new Error("서버 응답 없음");
	
	                const data = await response.json();
	                if (data.status === 201) {
	                    alert("이미지 업로드가 완료되었습니다.");
	                    
	                    // ✅ 서버에서 이미지가 완전히 저장된 후 갤러리 새로고침
	                    setTimeout(async () => {
	                        await refreshGallery();
	                    }, 500); // 서버 반영을 기다릴 딜레이 추가
	
	                } else {
	                    alert(`업로드 실패: ${data.message}`);
	                }
	            } catch (error) {
	                alert(`오류 발생: ${error.message}`);
	            }
	        });
	
	        input.click();
	    });
	}


    // ✅ 다운로드 버튼
    if (downloadBtn) {
        downloadBtn.addEventListener("click", function () {
            if (selectedImages.size === 0) {
                alert("다운로드할 이미지를 선택해주세요.");
                return;
            }
            window.location.href = `/box/${bid}/picture/download?pid=${Array.from(selectedImages).join(",")}`;
        });
    }

    // ✅ 삭제 버튼
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

    // ✅ 박스 삭제 버튼
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
    
    const downloadModalBtn = document.getElementById("downloadModalBtn");
    if (downloadModalBtn) {
        downloadModalBtn.addEventListener("click", async function () {
            if (currentImageId) {
			    // currentImageId를 List 형태로 변환하여 서버로 보냄
			    const response = await fetch(`/box/${bid}/picture/download?pid=${currentImageId}`, {
			        method: "GET",
			        headers: {
			            "Authorization": `Bearer ${token}`,
			            "Content-Type": "application/json"
			        }
			    });
			
			    const data = await response.json();
			    console.log(data);
			    const downloadUrl = data.downloadUrl;
			    // 브라우저를 해당 URL로 리다이렉트하여 다운로드 실행
			    window.location.href = downloadUrl;
			}
        });
    }

    const deleteModalBtn = document.getElementById("deleteModalBtn");
    if (deleteModalBtn) {
        deleteModalBtn.addEventListener("click", async function () {
            if (currentImageId) {
                if (confirm("이 이미지를 삭제하시겠습니까?")) {
					
                    const response = await authenticatedFetch(`/box/${bid}/picture/${currentImageId}`, {
                        method: "DELETE"
                    });

                    if (response && response.ok) {
                        document.querySelector(`[data-id='${currentImageId}']`).remove();
                        const modal = bootstrap.Modal.getInstance(document.getElementById("imageModal"));
                        modal.hide();
                    } else {
                        alert("삭제 실패");
                    }
                }
            }
        });
    }

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            const modal = bootstrap.Modal.getInstance(document.getElementById("imageModal"));
            modal.hide();
        }
    });
    
    const modalElement = document.getElementById("imageModal");
    modalElement.addEventListener("click", function (event) {
        if (event.target === modalElement) {
            const modal = bootstrap.Modal.getInstance(modalElement);
            modal.hide();
        }
    });
});
