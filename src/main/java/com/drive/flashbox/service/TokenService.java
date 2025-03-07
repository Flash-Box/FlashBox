package com.drive.flashbox.service;

import com.drive.flashbox.entity.RefreshToken;
import com.drive.flashbox.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final StringRedisTemplate stringRedisTemplate;

    private final TokenRepository tokenRepository;

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
        return token;
    }

    public String getTokenFromRepo(Long userId) {
        RefreshToken token = tokenRepository.findById(userId).orElseThrow(() -> new NoSuchElementException());
        return token.getRefreshToken();
    }

    // Refresh Token 삭제
    public void deleteToken(String userId) {
        stringRedisTemplate.delete("user:" + userId);
    }
}

