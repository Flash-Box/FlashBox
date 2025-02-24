//package com.drive.flashbox.config;
//
//import static org.springframework.security.config.Customizer.withDefaults;
//
//import com.drive.flashbox.service.UserService;
//import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.password.NoOpPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//
//
//@Configuration
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(
//            HttpSecurity http
//    ) throws Exception {
//
//        http
//                .csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations())
//                        .permitAll()
//                        .requestMatchers("/posts","/signup")
//                        .permitAll()
//                        .anyRequest().authenticated()
//                )
////                .formLogin(withDefaults())
//                .logout(logout -> logout.logoutSuccessUrl("/"));
//
//        return http.build();
//    }
//
//    // 인증된 데이터를 가져오는 로직(email, password 비교)
////    @Bean
////    public UserDetailsService userDetailsService(UserService userService) {
////
////        return email -> userService.searchUser(email)
////                .map(BoardDetails::from)
////                .orElseThrow();
////
////    }
//
//    // 현재 db에는 암호화 x -> 이를 그대로 사용하기 위한 encoder 설정
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return NoOpPasswordEncoder.getInstance();
//    }
//
//}
//
