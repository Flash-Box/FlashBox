package com.drive.flashbox.security;

import com.drive.flashbox.dto.TokenDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import io.jsonwebtoken.security.SecurityException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtTokenProvider {
    private static final String AUTHORITIES_KEY = "auth";
    private static final String ID_KEY = "id";
    private static final String ISSUER = "FlashBox";
    private static final String BEARER_TYPE = "Bearer";

    @Value("${jwt.access.exp_time}")
    private long ACCESS_TOKEN_EXPIRE_TIME;            // 30분
    @Value("${jwt.access.exp_time}")
    private long REFRESH_TOKEN_EXPIRE_TIME;  // 7일

    private final SecretKey key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        // application.properties 에서 secret 값 가져와서 key에 저장
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public TokenDto generateToken(Authentication authentication) {
        // 권한들 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();

//        UserDetails에서 사용자 ID 추출
        FBUserDetails userDetails = (FBUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUid();  // 사용자 ID

        // Access Token 생성
        String accessToken = Jwts.builder()
                .subject(authentication.getName())       // payload "sub": "name"
                .claim(AUTHORITIES_KEY, authorities)        // payload "auth": "USER"
                .claim(ID_KEY, userId)
                .issuer(ISSUER)
                .expiration(new Date(now + ACCESS_TOKEN_EXPIRE_TIME))        // payload "exp": 151621022 (ex)
                .signWith(key)    // header "alg": "HS512"
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .subject(authentication.getName())       // payload "sub": "name"
                .claim(AUTHORITIES_KEY, authorities)        // payload "auth": "USER"
                .claim(ID_KEY, userId)
                .issuer(ISSUER)
                .expiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(key)
                .compact();

        return TokenDto.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // Jwt 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 메서드
    public Authentication getAuthentication(String accessToken) {
        // 토큰 복호화
        Claims claims = Jwts.parser()
                            .verifyWith(key)
                            .build()// secretKey 설정
                            .parseSignedClaims(accessToken) // JWT 파싱
                            .getPayload();;

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // UserDetails 객체를 만들어서 Authentication 리턴
        UserDetails principal = new FBUserDetails(((Integer) claims.get(ID_KEY)).longValue(), "", "", claims.getSubject());

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public Long validateAndParseIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()// secretKey 설정
                    .parseSignedClaims(token) // JWT 파싱
                    .getPayload();

            System.out.println("토큰 내용: " + claims.toString());

            return ((Integer) claims.get(ID_KEY)).longValue();
        } catch (SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.");
        }
        return null;
    }

    public TokenDto refreshTokens(Long userId, String name){

        long now = (new Date()).getTime();

        // Access Token 생성
        String accessToken = Jwts.builder()
                .subject(name)       // payload "sub": "name"
                .claim(AUTHORITIES_KEY, "ROLE_USER")        // payload "auth": "USER"
                .claim(ID_KEY, userId)
                .issuer(ISSUER)
                .expiration(new Date(now + ACCESS_TOKEN_EXPIRE_TIME))        // payload "exp": 151621022 (ex)
                .signWith(key)    // header "alg": "HS512"
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .subject(name)       // payload "sub": "name"
                .claim(AUTHORITIES_KEY, "ROLE_USER")        // payload "auth": "USER"
                .claim(ID_KEY, userId)
                .issuer(ISSUER)
                .expiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(key)
                .compact();

        return TokenDto.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

}
