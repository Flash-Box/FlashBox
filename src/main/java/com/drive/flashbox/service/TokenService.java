package com.drive.flashbox.service;

import com.drive.flashbox.dto.TokenDto;
import com.drive.flashbox.dto.response.RefreshTokenResponse;
import com.drive.flashbox.entity.User;
import com.drive.flashbox.repository.UserRepository;
import com.drive.flashbox.security.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final StringRedisTemplate stringRedisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;


    private static final long TOKEN_TTL = 7 * 24 * 60 * 60; // 7일 (초 단위)

    // Refresh Token 저장 (Key-Value)
    public void saveToken(Long userId, String refreshToken) {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("user:" + userId, refreshToken, Duration.ofSeconds(TOKEN_TTL));
    }

    // Refresh Token 조회
    public String getToken(Long userId) {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String token = ops.get("user:" + userId);
        System.out.println("GET TOKEN: " + token);
        return token;
    }

    // Refresh Token 삭제
    public void deleteToken(Long userId) {
        stringRedisTemplate.delete("user:" + userId);
    }

    @Transactional
    public RefreshTokenResponse refreshToken(String token){
        Long id = jwtTokenProvider.validateAndParseIdFromToken(token);

        // redis에 저장된 refresh token 값과 일치하는지 확인
        String redisRefreshToken = getToken(id);

        System.out.println("redis 에 저장된 : "+ redisRefreshToken);
        if(!redisRefreshToken.equals(token)){
            throw new IllegalStateException("유효하지 않은 refresh token 값 입니다.");
        }

        if(id == null){
            throw new JwtException("jwt 토큰 예외");
        }

        User user = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("해당 id의 유저를 찾을 수 없습니다."));

        TokenDto tokenDto = jwtTokenProvider.refreshTokens(id, user.getName());

        // redis에 새로운 refreshToken 객체 저장
        saveToken(id, tokenDto.getRefreshToken());


        RefreshTokenResponse tokenResponse = RefreshTokenResponse.builder()
                .uid(id)
                .name(user.getName())
                .accessToken(tokenDto.getAccessToken())
                .refreshToken(tokenDto.getRefreshToken())
                .build();

        return tokenResponse;

    }
}

