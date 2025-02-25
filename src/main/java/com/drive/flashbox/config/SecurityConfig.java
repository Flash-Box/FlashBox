package com.drive.flashbox.config;

import com.drive.flashbox.controller.AuthController;
import com.drive.flashbox.dto.TokenDto;
import com.drive.flashbox.security.FBUserDetails;
import com.drive.flashbox.security.JwtAuthenticationFilter;
import com.drive.flashbox.security.JwtTokenProvider;
import com.drive.flashbox.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

import static org.springframework.security.config.Customizer.withDefaults;


@Slf4j
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    @Lazy
    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/posts","/signup","/api/login","/login","/box").permitAll()
                        .anyRequest().authenticated()
                )
//                .formLogin(login -> login
//                        .loginProcessingUrl("/api/login")
//                        .defaultSuccessUrl("/box", true) // 로그인 성공 후 "/box"로 이동 (강제 이동)

//                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안 함

                .formLogin(AbstractHttpConfigurer::disable) // 기본 로그인 비활성화
//                .formLogin(login -> login
//                        .loginPage("/login") // 커스텀 로그인 페이지가 있을 경우
//                        .loginProcessingUrl("/login") // 로그인 폼 전송 URL 변경
//                        .defaultSuccessUrl("/box", true) // 로그인 성공 시 "/box"로 이동
//
//                        .permitAll()
//                )


                // JWT 인증을 위하여 직접 구현한 필터를 UsernamePasswordAuthenticationFilter 전에 실행
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout.logoutSuccessUrl("/"));

        return http.build();
    }

    // 인증된 데이터를 가져오는 로직(email, password 비교)
    @Bean
    public UserDetailsService userDetailsService(AuthService authService) {

        log.info("로그인 요청: " + authService);
        return email -> authService.searchUser(email)
                .map(FBUserDetails::from)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt Encoder 사용
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}

