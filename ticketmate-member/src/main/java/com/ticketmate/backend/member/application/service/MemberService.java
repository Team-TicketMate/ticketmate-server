package com.ticketmate.backend.member.application.service;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.member.application.dto.response.MemberInfoResponse;
import com.ticketmate.backend.member.application.mapper.MemberMapper;
import com.ticketmate.backend.member.core.constant.MemberType;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.member.infrastructure.repository.MemberRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

  private final MemberRepository memberRepository;
  private final MemberMapper memberMapper;

  /**
   * JWT 기반 회원정보 조회
   *
   * @param member @AuthenticationPrincipal 을 통한 인증된 회원 정보
   */
  @Transactional(readOnly = true)
  public MemberInfoResponse getMemberInfo(Member member) {
    return memberMapper.toMemberInfoResponse(member);
  }

  /**
   * 요청 된 Member.MemberType 과 MemberType 비교
   *
   * @param member     검증하고자 하는 사용자
   * @param memberType 예상 MemberType
   */
  public void validateMemberType(Member member, MemberType memberType) {
    if (!member.getMemberType().equals(memberType)) {
      log.error("잘못된 MemberType 입니다.. 사용자 MemberType: {}", member.getMemberType());
      throw new CustomException(ErrorCode.INVALID_MEMBER_TYPE);
    }
  }

  /**
   * memberId에 해당하는 회원 반환
   *
   * @param memberId 회원 PK
   */
  public Member findMemberById(UUID memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> {
          log.error("요청한 PK값에 해당하는 회원을 찾을 수 없습니다. 요청 PK: {}", memberId);
          return new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        });
  }

  /**
   * 팔로잉 수 변동
   *
   * @param member 팔로잉 수를 변동하려는 회원
   * @param count  변동량
   */
  @Transactional
  public void updateFollowingCount(Member member, long count) {
    memberRepository.updateFollowingCount(member.getMemberId(), count);
  }

  /**
   * 팔로워 수 변동
   *
   * @param member 팔로워 수를 변동하려는 회원
   * @param count  변동량
   */
  @Transactional
  public void updateFollowerCount(Member member, long count) {
    memberRepository.updateFollowerCount(member.getMemberId(), count);
  }
}
