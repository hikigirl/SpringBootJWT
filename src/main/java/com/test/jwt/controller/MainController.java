package com.test.jwt.controller;

import com.test.jwt.auth.JWTUtil;
import com.test.jwt.dto.MemberDTO;
import com.test.jwt.repository.RefreshTokenRepository;
import com.test.jwt.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class MainController {

    private final MemberService service;
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository repository;

    @GetMapping ("/")
    public String index(){
        return "MainController >>>>> /index";
    }
    @GetMapping ("/member")
    public String member(){
        return "MainController >>>>> /member";
    }
    @GetMapping(value="/member/info")
    public ResponseEntity<?> getMemberInfo() {
        //Rest Server 응답 데이터
        //1. 문자열 or JSON
        //2. 문자열 or JSON + 상태 코드(200, 500, 등등)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("인증되지 않은 사용자입니다.");
        }

        String username = authentication.getName();

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("UNKNOWN");

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", username);
        userInfo.put("role", role);

        // userInfo로 json 생성
        // 상태코드는 200
        return ResponseEntity.ok(userInfo);
    }
    @GetMapping ("/admin")
    public String admin(){
        return "MainController >>>>> /admin";
    }
    @PostMapping("/joinok")
    public String joinok(MemberDTO dto) {
        System.out.println("MainController >>>>> " + dto);
        service.join(dto);
        return "MainController >>>>> /joinok";
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(value = "refresh_cookie", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("Refresh token is missing");
        }
        try {
            //로그아웃 + 리프레시토큰 -> username -> DB삭제, 쿠키만료
            String username = jwtUtil.getUsername(refreshToken);
            repository.deleteByUsername(username);

            // 쿠키 만료시키기(만료시간이 0인 쿠키를 전송해서 쿠키를 삭제시키기)
            Cookie cookie = new Cookie("refresh_cookie", null);
            cookie.setMaxAge(0); //쿠키 만료
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok("Logout successful");
    }
}
