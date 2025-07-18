package com.ticketmate.backend.domain.member.service;

import com.ticketmate.backend.domain.member.domain.dto.request.FollowRequest;
import com.ticketmate.backend.domain.member.domain.entity.Member;
import com.ticketmate.backend.domain.member.domain.entity.MemberFollow;
import com.ticketmate.backend.domain.member.repository.MemberFollowRepository;
import com.ticketmate.backend.global.validator.member.MemberFollowValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberFollowService {

  private final MemberFollowRepository memberFollowRepository;
  private final MemberService memberService;

  /**
   * 팔로우
   *
   * @param request followeeId 팔로우 대상 PK
   */
  @Transactional
  public void follow(Member follower, FollowRequest request) {
    Member followee = memberService.findMemberById(request.getFolloweeId());

    MemberFollowValidator.of(follower, followee)
        .validateSelfFollow() // 자기자신 팔로우 검증
        .validateMemberTypeNotSame() // 의뢰인 <-> 대리인 팔로우 검증
        .validateDuplicateFollow(memberFollowRepository); // 이미 팔로우 한 회원 검증

    saveMemberFollowEntity(follower, followee);
  }

  /**
   * 언팔로우
   *
   * @param request followeeId 언팔로우 대상 PK
   */
  @Transactional
  public void unfollow(Member follower, FollowRequest request) {
    Member followee = memberService.findMemberById(request.getFolloweeId());

    MemberFollowValidator.of(follower, followee)
        .validateSelfFollow()
        .validateMemberTypeNotSame()
        .validateUnfollowAble(memberFollowRepository);

    deleteMemberFollowEntity(follower, followee);
  }

  /**
   * MemberFollow 엔티티 저장
   * Client & Agent 팔로우 수, 팔로워 수 변동
   */
  private void saveMemberFollowEntity(Member follower, Member followee) {
    MemberFollow memberFollow = createMemberFollowEntity(follower, followee);
    memberFollowRepository.save(memberFollow);
    memberService.updateFollowingCount(follower, 1);
    memberService.updateFollowerCount(followee, 1);
  }

  /**
   * MemberFollow 엔티티 생성
   */
  private MemberFollow createMemberFollowEntity(Member follower, Member followee) {
    return MemberFollow.builder()
        .follower(follower)
        .followee(followee)
        .build();
  }

  /**
   * MemberFollow 엔티티 삭제
   */
  private void deleteMemberFollowEntity(Member follower, Member followee) {
    memberFollowRepository.deleteByFollowerAndFollowee(follower, followee);
    memberService.updateFollowingCount(follower, -1);
    memberService.updateFollowerCount(followee, -1);
  }
}
