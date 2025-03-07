package com.drive.flashbox.controller;

import com.drive.flashbox.common.CustomResponse;
import com.drive.flashbox.dto.response.RefreshTokenResponse;
import com.drive.flashbox.service.TokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RequiredArgsConstructor
@RestController
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/token/refresh")
    public ResponseEntity<CustomResponse<RefreshTokenResponse>> refreshToken(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {
//        log.info("refreshToken: "+ refreshToken);

        RefreshTokenResponse data = tokenService.refreshToken(refreshToken);
        CustomResponse<RefreshTokenResponse> refreshTokenResponse = new CustomResponse<>(
                HttpStatus.OK.value(),
                true,
                "토큰 재발급 성공",
                data
        );

        // 새로운 refresh token cookie 에 저장
        ResponseCookie cookie = ResponseCookie.from("refreshToken", data.getRefreshToken())
                .httpOnly(true) // JavaScript에서 접근 불가능
                .secure(true) // HTTPS에서만 전송
                .sameSite("Strict") // CSRF 방어
                .path("/") // 모든 경로에서 사용 가능
                .maxAge(1000 * 7 * 24 * 60 * 60) // 7일 동안 유효
                .build();

        response.setHeader("Set-Cookie", cookie.toString());

        // 새로운 refresh token redis 에 저장
        tokenService.saveToken(data.getUid(),data.getRefreshToken());

        return ResponseEntity.ok(refreshTokenResponse);
    }
}
