async function refreshToken() {

    try {

        const response = await fetch("/token/refresh", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },

        })

        const responseData = await response.json()
        if (responseData.success) {
            alert("✅토큰 재발급 성공!");
            // Access Token -> session storage에 저장
            sessionStorage.setItem("accessToken", responseData.data.accessToken);
            return responseData

        } else {
            alert("❌토큰 재발급 실패");
            console.log("여기???")
            throw new Error("토큰 재발급 실패")
        }


    }catch(error){
        console.error("Error:", error)
        throw error;  // 실패 시 에러를 던져서 호출한 곳에서 처리
    }


}