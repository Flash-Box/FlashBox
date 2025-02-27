async function refreshToken() {

    try {
        console.log("auth.js refreshtoken() 함수 호출")

        const response = await fetch("/token/refresh", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },

        })

        if (!response.ok) {
            // 상태 코드 확인
            console.log(`HTTP Error: ${response.status}`);
        }
        const responseData = response.json()

        if (responseData.success) {
            alert("✅박스 생성 성공!");
        } else {
            alert("❌박스 생성 실패");
        }
    }catch(error){
        console.error("Error:", error)
    }


}