package com.test.jwt.config;

import com.test.jwt.auth.JWTFilter;
import com.test.jwt.auth.JWTUtil;
import com.test.jwt.auth.LoginFilter;
import com.test.jwt.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    //주입(필터 등록때 사용)
    private final AuthenticationConfiguration configuration;
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    //OAuth2가 아니므로 password encoder 필요
    @Bean
    BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //CORS 설정
        http.cors(auth -> auth.configurationSource(corsConfigurationSource()));

        //CSRF 비활성
        http.csrf(auth -> auth.disable());
        // 아래 세가지는 거의 고정값
        //폼 로그인 -> 비활성
        http.formLogin(auth -> auth.disable());
        http.logout(auth -> auth.disable());
        //기본 인증 비활성
        http.httpBasic(auth -> auth.disable());
        //세션 설정: 기존의 세션 인증 방식을 비활성화
        // 대신 JWT 사용
        http.sessionManagement(auth -> auth
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        //허가 URL
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login/**", "/join/**", "/joinok/**", "/logout/**", "/reissue").permitAll()
                .requestMatchers("/member/**").hasAnyRole("MEMBER", "ADMIN")
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

        //필터 작성 순서는 의미 없고 메서드에서 지정하는 순서가 중요..
        //JWTFilter 등록하기 -> LoginFilter보다 JWTFilter가 더 먼저 실행되어야 한다.
        http.addFilterBefore(new JWTFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        //LoginFilter 등록하기
        // - /login 요청 -> 이 필터가 가로채서 동작한다.
        // - UsernamePasswordAuthenticationFilter(시큐리티 기본 인증 필터) -> LoginFilter(사용자 정의)로 교체
        // - 생성자 수정(refreshtoken, 리프레쉬토큰 만료시간)해서 여기에도 추가 필요
        http.addFilterAt(new LoginFilter(manager(configuration), jwtUtil, refreshTokenRepository, jwtUtil.getRefreshExpiredMs()), UsernamePasswordAuthenticationFilter.class);

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

    //CORS 설정
    @Bean
    CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.addAllowedOrigin("http://localhost:8081"); //클라이언트 주소를 허용

        config.addAllowedMethod("*");

        config.addAllowedHeader("*");

        config.setAllowCredentials(true);

        config.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
