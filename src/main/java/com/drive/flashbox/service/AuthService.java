package com.drive.flashbox.service;

import com.drive.flashbox.dto.TokenDto;
import com.drive.flashbox.dto.UserDto;
import com.drive.flashbox.dto.request.SignupRequestDTO;
import com.drive.flashbox.dto.response.LoginResponse;
import com.drive.flashbox.dto.response.RefreshTokenResponse;
import com.drive.flashbox.dto.response.SignupResponseDTO;
import com.drive.flashbox.entity.Token;
import com.drive.flashbox.entity.User;
import com.drive.flashbox.repository.TokenRepository;
import com.drive.flashbox.repository.UserRepository;
import com.drive.flashbox.security.FBUserDetails;
import com.drive.flashbox.security.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    @Lazy
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public SignupResponseDTO registerUser(SignupRequestDTO signupRequestDTO) {

        Optional<UserDto> found = searchUser(signupRequestDTO.getEmail());
        if(found.isPresent()) {
            throw new IllegalStateException("동일한 email의 유저가 이미 존재합니다.");
        }


        User user = signupRequestDTO.toEntity(passwordEncoder.encode(signupRequestDTO.getPassword()));
        return SignupResponseDTO.of(userRepository.save(user));
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
        tokenRepository.save(new Token(fbUserDetails.getUid(), jwtToken.getRefreshToken()));



        LoginResponse loginResponse = LoginResponse.builder()
                .uid(fbUserDetails.getUid())
                .name(fbUserDetails.getName())
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .build();

        return loginResponse;
    }

    @Transactional
    public RefreshTokenResponse refreshToken(String token){
        Long id = jwtTokenProvider.validateAndParseIdFromToken(token);

        // redis에 저장된 refresh token 값과 일치하는지 확인
        Token redisToken = tokenRepository.findById(id).orElseThrow(() -> new NoSuchElementException("refresh token값을 찾을 수 없습니다"));

        System.out.println("redis 에 저장된 : "+redisToken.getRefreshToken());
        if(!redisToken.getRefreshToken().equals(token)){
            throw new IllegalStateException("유효하지 않은 refresh token 값 입니다.");
        }

        if(id == null){
            throw new JwtException("jwt 토큰 예외");
        }

        User user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("해당 id의 유저를 찾을 수 없습니다."));

        TokenDto tokenDto = jwtTokenProvider.refreshTokens(id, user.getName());

        // redis에 refreshToken 객체 저장
        tokenRepository.save(new Token(id, tokenDto.getRefreshToken()));


        RefreshTokenResponse tokenResponse = RefreshTokenResponse.builder()
                        .uid(id)
                        .name(user.getName())
                        .accessToken(tokenDto.getAccessToken())
                        .refreshToken(tokenDto.getRefreshToken())
                        .build();

        return tokenResponse;

    }
}
