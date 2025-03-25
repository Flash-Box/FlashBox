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

// spring security에서 기본적으로 제공하는 /logout API 호출
async function logout(){
    try {

        const response = await fetch("/logout", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            }
        })

        const responseData = await response.json();
        console.log("서버 응답 데이터:", responseData);

        if (responseData.success) {
            sessionStorage.clear(); // 저장된 값들 전부 삭제
            alert("✅로그아웃 성공!");
            window.location.href = '/login';

        }else {
            alert("❌로그아웃 실패");
        }

    }catch (e) {
        alert("✅로그아웃 성공!");
        console.error("Error:", e)
    }

}

// 우리 서비스 로그아웃 API 호출 (위의 것과 구분을 위해 앞에 /api 추가)
async function FBlogout(){
    try {
        const JWT_ERROR_MSG = "jwt 토큰 인증 실패"
        const token = sessionStorage.getItem("accessToken"); // 저장된 토큰 가져오기

        const response = await fetch("/api/logout", {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        })

        const responseData = await response.json();
        console.log("서버 응답 데이터:", responseData);

        if (responseData.success) {
            sessionStorage.clear(); // 저장된 값들 전부 삭제
            await logout();
        }else {
            alert("❌로그아웃 실패");
            if (responseData.message === JWT_ERROR_MSG) {
                alert("😭JWT 토큰 만료");
                try {
                    const newToken = await refreshToken();
                    if (newToken) {
                        await FBlogout(); // 새로운 토큰으로 재시도
                    }
                } catch (error) {
                    console.error("토큰 갱신 실패:", error);
                    alert("🔒재로그인이 필요합니다.");
                    window.location.href="/login"
                }
            }

        }
    }catch (e) {
        console.error("Error:", e)
    }



}