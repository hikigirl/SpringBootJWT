# Spring Security - JWT

### 초기 작업
1. build.gradle: JWT 의존성
2. application.yaml: JPA 세팅

### 파일 생성하기
- project root : script.sql
- src/main/java - com.test.jwt
  - dto
    - MemberDTO.java
  - entity
    - Member.java
  - repository
    - MemberRepository.java(I)
  - config
    - SecurityConfig.java
  - controller
    - MainController.java
  - service
    - MemberService.java

---
## 기능
1. 회원 가입(REST) - JWT와 무관, 단순 Insert
   1. MainController.java
   2. MemberService.java
   3. + JPA
2. JWT 기반 로그인 구현하기
   1. Access 토큰 구현하기(인증 티켓 역할)
   2. 

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