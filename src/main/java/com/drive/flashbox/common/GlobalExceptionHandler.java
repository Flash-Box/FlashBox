package com.drive.flashbox.common;

import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // 로그인 실패 (아이디 or 비밀번호 오류)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<CustomResponse<Object>> handleBadCredentialsException(BadCredentialsException e) {
        CustomResponse<Object> response = new CustomResponse<>(
                HttpStatus.UNAUTHORIZED.value(),
                false,
                "로그인 실패: " + e.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // 예상치 못한 상황
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<CustomResponse<Object>> handleIllegalStateException(IllegalStateException e) {
        CustomResponse<Object> response = new CustomResponse<>(
                HttpStatus.BAD_REQUEST.value(),
                false,
                "잘못된 요청: " + e.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<CustomResponse<Object>> handleJwtException(JwtException e) {
        CustomResponse<Object> response = new CustomResponse<>(
                HttpStatus.UNAUTHORIZED.value(),
                false,
                "jwt 토큰 인증 실패: " + e.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // 일반적인 모든 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomResponse<Object>> handleAllExceptions(Exception e) {
        e.printStackTrace(); // 콘솔에 예외 출력

        log.error("예외 발생 클래스: {}", e.getClass().getName());  // 전체 클래스 이름 출력

        if(e instanceof JwtException){
            log.info("==========JwtException===========");
            CustomResponse<Object> response = new CustomResponse<>(
                    HttpStatus.UNAUTHORIZED.value(),
                    false,
                    "jwt 토큰 인증 실패: " + e.getMessage(),
                    null
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }



        CustomResponse<Object> response = new CustomResponse<>(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                false,
                "예기치 못한 오류가 발생했습니다: " + e.getMessage(),
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
