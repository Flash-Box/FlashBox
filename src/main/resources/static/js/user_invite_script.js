document.addEventListener("DOMContentLoaded", function () {
    const inviteBtn = document.getElementById("invite-button");
    const emailInput = document.getElementById("email");
    const errorMessage = document.getElementById("error-message");

    inviteBtn.addEventListener("click", function () {
        const email = emailInput.value;
        const boxId = window.location.pathname.split("/")[2]; // URL에서 boxId 추출

        fetch(`/box/${boxId}/members`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams({ email })
        })
        .then(response => {
            if (!response.ok) {
                errorMessage.style.display = "block";
                return Promise.reject("초대 실패");
            }
            return response.text();
        })
        .then(() => {
            alert("초대가 완료되었습니다.");
            window.location.reload();
        })
        .catch(error => console.error(error));
    });
});
