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

        const responseData = await response.json()
        if (responseData.success) {
            alert("✅토큰 재발급 성공!");
            // Access Token -> session storage에 저장
            sessionStorage.setItem("accessToken", responseData.data.accessToken);
            return responseData

        } else {
            alert("❌토큰 재발급 실패");
        }


    }catch(error){
        console.error("Error:", error)
    }


}