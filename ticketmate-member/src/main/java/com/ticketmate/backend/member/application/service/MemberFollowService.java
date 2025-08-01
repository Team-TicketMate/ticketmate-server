package com.ticketmate.backend.member.application.service;

import com.ticketmate.backend.member.application.dto.request.FollowRequest;
import com.ticketmate.backend.member.application.validator.MemberFollowValidator;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.member.infrastructure.entity.MemberFollow;
import com.ticketmate.backend.member.infrastructure.repository.MemberFollowRepository;
import com.ticketmate.backend.notification.application.dto.request.NotificationPayload;
import com.ticketmate.backend.notification.application.type.FollowingNotificationType;
import com.ticketmate.backend.notification.core.service.NotificationService;
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
  private final NotificationService notificationService;

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
        .validateClientToAgentOnly() // 의뢰인 -> 대리인 팔로우 요청 검증
        .validateDuplicateFollow(memberFollowRepository); // 이미 팔로우 한 회원 검증

    NotificationPayload payload = buildFollowingNotificationPayload(follower);

    notificationService.sendToMember(followee.getMemberId(), payload);

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
        .validateClientToAgentOnly()
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

  /**
   * 팔로우 알림 Payload 생성
   */
  private NotificationPayload buildFollowingNotificationPayload(Member follower) {
    return FollowingNotificationType.FOLLOW.toPayload(follower.getNickname());
  }
}
