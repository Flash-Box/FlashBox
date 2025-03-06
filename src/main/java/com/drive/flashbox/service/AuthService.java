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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
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
        TokenDto jwtToken = jwtTokenProvider.generateToken(authentication);
        FBUserDetails fbUserDetails = (FBUserDetails) authentication.getPrincipal();

        // ✅ Redis에 refreshToken을 단순한 String 값으로 저장
        redisTemplate.opsForValue().set("token:" + fbUserDetails.getUid(), jwtToken.getRefreshToken(), Duration.ofDays(7));

        return LoginResponse.builder()
                .uid(fbUserDetails.getUid())
                .name(fbUserDetails.getName())
                .accessToken(jwtToken.getAccessToken())
                .refreshToken(jwtToken.getRefreshToken())
                .build();
    }

    @Transactional
    public RefreshTokenResponse refreshToken(String token) {
        Long id = jwtTokenProvider.validateAndParseIdFromToken(token);

        // ✅ Redis에서 refreshToken을 String 값으로 가져오기
        String storedRefreshToken = (String) redisTemplate.opsForValue().get("token:" + id);

        if (storedRefreshToken == null || !storedRefreshToken.equals(token)) {
            throw new IllegalStateException("유효하지 않은 refresh token 값 입니다.");
        }

        User user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("해당 id의 유저를 찾을 수 없습니다."));
        TokenDto tokenDto = jwtTokenProvider.refreshTokens(id, user.getName());

        // ✅ 새로운 refreshToken 저장
        redisTemplate.opsForValue().set("token:" + id, tokenDto.getRefreshToken(), Duration.ofDays(7));

        return RefreshTokenResponse.builder()
                .uid(id)
                .name(user.getName())
                .accessToken(tokenDto.getAccessToken())
                .refreshToken(tokenDto.getRefreshToken())
                .build();
    }

    @Transactional
    public void deleteRefreshToken(Long uid) {
        // redis에서 해당 uid 유저의 refreshToken 삭제
        tokenRepository.deleteById(uid);

    }



}
