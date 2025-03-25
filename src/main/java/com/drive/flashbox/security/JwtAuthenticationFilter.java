package com.drive.flashbox.security;

import com.drive.flashbox.common.CustomResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {
    private final JwtTokenProvider jwtTokenProvider;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            // 1. Request Header에서 JWT 토큰 추출
            String token = resolveToken((HttpServletRequest) request);

            System.out.println("token: " + token);

            if (token != null) {
                // 2. validateToken으로 토큰 유효성 검사
                if (jwtTokenProvider.validateAndParseIdFromToken(token) == null) {
                    throw new JwtException("jwt 토큰 예외");
                }
                else {
                    // 토큰이 유효할 경우 토큰에서 Authentication 객체를 가지고 와서 SecurityContext에 저장
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            chain.doFilter(request, response);

        }catch (JwtException e){

            // GlobalExceptionHandler로 가지 않으므로, 여기서 직접 JSON 응답을 작성
            CustomResponse<Object> errorResponse = new CustomResponse<>(
                    HttpStatus.UNAUTHORIZED.value(),
                    false,
                    "jwt 토큰 인증 실패",
                    null
            );

            // JSON 변환을 위해 ObjectMapper 사용
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);

            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 상태 코드
            httpServletResponse.setContentType("application/json");
            httpServletResponse.setCharacterEncoding("UTF-8");


            httpServletResponse.getWriter().write(jsonResponse);

        }
    }

    // Request Header에서 토큰 정보 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
