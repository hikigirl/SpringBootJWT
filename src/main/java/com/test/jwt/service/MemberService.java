package com.test.jwt.service;

import com.test.jwt.dto.MemberDTO;
import com.test.jwt.entity.Member;
import com.test.jwt.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    //주입(DB작업 위한 Repo, 비밀번호 암호화하는 encoder)
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public void join(MemberDTO dto) {
        //엔티티로 만들기(builder 패턴, DTO에 정의해두고 사용하자... 나중엔)
        Member member = Member.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .build();
        memberRepository.save(member);
    }
}
