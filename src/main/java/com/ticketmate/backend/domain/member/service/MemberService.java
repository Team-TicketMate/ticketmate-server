package com.ticketmate.backend.domain.member.service;

import com.ticketmate.backend.domain.member.domain.constant.MemberType;
import com.ticketmate.backend.domain.member.domain.dto.response.MemberInfoResponse;
import com.ticketmate.backend.domain.member.domain.entity.AgentPerformanceSummary;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.member.repository.AgentPerformanceSummaryRepository;
import com.ticketmate.backend.domain.member.repository.MemberRepository;
import com.ticketmate.backend.global.exception.CustomException;
import com.ticketmate.backend.global.exception.ErrorCode;
import com.ticketmate.backend.global.mapper.EntityMapper;
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
  private final AgentPerformanceSummaryRepository agentPerformanceSummaryRepository;
  private final EntityMapper entityMapper;

  /**
   * JWT 기반 회원정보 조회
   *
   * @param member @AuthenticationPrincipal 을 통한 인증된 회원 정보
   */
  @Transactional(readOnly = true)
  public MemberInfoResponse getMemberInfo(Member member) {
    return entityMapper.toMemberInfoResponse(member);
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
   * 의뢰인 -> 대리인 MemberType 변경
   *
   * @param member MemberType을 변경하려는 Member
   */
  @Transactional
  public void promoteToAgent(Member member) {
    member.setMemberType(MemberType.AGENT);

    if (!agentPerformanceSummaryRepository.existsById(member.getMemberId())) {
      AgentPerformanceSummary summary = AgentPerformanceSummary.builder()
          .agent(member)
          .totalScore(0.0)
          .averageRating(0.0)
          .reviewCount(0)
          .followerCount(0)
          .recentSuccessCount(0)
          .build();
      agentPerformanceSummaryRepository.save(summary);
    }
  }
}
