package com.drive.flashbox.controller;

import com.drive.flashbox.common.CustomResponse;
import com.drive.flashbox.dto.request.LoginRequest;
import com.drive.flashbox.dto.request.SignupRequest;
import com.drive.flashbox.dto.response.LoginResponse;
import com.drive.flashbox.dto.response.SignupResponse;
import com.drive.flashbox.security.FBUserDetails;
import com.drive.flashbox.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@Controller
public class AuthController {
    private final AuthService authService;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    // SCRUM-43-signup-page -------- signup.html 웹에 연결 
    @GetMapping("/signup")
    public String showSignupPage() {
        return "signup"; // signup.html 반환
    }
    



    
    // SCRUM-43-signup-page ----------- form 데이터 처리하도록 위 코드 교체
    @PostMapping("/signup")
    public String signup(SignupRequest signupRequest, Model model) {

        try {
            System.out.println(signupRequest.toString());
            SignupResponse data = authService.registerUser(signupRequest);
            return "redirect:/login"; // 성공 시 리디렉션
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "signup";
        }
    }    
        

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/main")
    public String mainPage() {
        return "main";
    }



    @ResponseBody
    @PostMapping("/login")
    public ResponseEntity<CustomResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response, Model model) {

        try {

            // 1. username + password 를 기반으로 Authentication 객체 생성
            // 이때 authentication 은 인증 여부를 확인하는 authenticated 값이 false
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

            // 2. 실제 검증. authenticate() 메서드를 통해 요청된 Member 에 대한 검증 진행
            // authenticate 메서드가 실행될 때 UserDetailsService 에서 만든 loadUserByUsername 메서드 실행
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);


            // 인증된 사용자 정보 얻기
            LoginResponse data = authService.login(authentication);


            ResponseCookie cookie = ResponseCookie.from("refreshToken", data.getRefreshToken())
                    .httpOnly(true) // JavaScript에서 접근 불가능
                    .secure(true) // HTTPS에서만 전송
                    .sameSite("Strict") // CSRF 방어
                    .path("/") // 모든 경로에서 사용 가능
                    .maxAge(1000 * 7 * 24 * 60 * 60) // 7일 동안 유효
                    .build();

            response.setHeader("Set-Cookie", cookie.toString());


            CustomResponse<LoginResponse> loginResponse = new CustomResponse<>(
                    HttpStatus.OK.value(),
                    true,
                    "로그인 성공",
                    data
            );
            return ResponseEntity.ok(loginResponse);

        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new CustomResponse<>(
                            HttpStatus.UNAUTHORIZED.value(),
                            false,
                            "로그인 실패: " + e.getMessage(),
                            null
                    ));
        }


    }

    @ResponseBody
    @PostMapping("/api/logout")
    public ResponseEntity<CustomResponse<?>> logout(@AuthenticationPrincipal FBUserDetails fbUserDetails) {

        authService.logout(fbUserDetails.getUid());

        CustomResponse<Object> loginResponse = new CustomResponse<>(
                HttpStatus.OK.value(),
                true,
                "토큰 삭제 성공",
                null
        );

        return ResponseEntity.ok(loginResponse);
    }



}