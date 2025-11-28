# Spring Security - JWT

### 초기 작업
1. build.gradle: JWT 의존성
2. application.yaml: JPA 세팅

### 파일 생성하기
- project root : script.sql
- src/main/java - com.test.jwt
  - auth
    - `JWTUtil.java`: JWT관련 기능 구현 객체
    - `LoginFilter.java`
    - `JWTFilter.java`
  - dto
    - `MemberDTO.java`
    - `CustomUserDetails.java`: Authentication, principal 인증객체
  - entity
    - `Member.java`
  - repository
    - `MemberRepository.java`(I)
  - config
    - `SecurityConfig.java`
  - controller
    - `MainController.java`
  - service
    - `MemberService.java`
    - `CustomUserDetailsService.java`

---

## 기능
1. 회원 가입(REST) - JWT와 무관, 단순 Insert
   1. MainController.java
   2. MemberService.java
   3. + JPA
2. JWT 기반 로그인 구현하기
   1. Access 토큰 구현하기(인증 티켓 역할)
   2. 관련 파일
    - `JWTUtil.java`, `LoginFilter.java`, `JWTFilter.java`
    - `CustomUserDetails.java`, `CustomUserDetailsService.java`


---

## JWT, Json Web Token
- 사용자 정보/권한을 json 형태로 저장 + 서버가 서명해서 만든 위변조 방지 토큰
- 세션 방식 대신 인증에 사용
- JWT는 세션을 사용하지 않는 인증 방식 (+ 그러나 일부 사용..)

### Session vs. JWT
- 세션 인증: 서버 측에서 사용자의 로그인 상태를 기억 + 관리 -> Stateful
- JWT 인증: 서버 측에서는 사용자에 대한 관리를 하지 않음. 토큰을 클라이언트 스스로 관리하면서 스스로를 인증하는 방식 -> Stateless

#### 인증 방식 선택 기준
- JWT: 분산 서버 환경, 이 기종 클라이언트를 지원하는 서버 환경. 모바일 웹/앱 서버용
  - 모바일 환경이 생기면서 등장하게 된 개념..
  - View단을 React로 개발한다면 JWT 사용하는 것이 편함(분산 서버이기 때문에)
  - React 사용할 때 세션 방식을 사용할 수는 있으나... 백엔드 서버 세션과 프론트 서버 세션을 동기화하는 서버가 한대 더 필요함
- 세션: 서버가 한 대일 때

#### 인증 방식 - 폼 & JWT
- 폼 인증: ID/PW 입력 -> Spring Security가 자동 처리 + 세션 기반 인증
- JWT 인증: ID/PW 입력 -> 커스텀 필터(LoginFilter) 동작 -> AuthenticationManager 직접 인증 처리 -> JWT 토큰 직접 발급 + 응답 헤더 반환 -> 클라이언트는 응답 토큰 보관 -> 이후에 서버로 접속 + 토큰 전달 -> 커스텀 필터(JWTFilter) 동작 + 토큰 유효성/검증

#### Spring Security
결론은 UserDetails 객체를 만드는 것..
1. 세션 인증 -> UserDetails <- 인증(DB+폼)
2. 소셜 인증(OAuth2) -> UserDetails <- 인증(제3자)
3. JWT 인증 -> UserDetails <- 인증(JWT)

#### JWT 토큰의 구조
1. header
2. payload: claim()으로 저장한 정보들
3. signature(서명): 토큰이 발급된 곳을 검증(위변조 여부를 판단)

#### 로그인 처리
1. 사용자 /login -> ID/PW 전송
2. LoginFilter 개입
3. AuthenticationManager에게 인증을 위임
   1. AuthenticationManager는 CustomUserDetailsService를 호출(loadUserByUsername() 메서드)
4. 인증 성공 -> JWTUtil 사용 -> 토큰 발급
   1. 액세스 토큰 발급(JWTUtil을 사용)
   2. JWT 토큰을 클라이언트에게 전달
   3. 응답 헤더에 토큰을 담아 반환
   4. Authorization: 키
   5. "Bearer " 접두어 + 토큰 : 값
5. 인증 실패 -> 예외 발생
6. LoginFilter를 SecurityConfig에 등록 후 테스트

#### JWTFilter - 인증 티켓을 확인하는 작업
- LoginFilter -> 매표소 -> 티켓 발급 = Authentication(인증)
- JWTFilter -> 게이트 -> 티켓 확인 = Authorization(허가)
  - 현재 요청(접속)한 사용자가 유효한 JWT 토큰을 가지고 있는지 검사
  - 이 사용자를 인증된 사용자로 인식하게 만든다.
