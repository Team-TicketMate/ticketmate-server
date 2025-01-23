package com.ticketmate.backend.service;

import com.ticketmate.backend.object.constants.AccountStatus;
import com.ticketmate.backend.object.constants.MemberType;
import com.ticketmate.backend.object.constants.Role;
import com.ticketmate.backend.object.dto.ApiResponse;
import com.ticketmate.backend.object.dto.SignUpDto;
import com.ticketmate.backend.object.postgres.Member;
import com.ticketmate.backend.repository.postgres.MemberRepository;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 회원가입
     *
     * @param dto username, password, nickname, birth, phone, profileUrl
     * @return 없음
     */
    @Transactional
    public ApiResponse<Void> signUp(SignUpDto dto) {

        // 사용자 이메일 검증 (중복 이메일 사용 불가)
        if (memberRepository.existsByUsername(dto.getUsername())) {
            log.error("이미 가입된 이메일 주소입니다: {}", dto.getUsername());
            throw new CustomException(ErrorCode.DUPLICATE_USERNAME);
        }

        memberRepository.save(Member.builder()
                .username(dto.getUsername())
                .password(bCryptPasswordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .birth(dto.getBirth())
                .phone(dto.getPhone())
                .profileUrl(null)
                .role(Role.ROLE_USER)
                .memberType(MemberType.CLIENT)
                .accountStatus(AccountStatus.ACTIVE_ACCOUNT)
                .lastLoginTime(LocalDateTime.now())
                .build()
        );
        log.debug("회원가입 성공: username={}", dto.getUsername());

        return ApiResponse.success(null);
    }


}
