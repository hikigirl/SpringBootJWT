package com.test.jwt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    //OAuth2가 아니므로 password encoder 필요
    @Bean
    BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //CSRF 비활성
        http.csrf(auth -> auth.disable());
        // 아래 세가지는 거의 고정값
        //폼 로그인 -> 비활성
        http.formLogin(auth -> auth.disable());
        //기본 인증 비활성
        http.httpBasic(auth -> auth.disable());
        //세션 설정: 기존의 세션 인증 방식을 비활성화
        // 대신 JWT 사용
        http.sessionManagement(auth -> auth
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        //허가 URL
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login/**", "/join/**", "/joinok/**").permitAll()
                .requestMatchers("/member").hasAnyRole("MEMBER", "ADMIN")
                .requestMatchers("/admin").hasRole("ADMIN")
                .anyRequest().authenticated()
        );

        //로그아웃
//        http.logout(auth->auth
//                .logoutUrl("/logout")
//                .logoutSuccessUrl("/")
//                .invalidateHttpSession(true)
//                .deleteCookies("JSESSIONID")
//        );

        return http.build();
    }

    // AuthenticationManager - JWT 인증 관리
    // 사용자가 로그인 시도 -> 실제로 ID와 PW가 일치하는지 검증
    // loadUserByUsername 관여
    // 직접 생성 이유 -> 폼 인증을 사용하지 않아서
    @Bean
    AuthenticationManager manager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
