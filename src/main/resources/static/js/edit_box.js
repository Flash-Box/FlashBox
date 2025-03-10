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

document.addEventListener("DOMContentLoaded", function () {
	
    const form = document.getElementById("updateForm");
    const bid = form.getAttribute("data-bid");
    const token = sessionStorage.getItem("accessToken");

    // 🔹 수정 요청 (PUT)
    form.addEventListener("submit", function (event) {
        event.preventDefault();

        const formData = {
            name: document.getElementById("name").value,
            eventStartDate: document.getElementById("eventStartDate").value,
            eventEndDate: document.getElementById("eventEndDate").value
        };

        fetch(`/box/${bid}`, {
            method: "PUT",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
        	},
            body: JSON.stringify(formData)
        })
        .then(response => {
            if (response.ok) {
		        return response.text().then(msg => {
		            alert(msg);
		            window.location.href = "/main";
		        });
		    } else {
		        return response.text().then(errMsg => {
		            alert("수정 실패: " + errMsg);
		        });
		    }
        })
        .catch(error => console.error("Error:", error));
    });

    // 🔹 삭제 요청 (DELETE)
    document.getElementById("deleteButton").addEventListener("click", function () {
        if (!confirm("정말 삭제하시겠습니까?")) {
            return;
        }

        const bidList = [parseInt(bid)]; 

        fetch(`/box`, {
            method: "DELETE",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
        	},
        	body: JSON.stringify(bidList)
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