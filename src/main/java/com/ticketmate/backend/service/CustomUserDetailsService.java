package com.ticketmate.backend.service;

import com.ticketmate.backend.object.CustomUserDetails;
import com.ticketmate.backend.object.postgres.Member;
import com.ticketmate.backend.repository.postgres.MemberRepository;
import com.ticketmate.backend.util.exception.CustomException;
import com.ticketmate.backend.util.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

  private final MemberRepository memberRepository;

  @Override
  public UserDetails loadUserByUsername(String stringMemberId) throws UsernameNotFoundException {
    UUID memberId;
    try {
      memberId = UUID.fromString(stringMemberId);
    } catch (IllegalArgumentException e) {
      log.error("유효하지 않은 UUID 형식: {}", stringMemberId);
      throw new UsernameNotFoundException("유효하지 않은 UUID 형식입니다.");
    }

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> {
          log.error("회원을 찾을 수 없습니다. 회원 Id: {}", memberId);
          return new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        });
    return new CustomUserDetails(member);
  }
}
