package com.test.jwt.auth;

import com.test.jwt.dto.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/* 
    1. 사용자 /login -> ID/PW 전송
    2. LoginFilter 개입
    3. AuthenticationManager에게 인증을 위임
    4. 인증 성공 -> JWTUtil 사용 -> 토큰 발급
    5. 인증 실패 -> 예외 발생
*/
public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authManager; //인증 담당
    private final JWTUtil jwtUtil; //토큰 발행 담당

    public LoginFilter(AuthenticationManager authManager, JWTUtil jwtUtil) {
        this.authManager = authManager;
        this.jwtUtil = jwtUtil;
    }

    //사용자 로그인 (/login)을 시도하면 -> 이 메서드가 호출됨
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        //사용자가 입력한 ID/PW
        String username = obtainUsername(request);
        String password = obtainPassword(request);
        
        System.out.println("LoginFilter username >>>>> " + username);
        System.out.println("LoginFilter password >>>>> " + password);
        
        // 위의 정보를 AuthenticationManager에게 전달 -> DTO(UsernamePasswordAuthenticationToken)로 포장해서 전달 필요
        UsernamePasswordAuthenticationToken authtoken = new UsernamePasswordAuthenticationToken(username, password);

        //AuthenticationManager에게 인증해달라고 요청
        return authManager.authenticate(authtoken);
    }

    // attemptAuthentication()의 결과 중 인증을 성공했을 때(ID/PW가 올바를 때) 이 메서드가 호출된다.
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        //Authentication -> 인증된 사용자 객체
        System.out.println("LoginFilter >>>>> Login Success");
        
        //JWT 발급
        CustomUserDetails customUserDetails = (CustomUserDetails)authResult.getPrincipal();
        String username = customUserDetails.getUsername(); //아이디 꺼내오기

        Collection<? extends GrantedAuthority> authorities = authResult.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();    //ROLE_MEMBER 객체
        String role = auth.getAuthority();          //ROLE_MEMBER 문자열
        //JWT 토큰 생성 -> 액세스토큰
        String accessToken = jwtUtil.createAccessToken(username, role);
        
        // JWT 토큰을 클라이언트에게 전달
        // 응답 헤더에 토큰을 담아 반환
        // Authorization: 키
        // "Bearer " 접두어 + 토큰 : 값
        response.setHeader("Authorization", "Bearer " + accessToken);
    }

    // 로그인 실패시 예외 처리 위한 메서드
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        System.out.println("LoginFilter >>>>> Login Failed");
        response.setStatus(401); //401 Unauthorized
    }
}
