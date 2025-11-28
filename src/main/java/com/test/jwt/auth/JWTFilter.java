package com.test.jwt.auth;

import com.test.jwt.dto.CustomUserDetails;
import com.test.jwt.entity.Member;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// - LoginFilter -> 매표소 -> 티켓 발급 = Authentication(인증)
// - JWTFilter -> 게이트 -> 티켓 확인 = Authorization(허가)

// JWTFilter
// - 현재 요청(접속)한 사용자가 유효한 JWT 토큰을 가지고 있는지 검사
// - 이 사용자를 인증된 사용자로 인식하게 만든다.
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {

    //주입
    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 토큰 유무 체크 & "Bearer" 접두어 유무 체크
        String authorization = request.getHeader("Authorization");
        if(authorization == null || !authorization.startsWith("Bearer ")) {
            System.out.println("JWTFilter >>>>> JWT Token invalid");

            // 인증되지 않은 사용자 -> 익명
            filterChain.doFilter(request, response);
            return;
        }
        //정상적 토큰
        String token = authorization.split(" ")[1]; //"Bearer " 제거
        // 토큰에서 username과 role을 추출 -> Spring Security 인증 객체 생성
        String username = null;
        String role = null;
        try {
            //정보 가져오기 + 토큰 위변조 검사 완료
            username = jwtUtil.getUsername(token);
            role = jwtUtil.getRole(token);



        } catch (Exception e) {
            System.out.println("JWTFilter >>>>> Unauthorized");
            //response.setStatus(401); //401 Unauthorized
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
            return;
        }
        
        // 토큰 정상 + username, role 확보 => Security 처리
        // a. Member 엔티티
        // b. CustomUserDetails(+Member) 인증 객체
        // c. 시큐리티에 적용

        // a. Member 엔티티
        Member member = Member.builder()
                .username(username)
                .role(role)
                //.password("필요없음... 무의미")
                .build();
        // b. CustomUserDetails(+Member) 인증 객체
        CustomUserDetails customUserDetails = new CustomUserDetails(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        // c. 시큐리티에 적용
        // SecurityContext = 시큐리티 작업 공간
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
