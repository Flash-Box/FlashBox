package com.drive.flashbox.service;

import com.drive.flashbox.dto.TokenDto;
import com.drive.flashbox.dto.UserDto;
import com.drive.flashbox.dto.request.SignupRequest;
import com.drive.flashbox.dto.response.LoginResponse;
import com.drive.flashbox.dto.response.SignupResponse;
import com.drive.flashbox.entity.User;
import com.drive.flashbox.repository.UserRepository;
import com.drive.flashbox.security.FBUserDetails;
import com.drive.flashbox.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;

    private final TokenService tokenService;
    @Lazy
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public SignupResponse registerUser(SignupRequest signupRequest) {

        Optional<UserDto> found = searchUser(signupRequest.getEmail());
        if(found.isPresent()) {
            throw new IllegalStateException("동일한 email의 유저가 이미 존재합니다.");
        }


        User user = signupRequest.toEntity(passwordEncoder.encode(signupRequest.getPassword()));
        return SignupResponse.of(userRepository.save(user));
    }

    public Optional<UserDto> searchUser(String email) {
        return userRepository.findByEmail(email)
                .map(UserDto::from);
    }

    @Transactional
    public LoginResponse login(Authentication authentication) {

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        TokenDto jwtToken = jwtTokenProvider.generateToken(authentication);
        FBUserDetails fbUserDetails = (FBUserDetails) authentication.getPrincipal();


        // redis에 refreshToken 객체 저장
        tokenService.saveToken(fbUserDetails.getUid(), jwtToken.getRefreshToken());


        LoginResponse loginResponse = LoginResponse.builder()
                .uid(fbUserDetails.getUid())
                .name(fbUserDetails.getName())
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .build();

        return loginResponse;
    }


    public void logout(Long uid) {

        // redis에 저장된 refresh token 삭제
        tokenService.deleteToken(uid);
    }
}