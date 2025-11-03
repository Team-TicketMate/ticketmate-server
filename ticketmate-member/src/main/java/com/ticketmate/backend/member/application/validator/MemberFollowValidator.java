package com.ticketmate.backend.member.application.validator;

import com.ticketmate.backend.common.application.exception.CustomException;
import com.ticketmate.backend.common.application.exception.ErrorCode;
import com.ticketmate.backend.member.core.constant.MemberType;
import com.ticketmate.backend.member.infrastructure.entity.Member;
import com.ticketmate.backend.member.infrastructure.repository.MemberFollowRepository;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemberFollowValidator {

  private final Member follower;
  private final Member followee;

  private MemberFollowValidator(Member follower, Member followee) {
    this.follower = follower;
    this.followee = followee;
  }

  public static MemberFollowValidator of(Member follower, Member followee) {
    return new MemberFollowValidator(follower, followee);
  }

  /**
   * 자기 자신 팔로우 검증
   */
  public MemberFollowValidator validateSelfFollow() {
    if (follower.getMemberId().equals(followee.getMemberId())) {
      log.error("자기 자신을 팔로우 할 수 없습니다. followerId: {}, followeeId: {}",
          follower.getMemberId(), followee.getMemberId());
      throw new CustomException(ErrorCode.SELF_FOLLOW_NOT_ALLOWED);
    }
    return this;
  }

  /**
   * Client 만 Agent 팔로우 가능
   */
  public MemberFollowValidator validateClientToAgentOnly() {
    if (!(follower.getMemberType() == MemberType.CLIENT && followee.getMemberType() == MemberType.AGENT)) {
      log.error("의뢰인만 대리인을 팔로우 할 수 있습니다. followerType={}, followeeType={}",
          follower.getMemberType(), followee.getMemberType());
      throw new CustomException(ErrorCode.CLIENT_FOLLOW_AGENT_ONLY);
    }
    return this;
  }

  /**
   * 이미 팔로우된 상태 검증
   */
  public MemberFollowValidator validateDuplicateFollow(MemberFollowRepository repository) {
    if (repository.existsByFollowerAndFollowee(follower, followee)) {
      log.error("이미 팔로우한 회원입니다. followerId={}, followeeId={}",
          follower.getMemberId(), followee.getMemberId());
      throw new CustomException(ErrorCode.DUPLICATE_FOLLOW_NOT_ALLOWED);
    }
    return this;
  }

  /**
   * 언팔로우 가능 여부 검증
   */
  public MemberFollowValidator validateUnfollowAble(MemberFollowRepository repository) {
    if (!repository.existsByFollowerAndFollowee(follower, followee)) {
      log.error("팔로우 되어있지 않은 회원입니다. 언팔로우 불가. followerId={}, followeeId={}",
          follower.getMemberId(), followee.getMemberId());
      throw new CustomException(ErrorCode.UNFOLLOW_NOT_ALLOWED);
    }
    return this;
  }
}
